package com.raksys.core.security

interface CredentialStore {
    fun save(key: String, secret: String)
    fun get(key: String): String?
    fun delete(key: String)
}
