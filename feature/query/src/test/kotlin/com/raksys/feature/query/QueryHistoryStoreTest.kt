package com.raksys.feature.query

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QueryHistoryStoreTest {

    private fun tempDir(): File = kotlin.io.path.createTempDirectory("raksys-history-test").toFile().apply { deleteOnExit() }

    private fun item(sql: String) = QueryHistoryItem(sql = sql, timestamp = 1L, executionTimeMs = 5, rowCount = 2)

    @Test
    fun `history round-trips through the file store`() {
        val dir = tempDir()
        FileQueryHistoryStore(dir).save("conn-1", listOf(item("SELECT 1"), item("SELECT 2")))

        val loaded = FileQueryHistoryStore(dir).load("conn-1")
        assertEquals(listOf("SELECT 1", "SELECT 2"), loaded.map { it.sql })
    }

    @Test
    fun `each connection has its own history`() {
        val store = FileQueryHistoryStore(tempDir())
        store.save("a", listOf(item("SELECT a")))
        store.save("b", listOf(item("SELECT b")))
        assertEquals("SELECT a", store.load("a").single().sql)
        assertEquals("SELECT b", store.load("b").single().sql)
    }

    @Test
    fun `saving an empty list deletes the file`() {
        val dir = tempDir()
        val store = FileQueryHistoryStore(dir)
        store.save("a", listOf(item("SELECT 1")))
        assertTrue(File(dir, "a.json").exists())
        store.save("a", emptyList())
        assertFalse(File(dir, "a.json").exists())
    }

    @Test
    fun `a corrupt file loads as empty history instead of crashing`() {
        val dir = tempDir()
        File(dir, "a.json").writeText("{ not json")
        assertEquals(emptyList(), FileQueryHistoryStore(dir).load("a"))
    }

    @Test
    fun `a profile id cannot escape the history directory`() {
        val dir = tempDir()
        val store = FileQueryHistoryStore(dir)
        store.save("../../evil", listOf(item("SELECT 1")))
        assertTrue(dir.listFiles().orEmpty().any { it.name == "evil.json" })
        assertFalse(File(dir.parentFile.parentFile, "evil.json").exists())
    }
}
