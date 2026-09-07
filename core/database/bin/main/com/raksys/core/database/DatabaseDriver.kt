package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.QueryResult
import com.raksys.core.model.TableSchema

interface DatabaseDriver {
    suspend fun testConnection(profile: ConnectionProfile, password: String): Result<Unit>
    suspend fun listTables(profile: ConnectionProfile, password: String): Result<List<TableSchema>>
    suspend fun executeQuery(profile: ConnectionProfile, password: String, sql: String): Result<QueryResult>
    fun cancel()
    fun close()
}
