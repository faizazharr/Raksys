package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import kotlin.test.Test
import kotlin.test.assertEquals

class JdbcUrlTest {

    private fun profile(dbType: DbType, sslEnabled: Boolean = false, database: String = "mydb") = ConnectionProfile(
        id = "test",
        name = "Test",
        dbType = dbType,
        database = database,
        sslEnabled = sslEnabled,
    )

    @Test
    fun `postgres url without ssl`() {
        val url = jdbcUrl(profile(DbType.POSTGRES), "localhost", 5432)
        assertEquals("jdbc:postgresql://localhost:5432/mydb", url)
    }

    @Test
    fun `postgres url with ssl appends sslmode require`() {
        val url = jdbcUrl(profile(DbType.POSTGRES, sslEnabled = true), "db.example.com", 5432)
        assertEquals("jdbc:postgresql://db.example.com:5432/mydb?sslmode=require", url)
    }

    @Test
    fun `mysql url with ssl appends useSSL params`() {
        val url = jdbcUrl(profile(DbType.MYSQL, sslEnabled = true), "db.example.com", 3306)
        assertEquals("jdbc:mysql://db.example.com:3306/mydb?useSSL=true&requireSSL=true", url)
    }

    @Test
    fun `sqlite url ignores host, port and ssl`() {
        val url = jdbcUrl(profile(DbType.SQLITE, sslEnabled = true, database = "/tmp/app.db"), "unused", 0)
        assertEquals("jdbc:sqlite:/tmp/app.db", url)
    }

    @Test
    fun `resolved host and port from ssh tunnel are used verbatim`() {
        val url = jdbcUrl(profile(DbType.POSTGRES), "127.0.0.1", 54321)
        assertEquals("jdbc:postgresql://127.0.0.1:54321/mydb", url)
    }
}
