package com.raksys.core.model

data class CollectionInfo(
    val name: String,
    val approximateCount: Long? = null,
)

data class MongoDocument(
    val id: String,
    val json: String,
)

data class RedisEntry(
    val key: String,
    val type: String,
    val value: String,
    val ttlSeconds: Long? = null,
)
