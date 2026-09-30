package com.raksys.feature.query

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/** Where a connection's query history lives between app launches. Best effort: failures never surface. */
interface QueryHistoryStore {
    fun load(profileId: String): List<QueryHistoryItem>
    fun save(profileId: String, items: List<QueryHistoryItem>)
}

class InMemoryQueryHistoryStore : QueryHistoryStore {
    private val data = mutableMapOf<String, List<QueryHistoryItem>>()
    override fun load(profileId: String) = data[profileId].orEmpty()
    override fun save(profileId: String, items: List<QueryHistoryItem>) {
        data[profileId] = items
    }
}

/**
 * One JSON file per connection under `~/.raksys/history/`.
 *
 * History can contain literal values typed into queries, so the files are readable by the owner only
 * where the OS supports it, and clearing history in the app deletes the entries from disk.
 */
class FileQueryHistoryStore(
    private val dir: File = File(System.getProperty("user.home"), ".raksys/history"),
) : QueryHistoryStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(QueryHistoryItem.serializer())

    override fun load(profileId: String): List<QueryHistoryItem> {
        val file = fileFor(profileId)
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString(serializer, file.readText()) }.getOrDefault(emptyList())
    }

    override fun save(profileId: String, items: List<QueryHistoryItem>) {
        runCatching {
            val file = fileFor(profileId)
            if (items.isEmpty()) {
                file.delete()
                return
            }
            dir.mkdirs()
            file.writeText(json.encodeToString(serializer, items))
            // Owner-only where supported (POSIX); a no-op on Windows.
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
        }
    }

    /** Profile ids come from our own file, but never let one escape the history directory. */
    private fun fileFor(profileId: String): File {
        val safe = profileId.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifEmpty { "unknown" }
        return File(dir, "$safe.json")
    }
}
