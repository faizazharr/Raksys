package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.QueryResult
import com.raksys.core.model.TableSchema

interface DatabaseDriver {
    suspend fun testConnection(profile: ConnectionProfile): Result<Unit>
    /**
     * Lists tables with their columns. The result is cached per profile (the schema screens —
     * navigator, ERD, permissions — all need it), so pass [forceRefresh] from an explicit refresh
     * or retry. The cache is also dropped automatically after DDL statements and on [invalidate].
     */
    suspend fun listTables(profile: ConnectionProfile, forceRefresh: Boolean = false): Result<List<TableSchema>>
    suspend fun executeQuery(profile: ConnectionProfile, sql: String): Result<QueryResult>
    suspend fun executeStatement(profile: ConnectionProfile, sql: String): Result<Unit>
    /**
     * Creates the database named by [profile.database] on the server (Postgres/MySQL only —
     * SQLite creates its file on first connect, no explicit step needed). Takes [password]
     * directly rather than reading [profile.id] from the credential store, because this runs
     * during the "add connection" flow, before the profile (and its credential) are persisted.
     */
    suspend fun createDatabase(profile: ConnectionProfile, password: String): Result<Unit>
    fun cancel()
    fun invalidate(profileId: String)
    fun close()
}
