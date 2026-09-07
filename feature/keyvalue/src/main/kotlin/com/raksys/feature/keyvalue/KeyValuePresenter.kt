package com.raksys.feature.keyvalue

import com.raksys.core.database.KeyValueDriver
import com.raksys.core.model.RedisEntry
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class KeyValuePresenter(private val driver: KeyValueDriver) {
    private val _state = MutableStateFlow<UiState<List<RedisEntry>>>(UiState.Idle)
    val state: StateFlow<UiState<List<RedisEntry>>> = _state

    suspend fun onEvent(event: KeyValueEvent) {
        when (event) {
            is KeyValueEvent.Scan -> {
                _state.value = UiState.Loading
                driver.scanKeys(event.profile, event.pattern)
                    .onSuccess { _state.value = UiState.Success(it) }
                    .onFailure { _state.value = UiState.Error(it.message ?: "Failed to scan keys") }
            }
            KeyValueEvent.Cancel -> driver.cancel()
        }
    }
}
