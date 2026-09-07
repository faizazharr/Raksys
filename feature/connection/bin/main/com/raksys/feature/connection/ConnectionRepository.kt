package com.raksys.feature.connection

import com.raksys.core.model.ConnectionProfile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ConnectionRepository(
    private val storeFile: File = File(System.getProperty("user.home"), ".raksys/connections.json"),
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun list(): List<ConnectionProfile> {
        if (!storeFile.exists()) return emptyList()
        return json.decodeFromString<List<ConnectionProfile>>(storeFile.readText())
    }

    fun save(profile: ConnectionProfile) {
        val updated = list().filterNot { it.id == profile.id } + profile
        write(updated)
    }

    fun delete(id: String) {
        write(list().filterNot { it.id == id })
    }

    private fun write(profiles: List<ConnectionProfile>) {
        storeFile.parentFile?.mkdirs()
        storeFile.writeText(json.encodeToString(profiles))
    }
}
