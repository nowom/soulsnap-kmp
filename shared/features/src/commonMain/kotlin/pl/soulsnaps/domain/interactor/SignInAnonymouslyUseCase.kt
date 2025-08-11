package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.features.auth.UserSessionManager

class SignInAnonymouslyUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager
) {
    suspend operator fun invoke(): UserSession {
        val userSession = authRepository.signInAnonymously()
        userSessionManager.onUserAuthenticated(userSession)
        return userSession
    }
}


