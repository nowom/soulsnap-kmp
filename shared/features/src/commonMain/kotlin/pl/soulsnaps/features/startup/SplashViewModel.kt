package pl.soulsnaps.features.startup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.domain.model.StartupUiState

/**
 * Thin ViewModel that acts as UI orchestrator
 * Delegates all business logic to StartupRepository
 * Only handles UI-specific concerns
 */
class SplashViewModel(
    private val startupRepository: StartupRepository
) : ViewModel() {
    
    // Expose repository state directly
    val state: StateFlow<StartupUiState> = startupRepository.state
    
    // UI-specific state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    init {
        initialize()
    }
    
    /**
     * Initialize startup flow
     */
    private fun initialize() {
        if (!_isInitialized.value) {
            startupRepository.initialize()
            _isInitialized.value = true
        }
    }
    
    /**
     * Recheck current state
     */
    fun recheck() {
        startupRepository.recheck()
    }
    
    /**
     * Start onboarding flow
     */
    fun startOnboarding() {
        startupRepository.startOnboarding()
    }
    
    /**
     * Complete onboarding
     */
    fun completeOnboarding() {
        startupRepository.completeOnboarding()
    }
    
    /**
     * Skip onboarding
     */
    fun skipOnboarding(forceGuestPlan: Boolean = true) {
        startupRepository.skipOnboarding(forceGuestPlan)
    }
    
    /**
     * Navigate to dashboard
     */
    fun goToDashboard() {
        startupRepository.goToDashboard()
    }
    
    /**
     * Navigate to auth
     */
    fun goToAuth() {
        startupRepository.goToAuth()
    }
    
    /**
     * Get current state
     */
    fun getCurrentState(): StartupUiState = startupRepository.state.value
}