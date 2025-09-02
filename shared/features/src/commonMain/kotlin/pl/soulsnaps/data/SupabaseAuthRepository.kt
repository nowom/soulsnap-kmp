package pl.soulsnaps.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.network.SupabaseAuthService

class SupabaseAuthRepository(
    private val supabaseAuthService: SupabaseAuthService
) : AuthRepository {
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var cachedUser: UserSession? = null
    
    override suspend fun signIn(email: String, password: String): UserSession {
        val user = supabaseAuthService.signIn(email, password)
        cachedUser = user
        return user
    }
    
    override suspend fun register(email: String, password: String): UserSession {
        val user = supabaseAuthService.register(email, password)
        cachedUser = user
        return user
    }
    
    override suspend fun signInAnonymously(): UserSession {
        val user = supabaseAuthService.signInAnonymously()
        cachedUser = user
        return user
    }
    
    override suspend fun signOut() {
        supabaseAuthService.signOut()
        cachedUser = null
    }
    
    override suspend fun currentUser(): UserSession? {
        return cachedUser
    }
    
    suspend fun refreshCurrentUser() {
        cachedUser = supabaseAuthService.getCurrentUser()
    }
    
    suspend fun refreshSession() {
        cachedUser = supabaseAuthService.refreshSession()
    }
}
