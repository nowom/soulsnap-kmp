package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.features.auth.UserSessionManager

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository
) {
    suspend operator fun invoke() {
        println("DEBUG: SignOutUseCase.invoke() - starting sign out process")
        
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
        
        // Sign out from auth service
        authRepository.signOut()
        
        // Clear user session
        userSessionManager.onUserSignedOut()
        
        println("DEBUG: SignOutUseCase.invoke() - sign out process completed")
    }
}
