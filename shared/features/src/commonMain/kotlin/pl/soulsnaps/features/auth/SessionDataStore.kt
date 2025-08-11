package pl.soulsnaps.features.auth

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.domain.model.UserSession

interface SessionDataStore {
    val currentSession: Flow<UserSession?>
    val isAuthenticated: Flow<Boolean>
    
    suspend fun saveSession(userSession: UserSession)
    suspend fun clearSession()
    suspend fun getStoredSession(): UserSession?
}

// In-memory implementation for MVP
class InMemorySessionDataStore : SessionDataStore {
    private var storedSession: UserSession? = null
    
    override val currentSession: Flow<UserSession?> = kotlinx.coroutines.flow.flowOf(storedSession)
    override val isAuthenticated: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(storedSession != null)
    
    override suspend fun saveSession(userSession: UserSession) {
        storedSession = userSession
    }
    
    override suspend fun clearSession() {
        storedSession = null
    }
    
    override suspend fun getStoredSession(): UserSession? = storedSession
}
