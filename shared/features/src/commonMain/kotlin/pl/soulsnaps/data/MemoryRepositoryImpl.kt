package pl.soulsnaps.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.soulsnaps.access.guard.CapacityGuard
import pl.soulsnaps.database.Memories
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.storage.FileStorageManager
import pl.soulsnaps.sync.manager.SyncManager
import pl.soulsnaps.sync.model.CreateMemory
import pl.soulsnaps.sync.model.StoragePaths
import pl.soulsnaps.util.NetworkMonitor
import kotlin.io.encoding.Base64

/**
 * Offline-first Memory Repository Implementation
 * * Strategy:
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
    private val onlineDataSource: OnlineDataSource,
    private val capacityGuard: CapacityGuard,
    private val syncManager: SyncManager,
    private val fileStorageManager: FileStorageManager
) : MemoryRepository {

    // Zmieniono Dispatchers.Default na Dispatchers.IO dla lepszej wydajności I/O
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Enqueue pending memories for sync (automatic migration)
     * Called by AppInitializer after app startup
     */
    override suspend fun enqueuePendingMemories() {
        try {
            if (!userSessionManager.isAuthenticated()) {
                println("ℹ️ MemoryRepositoryImpl.enqueuePendingMemories() - user not authenticated, skipping")
                return
            }

            val currentUser = userSessionManager.getCurrentUser() ?: return

            println("========================================")
            println("🔄 MemoryRepositoryImpl.enqueuePendingMemories() - CHECKING FOR UNSYNCED MEMORIES")
            println("========================================")

            // Poprawne pobranie jednorazowej migawki (first())
            val allMemories = memoryDao.getAll().first()
            val unsyncedMemories = allMemories.filter { memory ->
                memory.remoteId == null || memory.syncState == "PENDING" || memory.syncState == "FAILED"
            }

            println("📊 Found ${allMemories.size} total memories, ${unsyncedMemories.size} need sync")

            if (unsyncedMemories.isEmpty()) {
                println("✅ All memories already synced")
                return
            }

            println("📤 Auto-enqueueing ${unsyncedMemories.size} unsynced memories...")

            var enqueuedCount = 0
            unsyncedMemories.forEach { dbMemory ->
                try {
                    val createTask = CreateMemory(
                        localId = dbMemory.id,
                        plannedRemotePhotoPath = if (dbMemory.photoUri != null) {
                            StoragePaths.photoPath(currentUser.userId, dbMemory.id)
                        } else {
                            ""
                        },
                        plannedRemoteAudioPath = if (dbMemory.audioUri != null) {
                            StoragePaths.audioPath(currentUser.userId, dbMemory.id)
                        } else {
                            null
                        }
                    )

                    syncManager.enqueue(createTask)
                    enqueuedCount++
                    println("✅ Enqueued memory: id=${dbMemory.id}, title='${dbMemory.title}'")
                } catch (e: Exception) {
                    println("❌ Failed to enqueue memory ${dbMemory.id}: ${e.message}")
                }
            }

            println("========================================")
            println("✅ MemoryRepositoryImpl.enqueuePendingMemories() - ENQUEUED $enqueuedCount MEMORIES")
            println("========================================")

        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.enqueuePendingMemories() - failed: ${e.message}")
        }
    }

    /**
     * Decode Base64 string to ByteArray using KMP standard library (kotlin.io.encoding).
     * This requires 'kotlinx-io' or similar dependency for native/JS.
     */
    private fun decodeBase64(base64String: String): ByteArray {
        return try {
            // Używamy KMP-kompatybilnego Base64 ze standardowej biblioteki (dostępny od Kotlina 1.8)
            val cleanBase64 = base64String.substringAfter("base64,") // Jeśli wciąż zawiera nagłówek
            Base64.decode(cleanBase64)
        } catch (e: Exception) {
            println("ERROR: Failed to decode Base64: ${e.message}")
            // W przypadku błędu (np. nieprawidłowy format Base64) zwracamy pustą tablicę.
            byteArrayOf()
        }
    }

    // Usunięto przestarzałą i ryzykowną metodę base64CharToByte

    override suspend fun addMemory(memory: Memory): Int {
        println("DEBUG: MemoryRepositoryImpl.addMemory() - starting offline-first save")

        // Validate mood value
        if (memory.mood != null && !MoodType.isValidMood(memory.mood.databaseValue)) {
            throw IllegalArgumentException("Invalid mood value: ${memory.mood}")
        }

        return try {
            // 1. Save large content to phone storage, database stores only file paths
            val photoPath = memory.photoUri?.let { photoUri ->
                if (photoUri.startsWith("data:image")) {
                    // Extract Base64 data and save to phone storage
                    val photoData = decodeBase64(photoUri) // Zmieniono na wywołanie nowej funkcji
                    val fileName = fileStorageManager.savePhoto(photoData)
                    println("DEBUG: Saved photo to phone storage: $fileName")
                    fileName
                } else {
                    // Already a file path
                    photoUri
                }
            }

            val audioPath = memory.audioUri?.let { audioUri ->
                if (audioUri.startsWith("data:audio")) {
                    // Extract Base64 data and save to phone storage
                    val audioData = decodeBase64(audioUri) // Zmieniono na wywołanie nowej funkcji
                    val fileName = fileStorageManager.saveAudio(audioData)
                    println("DEBUG: Saved audio to phone storage: $fileName")
                    fileName
                } else {
                    // Already a file path
                    audioUri
                }
            }

            // 2. Save metadata to database (no large content)
            println("DEBUG: MemoryRepositoryImpl.addMemory() - memory.createdAt=${memory.createdAt}")
            val memoriesEntity = Memories(
                id = 0, // Database will auto-generate ID
                title = memory.title,
                description = memory.description,
                timestamp = memory.createdAt,
                mood = memory.mood?.name,
                photoUri = photoPath, // File path instead of Base64
                audioUri = audioPath, // File path instead of Base64
                locationName = memory.locationName,
                latitude = memory.latitude,
                longitude = memory.longitude,
                affirmation = memory.affirmation,
                isFavorite = memory.isFavorite,
                // Mark as unsynced using syncState
                remotePhotoPath = null,
                remoteAudioPath = null,
                remoteId = null,
                syncState = "PENDING",
                retryCount = 0,
                errorMessage = null
            )
            println("DEBUG: MemoryRepositoryImpl.addMemory() - memoriesEntity.timestamp=${memoriesEntity.timestamp}")

            val newId = memoryDao.insert(memoriesEntity)
            println("DEBUG: MemoryRepositoryImpl.addMemory() - saved locally with ID: $newId")

            // 2. Enqueue sync task using new sync system
            println("========================================")
            println("🔄 MemoryRepositoryImpl.addMemory() - CHECKING SYNC CONDITIONS")
            println("========================================")
            println("📊 isAuthenticated: ${userSessionManager.isAuthenticated()}")
            println("📊 isOnline: ${networkMonitor.isOnline()}")

            if (userSessionManager.isAuthenticated()) {
                val currentUser = userSessionManager.getCurrentUser()
                println("DEBUG: MemoryRepositoryImpl.addMemory() - currentUser: ${currentUser?.userId}, email: ${currentUser?.email}")

                if (currentUser != null) {
                    val plannedRemotePhotoPath = if (memory.photoUri != null) {
                        StoragePaths.photoPath(currentUser.userId, newId.toLong())
                    } else {
                        ""
                    }
                    val plannedRemoteAudioPath = if (memory.audioUri != null) {
                        StoragePaths.audioPath(currentUser.userId, newId.toLong())
                    } else {
                        null
                    }

                    println("DEBUG: MemoryRepositoryImpl.addMemory() - creating sync task")
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - localId: $newId")
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - plannedRemotePhotoPath: $plannedRemotePhotoPath")
                    println("DEBUG: MemoryRepositoryImpl.addMemory() - plannedRemoteAudioPath: $plannedRemoteAudioPath")

                    val createTask = CreateMemory(
                        localId = newId.toLong(),
                        plannedRemotePhotoPath = plannedRemotePhotoPath,
                        plannedRemoteAudioPath = plannedRemoteAudioPath
                    )

                    println("========================================")
                    println("📤 MemoryRepositoryImpl.addMemory() - ENQUEUEING SYNC TASK")
                    println("========================================")
                    syncManager.enqueue(createTask)
                    println("========================================")
                    println("✅ MemoryRepositoryImpl.addMemory() - SYNC TASK ENQUEUED!")
                    println("📋 Task ID: ${createTask.id}")
                    println("========================================")
                } else {
                    println("WARNING: MemoryRepositoryImpl.addMemory() - no current user, skipping sync")
                }
            } else {
                println("WARNING: MemoryRepositoryImpl.addMemory() - user not authenticated, skipping sync")
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
                validMemories.map { memory ->
                    // Convert each memory to domain model with file paths
                    runBlocking { memory.toDomainModel() }
                }
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
            if (networkMonitor.isOnline() && userSessionManager.isAuthenticated()) {
                // Używamy syncScope, który teraz działa na Dispatchers.IO
                syncScope.launch {
                    try {
                        val currentUser = userSessionManager.getCurrentUser()
                        if (currentUser != null) {
                            // Use OnlineDataSource to update favorite status remotely
                            val success = onlineDataSource.markAsFavorite(id.toLong(), isFavorite, currentUser.userId)
                            if (success) {
                                println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - remote favorite update completed for memory ID: $id")
                            } else {
                                println("ERROR: MemoryRepositoryImpl.markAsFavorite() - remote favorite update failed for memory ID: $id")
                            }
                        } else {
                            println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - no current user, skipping remote sync")
                        }
                    } catch (e: Exception) {
                        println("ERROR: MemoryRepositoryImpl.markAsFavorite() - remote sync failed: ${e.message}")
                    }
                }
            } else {
                if (!userSessionManager.isAuthenticated()) {
                    println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - user not authenticated, skipping sync")
                } else if (!networkMonitor.isOnline()) {
                    println("DEBUG: MemoryRepositoryImpl.markAsFavorite() - offline mode, sync will be retried later")
                }
            }
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.markAsFavorite() - local update failed: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteMemory(id: Int) {
        println("DEBUG: MemoryRepositoryImpl.deleteMemory() - deleting memory ID: $id")

        try {
            // Get memory details before deletion for sync
            val memory = memoryDao.getById(id.toLong())

            // Delete from local database
            memoryDao.delete(id.toLong())
            println("DEBUG: MemoryRepositoryImpl.deleteMemory() - deleted locally")

            // If user is authenticated, we should also handle remote deletion
            if (userSessionManager.isAuthenticated()) {
                val currentUser = userSessionManager.getCurrentUser()
                if (currentUser != null && memory != null) {
                    // Enqueue deletion sync task
                    val deleteTask = pl.soulsnaps.sync.model.DeleteMemory(
                        localId = id.toLong(),
                        remotePhotoPath = memory.remotePhotoPath,
                        remoteAudioPath = memory.remoteAudioPath
                    )
                    syncManager.enqueue(deleteTask)
                    println("DEBUG: MemoryRepositoryImpl.deleteMemory() - enqueued deletion sync task for memory ID: $id")
                } else {
                    println("DEBUG: MemoryRepositoryImpl.deleteMemory() - no current user or memory not found, local deletion only")
                }
            } else {
                println("DEBUG: MemoryRepositoryImpl.deleteMemory() - guest user, local deletion only")
            }
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.deleteMemory() - deletion failed: ${e.message}")
            throw e
        }
    }

    override suspend fun updateMemory(memory: Memory) {
        println("DEBUG: MemoryRepositoryImpl.updateMemory() - updating memory ID: ${memory.id}")

        try {
            // Update in local database
            val updatedEntity = memory.toDatabaseEntity()
            memoryDao.update(updatedEntity)
            println("DEBUG: MemoryRepositoryImpl.updateMemory() - updated locally")

            // If user is authenticated, we should also handle remote update
            if (userSessionManager.isAuthenticated()) {
                val currentUser = userSessionManager.getCurrentUser()
                if (currentUser != null) {
                    // Enqueue update sync task
                    val updateTask = pl.soulsnaps.sync.model.UpdateMemory(
                        localId = memory.id.toLong(),
                        reuploadPhoto = false, // Don't reupload photo unless it changed
                        reuploadAudio = false  // Don't reupload audio unless it changed
                    )
                    syncManager.enqueue(updateTask)
                    println("DEBUG: MemoryRepositoryImpl.updateMemory() - enqueued update sync task for memory ID: ${memory.id}")
                } else {
                    println("DEBUG: MemoryRepositoryImpl.updateMemory() - no current user, local update only")
                }
            } else {
                println("DEBUG: MemoryRepositoryImpl.updateMemory() - guest user, local update only")
            }
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.updateMemory() - update failed: ${e.message}")
            throw e
        }
    }

    /**
     * Public method to trigger sync of unsynced memories
     */
    suspend fun triggerSync() {
        syncUnsyncedMemories()
    }

    /**
     * Public method to trigger pull from remote
     */
    suspend fun triggerPull() {
        pullFromRemote()
    }

    /**
     * Sync unsynced memories to remote service
     */
    private suspend fun syncUnsyncedMemories() {
        if (!networkMonitor.isOnline() || !userSessionManager.isAuthenticated()) {
            if (!userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - user not authenticated, skipping sync")
            } else if (!networkMonitor.isOnline()) {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - offline, skipping sync")
            }
            return
        }

        println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - starting sync")

        try {
            // Poprawne pobranie jednorazowej migawki (first())
            val unsynced = memoryDao.getUnsynced()
            println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - found ${unsynced.size} unsynced memories")

            val currentUser = userSessionManager.getCurrentUser()
            if (currentUser == null) {
                println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - no current user, skipping sync")
                return
            }

            unsynced.forEach { memory ->
                try {
                    // Check quota before syncing each memory
                    val canAddResult = capacityGuard.canAddSnap(currentUser.userId)
                    if (!canAddResult.allowed) {
                        println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - quota exceeded, stopping sync: ${canAddResult.message}")
                        return
                    }

                    // Use OnlineDataSource to sync with remote service
                    val remoteId = onlineDataSource.insertMemory(memory.toDomainModel(), currentUser.userId)
                    if (remoteId != null) {
                        // Save remoteId to local database for future reference
                        memoryDao.updateMemorySyncState(
                            id = memory.id,
                            syncState = "SYNCED",
                            remoteId = remoteId.toString(),
                            retryCount = 0,
                            errorMessage = null
                        )
                        println("DEBUG: MemoryRepositoryImpl.syncUnsyncedMemories() - synced memory ID: ${memory.id} with remote ID: $remoteId")
                    } else {
                        println("ERROR: MemoryRepositoryImpl.syncUnsyncedMemories() - failed to get remote ID for memory ${memory.id}")
                    }
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
        if (!networkMonitor.isOnline() || !userSessionManager.isAuthenticated()) {
            if (!userSessionManager.isAuthenticated()) {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - user not authenticated, skipping pull")
            } else if (!networkMonitor.isOnline()) {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - offline, skipping pull")
            }
            return
        }

        println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - pulling latest data")

        try {
            val currentUser = userSessionManager.getCurrentUser()
            if (currentUser == null) {
                println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - no current user, skipping pull")
                return
            }

            // Fetch all memories from remote service
            val remoteMemories = onlineDataSource.getAllMemories(currentUser.userId)
            println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - fetched ${remoteMemories.size} memories from remote")

            // Zmieniono collect na first() w celu pobrania jednorazowej migawki danych lokalnych
            val localMemoriesEntities = memoryDao.getAll().first()
            val localMemories = localMemoriesEntities.map { it.toDomainModel() }
            println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - found ${localMemories.size} local memories")

            // Merge logic: handle conflicts and new memories
            var mergedCount = 0
            var newCount = 0
            var conflictCount = 0

            remoteMemories.forEach { remoteMemory ->
                // Używamy remoteId do mapowania lokalnego, zakładając, że remoteId jest przechowywane w localMemory
                val localMemory = localMemories.find { it.remoteId == remoteMemory.remoteId }

                if (localMemory == null) {
                    // New memory from remote - add to local database
                    val newEntity = remoteMemory.copy(syncState = pl.soulsnaps.domain.model.SyncState.SYNCED).toDatabaseEntity()
                    memoryDao.insert(newEntity)
                    newCount++
                    println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - added new memory from remote: ${remoteMemory.title}")
                } else {
                    // Memory exists locally - check for conflicts
                    val localTimestamp = localMemory.createdAt
                    val remoteTimestamp = remoteMemory.createdAt

                    if (localTimestamp != remoteTimestamp) {
                        // Conflict detected - use remote version (server wins)
                        val updatedEntity = remoteMemory.copy(syncState = pl.soulsnaps.domain.model.SyncState.SYNCED).toDatabaseEntity()
                        memoryDao.update(updatedEntity)
                        conflictCount++
                        println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - resolved conflict for memory: ${remoteMemory.title} (remote wins)")
                    } else {
                        // No conflict - just ensure sync status is correct
                        // Używamy updateMemorySyncState, aby zaktualizować stan na SYNCED bez zmiany innych pól.
                        memoryDao.updateMemorySyncState(
                            id = localMemory.id.toLong(),
                            syncState = "SYNCED",
                            remoteId = localMemory.remoteId,
                            retryCount = 0,
                            errorMessage = null
                        )
                        mergedCount++
                        println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - memory already in sync: ${remoteMemory.title}")
                    }
                }
            }

            println("DEBUG: MemoryRepositoryImpl.pullFromRemote() - merge completed: $newCount new, $mergedCount merged, $conflictCount conflicts resolved")
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.pullFromRemote() - pull failed: ${e.message}")
        }
    }

    /**
     * Sync single memory to remote service
     */
    private suspend fun syncToRemote(memory: Memory) {
        if (!userSessionManager.isAuthenticated()) {
            println("DEBUG: MemoryRepositoryImpl.syncToRemote() - user not authenticated, skipping sync")
            return
        }

        val currentUser = userSessionManager.getCurrentUser()
        if (currentUser == null) {
            println("DEBUG: MemoryRepositoryImpl.syncToRemote() - no current user, skipping sync")
            return
        }

        // Check if user can add more memories to Supabase (quota check)
        val canAddResult = capacityGuard.canAddSnap(currentUser.userId)
        if (!canAddResult.allowed) {
            println("DEBUG: MemoryRepositoryImpl.syncToRemote() - quota exceeded for user ${currentUser.userId}: ${canAddResult.message}")
            // Don't throw exception - local save was successful, just skip remote sync
            return
        }

        println("DEBUG: MemoryRepositoryImpl.syncToRemote() - syncing memory: ${memory.title}")

        try {
            // Use OnlineDataSource to sync with remote service
            val remoteId = onlineDataSource.insertMemory(memory, currentUser.userId)
            if (remoteId != null) {
                println("DEBUG: MemoryRepositoryImpl.syncToRemote() - memory synced successfully with remote ID: $remoteId")
                // Mark as synced in local database
                memoryDao.markAsSynced(memory.id.toLong())
            } else {
                println("ERROR: MemoryRepositoryImpl.syncToRemote() - failed to get remote ID for memory: ${memory.title}")
                throw Exception("Failed to sync memory to remote service")
            }
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
     * Clear all memories from local database (used during logout)
     */
    override suspend fun clearAllMemories(): Int {
        return try {
            println("DEBUG: MemoryRepositoryImpl.clearAllMemories() - clearing all memories from local database")
            memoryDao.clearAll()
            println("DEBUG: MemoryRepositoryImpl.clearAllMemories() - all memories cleared successfully")
            0 // Return 0 to indicate successful cleanup
        } catch (e: Exception) {
            println("ERROR: MemoryRepositoryImpl.clearAllMemories() - failed: ${e.message}")
            -1
        }
    }

    /**
     * Convert database entity to domain model
     */
    private suspend fun Memories.toDomainModel(): Memory {
        println("DEBUG: MemoryRepositoryImpl.toDomainModel() - converting: id=${id}, timestamp=${timestamp}, photoUri='${photoUri}'")

        // Get actual file paths from phone storage
        val actualPhotoUri = photoUri?.let { fileName ->
            fileStorageManager.getPhotoPath(fileName) ?: fileName
        }

        val actualAudioUri = audioUri?.let { fileName ->
            fileStorageManager.getAudioPath(fileName) ?: fileName
        }

        return Memory(
            id = id.toInt(),
            title = title,
            description = description,
            createdAt = timestamp,
            mood = mood?.let { MoodType.valueOf(it) },
            photoUri = actualPhotoUri, // File path to phone storage
            audioUri = actualAudioUri, // File path to phone storage
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            affirmation = affirmation,
            isFavorite = isFavorite,
            remoteId = remoteId
        )
    }

    // Konwersja z modelu domenowego do encji bazy danych
    private fun Memory.toDatabaseEntity(): Memories {
        // Domyślne wartości dla pól związanych z synchronizacją (jeśli nie są w modelu)
        val syncState = if (remoteId != null) "SYNCED" else "PENDING"

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
            remotePhotoPath = null, // Te pola są zarządzane przez SyncManager
            remoteAudioPath = null, // Te pola są zarządzane przez SyncManager
            remoteId = remoteId,
            syncState = syncState,
            retryCount = 0,
            errorMessage = null
        )
    }
}
