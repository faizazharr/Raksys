package com.raksys.feature.connection

import com.raksys.core.model.ConnectionProfile

sealed interface ConnectionEvent {
    data class Test(val profile: ConnectionProfile) : ConnectionEvent
    data class Add(
        val profile: ConnectionProfile,
        val password: String,
        val sshPassword: String = "",
        val createNew: Boolean = false,
    ) : ConnectionEvent
    data class Delete(val profile: ConnectionProfile) : ConnectionEvent
    data object ResetSaveState : ConnectionEvent
}
