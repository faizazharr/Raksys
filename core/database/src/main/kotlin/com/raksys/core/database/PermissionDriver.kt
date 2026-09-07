package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DatabaseRole
import com.raksys.core.model.PrivilegeType
import com.raksys.core.model.TablePrivilege

interface PermissionDriver {
    suspend fun listRoles(profile: ConnectionProfile): Result<List<DatabaseRole>>
    suspend fun listPrivileges(profile: ConnectionProfile): Result<List<TablePrivilege>>
    suspend fun grant(profile: ConnectionProfile, roleName: String, tableName: String, privilege: PrivilegeType): Result<Unit>
    suspend fun revoke(profile: ConnectionProfile, roleName: String, tableName: String, privilege: PrivilegeType): Result<Unit>
}
