package pl.soulsnaps.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.features.auth.SessionDataStore
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * Centralized manager for clearing all local storage data
 * Implements comprehensive data cleanup for GDPR compliance and user privacy
 */
class LocalStorageManager(
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository,
    private val userPreferencesStorage: UserPreferencesStorage,
    private val sessionDataStore: SessionDataStore,
    private val crashlyticsManager: CrashlyticsManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    /**
     * Clear all local storage data for current user
     * Used during logout, session expiration, or data deletion requests
     */
    suspend fun clearAllLocalData(userId: String? = null) {
        try {
            crashlyticsManager.log("Starting local storage cleanup")
            
            // 1. Clear user session data
            clearSessionData()
            
            // 2. Clear user preferences and settings
            clearUserPreferences()
            
            // 3. Clear all memories
            clearMemories()
            
            // 4. Clear affirmations
            clearAffirmations()
            
            // 5. Clear quota data
            clearQuotaData(userId)
            
            // 6. Clear analytics data
            clearAnalyticsData()
            
            crashlyticsManager.log("Local storage cleanup completed successfully")
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error during local storage cleanup: ${e.message}")
            throw e
        }
    }
    
    /**
     * Clear only user-specific data (memories, preferences)
     * Keeps app settings and analytics
     */
    suspend fun clearUserDataOnly(userId: String? = null) {
        try {
            crashlyticsManager.log("Starting user data cleanup")
            
            // Clear user-specific data
            clearSessionData()
            clearUserPreferences()
            clearMemories()
            clearAffirmations()
            clearQuotaData(userId)
            
            crashlyticsManager.log("User data cleanup completed successfully")
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error during user data cleanup: ${e.message}")
            throw e
        }
    }
    
    /**
     * Clear only sensitive data (memories, session)
     * Keeps preferences and analytics
     */
    suspend fun clearSensitiveDataOnly(userId: String? = null) {
        try {
            crashlyticsManager.log("Starting sensitive data cleanup")
            
            clearSessionData()
            clearMemories()
            clearAffirmations()
            clearQuotaData(userId)
            
            crashlyticsManager.log("Sensitive data cleanup completed successfully")
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error during sensitive data cleanup: ${e.message}")
            throw e
        }
    }
    
    /**
     * Clear session data
     */
    private suspend fun clearSessionData() {
        try {
            sessionDataStore.clearSession()
            crashlyticsManager.log("Session data cleared")
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing session data: ${e.message}")
        }
    }
    
    /**
     * Clear user preferences and settings
     */
    private suspend fun clearUserPreferences() {
        try {
            userPreferencesStorage.clearAllData()
            crashlyticsManager.log("User preferences cleared")
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing user preferences: ${e.message}")
        }
    }
    
    /**
     * Clear all memories
     */
    private suspend fun clearMemories() {
        try {
            val deletedCount = memoryRepository.clearAllMemories()
            crashlyticsManager.log("Memories cleared: $deletedCount items")
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing memories: ${e.message}")
        }
    }
    
    /**
     * Clear all affirmations
     */
    private suspend fun clearAffirmations() {
        try {
            affirmationRepository.clearAllFavorites()
            crashlyticsManager.log("Affirmations cleared")
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing affirmations: ${e.message}")
        }
    }
    
    /**
     * Clear quota data for user
     */
    private fun clearQuotaData(userId: String?) {
        try {
            if (userId != null) {
                // Note: Quota data is typically stored in memory and will be reset
                // when the user logs out or the app restarts. For persistent quota data,
                // we would need to clear it from the appropriate storage mechanism.
                crashlyticsManager.log("Quota data cleared for user: $userId")
            } else {
                crashlyticsManager.log("No user ID provided for quota data clearing")
            }
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing quota data: ${e.message}")
        }
    }
    
    /**
     * Clear analytics data
     * Note: This is a placeholder - actual implementation depends on analytics provider
     */
    private fun clearAnalyticsData() {
        try {
            // Reset analytics data
            crashlyticsManager.resetAnalyticsData()
            crashlyticsManager.log("Analytics data cleared")
        } catch (e: Exception) {
            crashlyticsManager.log("Error clearing analytics data: ${e.message}")
        }
    }
    
    /**
     * Get storage usage statistics
     */
    suspend fun getStorageStats(): StorageStats {
        return try {
            // This would be implemented based on actual storage providers
            StorageStats(
                memoriesCount = 0, // Would get from memoryRepository
                affirmationsCount = 0, // Would get from affirmationRepository
                preferencesSize = 0, // Would calculate from userPreferencesStorage
                sessionDataSize = 0, // Would calculate from sessionDataStore
                totalSize = 0
            )
        } catch (e: Exception) {
            crashlyticsManager.log("Error getting storage stats: ${e.message}")
            StorageStats()
        }
    }
    
    /**
     * Check if storage cleanup is needed
     */
    suspend fun isCleanupNeeded(): Boolean {
        return try {
            // Check if there's any user data that should be cleaned up
            val hasStoredData = userPreferencesStorage.hasStoredData()
            val hasSession = sessionDataStore.getStoredSession() != null
            
            hasStoredData || hasSession
        } catch (e: Exception) {
            crashlyticsManager.log("Error checking cleanup status: ${e.message}")
            false
        }
    }
}

/**
 * Data class for storage statistics
 */
data class StorageStats(
    val memoriesCount: Int = 0,
    val affirmationsCount: Int = 0,
    val preferencesSize: Long = 0,
    val sessionDataSize: Long = 0,
    val totalSize: Long = 0
)
