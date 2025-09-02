package pl.soulsnaps.data

import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

class FakeAuthService {
    
    private var currentUser: UserSession? = null
    private var userCounter = 0L
    
    suspend fun signIn(email: String, password: String): UserSession {
        val user = UserSession(
            userId = "user_${++userCounter}",
            email = email,
            isAnonymous = false,
            displayName = email.split("@").firstOrNull() ?: "User",
            createdAt = getCurrentTimeMillis(),
            lastActiveAt = getCurrentTimeMillis(),
            accessToken = "fake_token_${userCounter}",
            refreshToken = "fake_refresh_${userCounter}"
        )
        currentUser = user
        return user
    }
    
    suspend fun register(email: String, password: String): UserSession {
        val user = UserSession(
            userId = "user_${++userCounter}",
            email = email,
            isAnonymous = false,
            displayName = email.split("@").firstOrNull() ?: "User",
            createdAt = getCurrentTimeMillis(),
            lastActiveAt = getCurrentTimeMillis(),
            accessToken = "fake_token_${userCounter}",
            refreshToken = "fake_refresh_${userCounter}"
        )
        currentUser = user
        return user
    }
    
    suspend fun signInAnonymously(): UserSession {
        val user = UserSession(
            userId = "anon_${++userCounter}",
            email = "",
            isAnonymous = true,
            displayName = "Anonymous User",
            createdAt = getCurrentTimeMillis(),
            lastActiveAt = getCurrentTimeMillis(),
            accessToken = "fake_anon_token_${userCounter}",
            refreshToken = "fake_anon_refresh_${userCounter}"
        )
        currentUser = user
        return user
    }
    
    suspend fun signOut() {
        currentUser = null
    }
    
    suspend fun getCurrentUser(): UserSession? = currentUser
    
    suspend fun refreshSession(): UserSession? {
        currentUser?.let { user ->
            val refreshedUser = user.copy(
                lastActiveAt = getCurrentTimeMillis(),
                accessToken = "refreshed_token_${userCounter}"
            )
            currentUser = refreshedUser
            return refreshedUser
        }
        return null
    }
}


