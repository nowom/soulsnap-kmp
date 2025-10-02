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
