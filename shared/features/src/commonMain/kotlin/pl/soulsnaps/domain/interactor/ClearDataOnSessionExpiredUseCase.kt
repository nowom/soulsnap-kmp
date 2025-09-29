package pl.soulsnaps.domain.interactor

import pl.soulsnaps.storage.LocalStorageManager
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * UseCase for clearing user data when session expires
 * Follows SOLID principles - single responsibility for data cleanup
 */
class ClearDataOnSessionExpiredUseCase(
    private val localStorageManager: LocalStorageManager,
    private val crashlyticsManager: CrashlyticsManager
) {
    
    /**
     * Clear all user data when session expires
     * This should be called by the appropriate service/repository
     * when session expiration is detected
     */
    suspend fun execute(userId: String?) {
        try {
            crashlyticsManager.log("Clearing data due to session expiration for user: $userId")
            localStorageManager.clearAllLocalData(userId)
            crashlyticsManager.log("Data cleared successfully due to session expiration")
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error clearing data on session expiration: ${e.message}")
            throw e
        }
    }
}
