package com.raksys.core.database

import com.raksys.core.model.ColumnDefinition
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.ForeignKeyRef
import com.raksys.core.model.QueryResult
import com.raksys.core.model.TableSchema
import com.raksys.core.security.CredentialStore
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap

private const val MAX_ROWS = 10_000
private const val QUERY_TIMEOUT_SECONDS = 30
private const val SCHEMA_CACHE_TTL_MS = 5 * 60 * 1000L

private val SCHEMA_CHANGING_SQL = Regex("""^\s*(CREATE|ALTER|DROP|RENAME)\b""", RegexOption.IGNORE_CASE)

/** True for statements that can change the table/column layout, so a cached schema must be dropped. */
internal fun isSchemaChangingSql(sql: String): Boolean = SCHEMA_CHANGING_SQL.containsMatchIn(sql)

private class CachedSchema(val tables: List<TableSchema>, val loadedAt: Long = System.currentTimeMillis())

internal fun jdbcUrl(profile: ConnectionProfile, resolvedHost: String, resolvedPort: Int): String {
    val sslSuffix = when {
        !profile.sslEnabled -> ""
        profile.dbType == DbType.POSTGRES -> "?sslmode=require"
        profile.dbType == DbType.MYSQL -> "?useSSL=true&requireSSL=true"
        else -> ""
    }
    return when (profile.dbType) {
        DbType.POSTGRES -> "jdbc:postgresql://$resolvedHost:$resolvedPort/${profile.database}$sslSuffix"
        DbType.MYSQL -> "jdbc:mysql://$resolvedHost:$resolvedPort/${profile.database}$sslSuffix"
        DbType.SQLITE -> "jdbc:sqlite:${profile.database}"
        else -> error("${profile.dbType} is not a relational database")
    }
}

