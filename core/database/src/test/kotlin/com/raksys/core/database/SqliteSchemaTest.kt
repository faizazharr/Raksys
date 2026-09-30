package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.security.CredentialStore
import kotlinx.coroutines.runBlocking
import java.io.File
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class NoSecrets : CredentialStore {
    override fun save(key: String, secret: String) {}
    override fun get(key: String): String? = null
    override fun delete(key: String) {}
}

class SqliteSchemaTest {

    private fun dbWithTables(count: Int): File {
        val file = File.createTempFile("raksys-schema-test", ".db").apply { deleteOnExit() }
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { conn ->
            conn.createStatement().use { st ->
                st.execute("CREATE TABLE t0 (id INTEGER PRIMARY KEY, name TEXT)")
                for (i in 1 until count) {
                    st.execute("CREATE TABLE t$i (id INTEGER PRIMARY KEY, parent_id INTEGER REFERENCES t0(id), a TEXT, b TEXT, c TEXT)")
                }
            }
        }
        return file
    }

    private fun driver() = JdbcDatabaseDriver(NoSecrets(), SshTunnelManager(NoSecrets()))

    private fun profile(file: File) =
        ConnectionProfile(id = "p-${file.name}", name = "t", dbType = DbType.SQLITE, database = file.absolutePath)

    @Test
    fun `a schema with many tables still loads (bulk column reads exceed SQLite's compound SELECT limit)`() = runBlocking {
        val file = dbWithTables(120)
        val drv = driver()
        val tables = drv.listTables(profile(file)).getOrThrow()
        drv.close()

        assertEquals(120, tables.size)
        assertTrue(tables.all { it.columns.isNotEmpty() })
    }

    @Test
    fun `columns keys and foreign keys are attached to the right table`() = runBlocking {
        val file = dbWithTables(3)
        val drv = driver()
        val t1 = drv.listTables(profile(file)).getOrThrow().first { it.name == "t1" }
        drv.close()

        assertEquals(listOf("id", "parent_id", "a", "b", "c"), t1.columns.map { it.name })
        assertTrue(t1.columns.first { it.name == "id" }.isPrimaryKey)
        assertEquals("t0", t1.columns.first { it.name == "parent_id" }.foreignKey?.referencedTable)
    }

    @Test
    fun `a table name with an underscore does not pick up another table's columns`() = runBlocking {
        val file = File.createTempFile("raksys-underscore-test", ".db").apply { deleteOnExit() }
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { conn ->
            conn.createStatement().use { st ->
                st.execute("CREATE TABLE user_data (id INTEGER PRIMARY KEY, a TEXT)")
                st.execute("CREATE TABLE userXdata (id INTEGER PRIMARY KEY, b TEXT, c TEXT)")
            }
        }
        val drv = driver()
        val tables = drv.listTables(profile(file)).getOrThrow().associateBy { it.name }
        drv.close()

        assertEquals(listOf("id", "a"), tables.getValue("user_data").columns.map { it.name })
        assertEquals(listOf("id", "b", "c"), tables.getValue("userXdata").columns.map { it.name })
    }
}
