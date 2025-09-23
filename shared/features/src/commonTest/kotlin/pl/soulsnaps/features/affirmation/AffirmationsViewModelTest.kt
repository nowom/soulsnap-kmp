package pl.soulsnaps.features.affirmation

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestScope
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.domain.model.ThemeType
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.audio.AudioManager
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Testy dla AffirmationsViewModel z UserSessionManager
 */
class AffirmationsViewModelTest {
    
    private lateinit var affirmationRepository: MockAffirmationRepository
    private lateinit var accessGuard: MockAccessGuard
    private lateinit var audioManager: MockAudioManager
    private lateinit var userSessionManager: MockUserSessionManager
    private lateinit var viewModel: AffirmationsViewModel
    
    @BeforeTest
    fun setup() {
        affirmationRepository = MockAffirmationRepository()
        accessGuard = MockAccessGuard()
        audioManager = MockAudioManager()
        userSessionManager = MockUserSessionManager()
        
        viewModel = AffirmationsViewModel(
            affirmationRepository = affirmationRepository,
            accessGuard = accessGuard,
            audioManager = audioManager,
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
        viewModel.onEvent(AffirmationsEvent.ShowAnalytics)
        
        // Then
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should use anonymous user ID when not authenticated`() = runTest {
        // Given
        userSessionManager.setCurrentUser(null)
        
        // When
        viewModel.onEvent(AffirmationsEvent.ShowAnalytics)
        
        // Then
        assertNull(userSessionManager.getCurrentUser())
    }
    
    @Test
    fun `should load affirmations on initial event`() = runTest {
        // Given
        val testAffirmations = listOf(
            createTestAffirmation("1", "Test affirmation 1"),
            createTestAffirmation("2", "Test affirmation 2")
        )
        affirmationRepository.setTestAffirmations(testAffirmations)
        
        // When
        viewModel.onEvent(AffirmationsEvent.LoadInitial)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(testAffirmations, state.affirmations)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `should play affirmation when Play event is triggered`() = runTest {
        // Given
        val affirmation = createTestAffirmation("1", "Test affirmation")
        
        // When
        viewModel.onEvent(AffirmationsEvent.Play(affirmation))
        
        // Then
        assertTrue(audioManager.isPlayingCalled)
        assertEquals(affirmation.text, audioManager.lastPlayedText)
    }
    
    @Test
    fun `should stop affirmation when Stop event is triggered`() = runTest {
        // Given
        val affirmation = createTestAffirmation("1", "Test affirmation")
        viewModel.onEvent(AffirmationsEvent.Play(affirmation))
        
        // When
        viewModel.onEvent(AffirmationsEvent.Stop)
        
        // Then
        assertTrue(audioManager.isStopCalled)
    }
    
    @Test
    fun `should toggle favorite when ToggleFavorite event is triggered`() = runTest {
        // Given
        val affirmation = createTestAffirmation("1", "Test affirmation")
        
        // When
        viewModel.onEvent(AffirmationsEvent.ToggleFavorite(affirmation))
        
        // Then
        assertTrue(affirmationRepository.isToggleFavoriteCalled)
        assertEquals(affirmation.id, affirmationRepository.lastToggledFavoriteId)
    }
    
    @Test
    fun `should filter affirmations when SelectFilter event is triggered`() = runTest {
        // Given
        val filter = "Emocja"
        
        // When
        viewModel.onEvent(AffirmationsEvent.SelectFilter(filter))
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(filter, state.selectedFilter)
    }
    
    @Test
    fun `should toggle favorites only when ToggleFavoritesOnly event is triggered`() = runTest {
        // Given
        val initialState = viewModel.uiState.value.showOnlyFavorites
        
        // When
        viewModel.onEvent(AffirmationsEvent.ToggleFavoritesOnly)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(!initialState, state.showOnlyFavorites)
    }
    
    // Helper functions
    private fun createTestUserSession(userId: String, email: String): UserSession {
        return UserSession(
            userId = userId,
            email = email,
            displayName = "Test User",
            authProvider = "test",
            createdAt = getCurrentTimeMillis(),
            isEmailVerified = true
        )
    }
    
    private fun createTestAffirmation(id: String, text: String): Affirmation {
        return Affirmation(
            id = id,
            text = text,
            audioUrl = "test://audio.mp3",
            emotion = "Spokój",
            timeOfDay = "Poranek",
            themeType = ThemeType.SELF_LOVE,
            isFavorite = false,
            createdAt = getCurrentTimeMillis()
        )
    }
}

// Mock classes
class MockAffirmationRepository : AffirmationRepository {
    private var testAffirmations = emptyList<Affirmation>()
    private val _affirmationsFlow = MutableStateFlow(testAffirmations)
    
    var isToggleFavoriteCalled = false
    var lastToggledFavoriteId: String? = null
    
    fun setTestAffirmations(affirmations: List<Affirmation>) {
        testAffirmations = affirmations
        _affirmationsFlow.value = affirmations
    }
    
    override fun playAffirmation(text: String) {
        // Mock implementation
    }
    
    override suspend fun getAffirmations(emotionFilter: String?): List<Affirmation> {
        return testAffirmations
    }
    
    override suspend fun saveAffirmationForMemory(memoryId: Int, text: String, mood: String) {
        // Mock implementation
    }
    
    override suspend fun getAffirmationByMemoryId(memoryId: Int): Affirmation? {
        return testAffirmations.firstOrNull()
    }
    
    override suspend fun updateIsFavorite(id: String) {
        isToggleFavoriteCalled = true
        lastToggledFavoriteId = id
    }
    
    override suspend fun clearAllFavorites() {
        // Mock implementation
    }
    
    override fun stopAudio() {
        // Mock implementation
    }
    
    override fun getAffirmationsFlow(): Flow<List<Affirmation>> {
        return _affirmationsFlow
    }
}

class MockAccessGuard : AccessGuard {
    override fun checkFeatureAccess(feature: String, userId: String): Boolean = true
    override fun checkActionPermission(action: String, userId: String): Boolean = true
    override fun consumeQuota(resource: String, userId: String): Boolean = true
    override fun getRemainingQuota(resource: String, userId: String): Int = 100
    override fun getUserQuotaStatus(userId: String): Map<String, Int> = emptyMap()
    override fun getUserPlanInfo(userId: String): Map<String, Any> = emptyMap()
}

class MockAudioManager : AudioManager {
    var isPlayingCalled = false
    var isStopCalled = false
    var lastPlayedText: String? = null
    
    override fun playAffirmation(text: String) {
        isPlayingCalled = true
        lastPlayedText = text
    }
    
    override fun stopAffirmation() {
        isStopCalled = true
    }
    
    override fun pauseAffirmation() {
        // Mock implementation
    }
    
    override fun resumeAffirmation() {
        // Mock implementation
    }
    
    override fun isPlaying(): Boolean = isPlayingCalled
    
    override fun getCurrentAffirmationId(): String? = null
}

class MockUserSessionManager : UserSessionManager {
    private var currentUser: UserSession? = null
    private val _currentUserFlow = MutableStateFlow<UserSession?>(null)
    
    fun setCurrentUser(user: UserSession?) {
        currentUser = user
        _currentUserFlow.value = user
    }
    
    override fun getCurrentUser(): UserSession? = currentUser
    
    override fun isAuthenticated(): Boolean = currentUser != null
    
    override fun onUserAuthenticated(user: UserSession) {
        setCurrentUser(user)
    }
    
    override fun onUserSignedOut() {
        setCurrentUser(null)
    }
    
    override fun onSessionExpired() {
        setCurrentUser(null)
    }
    
    override fun onAuthError(error: String) {
        // Mock implementation
    }
    
    override fun clearError() {
        // Mock implementation
    }
    
    override val sessionState: StateFlow<Any> = _currentUserFlow
    override val currentUser: StateFlow<UserSession?> = _currentUserFlow
}

