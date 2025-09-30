package pl.soulsnaps.domain

import kotlinx.coroutines.flow.StateFlow
import pl.soulsnaps.domain.model.StartupUiState

/**
 * Repository for startup flow management
 * Simple command-based interface without MVI complexity
 * Single source of truth for startup state
 */
interface StartupRepository {
    val state: StateFlow<StartupUiState>
    fun initialize()
    fun recheck()
    fun startOnboarding()
    fun completeOnboarding()
    fun skipOnboarding(forceGuestPlan: Boolean = true)
    fun goToAuth()
    fun goToDashboard()
}
