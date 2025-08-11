package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.features.auth.UserSessionManager

class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager
) {
    suspend operator fun invoke(email: String, password: String): UserSession {
        val userSession = authRepository.register(email, password)
        userSessionManager.onUserAuthenticated(userSession)
        return userSession
    }
}


