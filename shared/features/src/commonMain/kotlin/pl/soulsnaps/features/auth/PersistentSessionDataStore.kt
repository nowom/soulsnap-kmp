package pl.soulsnaps.features.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Persistent session data store using UserPreferencesStorage
 * Saves session across app restarts
 */
class PersistentSessionDataStore(
    private val userPreferencesStorage: UserPreferencesStorage
) : SessionDataStore {
    
    companion object {
        private const val KEY_SESSION_USER_ID = "session_user_id"
        private const val KEY_SESSION_EMAIL = "session_email"
        private const val KEY_SESSION_DISPLAY_NAME = "session_display_name"
        private const val KEY_SESSION_IS_ANONYMOUS = "session_is_anonymous"
        private const val KEY_SESSION_CREATED_AT = "session_created_at"
        private const val KEY_SESSION_LAST_ACTIVE_AT = "session_last_active_at"
        private const val KEY_SESSION_ACCESS_TOKEN = "session_access_token"
        private const val KEY_SESSION_REFRESH_TOKEN = "session_refresh_token"
    }
    
    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: Flow<UserSession?> = _currentSession.asStateFlow()
    override val isAuthenticated: Flow<Boolean> = _currentSession.map { it != null }
    
    init {
        // Load session on initialization
        CoroutineScope(Dispatchers.Default).launch {
            val session = loadSessionFromStorage()
            _currentSession.value = session
            println("========================================")
            println("💾 PersistentSessionDataStore.init() - LOADING SESSION")
            println("========================================")
            if (session != null) {
                println("✅ Session loaded: userId=${session.userId}, email=${session.email}")
            } else {
                println("ℹ️ No stored session found")
            }
            println("========================================")
        }
    }
    
    override suspend fun saveSession(userSession: UserSession) {
        println("========================================")
        println("💾 PersistentSessionDataStore.saveSession() - SAVING SESSION")
        println("========================================")
        println("📊 userId: ${userSession.userId}")
        println("📊 email: ${userSession.email}")
        println("📊 displayName: ${userSession.displayName}")
        println("📊 isAnonymous: ${userSession.isAnonymous}")
        
        try {
            // Save to persistent storage
            userPreferencesStorage.saveString(KEY_SESSION_USER_ID, userSession.userId)
            userPreferencesStorage.saveString(KEY_SESSION_EMAIL, userSession.email)
            userSession.displayName?.let { 
                userPreferencesStorage.saveString(KEY_SESSION_DISPLAY_NAME, it)
            }
            userPreferencesStorage.saveBoolean(KEY_SESSION_IS_ANONYMOUS, userSession.isAnonymous)
            userPreferencesStorage.saveLong(KEY_SESSION_CREATED_AT, userSession.createdAt)
            userPreferencesStorage.saveLong(KEY_SESSION_LAST_ACTIVE_AT, userSession.lastActiveAt)
            userSession.accessToken?.let {
                userPreferencesStorage.saveString(KEY_SESSION_ACCESS_TOKEN, it)
            }
            userSession.refreshToken?.let {
                userPreferencesStorage.saveString(KEY_SESSION_REFRESH_TOKEN, it)
            }
            
            // Update flow
            _currentSession.value = userSession
            
            println("========================================")
            println("✅ PersistentSessionDataStore.saveSession() - SESSION SAVED")
            println("========================================")
        } catch (e: Exception) {
            println("========================================")
            println("❌ PersistentSessionDataStore.saveSession() - FAILED: ${e.message}")
            println("========================================")
            e.printStackTrace()
        }
    }
    
    override suspend fun clearSession() {
        println("========================================")
        println("🗑️ PersistentSessionDataStore.clearSession() - CLEARING SESSION")
        println("========================================")
        
        try {
            // Clear from persistent storage
            userPreferencesStorage.removeString(KEY_SESSION_USER_ID)
            userPreferencesStorage.removeString(KEY_SESSION_EMAIL)
            userPreferencesStorage.removeString(KEY_SESSION_DISPLAY_NAME)
            userPreferencesStorage.removeBoolean(KEY_SESSION_IS_ANONYMOUS)
            userPreferencesStorage.removeLong(KEY_SESSION_CREATED_AT)
            userPreferencesStorage.removeLong(KEY_SESSION_LAST_ACTIVE_AT)
            userPreferencesStorage.removeString(KEY_SESSION_ACCESS_TOKEN)
            userPreferencesStorage.removeString(KEY_SESSION_REFRESH_TOKEN)
            
            // Update flow
            _currentSession.value = null
            
            println("========================================")
            println("✅ PersistentSessionDataStore.clearSession() - SESSION CLEARED")
            println("========================================")
        } catch (e: Exception) {
            println("========================================")
            println("❌ PersistentSessionDataStore.clearSession() - FAILED: ${e.message}")
            println("========================================")
            e.printStackTrace()
        }
    }
    
    override suspend fun getStoredSession(): UserSession? {
        return loadSessionFromStorage()
    }
    
    private suspend fun loadSessionFromStorage(): UserSession? {
        return try {
            val userId = userPreferencesStorage.getString(KEY_SESSION_USER_ID)
            val email = userPreferencesStorage.getString(KEY_SESSION_EMAIL)
            
            if (userId != null && email != null) {
                val displayName = userPreferencesStorage.getString(KEY_SESSION_DISPLAY_NAME)
                val isAnonymous = userPreferencesStorage.getBoolean(KEY_SESSION_IS_ANONYMOUS) ?: false
                val createdAt = userPreferencesStorage.getLong(KEY_SESSION_CREATED_AT)
                val lastActiveAt = userPreferencesStorage.getLong(KEY_SESSION_LAST_ACTIVE_AT)
                val accessToken = userPreferencesStorage.getString(KEY_SESSION_ACCESS_TOKEN)
                val refreshToken = userPreferencesStorage.getString(KEY_SESSION_REFRESH_TOKEN)
                requireNotNull(createdAt)
                requireNotNull(lastActiveAt)
                UserSession(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    isAnonymous = isAnonymous,
                    createdAt = createdAt,
                    lastActiveAt = lastActiveAt,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("ERROR: PersistentSessionDataStore.loadSessionFromStorage() - failed: ${e.message}")
            null
        }
    }
}
