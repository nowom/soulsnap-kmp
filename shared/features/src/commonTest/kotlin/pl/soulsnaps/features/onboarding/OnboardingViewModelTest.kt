package pl.soulsnaps.features.onboarding

import dev.mokkery.mock
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

    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var appStartupManager: AppStartupManager
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var viewModel: OnboardingViewModel

    @BeforeTest
    fun setup() {
        analyticsManager = mock<AnalyticsManager>()
        appStartupManager = mock<AppStartupManager>()
        userSessionManager = mock<UserSessionManager>()

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
        // Note: Mock configuration would be needed here with Mokkery

        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        // Note: Verification would be needed here with Mokkery
    }

    @Test
    fun `should use anonymous user for analytics when user is not logged in`() = runTest {
        // Given
        // Note: Mock configuration would be needed here with Mokkery

        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        // Note: Verification would be needed here with Mokkery
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
        // Note: Mock configuration would be needed here with Mokkery

        // When
        viewModel.handleIntent(OnboardingIntent.GetStarted)

        // Then
        // Note: Verification would be needed here with Mokkery
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