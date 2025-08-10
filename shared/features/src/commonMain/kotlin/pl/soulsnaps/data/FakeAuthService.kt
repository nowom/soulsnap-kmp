package pl.soulsnaps.data

import kotlinx.coroutines.delay
import pl.soulsnaps.domain.model.UserSession

class FakeAuthService {
    private var current: UserSession? = null

    suspend fun signIn(email: String, password: String): UserSession {
        delay(300)
        current = UserSession(userId = "u_${email.hashCode()}", email = email, isAnonymous = false)
        return current!!
    }

    suspend fun register(email: String, password: String): UserSession {
        delay(500)
        current = UserSession(userId = "u_${email.hashCode()}", email = email, isAnonymous = false)
        return current!!
    }

    suspend fun signInAnonymously(): UserSession {
        delay(200)
        // Use a simple increasing id for KMP compatibility
        current = UserSession(userId = "guest_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}", email = null, isAnonymous = true)
        return current!!
    }

    fun signOut() { current = null }

    fun currentUser(): UserSession? = current
}


