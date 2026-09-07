package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MongoConnectionStringTest {

    private fun profile(username: String, sslEnabled: Boolean = false) = ConnectionProfile(
        id = "test",
        name = "Test",
        dbType = DbType.MONGODB,
        database = "mydb",
        username = username,
        sslEnabled = sslEnabled,
    )

    @Test
    fun `password with special characters is url-encoded, not left raw`() {
        // Regression test for the bug where an unencoded password containing '@' or ':'
        // broke connection string parsing (host/port were read from inside the password).
        val connectionString = mongoConnectionString(profile("admin"), "localhost", 27017, "p@ss:word/1%")

        assertFalse(connectionString.contains("p@ss:word/1%"), "raw special characters must not appear unescaped")
        assertEquals(
            "mongodb://admin:${urlEncode("p@ss:word/1%")}@localhost:27017/mydb",
            connectionString,
        )
    }

    @Test
    fun `no username means no credentials segment`() {
        val connectionString = mongoConnectionString(profile(""), "localhost", 27017, "unused")
        assertEquals("mongodb://localhost:27017/mydb", connectionString)
    }

    @Test
    fun `ssl enabled appends ssl query param`() {
        val connectionString = mongoConnectionString(profile("", sslEnabled = true), "cluster.example.com", 27017, "")
        assertEquals("mongodb://cluster.example.com:27017/mydb?ssl=true", connectionString)
    }

    @Test
    fun `urlEncode escapes reserved connection-string characters`() {
        assertEquals("p%40ss%3Aword%2F1%25", urlEncode("p@ss:word/1%"))
    }
}
