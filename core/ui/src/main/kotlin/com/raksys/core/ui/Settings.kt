package com.raksys.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** How the app picks light or dark. SYSTEM follows the operating system, which is the default. */
enum class Appearance { SYSTEM, LIGHT, DARK }

@Serializable
data class SettingsData(
    val appearance: Appearance = Appearance.SYSTEM,
    /** Keep each connection's query history on disk between launches. */
    val saveQueryHistory: Boolean = true,
)

/** Reads and writes [SettingsData] as JSON. A missing or unreadable file means defaults, never a crash. */
class SettingsStore(private val file: File) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun read(): SettingsData =
        runCatching { json.decodeFromString<SettingsData>(file.readText()) }.getOrDefault(SettingsData())

    fun write(data: SettingsData) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(SettingsData.serializer(), data))
        }
    }
}

/**
 * The app's live settings. Properties are Compose state, so screens that read them update when a
 * setting changes; every change is written to `~/.raksys/settings.json` straight away.
 */
object RaksysSettings {
    private var store = SettingsStore(File(System.getProperty("user.home"), ".raksys/settings.json"))

    var appearance by mutableStateOf(Appearance.SYSTEM)
        private set
    var saveQueryHistory by mutableStateOf(true)
        private set

    /** Call once at startup, before the first window is composed. */
    fun load(from: SettingsStore = store) {
        store = from
        val data = from.read()
        appearance = data.appearance
        saveQueryHistory = data.saveQueryHistory
    }

    fun changeAppearance(value: Appearance) {
        appearance = value
        persist()
    }

    fun changeSaveQueryHistory(value: Boolean) {
        saveQueryHistory = value
        persist()
    }

    private fun persist() = store.write(SettingsData(appearance, saveQueryHistory))
}
