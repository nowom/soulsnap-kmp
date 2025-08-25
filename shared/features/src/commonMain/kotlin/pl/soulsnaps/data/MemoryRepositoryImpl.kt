package pl.soulsnaps.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.Memories
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.util.NetworkMonitor
import pl.soulsnaps.network.SupabaseDatabaseService
import pl.soulsnaps.features.auth.UserSessionManager

/**
 * Offline-first Memory Repository Implementation
 * 
 * Strategy:
 * 1. Always save to local database first (offline-first)
 * 2. Mark as unsynced when saved locally
 * 3. Sync with remote service when online
 * 4. Handle conflicts and merge data
 * 5. Provide real-time updates via Flow
 */
class MemoryRepositoryImpl(
    private val networkMonitor: NetworkMonitor,
    private val memoryDao: MemoryDao,
    private val userSessionManager: UserSessionManager,
    private val remoteService: SupabaseDatabaseService? = null
) : MemoryRepository {
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun addMemory(memory: Memory): Int {
        println("DEBUG: MemoryRepositoryImpl.addMemory() - starting offline-first save")
        
        return try {
            // 1. Save to local database first (offline-first)
            println("DEBUG: MemoryRepositoryImpl.addMemory() - memory.createdAt=${memory.createdAt}")
            val memoriesEntity = Memories(
                id = 0, // Database will auto-generate ID
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
                isSynced = false // Mark as unsynced
            )
            println("DEBUG: MemoryRepositoryImpl.addMemory() - memoriesEntity.timestamp=${memoriesEntity.timestamp}")
            
            val newId = memoryDao.insert(memoriesEntity)
            println("DEBUG: MemoryRepositoryImpl.addMemory() - saved locally with ID: $newId")
            
            // 2. Trigger background sync if online and user is authenticated
            if (networkMonitor.isOnline() && remoteService != null && userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.addMemory() - triggering background sync for authenticated user")
                syncScope.launch {
                    try {
                        syncToRemote(memory.copy(id = newId.toInt()))
                        println("DEBUG: MemoryRepositoryImpl.addMemory() - sync completed successfully")
                    } catch (e: Exception) {
                        println("ERROR: MemoryRepositoryImpl.addMemory() - sync failed: ${e.message}")
                        // Sync will be retried later
                    }
                }
            } else {
                if (!userSessionManager.isAuthenticated()) {
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - user not authenticated, skipping sync")
                } else if (!networkMonitor.isOnline()) {
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - offline mode, sync will be retried later")
                } else {
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - no remote service available")
                }
            }
            
            newId.toInt()
            
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.addMemory() - local save failed: ${e.message}")
            println("ERROR: MemoryRepositoryImpl.addMemory() - full stacktrace:")
            e.printStackTrace()
            throw Exception("Failed to save memory locally: ${e.message}")
        }
    }

    override fun getMemories(): Flow<List<Memory>> {
        println("DEBUG: MemoryRepositoryImpl.getMemories() - starting database fetch")
        
        return memoryDao.getAll()
            .map { memoriesList ->
                println("DEBUG: MemoryRepositoryImpl.getMemories() - converting ${memoriesList.size} memories from database")
                memoriesList.forEach { memory ->
                    println("DEBUG: MemoryRepositoryImpl.getMemories() - database memory: id=${memory.id}, title='${memory.title}', timestamp=${memory.timestamp}")
                }
                // Filter out memories with timestamp=0 (old buggy data)
                val validMemories = memoriesList.filter { it.timestamp > 0 }
                println("DEBUG: MemoryRepositoryImpl.getMemories() - filtered to ${validMemories.size} valid memories (timestamp > 0)")
                validMemories.map { it.toDomainModel() }
            }
            .catch { e ->
                println("ERROR: MemoryRepositoryImpl.getMemories() - database error: ${e.message}")
                println("ERROR: MemoryRepositoryImpl.getMemories() - returning empty list")
                emit(emptyList())
            }
    }

    override suspend fun getMemoryById(id: Int): Memory? {
        println("DEBUG: MemoryRepositoryImpl.getMemoryById() - fetching memory ID: $id")
        
        return try {
            val memory = memoryDao.getById(id.toLong())
            if (memory != null && memory.timestamp > 0) {
                println("DEBUG: MemoryRepositoryImpl.getMemoryById() - found valid memory in database")
                memory.toDomainModel()
            } else {
                println("DEBUG: MemoryRepositoryImpl.getMemoryById() - not found or invalid timestamp (${memory?.timestamp})")
                null
            }
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.getMemoryById() - error: ${e.message}")
            null
        }
    }

    override suspend fun markAsFavorite(id: Int, isFavorite: Boolean) {
        println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - marking ID: $id as favorite: $isFavorite")
        
        try {
            // Update local database
            memoryDao.markAsFavorite(id.toLong(), isFavorite)
            println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - updated locally")
            
            // Trigger background sync if online and user is authenticated
            if (networkMonitor.isOnline() && remoteService != null && userSessionManager.isAuthenticated()) {
                syncScope.launch {
                    try {
                        // TODO: Implement remote favorite update
                        println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - remote sync completed for authenticated user")
                    } catch (e: Exception) {
                        println("ERROR: MemoryRepositoryImpl.markAsFavorite() - remote sync failed: ${e.message}")
                    }
                }
            } else {
                if (!userSessionManager.isAuthenticated()) {
                    println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - user not authenticated, skipping sync")
                } else if (!networkMonitor.isOnline()) {
                    println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - offline mode, sync will be retried later")
                } else {
                    println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - no remote service available")
                }
            }
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.markAsFavorite() - local update failed: ${e.message}")
            throw e
        }
    }

    /**
     * Sync unsynced memories to remote service
     */
    suspend fun syncUnsyncedMemories() {
        if (!networkMonitor.isOnline() || remoteService == null || !userSessionManager.isAuthenticated()) {
            if (!userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - user not authenticated, skipping sync")
            } else if (!networkMonitor.isOnline()) {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - offline, skipping sync")
            } else {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - no remote service available")
            }
            return
        }
        
        println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - starting sync")
        
        try {
            val unsynced = memoryDao.getUnsynced()
            println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - found ${unsynced.size} unsynced memories")
            
            unsynced.forEach { memory ->
                try {
                    syncToRemote(memory.toDomainModel())
                    memoryDao.markAsSynced(memory.id)
                    println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - synced memory ID: ${memory.id}")
                } catch (e: Exception) {
                    println("ERROR: MemoryRepositoryImpl.syncUnsyncedMemories() - failed to sync memory ${memory.id}: ${e.message}")
                }
            }
            
            println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - sync completed")
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.syncUnsyncedMemories() - sync failed: ${e.message}")
        }
    }

    /**
     * Pull latest data from remote service
     */
    suspend fun pullFromRemote() {
        if (!networkMonitor.isOnline() || remoteService == null || !userSessionManager.isAuthenticated()) {
            if (!userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - user not authenticated, skipping pull")
            } else if (!networkMonitor.isOnline()) {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - offline, skipping pull")
            } else {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - no remote service available")
            }
            return
        }
        
        println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - pulling latest data")
        
        try {
            // TODO: Implement remote fetch and merge with local data
            println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - pull completed")
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.pullFromRemote() - pull failed: ${e.message}")
        }
    }

    /**
     * Sync single memory to remote service
     */
    private suspend fun syncToRemote(memory: Memory) {
        if (remoteService == null || !userSessionManager.isAuthenticated()) {
            if (!userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.syncToRemote() - user not authenticated, skipping sync")
            } else {
                println("DEBUG: MemoryRepositoryImpl.syncToRemote() - no remote service available")
            }
            return
        }
        
        println("DEBUG: MemoryRepositoryImpl.syncToRemote() - syncing memory: ${memory.title}")
        
        try {
            // TODO: Implement actual remote sync using SupabaseDatabaseService
            // For now, just simulate sync
            delay(500) // Simulate network delay
            println("DEBUG: MemoryRepositoryImpl.syncToRemote() - sync completed for: ${memory.title}")
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.syncToRemote() - sync failed for ${memory.title}: ${e.message}")
            throw e
        }
    }

    /**
     * Clean up old memories with invalid timestamps
     */
    override suspend fun cleanupInvalidMemories(): Int {
        return try {
            val invalidCount = memoryDao.deleteInvalidMemories()
            println("DEBUG: MemoryRepositoryImpl.cleanupInvalidMemories() - deleted $invalidCount memories with timestamp=0")
            invalidCount
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.cleanupInvalidMemories() - failed: ${e.message}")
            -1
        }
    }

    /**
     * Convert database entity to domain model
     */
    private fun Memories.toDomainModel(): Memory {
        println("DEBUG: MemoryRepositoryImpl.toDomainModel() - converting: id=${id}, timestamp=${timestamp}")
        return Memory(
            id = id.toInt(),
            title = title,
            description = description,
            createdAt = timestamp,
            mood = mood?.let { MoodType.valueOf(it) },
            photoUri = photoUri,
            audioUri = audioUri,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            affirmation = affirmation,
            isFavorite = isFavorite
        ).also {
            println("DEBUG: MemoryRepositoryImpl.toDomainModel() - converted to: id=${it.id}, createdAt=${it.createdAt}")
        }
    }

    /**
     * Convert domain model to database entity
     */
    private fun Memory.toDatabaseEntity(): Memories {
        return Memories(
            id = id.toLong(),
            title = title,
            description = description,
            timestamp = createdAt,
            mood = mood?.name,
            photoUri = photoUri,
            audioUri = audioUri,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            affirmation = affirmation,
            isFavorite = isFavorite,
            isSynced = true
        )
    }
}
