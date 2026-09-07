package com.raksys.core.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.raksys.core.model.CollectionInfo
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.MongoDocument
import com.raksys.core.security.CredentialStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

internal fun urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

internal fun mongoConnectionString(profile: ConnectionProfile, resolvedHost: String, resolvedPort: Int, password: String): String {
    val credentials = if (profile.username.isNotBlank()) {
        "${urlEncode(profile.username)}:${urlEncode(password)}@"
    } else {
        ""
    }
    val sslParam = if (profile.sslEnabled) "?ssl=true" else ""
    return "mongodb://$credentials$resolvedHost:$resolvedPort/${profile.database}$sslParam"
}

class MongoDatabaseDriver(
    private val credentialStore: CredentialStore,
    private val sshTunnelManager: SshTunnelManager,
) : DocumentDatabaseDriver {

    private val clients = ConcurrentHashMap<String, MongoClient>()
    @Volatile private var activeProfileId: String? = null

    private fun clientFor(profile: ConnectionProfile): MongoClient =
        clients.getOrPut(profile.id) {
            val (resolvedHost, resolvedPort) = sshTunnelManager.resolve(profile)
            val password = credentialStore.get(profile.id).orEmpty()
            val settings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(mongoConnectionString(profile, resolvedHost, resolvedPort, password)))
                .applyToSocketSettings {
                    it.connectTimeout(10_000, TimeUnit.MILLISECONDS)
                    it.readTimeout(30_000, TimeUnit.MILLISECONDS)
                }
                .build()
            MongoClients.create(settings)
        }

    override suspend fun testConnection(profile: ConnectionProfile): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                activeProfileId = profile.id
                clientFor(profile).getDatabase(profile.database).runCommand(org.bson.Document("ping", 1))
                Unit
            }
        }

    override suspend fun listCollections(profile: ConnectionProfile): Result<List<CollectionInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                activeProfileId = profile.id
                val db = clientFor(profile).getDatabase(profile.database)
                db.listCollectionNames().toList().map { name ->
                    val count = runCatching { db.getCollection(name).estimatedDocumentCount() }.getOrNull()
                    CollectionInfo(name, count)
                }
            }
        }

    override suspend fun findDocuments(profile: ConnectionProfile, collection: String, limit: Int): Result<List<MongoDocument>> =
        withContext(Dispatchers.IO) {
            runCatching {
                activeProfileId = profile.id
                clientFor(profile).getDatabase(profile.database).getCollection(collection)
                    .find()
                    .limit(limit)
                    .map { doc -> MongoDocument(id = doc.get("_id")?.toString().orEmpty(), json = doc.toJson()) }
                    .toList()
            }
        }

    override fun cancel() {
        // mongodb-driver-sync has no cooperative cancel for an in-flight blocking call.
        // Closing the active client forces the blocked socket read to fail; the client
        // reconnects lazily on the next call via clientFor().
        activeProfileId?.let { invalidate(it) }
    }

    override fun invalidate(profileId: String) {
        clients.remove(profileId)?.close()
        sshTunnelManager.invalidate(profileId)
    }

    override fun close() {
        clients.values.forEach { it.close() }
        clients.clear()
    }
}
