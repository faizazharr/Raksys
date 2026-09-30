package com.raksys.feature.query

import kotlinx.serialization.Serializable
import com.raksys.core.database.DatabaseDriver
import com.raksys.core.model.QueryResult
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Serializable
data class QueryHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sql: String,
    val timestamp: Long = System.currentTimeMillis(),
    val executionTimeMs: Long? = null,
    val rowCount: Int? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
)

class QueryPresenter(
    private val driver: DatabaseDriver,
    private val historyStore: QueryHistoryStore = InMemoryQueryHistoryStore(),
) {
    private val _state = MutableStateFlow<UiState<QueryResult>>(UiState.Idle)
    val state: StateFlow<UiState<QueryResult>> = _state

    private val _history = MutableStateFlow<List<QueryHistoryItem>>(emptyList())
    val history: StateFlow<List<QueryHistoryItem>> = _history

    private var activeProfileId: String? = null

    /** History per connection, kept in memory so it works even when saving to disk is switched off. */
    private val memory = mutableMapOf<String, List<QueryHistoryItem>>()

    private fun historyFor(profileId: String): List<QueryHistoryItem> =
        memory.getOrPut(profileId) { historyStore.load(profileId) }

    /** Newest first, capped at [MAX_HISTORY]; also handed to the store for the connection that ran it. */
    private fun record(profileId: String, item: QueryHistoryItem) {
        val updated = (listOf(item) + historyFor(profileId)).take(MAX_HISTORY)
        memory[profileId] = updated
        historyStore.save(profileId, updated)
        if (activeProfileId == profileId) _history.value = updated
    }

    suspend fun onEvent(event: QueryEvent) {
        when (event) {
            is QueryEvent.UseProfile -> {
                // The presenter is a singleton shared by every connection. Without this, connection A's
                // result grid stays on screen after switching to connection B.
                if (activeProfileId != event.profileId) {
                    if (state.value is UiState.Loading) driver.cancel()
                    _state.value = UiState.Idle
                    activeProfileId = event.profileId
                    _history.value = historyFor(event.profileId)
                }
            }
            is QueryEvent.Execute -> {
                activeProfileId = event.profile.id
                _state.value = UiState.Loading
                val startTime = System.currentTimeMillis()
                driver.executeQuery(event.profile, event.sql)
                    .onSuccess {
                        // Ignore a late answer for a connection the user has already switched away from.
                        if (activeProfileId == event.profile.id) _state.value = UiState.Success(it)
                        val item = QueryHistoryItem(
                            sql = event.sql,
                            timestamp = startTime,
                            executionTimeMs = it.executionTimeMs,
                            rowCount = it.rows.size,
                            isSuccess = true,
                        )
                        record(event.profile.id, item)
                    }
                    .onFailure {
                        val msg = it.message ?: "Query failed"
                        if (activeProfileId == event.profile.id) _state.value = UiState.Error(msg)
                        val item = QueryHistoryItem(
                            sql = event.sql,
                            timestamp = startTime,
                            executionTimeMs = System.currentTimeMillis() - startTime,
                            isSuccess = false,
                            errorMessage = msg,
                        )
                        record(event.profile.id, item)
                    }
            }
            QueryEvent.Cancel -> driver.cancel()
            QueryEvent.ClearHistory -> {
                activeProfileId?.let {
                    memory[it] = emptyList()
                    historyStore.save(it, emptyList())
                }
                _history.value = emptyList()
            }
            QueryEvent.ClearAllSavedHistory -> {
                memory.clear()
                historyStore.clearAll()
                _history.value = emptyList()
            }
        }
    }
}

private const val MAX_HISTORY = 100
