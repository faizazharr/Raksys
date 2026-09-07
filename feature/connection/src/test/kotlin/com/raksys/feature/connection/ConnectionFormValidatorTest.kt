package com.raksys.feature.connection

import com.raksys.core.model.DbType
import com.raksys.core.model.SshAuthMethod
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ConnectionFormValidatorTest {

    private fun validFields(dbType: DbType = DbType.POSTGRES) = ConnectionFormFields(
        dbType = dbType,
        host = "localhost",
        port = "5432",
        database = "mydb",
        sshEnabled = false,
        sshHost = "",
        sshUsername = "",
        sshAuthMethod = SshAuthMethod.PASSWORD,
        sshPassword = "",
        sshPrivateKeyPath = "",
    )

    @Test
    fun `valid postgres form passes`() {
        assertNull(validateConnectionForm(validFields()))
    }

    @Test
    fun `blank host fails for non-sqlite`() {
        assertNotNull(validateConnectionForm(validFields().copy(host = "")))
    }

    @Test
    fun `invalid port fails`() {
        assertNotNull(validateConnectionForm(validFields().copy(port = "not-a-number")))
        assertNotNull(validateConnectionForm(validFields().copy(port = "99999")))
        assertNotNull(validateConnectionForm(validFields().copy(port = "0")))
    }

    @Test
    fun `sqlite only requires database path, not host or port`() {
        val fields = validFields(DbType.SQLITE).copy(host = "", port = "", database = "/tmp/app.db")
        assertNull(validateConnectionForm(fields))
    }

    @Test
    fun `sqlite with blank database path fails`() {
        val fields = validFields(DbType.SQLITE).copy(host = "", port = "", database = "")
        assertNotNull(validateConnectionForm(fields))
    }

    @Test
    fun `mongodb requires a database name`() {
        val fields = validFields(DbType.MONGODB).copy(database = "")
        assertNotNull(validateConnectionForm(fields))
    }

    @Test
    fun `ssh enabled requires host, username and a credential`() {
        val base = validFields().copy(sshEnabled = true)
        assertNotNull(validateConnectionForm(base)) // missing ssh host

        val withHost = base.copy(sshHost = "bastion.example.com")
        assertNotNull(validateConnectionForm(withHost)) // missing ssh username

        val withUser = withHost.copy(sshUsername = "deploy")
        assertNotNull(validateConnectionForm(withUser)) // missing ssh password (auth method = PASSWORD)

        val complete = withUser.copy(sshPassword = "secret")
        assertNull(validateConnectionForm(complete))
    }

    @Test
    fun `ssh private key auth requires a key path instead of a password`() {
        val fields = validFields().copy(
            sshEnabled = true,
            sshHost = "bastion.example.com",
            sshUsername = "deploy",
            sshAuthMethod = SshAuthMethod.PRIVATE_KEY,
        )
        assertNotNull(validateConnectionForm(fields)) // missing key path

        assertNull(validateConnectionForm(fields.copy(sshPrivateKeyPath = "/Users/me/.ssh/id_rsa")))
    }
}
