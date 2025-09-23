package pl.soulsnaps.features.onboarding

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.access.manager.AppStartupManager
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Testy dla OnboardingViewModel z UserSessionManager
 */
class OnboardingViewModelTest {
    
    private lateinit var analyticsManager: MockAnalyticsManager
    private lateinit var appStartupManager: MockAppStartupManager
    private lateinit var userSessionManager: MockUserSessionManager
    private lateinit var viewModel: OnboardingViewModel
    
    @BeforeTest
    fun setup() {
        analyticsManager = MockAnalyticsManager()
        appStartupManager = MockAppStartupManager()
        userSessionManager = MockUserSessionManager()
        
        viewModel = OnboardingViewModel(
            analyticsManager = analyticsManager,
            appStartupManager = appStartupManager,
            userSessionManager = userSessionManager
        )
    }
    
    @Test
    fun `should use authenticated user for analytics when user is logged in`() = runTest {
        // Given
        val userId = "authenticated_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)
        
        // Then
        assertTrue(analyticsManager.wasCompleteOnboardingCalled)
        assertEquals("authenticated", analyticsManager.calledWithAuthMethod)
        assertEquals(userId, userSessionManager.getCurrentUser()?.userId)
    }
    
    @Test
    fun `should use anonymous user for analytics when user is not logged in`() = runTest {
        // Given
        userSessionManager.setCurrentUser(null)
        
        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)
        
        // Then
        assertTrue(analyticsManager.wasCompleteOnboardingCalled)
        assertEquals("anonymous", analyticsManager.calledWithAuthMethod)
        assertNull(userSessionManager.getCurrentUser())
    }
    
    @Test
    fun `should navigate to next step when NextStep intent is triggered`() = runTest {
        // Given
        val initialState = viewModel.state.value.currentStep
        
        // When
        viewModel.handleIntent(OnboardingIntent.NextStep)
        
        // Then
        val newState = viewModel.state.value
        assertNotEquals(initialState, newState.currentStep)
        assertTrue(analyticsManager.wasCompleteStepCalled)
    }
    
    @Test
    fun `should navigate to previous step when PreviousStep intent is triggered`() = runTest {
        // Given
        // First go to next step
        viewModel.handleIntent(OnboardingIntent.NextStep)
        val afterNextState = viewModel.state.value.currentStep
        
        // When
        viewModel.handleIntent(OnboardingIntent.PreviousStep)
        
        // Then
        val newState = viewModel.state.value
        assertNotEquals(afterNextState, newState.currentStep)
        assertTrue(analyticsManager.wasCompleteStepCalled)
    }
    
    @Test
    fun `should skip tour when SkipTour intent is triggered`() = runTest {
        // Given
        val initialState = viewModel.state.value.currentStep
        
        // When
        viewModel.handleIntent(OnboardingIntent.SkipTour)
        
        // Then
        val newState = viewModel.state.value
        assertEquals(OnboardingStep.GET_STARTED, newState.currentStep)
        assertTrue(analyticsManager.wasSkipStepCalled)
    }
    
    @Test
    fun `should select focus when SelectFocus intent is triggered`() = runTest {
        // Given
        val focus = UserFocus.STRESS_MANAGEMENT
        
        // When
        viewModel.handleIntent(OnboardingIntent.SelectFocus(focus))
        
        // Then
        val state = viewModel.state.value
        assertEquals(focus, state.selectedFocus)
    }
    
    @Test
    fun `should authenticate when Authenticate intent is triggered`() = runTest {
        // Given
        val authType = AuthType.EMAIL
        
        // When
        viewModel.handleIntent(OnboardingIntent.Authenticate(authType))
        
        // Then
        val state = viewModel.state.value
        assertEquals(authType, state.authType)
    }
    
    @Test
    fun `should update email when UpdateEmail intent is triggered`() = runTest {
        // Given
        val email = "test@example.com"
        
        // When
        viewModel.handleIntent(OnboardingIntent.UpdateEmail(email))
        
        // Then
        val state = viewModel.state.value
        assertEquals(email, state.email)
    }
    
    @Test
    fun `should update password when UpdatePassword intent is triggered`() = runTest {
        // Given
        val password = "testpassword123"
        
        // When
        viewModel.handleIntent(OnboardingIntent.UpdatePassword(password))
        
        // Then
        val state = viewModel.state.value
        assertEquals(password, state.password)
    }
    
    @Test
    fun `should complete onboarding when GetStarted intent is triggered`() = runTest {
        // Given
        val userId = "test_user"
        val userSession = createTestUserSession(userId, "test@example.com")
        userSessionManager.setCurrentUser(userSession)
        
        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)
        
        // Then
        assertTrue(analyticsManager.wasCompleteOnboardingCalled)
        assertTrue(appStartupManager.wasCompleteOnboardingCalled)
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
}

// Mock classes
class MockAnalyticsManager : AnalyticsManager {
    var wasCompleteOnboardingCalled = false
    var wasCompleteStepCalled = false
    var wasSkipStepCalled = false
    var wasStartOnboardingCalled = false
    var wasStartStepCalled = false
    var wasTrackErrorCalled = false
    var wasTrackEventCalled = false
    
    var calledWithAuthMethod: String? = null
    var lastCompletedStep: String? = null
    var lastSkippedStep: String? = null
    var lastStartedStep: String? = null
    var lastError: String? = null
    var lastEvent: String? = null
    
    override fun completeOnboarding(selectedFocus: String?, authMethod: String?) {
        wasCompleteOnboardingCalled = true
        calledWithAuthMethod = authMethod
    }
    
    override fun startOnboarding() {
        wasStartOnboardingCalled = true
    }
    
    override fun startStep(step: String) {
        wasStartStepCalled = true
        lastStartedStep = step
    }
    
    override fun completeStep(step: String) {
        wasCompleteStepCalled = true
        lastCompletedStep = step
    }
    
    override fun skipStep(step: String) {
        wasSkipStepCalled = true
        lastSkippedStep = step
    }
    
    override fun trackError(error: String, screen: String) {
        wasTrackErrorCalled = true
        lastError = error
    }
    
    override fun trackEvent(event: String, properties: Map<String, Any>?) {
        wasTrackEventCalled = true
        lastEvent = event
    }
}

class MockAppStartupManager : AppStartupManager {
    var wasCompleteOnboardingCalled = false
    var wasStartOnboardingCalled = false
    var wasStartStepCalled = false
    var wasCompleteStepCalled = false
    var wasTrackErrorCalled = false
    var wasTrackEventCalled = false
    
    var lastCompletedStep: String? = null
    var lastStartedStep: String? = null
    var lastError: String? = null
    var lastEvent: String? = null
    
    override fun completeOnboarding() {
        wasCompleteOnboardingCalled = true
    }
    
    override fun startOnboarding() {
        wasStartOnboardingCalled = true
    }
    
    override fun startStep(step: String) {
        wasStartStepCalled = true
        lastStartedStep = step
    }
    
    override fun completeStep(step: String) {
        wasCompleteStepCalled = true
        lastCompletedStep = step
    }
    
    override fun trackError(error: String, screen: String) {
        wasTrackErrorCalled = true
        lastError = error
    }
    
    override fun trackEvent(event: String, properties: Map<String, Any>?) {
        wasTrackEventCalled = true
        lastEvent = event
    }
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

