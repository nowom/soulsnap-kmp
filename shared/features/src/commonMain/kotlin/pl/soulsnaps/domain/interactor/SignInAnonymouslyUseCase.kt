package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession

class SignInAnonymouslyUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserSession = authRepository.signInAnonymously()
}


