package pl.soulsnaps.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.access.manager.AppStartupManager
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.features.auth.UserSessionManager

class OnboardingViewModel(
    private val analyticsManager: AnalyticsManager,
    private val appStartupManager: AppStartupManager,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        analyticsManager.startOnboarding()
        analyticsManager.startStep("WELCOME")
    }

    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NextStep -> {
                analyticsManager.completeStep(_state.value.currentStep.name)
                nextStep()
            }
            is OnboardingIntent.PreviousStep -> {
                analyticsManager.completeStep(_state.value.currentStep.name)
                previousStep()
            }
            is OnboardingIntent.SkipTour -> {
                analyticsManager.skipStep("APP_TOUR")
                // Skip the app tour and go to get started
                _state.update { it.copy(currentStep = OnboardingStep.GET_STARTED) }
            }
            is OnboardingIntent.SelectFocus -> {
                analyticsManager.completeStep(_state.value.currentStep.name)
                _state.update { it.copy(selectedFocus = intent.focus) }
                nextStep()
            }
            is OnboardingIntent.Authenticate -> {
                analyticsManager.trackAuthAttempt(intent.authType.name, true)
                when (intent.authType) {
                    pl.soulsnaps.features.onboarding.AuthType.ANONYMOUS -> {
                        // Ustaw plan GUEST i ukończ onboarding
                        appStartupManager.skipOnboarding()
                        nextStep()
                    }
                    else -> {
                        // Dla innych typów auth, tylko przejdź dalej
                        nextStep()
                    }
                }
            }
            is OnboardingIntent.UpdateEmail -> {
                _state.update { it.copy(email = intent.email) }
            }
            is OnboardingIntent.UpdatePassword -> {
                _state.update { it.copy(password = intent.password) }
            }
            is OnboardingIntent.GetStarted -> {
                analyticsManager.completeOnboarding(
                    selectedFocus = _state.value.selectedFocus?.name,
                    authMethod = userSessionManager.getCurrentUser()?.let { "authenticated" } ?: "anonymous"
                )
                // Ukończ onboarding przez AppStartupManager
                appStartupManager.completeOnboarding()
                completeOnboarding()
            }
        }
    }

    private fun nextStep() {
        val currentStep = _state.value.currentStep
        val nextStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.APP_TOUR
            OnboardingStep.APP_TOUR -> OnboardingStep.PERSONALIZATION
            OnboardingStep.PERSONALIZATION -> OnboardingStep.AUTH
            OnboardingStep.AUTH -> OnboardingStep.GET_STARTED
            OnboardingStep.GET_STARTED -> OnboardingStep.GET_STARTED // Already at end
        }
        _state.update { it.copy(currentStep = nextStep) }
        analyticsManager.startStep(nextStep.name)
    }

    private fun previousStep() {
        val currentStep = _state.value.currentStep
        val previousStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME // Already at start
            OnboardingStep.APP_TOUR -> OnboardingStep.WELCOME
            OnboardingStep.PERSONALIZATION -> OnboardingStep.APP_TOUR
            OnboardingStep.AUTH -> OnboardingStep.PERSONALIZATION
            OnboardingStep.GET_STARTED -> OnboardingStep.AUTH
        }
        _state.update { it.copy(currentStep = previousStep) }
        analyticsManager.startStep(previousStep.name)
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Save onboarding completion status
                // Note: UserSessionManager already handles session persistence
                kotlinx.coroutines.delay(500)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                analyticsManager.trackError(e.message ?: "Unknown error", "Onboarding")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to complete onboarding: ${e.message}"
                    )
                }
            }
        }
    }

    fun canGoNext(): Boolean {
        return when (_state.value.currentStep) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.APP_TOUR -> true // Can skip
            OnboardingStep.PERSONALIZATION -> _state.value.selectedFocus != null
            OnboardingStep.AUTH -> true // Can always proceed from auth
            OnboardingStep.GET_STARTED -> true
        }
    }

    fun canGoPrevious(): Boolean {
        return _state.value.currentStep != OnboardingStep.WELCOME
    }

    fun getProgress(): Float {
        return (_state.value.currentStep.ordinal + 1).toFloat() / OnboardingStep.values().size
    }
} 