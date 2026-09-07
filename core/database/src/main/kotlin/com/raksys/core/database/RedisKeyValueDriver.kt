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
                    val entries = mutableListOf<RedisEntry>()
                    var cursor = ScanParams.SCAN_POINTER_START
                    val params = ScanParams().match(pattern).count(100)
                    do {
                        val result = jedis.scan(cursor, params)
                        cursor = result.cursor
                        for (key in result.result) {
                            if (entries.size >= limit) break
                            val type = jedis.type(key)
                            val value = when (type) {
                                "string" -> jedis.get(key)
                                "hash" -> formatRedisHash(jedis.hgetAll(key))
                                "list" -> formatRedisList(jedis.lrange(key, 0, -1))
                                "set" -> formatRedisSet(jedis.smembers(key))
                                "zset" -> formatRedisList(jedis.zrange(key, 0, -1).toList())
                                else -> "(unsupported type: $type)"
                            }
                            val ttl = jedis.ttl(key).takeIf { it >= 0 }
                            entries += RedisEntry(key, type, value.orEmpty(), ttl)
                        }
                    } while (cursor != ScanParams.SCAN_POINTER_START && entries.size < limit)
                    entries
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
