package pl.soulsnaps.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession as SupabaseUserSession
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SupabaseAuthService(private val client: SupabaseClient) {

    suspend fun signIn(email: String, password: String): UserSession {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // Po udanym logowaniu, pobierz aktualną sesję i użytkownika
            return getCurrentUser() ?: throw IllegalStateException("Authentication failed: No session or user returned")
        } catch (e: Exception) {
            throw AuthException("Sign in failed: ${e.message}", e)
        }
    }

    suspend fun register(email: String, password: String): UserSession {
        try {
            val result = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // Z auto-confirm, użytkownik powinien być automatycznie zalogowany
            // Ale sesja może potrzebować chwili żeby się zaktualizować
            var attempts = 0
            var userSession: UserSession? = null

            while (attempts < 5 && userSession == null) {
                kotlinx.coroutines.delay(500) // Czekaj 500ms
                userSession = getCurrentUser()
                attempts++
            }

            return userSession ?: throw IllegalStateException("Registration failed: Session not available after auto-confirm")
        } catch (e: Exception) {
            throw AuthException("Registration failed: ${e.message}", e)
        }
    }

    suspend fun signInAnonymously(): UserSession {
        try {
            client.auth.signInAnonymously()

            // Po udanym logowaniu anonimowym, pobierz aktualną sesję i użytkownika
            return getCurrentUser() ?: throw IllegalStateException("Anonymous sign in failed: No session or user returned")
        } catch (e: Exception) {
            throw AuthException("Anonymous sign in failed: ${e.message}", e)
        }
    }

    suspend fun signOut() {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            throw AuthException("Sign out failed: ${e.message}", e)
        }
    }

    suspend fun getCurrentUser(): UserSession? {
        return try {
            val currentSession: SupabaseUserSession? = client.auth.currentSessionOrNull()
            val currentUser: UserInfo? = client.auth.currentUserOrNull()

            if (currentSession != null && currentUser != null) {
                UserSession(
                    userId = currentUser.id,
                    email = currentUser.email ?: "",
                    isAnonymous = currentUser.appMetadata?.get("provider")?.let {
                        (it as? JsonPrimitive)?.content == "anonymous"
                    } ?: false,
                    displayName = currentUser.userMetadata?.get("full_name")?.let {
                        (it as? JsonPrimitive)?.content
                    },
                    createdAt = currentUser.createdAt?.toEpochMilliseconds() ?: getCurrentTimeMillis(),
                    lastActiveAt = getCurrentTimeMillis(),
                    accessToken = currentSession.accessToken,
                    refreshToken = currentSession.refreshToken
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshSession(): UserSession? {
        return try {
            client.auth.refreshCurrentSession()
            getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(displayName: String? = null, avatarUrl: String? = null): UserSession? {
        return try {
            // Tymczasowo zwracamy aktualnego użytkownika
            getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resetPassword(email: String) {
        try {
            client.auth.resetPasswordForEmail(email)
        } catch (e: Exception) {
            throw AuthException("Password reset failed: ${e.message}", e)
        }
    }

    suspend fun changePassword(newPassword: String) {
        try {
            // Tymczasowo nie robimy nic
            // client.auth.updateUser { password = newPassword }
        } catch (e: Exception) {
            throw AuthException("Password change failed: ${e.message}", e)
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
}

class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
