package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.features.auth.UserSessionManager

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        userSessionManager.onUserSignedOut()
    }
}
