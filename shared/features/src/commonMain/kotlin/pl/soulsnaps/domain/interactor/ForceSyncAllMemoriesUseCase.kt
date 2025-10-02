package pl.soulsnaps.domain.interactor

import kotlinx.coroutines.flow.first
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.sync.manager.SyncManager
import pl.soulsnaps.sync.model.CreateMemory
import pl.soulsnaps.sync.model.StoragePaths

/**
 * Force sync all local memories to server
 * Useful for migrating existing data or manual sync trigger
 */
class ForceSyncAllMemoriesUseCase(
    private val memoryDao: MemoryDao,
    private val userSessionManager: UserSessionManager,
    private val syncManager: SyncManager,
    private val crashlyticsManager: CrashlyticsManager
) {
    
    suspend operator fun invoke(): SyncAllResult {
        return try {
            println("========================================")
            println("🔄 ForceSyncAllMemoriesUseCase - STARTING FORCE SYNC")
            println("========================================")
            
            // Check if user is authenticated
            val currentUser = userSessionManager.getCurrentUser()
            if (currentUser == null) {
                crashlyticsManager.log("ForceSyncAllMemoriesUseCase: User not authenticated")
                return SyncAllResult.Error("Musisz być zalogowany aby synchronizować dane")
            }
            
            println("✅ User authenticated: ${currentUser.userId}")
            
            // Get all local memories
            val allMemories = memoryDao.getAll().first()
            println("📊 Found ${allMemories.size} local memories")
            
            if (allMemories.isEmpty()) {
                println("⚠️ No memories to sync")
                return SyncAllResult.Success(0, "Brak wspomnień do synchronizacji")
            }
            
            // Filter memories that need sync (no remoteId or syncState = PENDING)
            val memoriesToSync = allMemories.filter { memory ->
                memory.remoteId == null || memory.syncState == "PENDING" || memory.syncState == "FAILED"
            }
            
            println("📊 Found ${memoriesToSync.size} memories that need sync")
            
            if (memoriesToSync.isEmpty()) {
                println("✅ All memories already synced")
                return SyncAllResult.Success(0, "Wszystkie wspomnienia są już zsynchronizowane")
            }
            
            // Enqueue sync tasks for each memory
            var enqueuedCount = 0
            memoriesToSync.forEach { dbMemory ->
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
                    
                    println("📤 Enqueueing memory: id=${dbMemory.id}, title='${dbMemory.title}'")
                    syncManager.enqueue(createTask)
                    enqueuedCount++
                    
                } catch (e: Exception) {
                    crashlyticsManager.recordException(e)
                    crashlyticsManager.log("Failed to enqueue memory ${dbMemory.id}: ${e.message}")
                    println("❌ Failed to enqueue memory ${dbMemory.id}: ${e.message}")
                }
            }
            
            println("========================================")
            println("✅ ForceSyncAllMemoriesUseCase - ENQUEUED $enqueuedCount TASKS")
            println("========================================")
            
            SyncAllResult.Success(
                enqueuedCount,
                "Dodano $enqueuedCount wspomnień do kolejki synchronizacji. Synchronizacja w toku..."
            )
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("ForceSyncAllMemoriesUseCase failed: ${e.message}")
            println("========================================")
            println("❌ ForceSyncAllMemoriesUseCase - FAILED: ${e.message}")
            println("========================================")
            
            SyncAllResult.Error("Błąd synchronizacji: ${e.message}")
        }
    }
}

sealed class SyncAllResult {
    data class Success(val count: Int, val message: String) : SyncAllResult()
    data class Error(val message: String) : SyncAllResult()
}
