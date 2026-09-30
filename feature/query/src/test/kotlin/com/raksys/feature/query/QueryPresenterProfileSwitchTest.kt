package com.raksys.feature.query

import com.raksys.core.database.DatabaseDriver
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.QueryResult
import com.raksys.core.model.TableSchema
import com.raksys.core.model.UiState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeDriver : DatabaseDriver {
    override suspend fun testConnection(profile: ConnectionProfile) = Result.success(Unit)
    override suspend fun listTables(profile: ConnectionProfile, forceRefresh: Boolean) =
        Result.success(emptyList<TableSchema>())
    override suspend fun executeQuery(profile: ConnectionProfile, sql: String) =
        Result.success(QueryResult(listOf("id"), listOf(listOf(1)), 1))
    override suspend fun executeStatement(profile: ConnectionProfile, sql: String) = Result.success(Unit)
    override suspend fun createDatabase(profile: ConnectionProfile, password: String) = Result.success(Unit)
    override fun cancel() {}
    override fun invalidate(profileId: String) {}
    override fun close() {}
}

class QueryPresenterProfileSwitchTest {

    private fun profile(id: String) = ConnectionProfile(id = id, name = id, dbType = DbType.SQLITE, database = "$id.db")

    @Test
    fun `switching connection clears the previous result`() = runBlocking {
        val presenter = QueryPresenter(FakeDriver())
        presenter.onEvent(QueryEvent.UseProfile("a"))
        presenter.onEvent(QueryEvent.Execute(profile("a"), "SELECT 1"))
        assertTrue(presenter.state.value is UiState.Success)

        presenter.onEvent(QueryEvent.UseProfile("b"))

        assertEquals(UiState.Idle, presenter.state.value)
    }

    @Test
    fun `staying on the same connection keeps the result`() = runBlocking {
        val presenter = QueryPresenter(FakeDriver())
        presenter.onEvent(QueryEvent.Execute(profile("a"), "SELECT 1"))

        presenter.onEvent(QueryEvent.UseProfile("a"))

        assertTrue(presenter.state.value is UiState.Success)
    }
}
