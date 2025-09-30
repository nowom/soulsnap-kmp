package pl.soulsnaps.features.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.sync.offline.OfflineSyncQueue
import pl.soulsnaps.sync.offline.OfflineSyncProcessor
import pl.soulsnaps.sync.offline.SyncOperationType
import pl.soulsnaps.sync.offline.SyncPriority

/**
 * Guest to User Migration Service
 * Handles migration of local data when guest upgrades to registered user
 */
class GuestToUserMigration(
    private val memoryDao: MemoryDao,
    private val onlineDataSource: OnlineDataSource,
    private val userSessionManager: UserSessionManager,
    private val userPlanManager: UserPlanManager,
    private val crashlyticsManager: CrashlyticsManager,
    private val offlineSyncQueue: OfflineSyncQueue,
    private val offlineSyncProcessor: OfflineSyncProcessor
) {
    
    /**
     * Migrate guest data to registered user
     * This will:
     * 1. Update user plan from GUEST to FREE_USER
     * 2. Queue all local memories for sync
     * 3. Trigger sync to upload data to server
     */
    suspend fun migrateGuestToUser(
        userSession: UserSession
    ): MigrationResult {
        return try {
            crashlyticsManager.log("Starting guest to user migration for: ${userSession.userId}")
            
            // 1. Check if current plan is GUEST
            val currentPlan = userPlanManager.getPlanOrDefault()
            if (currentPlan != PlanType.GUEST.name) {
                crashlyticsManager.log("User is not a guest, skipping migration. Current plan: $currentPlan")
                return MigrationResult.Success(
                    migratedMemories = 0,
                    message = "Użytkownik nie jest gościem"
                )
            }
            
            // 2. Get all local memories using Flow
            val localMemories = memoryDao.getAll().first()
            
            crashlyticsManager.log("Found ${localMemories.size} local memories to migrate")
            
            if (localMemories.isEmpty()) {
                crashlyticsManager.log("No local memories to migrate")
                
                // Update plan to FREE_USER
                userPlanManager.setUserPlanAndWait(PlanType.FREE_USER.name)
                crashlyticsManager.log("Updated user plan to FREE_USER")
                
                return MigrationResult.Success(
                    migratedMemories = 0,
                    message = "Brak danych do migracji. Konto utworzone pomyślnie!"
                )
            }
            
            // 3. Queue all memories for sync with HIGH priority
            var queuedCount = 0
            localMemories.forEach { dbMemory: pl.soulsnaps.database.Memories ->
                try {
                    // Convert database memory to domain model
                    val domainMemory = pl.soulsnaps.domain.model.Memory(
                        id = dbMemory.id.toInt(),
                        title = dbMemory.title,
                        description = dbMemory.description,
                        createdAt = dbMemory.timestamp,
                        mood = dbMemory.mood?.let { 
                            pl.soulsnaps.domain.model.MoodType.fromDatabaseValue(it) 
                        },
                        photoUri = dbMemory.photoUri,
                        audioUri = dbMemory.audioUri,
                        locationName = dbMemory.locationName,
                        latitude = dbMemory.latitude,
                        longitude = dbMemory.longitude,
                        affirmation = dbMemory.affirmation,
                        isFavorite = dbMemory.isFavorite
                    )
                    
                    offlineSyncQueue.addOperation(
                        type = SyncOperationType.INSERT,
                        memory = domainMemory,
                        userId = userSession.userId,
                        priority = SyncPriority.HIGH
                    )
                    queuedCount++
                    crashlyticsManager.log("Queued memory ${dbMemory.id} for sync")
                } catch (e: Exception) {
                    crashlyticsManager.recordException(e)
                    crashlyticsManager.log("Failed to queue memory ${dbMemory.id}: ${e.message}")
                }
            }
            
            crashlyticsManager.log("Queued $queuedCount memories for sync")
            
            // 4. Update user plan to FREE_USER
            userPlanManager.setUserPlanAndWait(PlanType.FREE_USER.name)
            crashlyticsManager.log("Updated user plan to FREE_USER")
            
            // 5. Start sync processor to upload data
            offlineSyncProcessor.start()
            offlineSyncProcessor.processPendingOperations()
            crashlyticsManager.log("Started sync processor to upload guest data")
            
            MigrationResult.Success(
                migratedMemories = queuedCount,
                message = "Pomyślnie zmigrowano $queuedCount wspomnień. Dane są synchronizowane z serwerem..."
            )
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Guest to user migration failed: ${e.message}")
            
            MigrationResult.Error(
                message = "Błąd podczas migracji danych: ${e.message}"
            )
        }
    }
    
    /**
     * Check if migration is needed
     */
    fun isMigrationNeeded(): Boolean {
        val currentPlan = userPlanManager.getPlanOrDefault()
        val isGuest = currentPlan == PlanType.GUEST.name
        crashlyticsManager.log("Migration needed check: isGuest=$isGuest, currentPlan=$currentPlan")
        return isGuest
    }
    
    /**
     * Get migration status
     */
    fun getMigrationStatus(): MigrationStatus {
        val syncStatus = offlineSyncProcessor.getSyncStatus()
        return MigrationStatus(
            pendingOperations = syncStatus.pendingOperations,
            failedOperations = syncStatus.failedOperations,
            isProcessing = syncStatus.isProcessing,
            isComplete = syncStatus.pendingOperations == 0 && syncStatus.failedOperations == 0
        )
    }
}

/**
 * Migration result
 */
sealed class MigrationResult {
    data class Success(
        val migratedMemories: Int,
        val message: String
    ) : MigrationResult()
    
    data class Error(
        val message: String
    ) : MigrationResult()
}

/**
 * Migration status
 */
data class MigrationStatus(
    val pendingOperations: Int,
    val failedOperations: Int,
    val isProcessing: Boolean,
    val isComplete: Boolean
)
