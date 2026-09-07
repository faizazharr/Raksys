package com.raksys.core.model

import kotlinx.serialization.Serializable

enum class DbType {
    POSTGRES,
    MYSQL,
    SQLITE,
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
)
