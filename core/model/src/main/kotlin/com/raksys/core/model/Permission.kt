package com.raksys.core.model

enum class PrivilegeType {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
}

data class DatabaseRole(
    val name: String,
    val canLogin: Boolean,
    val isSuperuser: Boolean,
)

data class TablePrivilege(
    val roleName: String,
    val tableName: String,
    val privileges: Set<PrivilegeType>,
)
