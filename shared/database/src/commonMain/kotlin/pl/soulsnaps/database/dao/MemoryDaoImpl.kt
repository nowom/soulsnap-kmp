package pl.soulsnaps.database.dao

import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.soulsnaps.database.Memories
import pl.soulsnaps.database.SoulSnapDatabase

class MemoryDaoImpl(private val db: SoulSnapDatabase) : MemoryDao {
    private val queries = db.soulSnapDatabaseQueries

    override suspend fun insert(memory: Memories): Long {
        return queries.insertMemory(
            title = memory.title,
            description = memory.description,
            timestamp = memory.timestamp,
            mood = memory.mood,
            photoUri = memory.photoUri,
            audioUri = memory.audioUri,
            locationName = memory.locationName,
            latitude = memory.latitude,
            longitude = memory.longitude,
            affirmation = memory.affirmation,
            isFavorite = memory.isFavorite,
            isSynced = memory.isSynced,
            remotePhotoPath = memory.remotePhotoPath,
            remoteAudioPath = memory.remoteAudioPath,
            remoteId = memory.remoteId,
            syncState = memory.syncState,
            retryCount = memory.retryCount,
            errorMessage = memory.errorMessage
        ).value
    }

    override fun getAll(): Flow<List<Memories>> =
        queries.selectAll().asFlow().map { it.executeAsList() }

    override suspend fun getById(id: Long): Memories? =
        queries.selectById(id).executeAsOneOrNull()

    override suspend fun delete(id: Long) {
        queries.deleteMemoryById(id)
    }

    override suspend fun update(memory: Memories) {
        queries.updateMemory(
            title = memory.title,
            description = memory.description,
            timestamp = memory.timestamp,
            mood = memory.mood,
            photoUri = memory.photoUri,
            audioUri = memory.audioUri,
            locationName = memory.locationName,
            latitude = memory.latitude,
            longitude = memory.longitude,
            affirmation = memory.affirmation,
            isFavorite = memory.isFavorite,
            isSynced = memory.isSynced,
            remotePhotoPath = memory.remotePhotoPath,
            remoteAudioPath = memory.remoteAudioPath,
            remoteId = memory.remoteId,
            syncState = memory.syncState,
            retryCount = memory.retryCount,
            errorMessage = memory.errorMessage,
            id = memory.id
        )
    }

    override suspend fun markAsFavorite(id: Long, isFavorite: Boolean) {
        queries.markAsFavorite(isFavorite, id)
    }

    override suspend fun clearAll() {
        queries.clearAll()
    }

    override suspend fun getUnsynced(): List<Memories> =
        queries.getUnsyncedMemories().executeAsList()

    override suspend fun markAsSynced(id: Long) {
        queries.markAsSynced(id)
    }

    override suspend fun deleteInvalidMemories(): Int {
        queries.deleteInvalidMemories()
        return 1 // Return 1 to indicate cleanup was performed
    }
    
    // New sync methods
    override suspend fun updateMemory(
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
    ) {
        queries.updateMemoryWithSync(
            id = id,
            title = title,
            description = description,
            timestamp = timestamp,
            mood = mood,
            photoUri = photoUri,
            audioUri = audioUri,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            affirmation = affirmation,
            isFavorite = isFavorite,
            isSynced = isSynced,
            remotePhotoPath = remotePhotoPath,
            remoteAudioPath = remoteAudioPath,
            remoteId = remoteId,
            syncState = syncState,
            retryCount = retryCount.toLong(),
            errorMessage = errorMessage
        )
    }
    
    override suspend fun getPendingMemories(): List<Memories> =
        queries.selectPendingMemories().executeAsList()
    
    override suspend fun getSyncingMemories(): List<Memories> =
        queries.selectSyncingMemories().executeAsList()
    
    override suspend fun getFailedMemories(): List<Memories> =
        queries.selectFailedMemories().executeAsList()
    
    override suspend fun updateMemorySyncState(
        id: Long,
        syncState: String,
        remoteId: String?,
        retryCount: Int,
        errorMessage: String?
    ) {
        queries.updateMemorySyncState(
            id = id,
            syncState = syncState,
            remoteId = remoteId,
            retryCount = retryCount.toLong(),
            errorMessage = errorMessage
        )
    }
    
    override suspend fun updateMemoryRemotePaths(
        id: Long,
        remotePhotoPath: String?,
        remoteAudioPath: String?,
        syncState: String,
        remoteId: String?
    ) {
        queries.updateMemoryRemotePaths(
            id = id,
            remotePhotoPath = remotePhotoPath,
            remoteAudioPath = remoteAudioPath,
            syncState = syncState,
            remoteId = remoteId
        )
    }
    
    override suspend fun incrementMemoryRetryCount(id: Long, errorMessage: String?) {
        queries.incrementMemoryRetryCount(
            id = id,
            errorMessage = errorMessage
        )
    }
}
