package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertContains

class ConnectionErrorMapperTest {

    private val profile = ConnectionProfile(
        id = "test",
        name = "Test",
        dbType = DbType.POSTGRES,
        host = "db.example.com",
        port = 5432,
        database = "mydb",
    )

    @Test
    fun `connection refused mentions the host and port, not a raw stack message`() {
        val message = friendlyConnectionError(profile, ConnectException("Connection refused"))
        assertContains(message, "db.example.com:5432")
    }

    @Test
    fun `unknown host is reported as a host problem`() {
        val message = friendlyConnectionError(profile, UnknownHostException("db.example.com"))
        assertContains(message, "gak ditemukan")
    }

    @Test
    fun `database already exists maps to a create-mode-specific hint`() {
        val message = friendlyConnectionError(profile, RuntimeException("database \"mydb\" already exists"))
        assertContains(message, "udah ada")
    }

    @Test
    fun `wrong credentials are reported clearly`() {
        val message = friendlyConnectionError(profile, RuntimeException("FATAL: password authentication failed for user \"x\""))
        assertContains(message, "salah")
    }

    @Test
    fun `unrecognized message falls back to the raw message`() {
        val message = friendlyConnectionError(profile, RuntimeException("some unmapped driver error"))
        assertContains(message, "some unmapped driver error")
    }

    @Test
    fun `blank message falls back to a generic message instead of empty text`() {
        val message = friendlyConnectionError(profile, RuntimeException(""))
        assertContains(message, "Gagal connect")
    }
}
