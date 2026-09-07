package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.model.DatabaseRole
import com.raksys.core.model.PrivilegeType
import com.raksys.core.model.TablePrivilege

internal fun quotedIdentifier(dbType: DbType, name: String): String = when (dbType) {
    DbType.MYSQL -> "`${name.replace("`", "``")}`"
    else -> "\"${name.replace("\"", "\"\"")}\""
}

/**
 * Real DB user/role privilege management (GRANT/REVOKE), backed by each engine's own catalog.
 * SQLite has no user/permission system at all, so it always fails with a clear message here
 * rather than silently no-op-ing.
 */
class JdbcPermissionDriver(private val sqlDriver: DatabaseDriver) : PermissionDriver {

    override suspend fun listRoles(profile: ConnectionProfile): Result<List<DatabaseRole>> = when (profile.dbType) {
        DbType.POSTGRES -> sqlDriver.executeQuery(profile, "SELECT rolname, rolcanlogin, rolsuper FROM pg_roles ORDER BY rolname")
            .map { result ->
                result.rows.map { row ->
                    DatabaseRole(
                        name = row[0].toString(),
                        canLogin = row[1] as? Boolean ?: row[1].toString().toBoolean(),
                        isSuperuser = row[2] as? Boolean ?: row[2].toString().toBoolean(),
                    )
                }
            }

        DbType.MYSQL -> sqlDriver.executeQuery(
            profile,
            "SELECT GRANTEE, PRIVILEGE_TYPE FROM information_schema.USER_PRIVILEGES ORDER BY GRANTEE",
        ).map { result ->
            result.rows
                .groupBy { it[0].toString() }
                .map { (grantee, rows) ->
                    DatabaseRole(
                        name = grantee.trim('\'').replace("'@'", "@"),
                        canLogin = true,
                        isSuperuser = rows.any { it[1].toString().equals("SUPER", ignoreCase = true) },
                    )
                }
        }

        else -> Result.failure(UnsupportedOperationException("${profile.dbType} has no user/role permission system"))
    }

    override suspend fun listPrivileges(profile: ConnectionProfile): Result<List<TablePrivilege>> = when (profile.dbType) {
        DbType.POSTGRES -> sqlDriver.executeQuery(
            profile,
            "SELECT grantee, table_name, privilege_type FROM information_schema.role_table_grants " +
                "WHERE table_schema = 'public' ORDER BY grantee, table_name",
        ).map { result -> groupIntoPrivileges(result.rows) }

        DbType.MYSQL -> sqlDriver.executeQuery(
            profile,
            "SELECT GRANTEE, TABLE_NAME, PRIVILEGE_TYPE FROM information_schema.TABLE_PRIVILEGES " +
                "WHERE TABLE_SCHEMA = DATABASE() ORDER BY GRANTEE, TABLE_NAME",
        ).map { result ->
            groupIntoPrivileges(
                result.rows.map { row -> listOf(row[0].toString().trim('\'').replace("'@'", "@"), row[1], row[2]) }
            )
        }

        else -> Result.failure(UnsupportedOperationException("${profile.dbType} has no user/role permission system"))
    }

    private fun groupIntoPrivileges(rows: List<List<Any?>>): List<TablePrivilege> =
        rows.groupBy { it[0].toString() to it[1].toString() }
            .map { (key, groupRows) ->
                val (role, table) = key
                val privileges = groupRows.mapNotNull { row ->
                    runCatching { PrivilegeType.valueOf(row[2].toString().uppercase()) }.getOrNull()
                }.toSet()
                TablePrivilege(roleName = role, tableName = table, privileges = privileges)
            }

    override suspend fun grant(profile: ConnectionProfile, roleName: String, tableName: String, privilege: PrivilegeType): Result<Unit> {
        if (profile.dbType == DbType.SQLITE) {
            return Result.failure(UnsupportedOperationException("SQLite has no user/role permission system"))
        }
        val sql = "GRANT ${privilege.name} ON ${quotedIdentifier(profile.dbType, tableName)} TO ${roleTarget(profile.dbType, roleName)}"
        return sqlDriver.executeStatement(profile, sql)
    }

    override suspend fun revoke(profile: ConnectionProfile, roleName: String, tableName: String, privilege: PrivilegeType): Result<Unit> {
        if (profile.dbType == DbType.SQLITE) {
            return Result.failure(UnsupportedOperationException("SQLite has no user/role permission system"))
        }
        val sql = "REVOKE ${privilege.name} ON ${quotedIdentifier(profile.dbType, tableName)} FROM ${roleTarget(profile.dbType, roleName)}"
        return sqlDriver.executeStatement(profile, sql)
    }

    private fun roleTarget(dbType: DbType, roleName: String): String = when (dbType) {
        DbType.MYSQL -> {
            val (user, host) = roleName.split("@", limit = 2).let { it[0] to it.getOrElse(1) { "%" } }
            "'${user.replace("'", "''")}'@'${host.replace("'", "''")}'"
        }
        else -> quotedIdentifier(dbType, roleName)
    }
}
