package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.access.guard.AccessGuard

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository,
    private val accessGuard: AccessGuard
) {
    suspend operator fun invoke() {
        println("DEBUG: SignOutUseCase.invoke() - starting sign out process")
        
        // Get current user ID before clearing session
        val currentUser = userSessionManager.getCurrentUser()
        val userId = currentUser?.userId ?: "unknown"
        
        // Clear all local memories before signing out
        try {
            println("DEBUG: SignOutUseCase.invoke() - clearing all local memories")
            memoryRepository.clearAllMemories()
            println("DEBUG: SignOutUseCase.invoke() - local memories cleared successfully")
        } catch (e: Exception) {
            println("ERROR: SignOutUseCase.invoke() - failed to clear local memories: ${e.message}")
        }
        
        // Clear all favorite affirmations before signing out
        try {
            println("DEBUG: SignOutUseCase.invoke() - clearing all favorite affirmations")
            affirmationRepository.clearAllFavorites()
            println("DEBUG: SignOutUseCase.invoke() - favorite affirmations cleared successfully")
        } catch (e: Exception) {
            println("ERROR: SignOutUseCase.invoke() - failed to clear favorite affirmations: ${e.message}")
        }
        
        // Clear quota data for the user
        try {
            println("DEBUG: SignOutUseCase.invoke() - clearing quota data for user: $userId")
            accessGuard.clearUserQuotaData(userId)
            println("DEBUG: SignOutUseCase.invoke() - quota data cleared successfully")
        } catch (e: Exception) {
            println("ERROR: SignOutUseCase.invoke() - failed to clear quota data: ${e.message}")
        }
        
        // Sign out from auth service
        authRepository.signOut()
        
        // Clear user session
        userSessionManager.onUserSignedOut()
        
        println("DEBUG: SignOutUseCase.invoke() - sign out process completed")
    }
}
