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
                    maximumPoolSize = 5
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

    override suspend fun listTables(profile: ConnectionProfile): Result<List<TableSchema>> =
        withContext(Dispatchers.IO) {
            runCatching {
                poolFor(profile).connection.use { conn ->
                    val metadata = conn.metaData
                    val tables = mutableListOf<TableSchema>()
                    metadata.getTables(conn.catalog, conn.schema, "%", arrayOf("TABLE")).use { rs ->
                        while (rs.next()) {
                            val tableName = rs.getString("TABLE_NAME")
                            tables += TableSchema(tableName, columnsFor(metadata, conn.catalog, conn.schema, tableName))
                        }
                    }
                    tables
                }
            }
        }

    private fun columnsFor(
        metadata: java.sql.DatabaseMetaData,
        catalog: String?,
        schema: String?,
        tableName: String,
    ): List<ColumnDefinition> {
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
        val columns = mutableListOf<ColumnDefinition>()
        metadata.getColumns(catalog, schema, tableName, "%").use { rs ->
            while (rs.next()) {
                val name = rs.getString("COLUMN_NAME")
                columns += ColumnDefinition(
                    name = name,
                    type = rs.getString("TYPE_NAME"),
                    nullable = rs.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable,
                    isPrimaryKey = name in primaryKeys,
                    defaultValue = rs.getString("COLUMN_DEF"),
                    foreignKey = foreignKeys[name],
                )
            }
        }
        return columns
    }

    override suspend fun executeQuery(profile: ConnectionProfile, sql: String): Result<QueryResult> =
        withContext(Dispatchers.IO) {
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
        pools.remove(profileId)?.close()
        sshTunnelManager.invalidate(profileId)
    }

    override fun close() {
        pools.values.forEach { it.close() }
        pools.clear()
        sshTunnelManager.closeAll()
    }
}
