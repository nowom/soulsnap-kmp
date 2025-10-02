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
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.config.AuthConfig
import pl.soulsnaps.utils.getCurrentTimeMillis

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
    suspend fun validateAndRefreshSession()
}

class UserSessionManagerImpl(
    private val sessionDataStore: SessionDataStore,
    private val crashlyticsManager: CrashlyticsManager,
    private val supabaseAuthService: SupabaseAuthService,
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
            println("========================================")
            println("🔍 UserSessionManagerImpl.checkExistingSession() - CHECKING")
            println("========================================")
            
            // Validate and refresh session (checks both DataStore and Supabase)
            validateAndRefreshSession()
            
            println("========================================")
            println("✅ UserSessionManagerImpl.checkExistingSession() - COMPLETED")
            println("📊 isAuthenticated: ${isAuthenticated()}")
            println("📊 currentUser: ${_currentUser.value?.email}")
            println("========================================")
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

    override suspend fun validateAndRefreshSession() {
        println("========================================")
        println("🔄 UserSessionManagerImpl.validateAndRefreshSession() - VALIDATING SESSION")
        println("========================================")
        
        try {
            val storedSession = sessionDataStore.getStoredSession()
            
            // Check if Supabase session is still valid first
            val isSupabaseAuthenticated = supabaseAuthService.isAuthenticated()
            println("📊 Supabase session valid: $isSupabaseAuthenticated")
            
            if (isSupabaseAuthenticated) {
                // Supabase has valid session - get current user
                val currentUser = supabaseAuthService.getCurrentUser()
                if (currentUser != null) {
                    println("✅ Supabase session valid, user: ${currentUser.email}")
                    
                    // Save session to our storage if not already saved
                    if (storedSession == null || storedSession.userId != currentUser.userId) {
                        println("💾 Saving Supabase session to our storage")
                        sessionDataStore.saveSession(currentUser)
                    }
                    
                    _currentUser.update { currentUser }
                    _sessionState.update { SessionState.Authenticated(currentUser) }
                } else {
                    println("❌ Supabase session valid but no user returned")
                    _sessionState.update { SessionState.Unauthenticated }
                }
            } else if (storedSession != null) {
                // No Supabase session but we have stored session - validate stored session
                println("⚠️ No Supabase session but stored session found, validating stored session...")
                
                // Check if stored session is still valid (not expired)
                val isStoredSessionValid = isStoredSessionValid(storedSession)
                
                if (isStoredSessionValid) {
                    println("✅ Stored session is valid, restoring user session")
                    println("📊 Restored user: ${storedSession.email}")
                    _currentUser.update { storedSession }
                    _sessionState.update { SessionState.Authenticated(storedSession) }
                    
                    // Try to refresh in background to sync with Supabase
                    coroutineScope.launch {
                        try {
                            println("🔄 Attempting background session refresh...")
                            val refreshedSession = supabaseAuthService.refreshSession()
                            if (refreshedSession != null) {
                                println("✅ Background refresh successful, updating session")
                                sessionDataStore.saveSession(refreshedSession)
                                _currentUser.update { refreshedSession }
                            } else {
                                println("ℹ️ Background refresh failed, but keeping stored session")
                            }
                        } catch (e: Exception) {
                            println("ℹ️ Background refresh failed: ${e.message}, but keeping stored session")
                        }
                    }
                } else {
                    println("❌ Stored session is expired, clearing session")
                    sessionDataStore.clearSession()
                    _currentUser.update { null }
                    _sessionState.update { SessionState.SessionExpired }
                    crashlyticsManager.log("Stored session expired, user logged out")
                }
            } else {
                // No session anywhere
                println("ℹ️ No session found in Supabase or storage")
                _sessionState.update { SessionState.Unauthenticated }
            }
            
            println("========================================")
            
        } catch (e: Exception) {
            println("ERROR: UserSessionManagerImpl.validateAndRefreshSession() - failed: ${e.message}")
            crashlyticsManager.recordException(e)
            
            // On error, clear session to be safe
            sessionDataStore.clearSession()
            _currentUser.update { null }
            _sessionState.update { SessionState.Unauthenticated }
        }
    }
    
    private fun isStoredSessionValid(storedSession: UserSession): Boolean {
        // Check if session is not expired using configurable validity duration
        val sessionValidDuration = AuthConfig.SESSION_VALIDITY_DAYS * 24 * 60 * 60 * 1000L // Convert days to milliseconds
        val currentTime = getCurrentTimeMillis()
        val sessionAge = currentTime - storedSession.lastActiveAt
        
        val isValid = sessionAge < sessionValidDuration
        
        println("📊 Stored session validation:")
        println("   - Session age: ${sessionAge / (24 * 60 * 60 * 1000)} days")
        println("   - Max validity: ${AuthConfig.SESSION_VALIDITY_DAYS} days")
        println("   - Is valid: $isValid")
        
        return isValid
    }
}

sealed class SessionState {
    data object Loading : SessionState()
    data object Unauthenticated : SessionState()
    data class Authenticated(val user: UserSession) : SessionState()
    data object SessionExpired : SessionState()
    data class Error(val message: String) : SessionState()
}
