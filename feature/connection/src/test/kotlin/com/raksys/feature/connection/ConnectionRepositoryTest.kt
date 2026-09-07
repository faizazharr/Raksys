package com.raksys.feature.connection

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConnectionRepositoryTest {

    private fun tempStoreFile(): File = File.createTempFile("raksys-connections-test", ".json").apply { deleteOnExit() }

    private fun profile(id: String, name: String = "Test $id") = ConnectionProfile(
        id = id,
        name = name,
        dbType = DbType.POSTGRES,
        host = "localhost",
        port = 5432,
    )

    @Test
    fun `list on missing file returns empty`() {
        val storeFile = File("/tmp/raksys-does-not-exist-${System.nanoTime()}.json")
        val repository = ConnectionRepository(storeFile)

        assertEquals(emptyList(), repository.list())
    }

    @Test
    fun `save then list returns the saved profile`() {
        val repository = ConnectionRepository(tempStoreFile())
        val profile = profile("a")

        repository.save(profile)

        assertEquals(listOf(profile), repository.list())
    }

    @Test
    fun `saving a profile with the same id replaces it, not duplicates it`() {
        val repository = ConnectionRepository(tempStoreFile())
        repository.save(profile("a", name = "Original"))
        repository.save(profile("a", name = "Renamed"))

        val result = repository.list()
        assertEquals(1, result.size)
        assertEquals("Renamed", result.single().name)
    }

    @Test
    fun `delete removes only the matching profile`() {
        val repository = ConnectionRepository(tempStoreFile())
        repository.save(profile("a"))
        repository.save(profile("b"))

        repository.delete("a")

        val result = repository.list()
        assertEquals(1, result.size)
        assertTrue(result.none { it.id == "a" })
        assertTrue(result.any { it.id == "b" })
    }
}
