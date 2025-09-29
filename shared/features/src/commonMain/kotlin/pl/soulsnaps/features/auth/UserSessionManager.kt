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
import pl.soulsnaps.crashlytics.CrashlyticsManager

interface UserSessionManager {
    val currentUser: StateFlow<UserSession?>
    val sessionState: StateFlow<SessionState>
    suspend fun onUserAuthenticated(userSession: UserSession)
    suspend fun onUserSignedOut()
    fun onSessionExpired()
    fun onAuthError(error: String)
    fun clearError()
    fun isAuthenticated(): Boolean
    fun getCurrentUser(): UserSession?
}

class UserSessionManagerImpl(
    private val sessionDataStore: SessionDataStore,
    private val crashlyticsManager: CrashlyticsManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
): UserSessionManager {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    override val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

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

    override suspend fun onUserAuthenticated(userSession: UserSession) {
        // Log to Crashlytics
        crashlyticsManager.setUserId(userSession.userId)
        crashlyticsManager.log("User authenticated: ${userSession.userId}")
        crashlyticsManager.setCustomKey("user_email", userSession.email)
        crashlyticsManager.setCustomKey("session_state", "authenticated")

        sessionDataStore.saveSession(userSession)
        _currentUser.update { userSession }
        _sessionState.update { SessionState.Authenticated(userSession) }
    }

    override suspend fun onUserSignedOut() {
        // Log to Crashlytics
        crashlyticsManager.log("User signed out")
        crashlyticsManager.setCustomKey("session_state", "unauthenticated")

        sessionDataStore.clearSession()
        _currentUser.update { null }
        _sessionState.update { SessionState.Unauthenticated }
    }

    override fun onSessionExpired() {
        _currentUser.update { null }
        _sessionState.update { SessionState.SessionExpired }
        
        // Log session expiration
        crashlyticsManager.log("Session expired")
    }

    override fun onAuthError(error: String) {
        // Log to Crashlytics
        crashlyticsManager.log("Authentication error: $error")
        crashlyticsManager.setCustomKey("auth_error", error)
        crashlyticsManager.setCustomKey("session_state", "error")

        _sessionState.update { SessionState.Error(error) }
    }

    override fun clearError() {
        _sessionState.update {
            when (val current = it) {
                is SessionState.Error -> SessionState.Unauthenticated
                else -> current
            }
        }
    }

    override fun isAuthenticated(): Boolean {
        return _sessionState.value is SessionState.Authenticated
    }

    override fun getCurrentUser(): UserSession? {
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
