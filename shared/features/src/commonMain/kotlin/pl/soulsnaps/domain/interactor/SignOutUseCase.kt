package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.storage.LocalStorageManager

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val localStorageManager: LocalStorageManager
) {
    suspend operator fun invoke() {
        println("DEBUG: SignOutUseCase.invoke() - starting sign out process")
        
        // Get current user ID before clearing session
        val currentUser = userSessionManager.getCurrentUser()
        val userId = currentUser?.userId ?: "unknown"
        
        // Clear all local storage data using LocalStorageManager
        try {
            println("DEBUG: SignOutUseCase.invoke() - clearing all local storage data")
            localStorageManager.clearAllLocalData(userId)
            println("DEBUG: SignOutUseCase.invoke() - local storage cleared successfully")
        } catch (e: Exception) {
            println("ERROR: SignOutUseCase.invoke() - failed to clear local storage: ${e.message}")
        }
        
        // Sign out from auth service
        authRepository.signOut()
        
        // Clear user session
        userSessionManager.onUserSignedOut()
        
        println("DEBUG: SignOutUseCase.invoke() - sign out process completed")
    }
}