class JdbcDatabaseDriver(
    private val credentialStore: CredentialStore,
    private val sshTunnelManager: SshTunnelManager,
) : DatabaseDriver {

    private val pools = ConcurrentHashMap<String, HikariDataSource>()
    private val schemaCache = ConcurrentHashMap<String, CachedSchema>()
    @Volatile private var activeStatement: Statement? = null

    private fun poolFor(profile: ConnectionProfile): HikariDataSource =
        pools.getOrPut(profile.id) {
            val (resolvedHost, resolvedPort) = sshTunnelManager.resolve(profile)
            val password = credentialStore.get(profile.id).orEmpty()
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = jdbcUrl(profile, resolvedHost, resolvedPort)
                    username = profile.username.ifBlank { null }
                    this.password = password.ifBlank { null }
                    maximumPoolSize = 4
                    // Keep no idle connections around: a desktop client is idle most of the time, and
                    // every profile that was ever opened would otherwise hold a full pool on the server.
                    minimumIdle = 0
                    idleTimeout = 60_000
                    connectionTimeout = 10_000
                    validationTimeout = 5_000
                }
            )
        }

    override suspend fun testConnection(profile: ConnectionProfile): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                poolFor(profile).connection.use { it.isValid(5) }
            }.mapCatching { valid ->
                if (!valid) error("Connection reported invalid") else Unit
            }
        }

    override suspend fun listTables(profile: ConnectionProfile, forceRefresh: Boolean): Result<List<TableSchema>> =
        withContext(Dispatchers.IO) {
            val cached = schemaCache[profile.id]
            if (!forceRefresh && cached != null && System.currentTimeMillis() - cached.loadedAt < SCHEMA_CACHE_TTL_MS) {
                return@withContext Result.success(cached.tables)
            }
            runCatching {
                poolFor(profile).connection.use { conn ->
                    val metadata = conn.metaData
                    val tableNames = mutableListOf<String>()
                    metadata.getTables(conn.catalog, conn.schema, "%", arrayOf("TABLE")).use { rs ->
                        while (rs.next()) tableNames += rs.getString("TABLE_NAME")
                    }
                    // One getColumns call for the whole schema is much cheaper than one per table, but the
                    // SQLite driver answers it with a compound SELECT that exceeds SQLite's term limit on
                    // schemas with a few dozen tables. So SQLite reads per table, and any other engine
                    // that rejects the bulk call falls back to per-table reads too.
                    val bulk = if (profile.dbType == DbType.SQLITE) null
                    else runCatching { readColumns(metadata, conn.catalog, conn.schema, "%") }.getOrNull()
                    tableNames.map { name ->
                        val columns = bulk?.get(name)
                            ?: readColumns(metadata, conn.catalog, conn.schema, escapeLikePattern(metadata, name))[name].orEmpty()
                        val (primaryKeys, foreignKeys) = keysFor(metadata, conn.catalog, conn.schema, name)
                        TableSchema(
                            name = name,
                            columns = columns.map { col ->
                                col.copy(
                                    isPrimaryKey = col.name in primaryKeys,
                                    foreignKey = foreignKeys[col.name],
                                )
                            },
                        )
                    }
                }
            }.onSuccess { schemaCache[profile.id] = CachedSchema(it) }
        }

    /** Reads columns for every table matching [tablePattern], grouped by exact table name. */
    private fun readColumns(
        metadata: java.sql.DatabaseMetaData,
        catalog: String?,
        schema: String?,
        tablePattern: String,
    ): Map<String, List<ColumnDefinition>> {
        val result = LinkedHashMap<String, MutableList<ColumnDefinition>>()
        metadata.getColumns(catalog, schema, tablePattern, "%").use { rs ->
            while (rs.next()) {
                val table = rs.getString("TABLE_NAME")
                result.getOrPut(table) { mutableListOf() } += ColumnDefinition(
                    name = rs.getString("COLUMN_NAME"),
                    type = rs.getString("TYPE_NAME"),
                    nullable = rs.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable,
                    isPrimaryKey = false,
                    defaultValue = rs.getString("COLUMN_DEF"),
                    foreignKey = null,
                )
            }
        }
        return result
    }

    /** JDBC metadata takes LIKE patterns, so escape `_` and `%` in a literal table name. */
    private fun escapeLikePattern(metadata: java.sql.DatabaseMetaData, name: String): String {
        val esc = metadata.searchStringEscape.orEmpty()
        if (esc.isEmpty()) return name
        return name.replace(esc, esc + esc).replace("_", esc + "_").replace("%", esc + "%")
    }

    private fun keysFor(
        metadata: java.sql.DatabaseMetaData,
        catalog: String?,
        schema: String?,
        tableName: String,
    ): Pair<Set<String>, Map<String, ForeignKeyRef>> {
        val primaryKeys = mutableSetOf<String>()
        metadata.getPrimaryKeys(catalog, schema, tableName).use { rs ->
            while (rs.next()) primaryKeys += rs.getString("COLUMN_NAME")
        }
        val foreignKeys = mutableMapOf<String, ForeignKeyRef>()
        metadata.getImportedKeys(catalog, schema, tableName).use { rs ->
            while (rs.next()) {
                foreignKeys[rs.getString("FKCOLUMN_NAME")] = ForeignKeyRef(
                    referencedTable = rs.getString("PKTABLE_NAME"),
                    referencedColumn = rs.getString("PKCOLUMN_NAME"),
                )
            }
        }
        return primaryKeys to foreignKeys
    }

    override suspend fun executeQuery(profile: ConnectionProfile, sql: String): Result<QueryResult> =
        withContext(Dispatchers.IO) {
            // Dropped up front, not after success: some drivers report DDL run through executeQuery
            // as an error even though it took effect.
            if (isSchemaChangingSql(sql)) schemaCache.remove(profile.id)
            runCatching {
                val startedAt = System.currentTimeMillis()
                poolFor(profile).connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.queryTimeout = QUERY_TIMEOUT_SECONDS
                        statement.maxRows = MAX_ROWS + 1
                        activeStatement = statement
                        try {
                            statement.executeQuery(sql).use { rs ->
                                val columnCount = rs.metaData.columnCount
                                val columns = (1..columnCount).map { rs.metaData.getColumnLabel(it) }
                                val rows = mutableListOf<List<Any?>>()
                                while (rs.next()) {
                                    rows += (1..columnCount).map { rs.getObject(it) }
                                }
                                val truncated = rows.size > MAX_ROWS
                                val visibleRows = if (truncated) rows.subList(0, MAX_ROWS) else rows
                                QueryResult(columns, visibleRows, System.currentTimeMillis() - startedAt, truncated)
                            }
                        } finally {
                            activeStatement = null
                        }
                    }
                }
            }
        }

    override suspend fun executeStatement(profile: ConnectionProfile, sql: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (isSchemaChangingSql(sql)) schemaCache.remove(profile.id)
            runCatching {
                poolFor(profile).connection.use { conn ->
                    conn.createStatement().use { statement -> statement.execute(sql) }
                }
                Unit
            }
        }

    override suspend fun createDatabase(profile: ConnectionProfile, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                when (profile.dbType) {
                    DbType.SQLITE -> Unit // SQLite creates its file on first connect — nothing to do here.
                    DbType.POSTGRES, DbType.MYSQL -> {
                        // Connect to an administrative/no-database session on the same server, then
                        // issue CREATE DATABASE — Postgres needs *some* existing DB to connect to
                        // first (its own default "postgres" maintenance DB); MySQL allows connecting
                        // with no database selected at all.
                        val adminProfile = if (profile.dbType == DbType.POSTGRES) {
                            profile.copy(database = "postgres")
                        } else {
                            profile.copy(database = "")
                        }
                        val (resolvedHost, resolvedPort) = sshTunnelManager.resolve(profile)
                        val effectivePassword = password.ifBlank { credentialStore.get(profile.id).orEmpty() }
                        val adminUrl = jdbcUrl(adminProfile, resolvedHost, resolvedPort)
                        val createSql = if (profile.dbType == DbType.MYSQL) {
                            "CREATE DATABASE IF NOT EXISTS ${quotedIdentifier(profile.dbType, profile.database)}"
                        } else {
                            "CREATE DATABASE ${quotedIdentifier(profile.dbType, profile.database)}"
                        }
                        java.sql.DriverManager.getConnection(
                            adminUrl,
                            adminProfile.username.ifBlank { null },
                            effectivePassword.ifBlank { null },
                        ).use { conn ->
                            conn.createStatement().use { it.execute(createSql) }
                        }
                        Unit
                    }
                    else -> error("${profile.dbType} does not support explicit database creation")
                }
            }
        }

    override fun cancel() {
        runCatching { activeStatement?.cancel() }
    }

    override fun invalidate(profileId: String) {
        schemaCache.remove(profileId)
        pools.remove(profileId)?.close()
        sshTunnelManager.invalidate(profileId)
    }

    override fun close() {
        pools.values.forEach { it.close() }
        pools.clear()
        schemaCache.clear()
        sshTunnelManager.closeAll()
    }
}
