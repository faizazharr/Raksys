package com.raksys.feature.erd

import com.raksys.core.database.DatabaseDriver
import com.raksys.core.model.TableSchema
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ErdPresenter(private val driver: DatabaseDriver) {
    private val _state = MutableStateFlow<UiState<List<TableSchema>>>(UiState.Idle)
    val state: StateFlow<UiState<List<TableSchema>>> = _state

    suspend fun onEvent(event: ErdEvent) {
        when (event) {
            is ErdEvent.Load -> {
                _state.value = UiState.Loading
                driver.listTables(event.profile)
                    .onSuccess { _state.value = UiState.Success(it) }
                    .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load schema") }
            }
        }
    }
}
