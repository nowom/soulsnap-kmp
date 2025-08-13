package pl.soulsnaps.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.serialization.Serializable
import pl.soulsnaps.domain.model.UserSession
import kotlinx.datetime.Clock



class SupabaseAuthService(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseAnonKey: String
) {
    
    suspend fun signIn(email: String, password: String): UserSession {
        // For now, simulate Supabase authentication
        // In a real implementation, you would make HTTP calls to Supabase
        return UserSession(
            userId = "supabase_${email.hashCode()}",
            email = email,
            isAnonymous = false,
            displayName = email.substringBefore("@"),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            lastActiveAt = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    suspend fun register(email: String, password: String): UserSession {
        // For now, simulate Supabase registration
        // In a real implementation, you would make HTTP calls to Supabase
        return UserSession(
            userId = "supabase_${email.hashCode()}",
            email = email,
            isAnonymous = false,
            displayName = email.substringBefore("@"),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            lastActiveAt = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    suspend fun signInAnonymously(): UserSession {
        // For now, simulate Supabase anonymous sign-in
        // In a real implementation, you would make HTTP calls to Supabase
        return UserSession(
            userId = "supabase_anonymous_${Clock.System.now().toEpochMilliseconds()}",
            email = "",
            isAnonymous = true,
            displayName = "Anonymous User",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            lastActiveAt = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    suspend fun signOut() {
        // For now, we'll just return success
        // In a real implementation, you'd call the signout endpoint
    }
    
    suspend fun getCurrentUser(): UserSession? {
        // For now, we'll return null
        // In a real implementation, you'd call the user endpoint
        return null
    }
    

}
