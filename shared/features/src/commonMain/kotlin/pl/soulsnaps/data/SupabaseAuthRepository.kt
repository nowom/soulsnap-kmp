package pl.soulsnaps.data

import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.network.SupabaseAuthService

class SupabaseAuthRepository(
    private val supabaseAuthService: SupabaseAuthService
) : AuthRepository {
    
    override suspend fun signIn(email: String, password: String): UserSession {
        return supabaseAuthService.signIn(email, password)
    }
    
    override suspend fun register(email: String, password: String): UserSession {
        return supabaseAuthService.register(email, password)
    }
    
    override suspend fun signInAnonymously(): UserSession {
        return supabaseAuthService.signInAnonymously()
    }
    
    override fun signOut() {
        // Note: This is a suspend function in the service, but the interface expects a non-suspend function
        // In a real implementation, you might want to handle this differently
    }
    
    override fun currentUser(): UserSession? {
        // Note: This is a suspend function in the service, but the interface expects a non-suspend function
        // In a real implementation, you might want to cache the current user or handle this differently
        return null
    }
}
