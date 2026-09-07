package com.raksys.feature.permission

import com.raksys.core.database.DatabaseDriver
import com.raksys.core.database.PermissionDriver
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DatabaseRole
import com.raksys.core.model.TablePrivilege
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PermissionData(
    val roles: List<DatabaseRole>,
    val tables: List<String>,
    val privileges: List<TablePrivilege>,
)

class PermissionPresenter(
    private val permissionDriver: PermissionDriver,
    private val sqlDriver: DatabaseDriver,
) {
    private val _state = MutableStateFlow<UiState<PermissionData>>(UiState.Idle)
    val state: StateFlow<UiState<PermissionData>> = _state

    suspend fun onEvent(event: PermissionEvent) {
        when (event) {
            is PermissionEvent.Load -> load(event.profile)
            is PermissionEvent.Grant -> mutate(event.profile) {
                permissionDriver.grant(event.profile, event.role, event.table, event.privilege)
            }
            is PermissionEvent.Revoke -> mutate(event.profile) {
                permissionDriver.revoke(event.profile, event.role, event.table, event.privilege)
            }
        }
    }

    private suspend fun mutate(profile: ConnectionProfile, action: suspend () -> Result<Unit>) {
        action()
            .onSuccess { load(profile) }
            .onFailure { _state.value = UiState.Error(it.message ?: "Operation failed") }
    }

    private suspend fun load(profile: ConnectionProfile) {
        _state.value = UiState.Loading
        val rolesResult = permissionDriver.listRoles(profile)
        val privilegesResult = permissionDriver.listPrivileges(profile)
        val tablesResult = sqlDriver.listTables(profile)

        val roles = rolesResult.getOrNull()
        val privileges = privilegesResult.getOrNull()
        val tables = tablesResult.getOrNull()

        if (roles != null && privileges != null && tables != null) {
            _state.value = UiState.Success(PermissionData(roles, tables.map { it.name }, privileges))
        } else {
            val error = rolesResult.exceptionOrNull() ?: privilegesResult.exceptionOrNull() ?: tablesResult.exceptionOrNull()
            _state.value = UiState.Error(error?.message ?: "Failed to load permissions")
        }
    }
}
