package pl.soulsnaps.domain.model

/**
 * UI State for startup flow
 * Single source of truth for startup state
 */
data class StartupUiState(
    val state: StartupState = StartupState.CHECKING,
    val shouldShowOnboarding: Boolean = false,
    val userPlan: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false
) {
    val isReady: Boolean get() = state == StartupState.READY_FOR_DASHBOARD
    val needsOnboarding: Boolean get() = state == StartupState.READY_FOR_ONBOARDING
    val needsAuth: Boolean get() = state == StartupState.READY_FOR_AUTH
    val isChecking: Boolean get() = state == StartupState.CHECKING
}

/**
 * Startup states
 */
enum class StartupState {
    CHECKING,
    READY_FOR_ONBOARDING,
    ONBOARDING_ACTIVE,
    READY_FOR_AUTH,
    READY_FOR_DASHBOARD
}

