package pl.soulsnaps.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.data.model.MemoryRow
import pl.soulsnaps.data.model.toRow
import pl.soulsnaps.data.model.toDomain
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.features.auth.UserSessionManager

class SupabaseMemoryDataSource(
    private val client: SupabaseClient,
    private val crashlyticsManager: CrashlyticsManager,
    private val memoryDao: MemoryDao,
    private val userSessionManager: UserSessionManager
) : OnlineDataSource {

    companion object {
        private const val TABLE = "memories"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    private suspend fun <T> withRetry(name: String, block: suspend () -> T): T? {
        var last: Exception? = null
        repeat(MAX_RETRY_ATTEMPTS) { i ->
            try { return block() } catch (e: Exception) {
                last = e
                crashlyticsManager.log("$name failed (${i+1}/$MAX_RETRY_ATTEMPTS): ${e.message}")
                if (i < MAX_RETRY_ATTEMPTS - 1) kotlinx.coroutines.delay(RETRY_DELAY_MS * (i + 1))
            }
        }
        crashlyticsManager.recordException(last ?: Exception("Unknown"))
        return null
    }

    override suspend fun getAllMemories(userId: String): List<Memory> =
        withRetry("getAllMemories") {
            client.from(TABLE).select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<MemoryRow>().map { it.toDomain() }
        } ?: emptyList()

    override suspend fun getMemoryById(id: Long): Memory? {
        return withRetry("getMemoryById") {
            // Get local memory to find remoteId
            val localMemory = memoryDao.getById(id)
            val remoteId = localMemory?.remoteId
            
            if (remoteId != null) {
                // Get current user
                val currentUser = userSessionManager.getCurrentUser()
                if (currentUser != null) {
                    // Use remoteId to get memory from Supabase
                    getMemoryByRemoteId(remoteId, currentUser.userId)
                } else {
                    crashlyticsManager.log("getMemoryById: No current user found")
                    null
                }
            } else {
                crashlyticsManager.log("getMemoryById: No remoteId found for local ID: $id")
                null
            }
        }
    }

    suspend fun getMemoryByRemoteId(remoteId: String, userId: String): Memory? =
        withRetry("getMemoryByRemoteId") {
            client.from(TABLE).select {
                filter { eq("id", remoteId); eq("user_id", userId) }
                limit(1)
            }.decodeList<MemoryRow>().firstOrNull()?.toDomain()
        }

    override suspend fun insertMemory(memory: Memory, userId: String): Long? =
        withRetry("insertMemory") {
            val inserted = client.from(TABLE).insert(memory.toRow(userId))
                .decodeSingle<MemoryRow>()
            // Zwracamy lokalny Long, ale zapisz inserted.id (uuid) do pola remoteId w lokalnej DB!
            (inserted.id?.hashCode() ?: 0).toLong()
        }

    override suspend fun updateMemory(memory: Memory, userId: String): Boolean =
        withRetry("updateMemory") {
            val remoteId = memory.remoteId ?: return@withRetry false
            val patch = Json.encodeToString(memory.toRow(userId)) // lub zbuduj tylko zmienione pola
            client.from(TABLE).update(patch) {
                filter { eq("id", remoteId); eq("user_id", userId) }
            }
            true
        } ?: false

    override suspend fun deleteMemory(id: Long, userId: String): Boolean {
        return withRetry("deleteMemory") {
            // Get local memory to find remoteId
            val localMemory = memoryDao.getById(id)
            val remoteId = localMemory?.remoteId
            
            if (remoteId != null) {
                // Use remoteId to delete memory from Supabase
                deleteMemoryByRemoteId(remoteId, userId)
            } else {
                crashlyticsManager.log("deleteMemory: No remoteId found for local ID: $id")
                false
            }
        } ?: false
    }

    suspend fun deleteMemoryByRemoteId(remoteId: String, userId: String): Boolean =
        withRetry("deleteMemoryByRemoteId") {
            client.from(TABLE).delete {
                filter { eq("id", remoteId); eq("user_id", userId) }
            }
            true
        } ?: false

    override suspend fun markAsFavorite(id: Long, isFavorite: Boolean, userId: String): Boolean {
        return withRetry("markAsFavorite") {
            // Get local memory to find remoteId
            val localMemory = memoryDao.getById(id)
            val remoteId = localMemory?.remoteId
            
            if (remoteId != null) {
                // Use remoteId to update favorite status in Supabase
                markAsFavoriteByRemoteId(remoteId, isFavorite, userId)
            } else {
                crashlyticsManager.log("markAsFavorite: No remoteId found for local ID: $id")
                false
            }
        } ?: false
    }

    suspend fun markAsFavoriteByRemoteId(remoteId: String, isFavorite: Boolean, userId: String): Boolean =
        withRetry("markAsFavoriteByRemoteId") {
            val patch = buildJsonObject { put("is_favorite", isFavorite) }
            client.from(TABLE).update(patch) {
                filter { eq("id", remoteId); eq("user_id", userId) }
            }
            true
        } ?: false

    override suspend fun getUnsyncedMemories(userId: String): List<Memory> =
        withRetry("getUnsyncedMemories") {
            client.from(TABLE).select {
                filter { eq("user_id", userId); eq("is_synced", false) }
                order("created_at", Order.DESCENDING)
            }.decodeList<MemoryRow>().map { it.toDomain() }
        } ?: emptyList()

    override suspend fun markAsSynced(id: Long, userId: String): Boolean {
        return withRetry("markAsSynced") {
            // Get local memory to find remoteId
            val localMemory = memoryDao.getById(id)
            val remoteId = localMemory?.remoteId
            
            if (remoteId != null) {
                // Use remoteId to mark as synced in Supabase
                markAsSyncedByRemoteId(remoteId, userId)
            } else {
                crashlyticsManager.log("markAsSynced: No remoteId found for local ID: $id")
                false
            }
        } ?: false
    }

    suspend fun markAsSyncedByRemoteId(remoteId: String, userId: String): Boolean =
        withRetry("markAsSyncedByRemoteId") {
            val patch = buildJsonObject { put("is_synced", true) }
            client.from(TABLE).update(patch) {
                filter { eq("id", remoteId); eq("user_id", userId) }
            }
            true
        } ?: false
}
