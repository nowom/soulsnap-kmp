package pl.soulsnaps.database.dao

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.database.Memories

interface MemoryDao {
    suspend fun insert(memory: Memories): Long
    fun getAll(): Flow<List<Memories>>
    suspend fun getById(id: Long): Memories?
    suspend fun delete(id: Long)
    suspend fun update(memory: Memories)
    suspend fun markAsFavorite(id: Long, isFavorite: Boolean)
    suspend fun clearAll()
    suspend fun getUnsynced(): List<Memories>
    suspend fun markAsSynced(id: Long)
    suspend fun deleteInvalidMemories(): Int
    
    // New sync methods
    suspend fun updateMemory(
        id: Long,
        title: String,
        description: String,
        timestamp: Long,
        mood: String?,
        photoUri: String?,
        audioUri: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?,
        affirmation: String?,
        isFavorite: Boolean,
        isSynced: Boolean,
        remotePhotoPath: String?,
        remoteAudioPath: String?,
        remoteId: String?,
        syncState: String,
        retryCount: Int,
        errorMessage: String?
    )
    
    suspend fun getPendingMemories(): List<Memories>
    suspend fun getSyncingMemories(): List<Memories>
    suspend fun getFailedMemories(): List<Memories>
    suspend fun updateMemorySyncState(
        id: Long,
        syncState: String,
        remoteId: String?,
        retryCount: Int,
        errorMessage: String?
    )
    suspend fun updateMemoryRemotePaths(
        id: Long,
        remotePhotoPath: String?,
        remoteAudioPath: String?,
        syncState: String,
        remoteId: String?
    )
    suspend fun incrementMemoryRetryCount(id: Long, errorMessage: String?)
}
