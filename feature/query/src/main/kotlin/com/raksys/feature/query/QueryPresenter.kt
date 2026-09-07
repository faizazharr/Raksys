package com.raksys.feature.query

import com.raksys.core.database.DatabaseDriver
import com.raksys.core.model.QueryResult
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class QueryHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sql: String,
    val timestamp: Long = System.currentTimeMillis(),
    val executionTimeMs: Long? = null,
    val rowCount: Int? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
)

class QueryPresenter(private val driver: DatabaseDriver) {
    private val _state = MutableStateFlow<UiState<QueryResult>>(UiState.Idle)
    val state: StateFlow<UiState<QueryResult>> = _state

    private val _history = MutableStateFlow<List<QueryHistoryItem>>(emptyList())
    val history: StateFlow<List<QueryHistoryItem>> = _history

    suspend fun onEvent(event: QueryEvent) {
        when (event) {
            is QueryEvent.Execute -> {
                _state.value = UiState.Loading
                val startTime = System.currentTimeMillis()
                driver.executeQuery(event.profile, event.sql)
                    .onSuccess {
                        _state.value = UiState.Success(it)
                        val item = QueryHistoryItem(
                            sql = event.sql,
                            timestamp = startTime,
                            executionTimeMs = it.executionTimeMs,
                            rowCount = it.rows.size,
                            isSuccess = true,
                        )
                        _history.value = (listOf(item) + _history.value).take(100)
                    }
                    .onFailure {
                        val msg = it.message ?: "Query failed"
                        _state.value = UiState.Error(msg)
                        val item = QueryHistoryItem(
                            sql = event.sql,
                            timestamp = startTime,
                            executionTimeMs = System.currentTimeMillis() - startTime,
                            isSuccess = false,
                            errorMessage = msg,
                        )
                        _history.value = (listOf(item) + _history.value).take(100)
                    }
            }
            QueryEvent.Cancel -> driver.cancel()
            QueryEvent.ClearHistory -> _history.value = emptyList()
        }
    }
}
