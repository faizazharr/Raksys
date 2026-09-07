package com.raksys.feature.connection

import com.raksys.core.database.DatabaseDriver
import com.raksys.core.database.DocumentDatabaseDriver
import com.raksys.core.database.KeyValueDriver
import com.raksys.core.database.friendlyConnectionError
import com.raksys.core.database.sshCredentialKey
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbFamily
import com.raksys.core.model.UiState
import com.raksys.core.security.CredentialStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionPresenter(
    private val sqlDriver: DatabaseDriver,
    private val documentDriver: DocumentDatabaseDriver,
    private val keyValueDriver: KeyValueDriver,
    private val credentialStore: CredentialStore,
    private val repository: ConnectionRepository,
) {
    private val _profiles = MutableStateFlow(repository.list())
    val profiles: StateFlow<List<ConnectionProfile>> = _profiles

    private val _testState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val testState: StateFlow<UiState<Unit>> = _testState

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState

    suspend fun onEvent(event: ConnectionEvent) {
        when (event) {
            is ConnectionEvent.Test -> handleTest(event.profile)
            is ConnectionEvent.Add -> handleAdd(event.profile, event.password, event.sshPassword, event.createNew)
            is ConnectionEvent.Delete -> handleDelete(event.profile)
            ConnectionEvent.ResetSaveState -> _saveState.value = UiState.Idle
        }
    }

    private suspend fun handleTest(profile: ConnectionProfile) {
        _testState.value = UiState.Loading
        val result = when (profile.dbType.family) {
            DbFamily.RELATIONAL -> sqlDriver.testConnection(profile)
            DbFamily.DOCUMENT -> documentDriver.testConnection(profile)
            DbFamily.KEY_VALUE -> keyValueDriver.testConnection(profile)
        }
        result
            .onSuccess { _testState.value = UiState.Success(Unit) }
            .onFailure { _testState.value = UiState.Error(friendlyConnectionError(profile, it)) }
    }

    private suspend fun handleAdd(profile: ConnectionProfile, password: String, sshPassword: String, createNew: Boolean) {
        _saveState.value = UiState.Loading

        if (createNew) {
            // Password is passed in directly rather than pre-saved to the keychain — a failed
            // CREATE DATABASE attempt shouldn't leave an orphaned credential entry behind under
            // a profile id that never makes it into the connection list.
            val result = sqlDriver.createDatabase(profile, password)
            if (result.isFailure) {
                _saveState.value = UiState.Error(friendlyConnectionError(profile, result.exceptionOrNull()!!))
                return
            }
        }

        if (password.isNotBlank()) credentialStore.save(profile.id, password)
        if (sshPassword.isNotBlank()) credentialStore.save(sshCredentialKey(profile.id), sshPassword)

        repository.save(profile)
        _profiles.value = repository.list()
        _saveState.value = UiState.Success(Unit)
    }

    private fun handleDelete(profile: ConnectionProfile) {
        repository.delete(profile.id)
        credentialStore.delete(profile.id)
        credentialStore.delete(sshCredentialKey(profile.id))
        when (profile.dbType.family) {
            DbFamily.RELATIONAL -> sqlDriver.invalidate(profile.id)
            DbFamily.DOCUMENT -> documentDriver.invalidate(profile.id)
            DbFamily.KEY_VALUE -> keyValueDriver.invalidate(profile.id)
        }
        _profiles.value = repository.list()
    }
}
