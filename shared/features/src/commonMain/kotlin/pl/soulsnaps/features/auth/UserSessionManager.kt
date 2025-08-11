package pl.soulsnaps.features.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.model.UserSession

class UserSessionManager(
    private val sessionDataStore: SessionDataStore,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    init {
        // Check for existing session on startup
        checkExistingSession()
        observeSessionChanges()
    }

    private fun checkExistingSession() {
        coroutineScope.launch {
            val storedSession = sessionDataStore.getStoredSession()
            if (storedSession != null) {
                _currentUser.update { storedSession }
                _sessionState.update { SessionState.Authenticated(storedSession) }
            } else {
                _sessionState.update { SessionState.Unauthenticated }
            }
        }
    }

    private fun observeSessionChanges() {
        coroutineScope.launch {
            sessionDataStore.currentSession.collectLatest { session ->
                _currentUser.update { session }
                _sessionState.update { 
                    if (session != null) SessionState.Authenticated(session) else SessionState.Unauthenticated
                }
            }
        }
    }

    suspend fun onUserAuthenticated(userSession: UserSession) {
        sessionDataStore.saveSession(userSession)
        _currentUser.update { userSession }
        _sessionState.update { SessionState.Authenticated(userSession) }
    }

    suspend fun onUserSignedOut() {
        sessionDataStore.clearSession()
        _currentUser.update { null }
        _sessionState.update { SessionState.Unauthenticated }
    }

    fun onSessionExpired() {
        _currentUser.update { null }
        _sessionState.update { SessionState.SessionExpired }
        // TODO: Clear local storage
    }

    fun onAuthError(error: String) {
        _sessionState.update { SessionState.Error(error) }
    }

    fun clearError() {
        _sessionState.update { 
            when (val current = it) {
                is SessionState.Error -> SessionState.Unauthenticated
                else -> current
            }
        }
    }

    fun isAuthenticated(): Boolean {
        return _sessionState.value is SessionState.Authenticated
    }

    fun getCurrentUser(): UserSession? {
        return _currentUser.value
    }
}

sealed class SessionState {
    data object Loading : SessionState()
    data object Unauthenticated : SessionState()
    data class Authenticated(val user: UserSession) : SessionState()
    data object SessionExpired : SessionState()
    data class Error(val message: String) : SessionState()
}
