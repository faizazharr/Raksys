package com.raksys.core.security

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException

private const val SERVICE = "com.raksys.dbtool"

class KeyringCredentialStore : CredentialStore {

    override fun save(key: String, secret: String) {
        Keyring.create().use { it.setPassword(SERVICE, key, secret) }
    }

    override fun get(key: String): String? {
        Keyring.create().use {
            return try {
                it.getPassword(SERVICE, key)
            } catch (e: PasswordAccessException) {
                null
            }
        }
    }

    override fun delete(key: String) {
        Keyring.create().use { it.deletePassword(SERVICE, key) }
    }
}
