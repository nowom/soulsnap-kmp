package pl.soulsnaps.data

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession

class AuthRepositoryImpl(
    private val service: FakeAuthService
): AuthRepository {
    override suspend fun signIn(email: String, password: String): UserSession =
        service.signIn(email, password)

    override suspend fun register(email: String, password: String): UserSession =
        service.register(email, password)

    override suspend fun signInAnonymously(): UserSession = service.signInAnonymously()

    override fun signOut() = service.signOut()

    override fun currentUser(): UserSession? = service.currentUser()
}


