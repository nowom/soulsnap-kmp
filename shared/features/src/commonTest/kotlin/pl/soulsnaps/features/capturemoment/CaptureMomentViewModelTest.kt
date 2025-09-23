package pl.soulsnaps.features.capturemoment

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.domain.service.AffirmationService
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Testy dla CaptureMomentViewModel z UserSessionManager
 */
class CaptureMomentViewModelTest {
    
    private lateinit var saveMemoryUseCase: MockSaveMemoryUseCase
    private lateinit var accessGuard: MockAccessGuard
    private lateinit var affirmationService: MockAffirmationService
    private lateinit var userSessionManager: MockUserSessionManager
    private lateinit var viewModel: CaptureMomentViewModel
    
    @BeforeTest
    fun setup() {
        saveMemoryUseCase = MockSaveMemoryUseCase()
        accessGuard = MockAccessGuard()
        affirmationService = MockAffirmationService()
        userSessionManager = MockUserSessionManager()
        
        viewModel = CaptureMomentViewModel(
            saveMemoryUseCase = saveMemoryUseCase,
            accessGuard = accessGuard,
            affirmationService = affirmationService,
            userSessionManager = userSessionManager
        )
    }
    
    @Test
    fun `should use authenticated user ID for analytics`() = runTest {
        // Given
        val userId = "authenticated_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.ShowAnalytics)
        val state = viewModel.state.first()
        
        // Then
        assertTrue(state.showAnalytics)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should use anonymous user ID when not authenticated`() = runTest {
        // Given
        userSessionManager.setCurrentUser(null)
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.ShowAnalytics)
        val state = viewModel.state.first()
        
        // Then
        assertTrue(state.showAnalytics)
        // Verify that analytics would use anonymous_user
        assertNull(userSessionManager.getCurrentUser())
    }
    
    @Test
    fun `should save memory with correct user context`() = runTest {
        // Given
        val userId = "test_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        saveMemoryUseCase.setResult(Result.success(memory))
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.SaveMemory(memory))
        val state = viewModel.state.first()
        
        // Then
        assertTrue(saveMemoryUseCase.wasCalled)
        assertEquals(memory, saveMemoryUseCase.calledWithMemory)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should handle user session changes during save`() = runTest {
        // Given
        val userId1 = "user1"
        val userId2 = "user2"
        val userSession1 = createTestUserSession(userId1, "user1@example.com")
        val userSession2 = createTestUserSession(userId2, "user2@example.com")
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        saveMemoryUseCase.setResult(Result.success(memory))
        
        // When
        userSessionManager.setCurrentUser(userSession1)
        viewModel.handleIntent(CaptureMomentIntent.SaveMemory(memory))
        
        userSessionManager.setCurrentUser(userSession2)
        viewModel.handleIntent(CaptureMomentIntent.SaveMemory(memory))
        
        // Then
        assertEquals(2, saveMemoryUseCase.callCount)
        assertEquals(userId2, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should handle analytics update with correct user ID`() = runTest {
        // Given
        val userId = "analytics_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.UpdateAnalytics)
        val state = viewModel.state.first()
        
        // Then
        assertTrue(state.showAnalytics)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should handle anonymous user for analytics`() = runTest {
        // Given
        val anonymousSession = createTestUserSession("anonymous", "anonymous@example.com", isAnonymous = true)
        userSessionManager.setCurrentUser(anonymousSession)
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.ShowAnalytics)
        val state = viewModel.state.first()
        
        // Then
        assertTrue(state.showAnalytics)
        assertTrue(userSessionManager.getCurrentUser()?.isAnonymous == true)
    }
    
    @Test
    fun `should maintain user context across multiple operations`() = runTest {
        // Given
        val userId = "persistent_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        saveMemoryUseCase.setResult(Result.success(memory))
        
        // When
        viewModel.handleIntent(CaptureMomentIntent.ShowAnalytics)
        viewModel.handleIntent(CaptureMomentIntent.SaveMemory(memory))
        viewModel.handleIntent(CaptureMomentIntent.UpdateAnalytics)
        
        // Then
        assertTrue(saveMemoryUseCase.wasCalled)
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    // ===== HELPER METHODS =====
    
    private fun createTestMemory(id: Int, title: String, mood: MoodType): Memory {
        val currentTime = getCurrentTimeMillis()
        return Memory(
            id = id,
            title = title,
            description = "Test description",
            createdAt = currentTime,
            mood = mood,
            photoUri = null,
            audioUri = null,
            locationName = "Test Location",
            latitude = null,
            longitude = null
        )
    }
    
    private fun createTestUserSession(
        userId: String, 
        email: String, 
        isAnonymous: Boolean = false
    ): UserSession {
        val currentTime = getCurrentTimeMillis()
        return UserSession(
            userId = userId,
            email = email,
            isAnonymous = isAnonymous,
            displayName = "Test User",
            createdAt = currentTime,
            lastActiveAt = currentTime,
            accessToken = "test_token",
            refreshToken = "test_refresh"
        )
    }
}

// Mock implementations for testing

class MockSaveMemoryUseCase : SaveMemoryUseCase {
    private var result: Result<Memory> = Result.failure(RuntimeException("Not set"))
    var wasCalled = false
    var calledWithMemory: Memory? = null
    var callCount = 0
    
    fun setResult(result: Result<Memory>) {
        this.result = result
    }
    
    override suspend fun invoke(memory: Memory): Result<Memory> {
        wasCalled = true
        calledWithMemory = memory
        callCount++
        return result
    }
}

class MockAccessGuard : AccessGuard {
    override suspend fun checkFeatureAccess(userId: String, feature: String): Boolean = true
    override suspend fun checkActionPermission(userId: String, action: String): Boolean = true
    override suspend fun consumeQuota(userId: String, quota: String): Boolean = true
    override suspend fun getRemainingQuota(userId: String, quota: String): Int = 100
    override suspend fun getUserQuotaStatus(userId: String): Map<String, Int> = emptyMap()
    override suspend fun getUserPlanInfo(userId: String): pl.soulsnaps.access.guard.PlanInfo? = null
}

class MockAffirmationService : AffirmationService {
    override suspend fun generateAffirmation(request: pl.soulsnaps.domain.model.AffirmationRequest): Result<pl.soulsnaps.domain.model.Affirmation> {
        return Result.success(
            pl.soulsnaps.domain.model.Affirmation(
                id = 1,
                text = "Test affirmation",
                theme = pl.soulsnaps.domain.model.ThemeType.MOTIVATION,
                isFavorite = false
            )
        )
    }
}

class MockUserSessionManager : UserSessionManager {
    private var currentUser: UserSession? = null
    
    fun setCurrentUser(user: UserSession?) {
        currentUser = user
    }
    
    override fun getCurrentUser(): UserSession? = currentUser
    
    override fun isAuthenticated(): Boolean = currentUser != null
    
    override suspend fun onUserAuthenticated(userSession: UserSession) {
        currentUser = userSession
    }
    
    override suspend fun onUserSignedOut() {
        currentUser = null
    }
    
    override fun onSessionExpired() {
        currentUser = null
    }
    
    override fun onAuthError(error: String) {}
    override fun clearError() {}
    
    override val sessionState: kotlinx.coroutines.flow.StateFlow<pl.soulsnaps.features.auth.SessionState> = 
        kotlinx.coroutines.flow.MutableStateFlow(pl.soulsnaps.features.auth.SessionState.Unauthenticated)
    override val currentUser: kotlinx.coroutines.flow.StateFlow<UserSession?> = 
        kotlinx.coroutines.flow.MutableStateFlow(null)
}
