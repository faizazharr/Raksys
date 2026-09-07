package com.raksys.core.model

import kotlinx.serialization.Serializable

enum class DbFamily {
    RELATIONAL,
    DOCUMENT,
    KEY_VALUE,
}

enum class DbType(val family: DbFamily) {
    POSTGRES(DbFamily.RELATIONAL),
    MYSQL(DbFamily.RELATIONAL),
    SQLITE(DbFamily.RELATIONAL),
    MONGODB(DbFamily.DOCUMENT),
    REDIS(DbFamily.KEY_VALUE),
}

enum class SshAuthMethod {
    PASSWORD,
    PRIVATE_KEY,
}

enum class EnvironmentType {
    DEVELOPMENT,
    STAGING,
    PRODUCTION,
}

@Serializable
data class ConnectionProfile(
    val id: String,
    val name: String,
    val dbType: DbType,
    val host: String = "",
    val port: Int = 0,
    val database: String = "",
    val username: String = "",
    val sslEnabled: Boolean = false,
    val sshEnabled: Boolean = false,
    val sshHost: String = "",
    val sshPort: Int = 22,
    val sshUsername: String = "",
    val sshAuthMethod: SshAuthMethod = SshAuthMethod.PASSWORD,
    val sshPrivateKeyPath: String = "",
    val environment: EnvironmentType = EnvironmentType.DEVELOPMENT,
)
