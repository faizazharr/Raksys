package com.raksys.core.database

import com.raksys.core.model.CollectionInfo
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.MongoDocument

interface DocumentDatabaseDriver {
    suspend fun testConnection(profile: ConnectionProfile): Result<Unit>
    suspend fun listCollections(profile: ConnectionProfile): Result<List<CollectionInfo>>
    suspend fun findDocuments(profile: ConnectionProfile, collection: String, limit: Int = 100): Result<List<MongoDocument>>
    fun cancel()
    fun invalidate(profileId: String)
    fun close()
}
