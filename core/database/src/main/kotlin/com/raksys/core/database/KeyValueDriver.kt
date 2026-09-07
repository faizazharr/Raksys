package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.RedisEntry

interface KeyValueDriver {
    suspend fun testConnection(profile: ConnectionProfile): Result<Unit>
    suspend fun scanKeys(profile: ConnectionProfile, pattern: String = "*", limit: Int = 200): Result<List<RedisEntry>>
    fun cancel()
    fun invalidate(profileId: String)
    fun close()
}
