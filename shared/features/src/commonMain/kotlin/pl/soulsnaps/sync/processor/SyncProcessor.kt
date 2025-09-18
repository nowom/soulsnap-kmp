package pl.soulsnaps.sync.processor

import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.sync.events.AppEvent
import pl.soulsnaps.sync.events.GlobalEventBus
import pl.soulsnaps.sync.model.CreateMemory
import pl.soulsnaps.sync.model.DeleteMemory
import pl.soulsnaps.sync.model.PullAll
import pl.soulsnaps.sync.model.StoragePaths
import pl.soulsnaps.sync.model.SyncTask
import pl.soulsnaps.sync.model.ToggleFavorite
import pl.soulsnaps.sync.model.UpdateMemory
import pl.soulsnaps.sync.storage.StorageClient
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Sync processor interface
 */
interface SyncProcessor {
    suspend fun run(task: SyncTask): Result<Unit>
}

/**
 * Sync processor implementation
 */
class SyncProcessorImpl(
    private val memoryDao: MemoryDao,
    private val onlineDataSource: OnlineDataSource,
    private val storageClient: StorageClient,
    private val userSessionManager: UserSessionManager,
    private val eventBus: pl.soulsnaps.sync.events.EventBus = GlobalEventBus
) : SyncProcessor {
    
    override suspend fun run(task: SyncTask): Result<Unit> {
        return try {
            println("DEBUG: SyncProcessor.run() - processing task: ${task.id}")
            
            when (task) {
                is CreateMemory -> processCreateMemory(task)
                is UpdateMemory -> processUpdateMemory(task)
                is ToggleFavorite -> processToggleFavorite(task)
                is DeleteMemory -> processDeleteMemory(task)
                is PullAll -> processPullAll()
            }
            
            println("DEBUG: SyncProcessor.run() - task completed successfully: ${task.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: SyncProcessor.run() - task failed: ${task.id}, error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun processCreateMemory(task: CreateMemory): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Get memory from local database
            val memory = memoryDao.getById(task.localId)?.toDomainModel()
                ?: return Result.failure(Exception("Memory not found: ${task.localId}"))
            
            println("DEBUG: SyncProcessor.processCreateMemory() - creating memory: ${memory.title}")
            
            // Upload photo if exists
            var remotePhotoPath: String? = null
            if (memory.photoUri != null) {
                val photoResult = storageClient.uploadImage(
                    bucket = "snap-images",
                    key = task.plannedRemotePhotoPath,
                    localUri = memory.photoUri,
                    maxLongEdgePx = 1920,
                    quality = 85,
                    upsert = true
                )
                if (photoResult.success) {
                    remotePhotoPath = photoResult.path
                    println("DEBUG: SyncProcessor.processCreateMemory() - photo uploaded: $remotePhotoPath")
                } else {
                    throw Exception("Photo upload failed: ${photoResult.errorMessage}")
                }
            }
            
            // Upload audio if exists
            var remoteAudioPath: String? = null
            if (memory.audioUri != null && task.plannedRemoteAudioPath != null) {
                val audioResult = storageClient.uploadFile(
                    bucket = "snap-audio",
                    key = task.plannedRemoteAudioPath,
                    localUri = memory.audioUri,
                    upsert = true
                )
                if (audioResult.success) {
                    remoteAudioPath = audioResult.path
                    println("DEBUG: SyncProcessor.processCreateMemory() - audio uploaded: $remoteAudioPath")
                } else {
                    throw Exception("Audio upload failed: ${audioResult.errorMessage}")
                }
            }
            
            // Insert to remote database
            val remoteId = onlineDataSource.insertMemory(memory, currentUser.userId)
            if (remoteId == null) {
                throw Exception("Failed to insert memory to remote database")
            }
            
            // Update local memory with remote paths and mark as synced
            val currentTime = getCurrentTimeMillis()
            memoryDao.updateMemory(
                id = task.localId,
                title = memory.title,
                description = memory.description,
                timestamp = memory.createdAt,
                mood = memory.mood?.name,
                photoUri = memory.photoUri,
                audioUri = memory.audioUri,
                locationName = memory.locationName,
                latitude = memory.latitude,
                longitude = memory.longitude,
                affirmation = memory.affirmation,
                isFavorite = memory.isFavorite,
                isSynced = true,
                remotePhotoPath = remotePhotoPath,
                remoteAudioPath = remoteAudioPath,
                remoteId = remoteId.toString(),
                syncState = "SYNCED",
                retryCount = 0,
                errorMessage = null
            )
            
            // Emit event
            eventBus.emit(AppEvent.SnapSynced(task.localId))
            
            println("DEBUG: SyncProcessor.processCreateMemory() - memory synced successfully: ${task.localId}")
            Result.success(Unit)
        } catch (e: Exception) {
            eventBus.emit(AppEvent.SnapSyncFailed(task.localId, e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
    
    private suspend fun processUpdateMemory(task: UpdateMemory): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val memory = memoryDao.getById(task.localId)?.toDomainModel()
                ?: return Result.failure(Exception("Memory not found: ${task.localId}"))
            
            println("DEBUG: SyncProcessor.processUpdateMemory() - updating memory: ${memory.title}")
            
            var remotePhotoPath = memory.remotePhotoPath
            var remoteAudioPath = memory.remoteAudioPath
            
            // Re-upload photo if needed
            if (task.reuploadPhoto && memory.photoUri != null) {
                val photoPath = StoragePaths.photoPath(currentUser.userId, task.localId)
                val photoResult = storageClient.uploadImage(
                    bucket = "snap-images",
                    key = photoPath,
                    localUri = memory.photoUri,
                    maxLongEdgePx = 1920,
                    quality = 85,
                    upsert = true
                )
                if (photoResult.success) {
                    remotePhotoPath = photoResult.path
                    println("DEBUG: SyncProcessor.processUpdateMemory() - photo re-uploaded: $remotePhotoPath")
                } else {
                    throw Exception("Photo re-upload failed: ${photoResult.errorMessage}")
                }
            }
            
            // Re-upload audio if needed
            if (task.reuploadAudio && memory.audioUri != null) {
                val audioPath = StoragePaths.audioPath(currentUser.userId, task.localId)
                val audioResult = storageClient.uploadFile(
                    bucket = "snap-audio",
                    key = audioPath,
                    localUri = memory.audioUri,
                    upsert = true
                )
                if (audioResult.success) {
                    remoteAudioPath = audioResult.path
                    println("DEBUG: SyncProcessor.processUpdateMemory() - audio re-uploaded: $remoteAudioPath")
                } else {
                    throw Exception("Audio re-upload failed: ${audioResult.errorMessage}")
                }
            }
            
            // Update remote database
            val success = onlineDataSource.updateMemory(memory, currentUser.userId)
            if (!success) {
                throw Exception("Failed to update memory in remote database")
            }
            
            // Update local memory
            val currentTime = getCurrentTimeMillis()
            memoryDao.updateMemory(
                id = task.localId,
                title = memory.title,
                description = memory.description,
                timestamp = memory.createdAt,
                mood = memory.mood?.name,
                photoUri = memory.photoUri,
                audioUri = memory.audioUri,
                locationName = memory.locationName,
                latitude = memory.latitude,
                longitude = memory.longitude,
                affirmation = memory.affirmation,
                isFavorite = memory.isFavorite,
                isSynced = true,
                remotePhotoPath = remotePhotoPath,
                remoteAudioPath = remoteAudioPath,
                remoteId = memory.remoteId,
                syncState = "SYNCED",
                retryCount = 0,
                errorMessage = null
            )
            
            eventBus.emit(AppEvent.SnapSynced(task.localId))
            
            println("DEBUG: SyncProcessor.processUpdateMemory() - memory updated successfully: ${task.localId}")
            Result.success(Unit)
        } catch (e: Exception) {
            eventBus.emit(AppEvent.SnapSyncFailed(task.localId, e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
    
    private suspend fun processToggleFavorite(task: ToggleFavorite): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val success = onlineDataSource.markAsFavorite(
                id = task.localId,
                isFavorite = task.isFavorite,
                userId = currentUser.userId
            )
            
            if (!success) {
                throw Exception("Failed to update favorite status in remote database")
            }
            
            // Update local memory
            memoryDao.markAsFavorite(task.localId, task.isFavorite)
            
            println("DEBUG: SyncProcessor.processToggleFavorite() - favorite status updated: ${task.localId} = ${task.isFavorite}")
            Result.success(Unit)
        } catch (e: Exception) {
            eventBus.emit(AppEvent.SnapSyncFailed(task.localId, e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
    
    private suspend fun processDeleteMemory(task: DeleteMemory): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Delete from remote database
            val success = onlineDataSource.deleteMemory(task.localId, currentUser.userId)
            if (!success) {
                throw Exception("Failed to delete memory from remote database")
            }
            
            // Delete files from storage
            if (task.remotePhotoPath != null) {
                storageClient.delete("snap-images", task.remotePhotoPath)
            }
            if (task.remoteAudioPath != null) {
                storageClient.delete("snap-audio", task.remoteAudioPath)
            }
            
            // Delete from local database
            memoryDao.delete(task.localId)
            
            eventBus.emit(AppEvent.SnapDeleted(task.localId))
            
            println("DEBUG: SyncProcessor.processDeleteMemory() - memory deleted: ${task.localId}")
            Result.success(Unit)
        } catch (e: Exception) {
            eventBus.emit(AppEvent.SnapSyncFailed(task.localId, e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
    
    private suspend fun processPullAll(): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))
            
            println("DEBUG: SyncProcessor.processPullAll() - pulling all memories")
            
            // Get all memories from remote
            val remoteMemories = onlineDataSource.getAllMemories(currentUser.userId)
            println("DEBUG: SyncProcessor.processPullAll() - fetched ${remoteMemories.size} remote memories")
            
            // Merge with local data (LWW strategy)
            for (remoteMemory in remoteMemories) {
                val localMemory = memoryDao.getById(remoteMemory.id.toLong())
                
                if (localMemory == null) {
                    // New memory - insert locally
                    val memoriesEntity = pl.soulsnaps.database.Memories(
                        id = remoteMemory.id.toLong(),
                        title = remoteMemory.title,
                        description = remoteMemory.description,
                        timestamp = remoteMemory.createdAt,
                        mood = remoteMemory.mood?.name,
                        photoUri = remoteMemory.photoUri,
                        audioUri = remoteMemory.audioUri,
                        locationName = remoteMemory.locationName,
                        latitude = remoteMemory.latitude,
                        longitude = remoteMemory.longitude,
                        affirmation = remoteMemory.affirmation,
                        isFavorite = remoteMemory.isFavorite,
                        isSynced = true,
                        remotePhotoPath = remoteMemory.remotePhotoPath,
                        remoteAudioPath = remoteMemory.remoteAudioPath,
                        remoteId = remoteMemory.remoteId,
                        syncState = "SYNCED",
                        retryCount = 0,
                        errorMessage = null
                    )
                    memoryDao.insert(memoriesEntity)
                    println("DEBUG: SyncProcessor.processPullAll() - inserted new memory: ${remoteMemory.id}")
                } else {
                    // Existing memory - merge if remote is newer
                    val localDomain = localMemory.toDomainModel()
                    if (remoteMemory.updatedAt > localDomain.updatedAt && 
                        localMemory.syncState != "PENDING" && 
                        localMemory.syncState != "FAILED") {
                        
                        // Update local with remote data
                        memoryDao.updateMemory(
                            id = remoteMemory.id.toLong(),
                            title = remoteMemory.title,
                            description = remoteMemory.description,
                            timestamp = remoteMemory.createdAt,
                            mood = remoteMemory.mood?.name,
                            photoUri = remoteMemory.photoUri,
                            audioUri = remoteMemory.audioUri,
                            locationName = remoteMemory.locationName,
                            latitude = remoteMemory.latitude,
                            longitude = remoteMemory.longitude,
                            affirmation = remoteMemory.affirmation,
                            isFavorite = remoteMemory.isFavorite,
                            isSynced = true,
                            remotePhotoPath = remoteMemory.remotePhotoPath,
                            remoteAudioPath = remoteMemory.remoteAudioPath,
                            remoteId = remoteMemory.remoteId,
                            syncState = "SYNCED",
                            retryCount = 0,
                            errorMessage = null
                        )
                        println("DEBUG: SyncProcessor.processPullAll() - updated memory: ${remoteMemory.id}")
                    }
                }
            }
            
            println("DEBUG: SyncProcessor.processPullAll() - pull completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: SyncProcessor.processPullAll() - pull failed: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * Extension to convert database entity to domain model
 */
private fun pl.soulsnaps.database.Memories.toDomainModel(): Memory {
    return Memory(
        id = id.toInt(),
        title = title,
        description = description,
        createdAt = timestamp,
        updatedAt = timestamp,
        mood = mood?.let { runCatching { MoodType.valueOf(it) }.getOrNull() },
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
        syncState = runCatching { pl.soulsnaps.domain.model.SyncState.valueOf(syncState) }.getOrElse { pl.soulsnaps.domain.model.SyncState.PENDING },
        retryCount = retryCount.toInt(),
        errorMessage = errorMessage
    )
}
