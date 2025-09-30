package pl.soulsnaps.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
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
import pl.soulsnaps.storage.FileStorageManager
import kotlin.random.Random

class SupabaseMemoryDataSource(
    private val client: SupabaseClient,
    private val crashlyticsManager: CrashlyticsManager,
    private val memoryDao: MemoryDao,
    private val userSessionManager: UserSessionManager,
    private val fileStorageManager: FileStorageManager
) : OnlineDataSource {

    companion object {
        private const val TABLE = "memories"
        private const val STORAGE_BUCKET = "memories"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    private suspend fun <T> withRetry(name: String, block: suspend () -> T): T? {
        var last: Exception? = null
        repeat(MAX_RETRY_ATTEMPTS) { i ->
            try { 
                return block() 
            } catch (e: Exception) {
                last = e
                val errorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error: ${e.message}"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Timeout error: ${e.message}"
                    e.message?.contains("unauthorized", ignoreCase = true) == true -> "Authentication error: ${e.message}"
                    e.message?.contains("forbidden", ignoreCase = true) == true -> "Permission error: ${e.message}"
                    e.message?.contains("not found", ignoreCase = true) == true -> "Resource not found: ${e.message}"
                    else -> "Unknown error: ${e.message}"
                }
                
                crashlyticsManager.log("$name failed (${i+1}/$MAX_RETRY_ATTEMPTS): $errorMessage")
                
                if (i < MAX_RETRY_ATTEMPTS - 1) {
                    val delayMs = RETRY_DELAY_MS * (i + 1) * (i + 1) // Exponential backoff
                    crashlyticsManager.log("$name retrying in ${delayMs}ms...")
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }
        crashlyticsManager.recordException(last ?: Exception("Unknown error in $name"))
        return null
    }

    /**
     * Upload photo file to Supabase Storage
     */
    private suspend fun uploadPhotoToStorage(photoUri: String, userId: String): String? {
        return withRetry("uploadPhotoToStorage") {
            val fileData = fileStorageManager.loadPhoto(photoUri)
            if (fileData == null) {
                crashlyticsManager.log("uploadPhotoToStorage: Failed to load photo data for URI: $photoUri")
                return@withRetry null
            }
            
            if (fileData.isEmpty()) {
                crashlyticsManager.log("uploadPhotoToStorage: Photo data is empty for URI: $photoUri")
                return@withRetry null
            }
            
            val fileName = "photos/${userId}/${Random.nextLong()}.jpg"
            
            try {
                client.storage.from(STORAGE_BUCKET).upload(
                    path = fileName,
                    data = fileData
                )
                
                // Return the public URL
                "${client.supabaseUrl}/storage/v1/object/public/$STORAGE_BUCKET/$fileName"
            } catch (e: Exception) {
                crashlyticsManager.log("uploadPhotoToStorage: Upload failed for file: $fileName, size: ${fileData.size} bytes")
                throw e
            }
        }
    }

    /**
     * Upload audio file to Supabase Storage
     */
    private suspend fun uploadAudioToStorage(audioUri: String, userId: String): String? {
        return withRetry("uploadAudioToStorage") {
            val fileData = fileStorageManager.loadAudio(audioUri)
            if (fileData == null) {
                crashlyticsManager.log("uploadAudioToStorage: Failed to load audio data for URI: $audioUri")
                return@withRetry null
            }
            
            if (fileData.isEmpty()) {
                crashlyticsManager.log("uploadAudioToStorage: Audio data is empty for URI: $audioUri")
                return@withRetry null
            }
            
            val fileName = "audio/${userId}/${Random.nextLong()}.m4a"
            
            try {
                client.storage.from(STORAGE_BUCKET).upload(
                    path = fileName,
                    data = fileData
                )
                
                // Return the public URL
                "${client.supabaseUrl}/storage/v1/object/public/$STORAGE_BUCKET/$fileName"
            } catch (e: Exception) {
                crashlyticsManager.log("uploadAudioToStorage: Upload failed for file: $fileName, size: ${fileData.size} bytes")
                throw e
            }
        }
    }

    /**
     * Delete photo file from Supabase Storage
     */
    private suspend fun deletePhotoFromStorage(photoUrl: String): Boolean {
        return withRetry("deletePhotoFromStorage") {
            val fileName = photoUrl.substringAfter("$STORAGE_BUCKET/")
            client.storage.from(STORAGE_BUCKET).delete(fileName)
            true
        } ?: false
    }

    /**
     * Delete audio file from Supabase Storage
     */
    private suspend fun deleteAudioFromStorage(audioUrl: String): Boolean {
        return withRetry("deleteAudioFromStorage") {
            val fileName = audioUrl.substringAfter("$STORAGE_BUCKET/")
            client.storage.from(STORAGE_BUCKET).delete(fileName)
            true
        } ?: false
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
            // Upload files to Supabase Storage first
            val remotePhotoPath = memory.photoUri?.let { photoUri ->
                uploadPhotoToStorage(photoUri, userId)
            }
            
            val remoteAudioPath = memory.audioUri?.let { audioUri ->
                uploadAudioToStorage(audioUri, userId)
            }
            
            // Create memory row with remote file paths
            val memoryRow = memory.copy(
                remotePhotoPath = remotePhotoPath,
                remoteAudioPath = remoteAudioPath
            ).toRow(userId)
            
            val inserted = client.from(TABLE).insert(memoryRow)
                .decodeSingle<MemoryRow>()
            
            // Return local Long ID, but save inserted.id (uuid) to remoteId field in local DB
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
            // Get local memory to find remoteId and file paths
            val localMemory = memoryDao.getById(id)
            val remoteId = localMemory?.remoteId
            
            if (remoteId != null) {
                // Delete files from Supabase Storage first
                localMemory.remotePhotoPath?.let { photoPath ->
                    deletePhotoFromStorage(photoPath)
                }
                
                localMemory.remoteAudioPath?.let { audioPath ->
                    deleteAudioFromStorage(audioPath)
                }
                
                // Then delete memory record from database
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
