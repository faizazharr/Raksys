package com.raksys.core.database

import com.raksys.core.model.ColumnDefinition
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.QueryResult
import com.raksys.core.model.TableSchema
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap

private fun jdbcUrl(profile: ConnectionProfile): String = when (profile.dbType) {
    DbType.POSTGRES -> "jdbc:postgresql://${profile.host}:${profile.port}/${profile.database}"
    DbType.MYSQL -> "jdbc:mysql://${profile.host}:${profile.port}/${profile.database}"
    DbType.SQLITE -> "jdbc:sqlite:${profile.database}"
}

class JdbcDatabaseDriver : DatabaseDriver {

    private val pools = ConcurrentHashMap<String, HikariDataSource>()
    @Volatile private var activeStatement: Statement? = null

    private fun poolFor(profile: ConnectionProfile, password: String): HikariDataSource =
        pools.getOrPut(profile.id) {
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = jdbcUrl(profile)
                    username = profile.username.ifBlank { null }
                    this.password = password.ifBlank { null }
                    maximumPoolSize = 5
                }
            )
        }

    override suspend fun testConnection(profile: ConnectionProfile, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                poolFor(profile, password).connection.use { it.isValid(5) }
            }.mapCatching { valid ->
                if (!valid) error("Connection reported invalid") else Unit
            }
        }

    override suspend fun listTables(profile: ConnectionProfile, password: String): Result<List<TableSchema>> =
        withContext(Dispatchers.IO) {
            runCatching {
                poolFor(profile, password).connection.use { conn ->
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
                )
            }
        }
        return columns
    }

    override suspend fun executeQuery(profile: ConnectionProfile, password: String, sql: String): Result<QueryResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val startedAt = System.currentTimeMillis()
                poolFor(profile, password).connection.use { conn ->
                    conn.createStatement().use { statement ->
                        activeStatement = statement
                        try {
                            statement.executeQuery(sql).use { rs ->
                                val columnCount = rs.metaData.columnCount
                                val columns = (1..columnCount).map { rs.metaData.getColumnLabel(it) }
                                val rows = mutableListOf<List<Any?>>()
                                while (rs.next()) {
                                    rows += (1..columnCount).map { rs.getObject(it) }
                                }
                                QueryResult(columns, rows, System.currentTimeMillis() - startedAt)
                            }
                        } finally {
                            activeStatement = null
                        }
                    }
                }
            }
        }

    override fun cancel() {
        runCatching { activeStatement?.cancel() }
    }

    override fun close() {
        pools.values.forEach { it.close() }
        pools.clear()
    }
}
