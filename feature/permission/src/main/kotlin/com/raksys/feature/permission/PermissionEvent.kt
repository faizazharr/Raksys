package com.raksys.feature.permission

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.PrivilegeType

sealed interface PermissionEvent {
    data class Load(val profile: ConnectionProfile) : PermissionEvent
    data class Grant(val profile: ConnectionProfile, val role: String, val table: String, val privilege: PrivilegeType) : PermissionEvent
    data class Revoke(val profile: ConnectionProfile, val role: String, val table: String, val privilege: PrivilegeType) : PermissionEvent
}
