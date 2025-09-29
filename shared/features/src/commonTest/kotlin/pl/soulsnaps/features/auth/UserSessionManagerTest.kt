package pl.soulsnaps.features.auth

import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testy dla UserSessionManager
 */
class UserSessionManagerTest {
    
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var sessionDataStore: SessionDataStore
    
    @BeforeTest
    fun setup() {
        sessionDataStore = mock<SessionDataStore>()
        coroutineScope = CoroutineScope(SupervisorJob())
        userSessionManager = mock()
    }
    
    @Test
    fun `should start with loading state when no stored session`() = runTest {
        // Given - MockSessionDataStore starts with null session
        
        // When
        val initialState = userSessionManager.sessionState.first()
        
        // Then
        assertTrue(initialState is SessionState.Loading)
    }
    
    @Test
    fun `should authenticate user and update state`() = runTest {
        // Given
        val userSession = createTestUserSession("user123", "test@example.com")
        
        // When
        userSessionManager.onUserAuthenticated(userSession)
        val sessionState = userSessionManager.sessionState.first()
        val currentUser = userSessionManager.getCurrentUser()
        
        // Then
        assertTrue(sessionState is SessionState.Authenticated)
        assertEquals(userSession, (sessionState as SessionState.Authenticated).user)
        assertEquals(userSession, currentUser)
        assertTrue(userSessionManager.isAuthenticated())
    }
    
    @Test
    fun `should sign out user and clear state`() = runTest {
        // Given
        val userSession = createTestUserSession("user123", "test@example.com")
        userSessionManager.onUserAuthenticated(userSession)
        
        // When
        userSessionManager.onUserSignedOut()
        val sessionState = userSessionManager.sessionState.first()
        val currentUser = userSessionManager.getCurrentUser()
        
        // Then
        assertTrue(sessionState is SessionState.Unauthenticated)
        assertNull(currentUser)
        assertFalse(userSessionManager.isAuthenticated())
    }
    
    @Test
    fun `should handle session expiration`() = runTest {
        // Given
        val userSession = createTestUserSession("user123", "test@example.com")
        userSessionManager.onUserAuthenticated(userSession)
        
        // When
        userSessionManager.onSessionExpired()
        val sessionState = userSessionManager.sessionState.first()
        val currentUser = userSessionManager.getCurrentUser()
        
        // Then
        assertTrue(sessionState is SessionState.SessionExpired)
        assertNull(currentUser)
        assertFalse(userSessionManager.isAuthenticated())
    }
    
    @Test
    fun `should handle auth errors`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        
        // When
        userSessionManager.onAuthError(errorMessage)
        val sessionState = userSessionManager.sessionState.first()
        
        // Then
        assertTrue(sessionState is SessionState.Error)
        assertEquals(errorMessage, (sessionState as SessionState.Error).message)
    }
    
    @Test
    fun `should clear error state`() = runTest {
        // Given
        userSessionManager.onAuthError("Test error")
        
        // When
        userSessionManager.clearError()
        val sessionState = userSessionManager.sessionState.first()
        
        // Then
        assertTrue(sessionState is SessionState.Unauthenticated)
    }
    
    @Test
    fun `should load existing session on startup`() = runTest {
        // Given
        val storedSession = createTestUserSession("user123", "test@example.com")
        sessionDataStore.saveSession(storedSession)
        
        // When
        val userSessionManager: UserSessionManager = mock()
        val sessionState = userSessionManager.sessionState.first()
        val currentUser = userSessionManager.getCurrentUser()
        
        // Then
        assertTrue(sessionState is SessionState.Authenticated)
        assertEquals(storedSession, (sessionState as SessionState.Authenticated).user)
        assertEquals(storedSession, currentUser)
        assertTrue(userSessionManager.isAuthenticated())
    }
    
    @Test
    fun `should handle anonymous user`() = runTest {
        // Given
        val anonymousSession = createTestUserSession("anonymous", "anonymous@example.com", isAnonymous = true)
        
        // When
        userSessionManager.onUserAuthenticated(anonymousSession)
        val sessionState = userSessionManager.sessionState.first()
        val currentUser = userSessionManager.getCurrentUser()
        
        // Then
        assertTrue(sessionState is SessionState.Authenticated)
        assertEquals(anonymousSession, (sessionState as SessionState.Authenticated).user)
        assertEquals(anonymousSession, currentUser)
        assertTrue(anonymousSession.isAnonymous)
    }
    
    @Test
    fun `should update state when session changes`() = runTest {
        // Given
        val userSession1 = createTestUserSession("user1", "user1@example.com")
        val userSession2 = createTestUserSession("user2", "user2@example.com")
        
        // When
        userSessionManager.onUserAuthenticated(userSession1)
        val state1 = userSessionManager.sessionState.first()
        
        userSessionManager.onUserAuthenticated(userSession2)
        val state2 = userSessionManager.sessionState.first()
        
        // Then
        assertTrue(state1 is SessionState.Authenticated)
        assertEquals(userSession1, (state1 as SessionState.Authenticated).user)
        
        assertTrue(state2 is SessionState.Authenticated)
        assertEquals(userSession2, (state2 as SessionState.Authenticated).user)
    }
    
    // ===== HELPER METHODS =====
    
    private fun createTestUserSession(
        userId: String,
        email: String,
        isAnonymous: Boolean = false,
        displayName: String? = null
    ): UserSession {
        val currentTime = getCurrentTimeMillis()
        return UserSession(
            userId = userId,
            email = email,
            isAnonymous = isAnonymous,
            displayName = displayName,
            createdAt = currentTime,
            lastActiveAt = currentTime,
            accessToken = "test_token_$userId",
            refreshToken = "test_refresh_$userId"
        )
    }
}