package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.RedisEntry
import com.raksys.core.security.CredentialStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.params.ScanParams
import java.util.concurrent.ConcurrentHashMap

/** Elements of a hash / list / set / zset shown per key; the rest is not fetched. */
private const val VALUE_PREVIEW_ELEMENTS = 100

class RedisKeyValueDriver(
    private val credentialStore: CredentialStore,
    private val sshTunnelManager: SshTunnelManager,
) : KeyValueDriver {

    private val pools = ConcurrentHashMap<String, JedisPool>()
    @Volatile private var activeProfileId: String? = null

    private fun poolFor(profile: ConnectionProfile): JedisPool =
        pools.getOrPut(profile.id) {
            val (resolvedHost, resolvedPort) = sshTunnelManager.resolve(profile)
            val password = credentialStore.get(profile.id).orEmpty().ifBlank { null }
            val database = profile.database.toIntOrNull() ?: 0
            JedisPool(JedisPoolConfig(), resolvedHost, resolvedPort, 10_000, password, database)
        }

    override suspend fun testConnection(profile: ConnectionProfile): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                activeProfileId = profile.id
                poolFor(profile).resource.use { it.ping() }
                Unit
            }
        }

    override suspend fun scanKeys(profile: ConnectionProfile, pattern: String, limit: Int): Result<List<RedisEntry>> =
        withContext(Dispatchers.IO) {
            runCatching {
                activeProfileId = profile.id
                poolFor(profile).resource.use { jedis ->
                    // 1) Collect up to `limit` key names with cursor-based SCAN (never KEYS).
                    val keys = LinkedHashSet<String>()
                    var cursor = ScanParams.SCAN_POINTER_START
                    val scanParams = ScanParams().match(pattern).count(100)
                    do {
                        val result = jedis.scan(cursor, scanParams)
                        cursor = result.cursor
                        for (key in result.result) {
                            if (keys.size >= limit) break
                            keys += key
                        }
                    } while (cursor != ScanParams.SCAN_POINTER_START && keys.size < limit)
                    if (keys.isEmpty()) return@use emptyList<RedisEntry>()

                    val keyList = keys.toList()

                    // 2) Types and TTLs for every key in ONE round trip instead of two per key.
                    val (types, ttls) = jedis.pipelined().use { pipe ->
                        val typeResponses = keyList.map { pipe.type(it) }
                        val ttlResponses = keyList.map { pipe.ttl(it) }
                        pipe.sync()
                        typeResponses.map { it.get() } to ttlResponses.map { it.get() }
                    }

                    // 3) Values, again in one round trip. Collections read only their first page so a
                    //    key holding millions of elements cannot exhaust memory.
                    val valueParams = ScanParams().count(VALUE_PREVIEW_ELEMENTS)
                    val lastIndex = (VALUE_PREVIEW_ELEMENTS - 1).toLong()
                    val values = jedis.pipelined().use { pipe ->
                        val readers: List<() -> String> = keyList.mapIndexed { index, key ->
                            when (val type = types[index]) {
                                "string" -> pipe.get(key).let { r -> { r.get().orEmpty() } }
                                "hash" -> pipe.hscan(key, ScanParams.SCAN_POINTER_START, valueParams)
                                    .let { r -> { formatRedisHash(r.get().result.associate { e -> e.key to e.value }) } }
                                "list" -> pipe.lrange(key, 0, lastIndex).let { r -> { formatRedisList(r.get()) } }
                                "set" -> pipe.sscan(key, ScanParams.SCAN_POINTER_START, valueParams)
                                    .let { r -> { formatRedisSet(r.get().result.toSet()) } }
                                "zset" -> pipe.zrange(key, 0, lastIndex).let { r -> { formatRedisList(r.get().toList()) } }
                                else -> { { "(unsupported type: $type)" } }
                            }
                        }
                        pipe.sync()
                        readers.map { it() }
                    }

                    keyList.mapIndexed { index, key ->
                        RedisEntry(key, types[index], values[index], ttls[index].takeIf { it >= 0 })
                    }
                }
            }
        }

    override fun cancel() {
        // Jedis has no cooperative cancel mid-command. Closing the active pool forces the
        // blocked socket read to fail; the pool reconnects lazily on the next call.
        activeProfileId?.let { invalidate(it) }
    }

    override fun invalidate(profileId: String) {
        pools.remove(profileId)?.close()
        sshTunnelManager.invalidate(profileId)
    }

    override fun close() {
        pools.values.forEach { it.close() }
        pools.clear()
    }
}
