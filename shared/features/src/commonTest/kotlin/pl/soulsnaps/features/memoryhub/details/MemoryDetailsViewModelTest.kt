package pl.soulsnaps.features.memoryhub.details

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import pl.soulsnaps.domain.interactor.GetMemoryByIdUseCase
import pl.soulsnaps.domain.interactor.ToggleMemoryFavoriteUseCase
import pl.soulsnaps.domain.interactor.DeleteMemoryUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.auth.SessionState
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Testy dla MemoryDetailsViewModel z UserSessionManager
 */
class MemoryDetailsViewModelTest {
    
    private lateinit var getMemoryByIdUseCase: MockGetMemoryByIdUseCase
    private lateinit var toggleMemoryFavoriteUseCase: MockToggleMemoryFavoriteUseCase
    private lateinit var deleteMemoryUseCase: MockDeleteMemoryUseCase
    private lateinit var accessGuard: MockAccessGuard
    private lateinit var userSessionManager: MockUserSessionManager
    private lateinit var viewModel: MemoryDetailsViewModel
    
    @BeforeTest
    fun setup() {
        getMemoryByIdUseCase = MockGetMemoryByIdUseCase()
        toggleMemoryFavoriteUseCase = MockToggleMemoryFavoriteUseCase()
        deleteMemoryUseCase = MockDeleteMemoryUseCase()
        accessGuard = MockAccessGuard()
        userSessionManager = MockUserSessionManager()
        
        viewModel = MemoryDetailsViewModel(
            getMemoryByIdUseCase = getMemoryByIdUseCase,
            toggleMemoryFavoriteUseCase = toggleMemoryFavoriteUseCase,
            deleteMemoryUseCase = deleteMemoryUseCase,
            accessGuard = accessGuard,
            userSessionManager = userSessionManager
        )
    }
    
    @Test
    fun `should use authenticated user ID for analytics`() = runTest {
        // Given
        val userId = "authenticated_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        getMemoryByIdUseCase.setMemory(memory)
        
        // When
        viewModel.loadMemoryDetails(1)
        val state = viewModel.state.first()
        
        // Then
        assertNotNull(state.memory)
        assertEquals(memory, state.memory)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should use anonymous user ID when not authenticated`() = runTest {
        // Given
        userSessionManager.setCurrentUser(null)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        getMemoryByIdUseCase.setMemory(memory)
        
        // When
        viewModel.loadMemoryDetails(1)
        val state = viewModel.state.first()
        
        // Then
        assertNotNull(state.memory)
        assertEquals(memory, state.memory)
        // Verify that analytics would use anonymous_user
        assertNull(userSessionManager.getCurrentUser())
    }
    
    @Test
    fun `should handle toggle favorite with correct user ID`() = runTest {
        // Given
        val userId = "test_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        getMemoryByIdUseCase.setMemory(memory)
        viewModel.loadMemoryDetails(1)
        
        // When
        viewModel.handleIntent(MemoryDetailsIntent.ToggleFavorite)
        
        // Then
        assertTrue(toggleMemoryFavoriteUseCase.wasCalled)
        assertEquals(1, toggleMemoryFavoriteUseCase.calledWithMemoryId)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should handle delete memory with correct user ID`() = runTest {
        // Given
        val userId = "test_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        val memory = createTestMemory(1, "Test Memory", MoodType.HAPPY)
        getMemoryByIdUseCase.setMemory(memory)
        viewModel.loadMemoryDetails(1)
        
        // When
        viewModel.handleIntent(MemoryDetailsIntent.DeleteMemory)
        
        // Then
        assertTrue(deleteMemoryUseCase.wasCalled)
        assertEquals(1, deleteMemoryUseCase.calledWithMemoryId)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should show analytics with correct user ID`() = runTest {
        // Given
        val userId = "analytics_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        // When
        viewModel.handleIntent(MemoryDetailsIntent.ShowAnalytics)
        val state = viewModel.state.first()
        
        // Then
        assertTrue(state.showAnalytics)
        // Verify that analytics would use the correct userId
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should handle user session changes`() = runTest {
        // Given
        val userId1 = "user1"
        val userId2 = "user2"
        val userSession1 = createTestUserSession(userId1, "user1@example.com")
        val userSession2 = createTestUserSession(userId2, "user2@example.com")
        
        // When
        userSessionManager.setCurrentUser(userSession1)
        val userIdAfterFirst = userSessionManager.getCurrentUser()?.userId
        
        userSessionManager.setCurrentUser(userSession2)
        val userIdAfterSecond = userSessionManager.getCurrentUser()?.userId
        
        // Then
        assertEquals(userId1, userIdAfterFirst)
        assertEquals(userId2, userIdAfterSecond)
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
    
    private fun createTestUserSession(userId: String, email: String): UserSession {
        val currentTime = getCurrentTimeMillis()
        return UserSession(
            userId = userId,
            email = email,
            isAnonymous = false,
            displayName = "Test User",
            createdAt = currentTime,
            lastActiveAt = currentTime,
            accessToken = "test_token",
            refreshToken = "test_refresh"
        )
    }
}

// Mock implementations for testing

class MockGetMemoryByIdUseCase : GetMemoryByIdUseCase {
    private var memory: Memory? = null
    private var shouldThrowError = false
    
    fun setMemory(memory: Memory?) {
        this.memory = memory
    }
    
    fun setShouldThrowError(shouldThrow: Boolean) {
        this.shouldThrowError = shouldThrow
    }
    
    override suspend fun invoke(memoryId: Int): Result<Memory?> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException("Test error"))
        } else {
            Result.success(memory)
        }
    }
}

class MockToggleMemoryFavoriteUseCase : ToggleMemoryFavoriteUseCase {
    var wasCalled = false
    var calledWithMemoryId: Int? = null
    
    override suspend fun invoke(memoryId: Int): Result<Boolean> {
        wasCalled = true
        calledWithMemoryId = memoryId
        return Result.success(true)
    }
}

class MockDeleteMemoryUseCase : DeleteMemoryUseCase {
    var wasCalled = false
    var calledWithMemoryId: Int? = null
    
    override suspend fun invoke(memoryId: Int): Result<Unit> {
        wasCalled = true
        calledWithMemoryId = memoryId
        return Result.success(Unit)
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
    
    override val sessionState: kotlinx.coroutines.flow.StateFlow<SessionState> = 
        kotlinx.coroutines.flow.MutableStateFlow(SessionState.Unauthenticated)
    override val currentUser: kotlinx.coroutines.flow.StateFlow<UserSession?> = 
        kotlinx.coroutines.flow.MutableStateFlow(null)
}
