package com.raksys.core.security

import com.github.javakeyring.BackendNotSupportedException
import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException

private const val SERVICE = "com.raksys.dbtool"

/**
 * Stores secrets in the OS keyring (Apple Keychain, Windows Credential Manager, Secret Service).
 *
 * A machine without a keyring backend (for example a Linux desktop with no GNOME Keyring or KWallet
 * running) must not break connections that need no secret, such as SQLite or a passwordless local
 * server. So reads and deletes treat a missing backend as "nothing stored"; only *saving* a secret
 * fails, with a message that says how to fix it, because silently dropping a password would be worse.
 */
class KeyringCredentialStore : CredentialStore {

    override fun save(key: String, secret: String) {
        try {
            Keyring.create().use { it.setPassword(SERVICE, key, secret) }
        } catch (e: BackendNotSupportedException) {
            throw IllegalStateException(
                "Keyring OS tidak tersedia, jadi password tidak bisa disimpan aman. " +
                    "Di Linux, jalankan GNOME Keyring atau KWallet (Secret Service), lalu coba lagi.",
                e,
            )
        }
    }

    override fun get(key: String): String? {
        val keyring = try {
            Keyring.create()
        } catch (e: BackendNotSupportedException) {
            return null
        }
        keyring.use {
            return try {
                it.getPassword(SERVICE, key)
            } catch (e: PasswordAccessException) {
                null
            }
        }
    }

    override fun delete(key: String) {
        val keyring = try {
            Keyring.create()
        } catch (e: BackendNotSupportedException) {
            return
        }
        keyring.use { it.deletePassword(SERVICE, key) }
    }
}
