package pl.soulsnaps.features.onboarding

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.access.manager.AppStartupManager
import pl.soulsnaps.access.manager.OnboardingManager
import pl.soulsnaps.analytics.FirebaseAnalyticsManager
import pl.soulsnaps.analyticsManager
import pl.soulsnaps.appStartupManager
import pl.soulsnaps.domain.model.UserSession
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.onboardingManager
import pl.soulsnaps.utils.getCurrentTimeMillis
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Testy dla OnboardingViewModel z UserSessionManager
 */
class OnboardingViewModelTest {

    private lateinit var userSessionManager: UserSessionManager
    private lateinit var viewModel: OnboardingViewModel

    @BeforeTest
    fun setup() {
        userSessionManager = mock<UserSessionManager>()
        val firebaseAnalyticsManager = mock<FirebaseAnalyticsManager> {
            every { logEvent(any()) } calls { /* no-op */ }
            every { logEvent(eq("onboarding_started"), any()) } calls { /* no-op */ }
            every { logScreenView(eq("WELCOME")) } calls { /* no-op */ }

        }
        val analyticsManager = AnalyticsManager(mock(), mock(), firebaseAnalyticsManager)
        val onboardingManager = OnboardingManager(mock())
        val appStartupManager = AppStartupManager(mock(), onboardingManager, SupabaseAuthService(mock()))

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
        val firebaseAnalyticsManager =  mock<FirebaseAnalyticsManager>(MockMode.autoUnit) {
            // Najprostsze: akceptuj dowolne parametry dla tego eventu
            every { logEvent(eq("onboarding_started"), any()) } calls { /* no-op */ }

            // (opcjonalnie) wszystkie eventy
            every { logEvent(any(), any()) } calls { /* no-op */ }
        }
        val analyticsManager = AnalyticsManager(mock(), mock(), firebaseAnalyticsManager)
        
        // Mock userSessionManager to return authenticated user
        every { userSessionManager.getCurrentUser() } returns userSession
        
        // Create new viewModel with mocked analyticsManager
        val onboardingManager = OnboardingManager(mock())
        val appStartupManager = AppStartupManager(mock(), onboardingManager, SupabaseAuthService(mock()))
        val testViewModel = OnboardingViewModel(
            analyticsManager = analyticsManager,
            appStartupManager = appStartupManager,
            userSessionManager = userSessionManager
        )

        // When
        testViewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        verify { 
            analyticsManager.completeOnboarding(
                selectedFocus = null, // No focus selected initially
                authMethod = "authenticated"
            ) 
        }
    }

    @Test
    fun `should use anonymous user for analytics when user is not logged in`() = runTest {
        // Given
        val firebaseAnalyticsManager = mock<FirebaseAnalyticsManager>()
        val analyticsManager = AnalyticsManager(mock(), mock(), mock())
        
        // Mock userSessionManager to return null (no authenticated user)
        every { userSessionManager.getCurrentUser() } returns null
        
        // Create new viewModel with mocked analyticsManager
        val onboardingManager = OnboardingManager(mock())
        val appStartupManager = AppStartupManager(mock(), onboardingManager, SupabaseAuthService(mock()))
        val testViewModel = OnboardingViewModel(
            analyticsManager = analyticsManager,
            appStartupManager = appStartupManager,
            userSessionManager = userSessionManager
        )

        // When
        testViewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        verify { 
            analyticsManager.completeOnboarding(
                selectedFocus = null, // No focus selected initially
                authMethod = "anonymous"
            ) 
        }
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
        // Note: Verification would be needed here with Mokkery
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
        // Note: Verification would be needed here with Mokkery
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
        // Note: Verification would be needed here with Mokkery
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
        // Note: Verification would be needed here with Mokkery
        // The authType is not stored in state, only tracked in analytics
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
        val firebaseAnalyticsManager = mock<FirebaseAnalyticsManager>()
        val analyticsManager = AnalyticsManager(mock(), mock(), mock())
        
        // Mock userSessionManager to return authenticated user
        every { userSessionManager.getCurrentUser() } returns userSession
        
        // Create new viewModel with mocked analyticsManager
        val onboardingManager = OnboardingManager(mock())
        val appStartupManager = AppStartupManager(mock(), onboardingManager, SupabaseAuthService(mock()))
        val testViewModel = OnboardingViewModel(
            analyticsManager = analyticsManager,
            appStartupManager = appStartupManager,
            userSessionManager = userSessionManager
        )

        // When
        testViewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        verify { 
            analyticsManager.completeOnboarding(
                selectedFocus = null, // No focus selected initially
                authMethod = "authenticated"
            ) 
        }
    }

    // Helper functions
    private fun createTestUserSession(userId: String, email: String): UserSession {
        return UserSession(
            userId = userId,
            email = email,
            displayName = "Test User",
            createdAt = getCurrentTimeMillis(),
            lastActiveAt = getCurrentTimeMillis()
        )
    }
}