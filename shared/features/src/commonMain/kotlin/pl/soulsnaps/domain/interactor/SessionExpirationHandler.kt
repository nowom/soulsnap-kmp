package pl.soulsnaps.domain.interactor

import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * Handler for session expiration events
 * This class coordinates between UserSessionManager and data cleanup
 * Follows SOLID principles - single responsibility for session expiration handling
 */
class SessionExpirationHandler(
    private val userSessionManager: UserSessionManager,
    private val clearDataOnSessionExpiredUseCase: ClearDataOnSessionExpiredUseCase,
    private val crashlyticsManager: CrashlyticsManager
) {
    
    /**
     * Handle session expiration
     * This should be called when session expiration is detected
     */
    suspend fun handleSessionExpiration() {
        try {
            crashlyticsManager.log("Handling session expiration")
            
            // Get current user ID before clearing session
            val currentUserId = userSessionManager.getCurrentUser()?.userId
            
            // Notify UserSessionManager about expiration
            userSessionManager.onSessionExpired()
            
            // Clear user data using the appropriate UseCase
            clearDataOnSessionExpiredUseCase.execute(currentUserId)
            
            crashlyticsManager.log("Session expiration handled successfully")
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error handling session expiration: ${e.message}")
            throw e
        }
    }
}
