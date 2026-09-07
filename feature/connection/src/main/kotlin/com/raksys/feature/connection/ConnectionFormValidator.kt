package com.raksys.feature.connection

import com.raksys.core.model.DbType
import com.raksys.core.model.SshAuthMethod

data class ConnectionFormFields(
    val dbType: DbType,
    val host: String,
    val port: String,
    val database: String,
    val sshEnabled: Boolean,
    val sshHost: String,
    val sshUsername: String,
    val sshAuthMethod: SshAuthMethod,
    val sshPassword: String,
    val sshPrivateKeyPath: String,
)

/** Returns a human-readable validation error, or null if the form is valid enough to save. */
fun validateConnectionForm(fields: ConnectionFormFields): String? {
    val sshApplicable = fields.dbType != DbType.SQLITE
    return when {
        fields.dbType == DbType.SQLITE && fields.database.isBlank() -> "Path file database wajib diisi"
        fields.dbType != DbType.SQLITE && fields.host.isBlank() -> "Host wajib diisi"
        fields.dbType != DbType.SQLITE && (fields.port.toIntOrNull() == null || fields.port.toIntOrNull() !in 1..65535) ->
            "Port harus angka 1-65535"
        fields.dbType == DbType.MONGODB && fields.database.isBlank() -> "Nama database wajib diisi untuk MongoDB"
        sshApplicable && fields.sshEnabled && fields.sshHost.isBlank() -> "SSH host wajib diisi"
        sshApplicable && fields.sshEnabled && fields.sshUsername.isBlank() -> "SSH username wajib diisi"
        sshApplicable && fields.sshEnabled && fields.sshAuthMethod == SshAuthMethod.PASSWORD && fields.sshPassword.isBlank() ->
            "SSH password wajib diisi"
        sshApplicable && fields.sshEnabled && fields.sshAuthMethod == SshAuthMethod.PRIVATE_KEY && fields.sshPrivateKeyPath.isBlank() ->
            "Path private key wajib diisi"
        else -> null
    }
}
