package pl.soulsnaps.domain.interactor

import pl.soulsnaps.storage.LocalStorageManager
import pl.soulsnaps.features.auth.UserSessionManager

/**
 * Use case for clearing user data
 * Provides different levels of data clearing for GDPR compliance
 */
class ClearUserDataUseCase(
    private val localStorageManager: LocalStorageManager,
    private val userSessionManager: UserSessionManager
) {
    
    /**
     * Clear all user data (full GDPR compliance)
     * This will remove all personal data from the device
     */
    suspend operator fun invoke() {
        val currentUser = userSessionManager.getCurrentUser()
        val userId = currentUser?.userId
        
        localStorageManager.clearAllLocalData(userId)
    }
    
    /**
     * Clear only user-specific data (memories, preferences)
     * Keeps app settings and analytics
     */
    suspend fun clearUserDataOnly() {
        val currentUser = userSessionManager.getCurrentUser()
        val userId = currentUser?.userId
        
        localStorageManager.clearUserDataOnly(userId)
    }
    
    /**
     * Clear only sensitive data (memories, session)
     * Keeps preferences and analytics
     */
    suspend fun clearSensitiveDataOnly() {
        val currentUser = userSessionManager.getCurrentUser()
        val userId = currentUser?.userId
        
        localStorageManager.clearSensitiveDataOnly(userId)
    }
    
    /**
     * Get storage statistics
     */
    suspend fun getStorageStats() = localStorageManager.getStorageStats()
    
    /**
     * Check if cleanup is needed
     */
    suspend fun isCleanupNeeded() = localStorageManager.isCleanupNeeded()
}

