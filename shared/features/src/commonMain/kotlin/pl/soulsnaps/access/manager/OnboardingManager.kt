package pl.soulsnaps.access.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OnboardingManager - zarządza flow onboarding i wyborem planu
 * 
 * Funkcjonalności:
 * - Sprawdzanie czy użytkownik przeszedł onboarding
 * - Obsługa wyboru planu podczas onboarding
 * - Automatyczne przekierowanie do dashboard po ukończeniu
 * - Zapamiętywanie stanu onboarding
 */
class OnboardingManager(
    private val userPlanManager: UserPlanManager
) {

    private val _currentStep = MutableStateFlow(OnboardingStep.WELCOME)
    private val _isOnboardingActive = MutableStateFlow(false)

    val currentStep: Flow<OnboardingStep> = _currentStep.asStateFlow()
    val isOnboardingActive: Flow<Boolean> = _isOnboardingActive.asStateFlow()

    /** Czy pokazać onboarding? */
    fun shouldShowOnboarding(): Boolean = !userPlanManager.isOnboardingCompleted()

    /** Start onboarding */
    fun startOnboarding() {
        _isOnboardingActive.value = true
        _currentStep.value = OnboardingStep.WELCOME
    }

    /** Następny krok */
    fun nextStep() {
        _currentStep.value = when (_currentStep.value) {
            OnboardingStep.WELCOME -> OnboardingStep.PLAN_SELECTION
            OnboardingStep.PLAN_SELECTION -> OnboardingStep.FEATURES_OVERVIEW
            OnboardingStep.FEATURES_OVERVIEW -> OnboardingStep.COMPLETED
            OnboardingStep.COMPLETED -> OnboardingStep.COMPLETED
        }
    }

    /** Poprzedni krok */
    fun previousStep() {
        _currentStep.value = when (_currentStep.value) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.PLAN_SELECTION -> OnboardingStep.WELCOME
            OnboardingStep.FEATURES_OVERVIEW -> OnboardingStep.PLAN_SELECTION
            OnboardingStep.COMPLETED -> OnboardingStep.FEATURES_OVERVIEW
        }
    }

    /** Wybór planu podczas onboarding */
    fun selectPlan(planName: String) {
        userPlanManager.setUserPlan(planName)
        nextStep()
    }

    /** Ukończ onboarding */
    suspend fun completeOnboarding() {
        _currentStep.value = OnboardingStep.COMPLETED
        _isOnboardingActive.value = false
        // Domyślny plan jeśli żaden nie wybrany
        if (!userPlanManager.hasPlanSet()) {
            userPlanManager.setUserPlanAndWait("GUEST")
        }
    }

    /** Pomiń onboarding */
    fun skipOnboarding() {
        userPlanManager.setUserPlan("GUEST")
        _isOnboardingActive.value = false
    }

    /** Reset (testy) */
    fun resetOnboarding() {
        userPlanManager.resetUserPlan()
        _currentStep.value = OnboardingStep.WELCOME
        _isOnboardingActive.value = false
    }

    fun getCurrentStep(): OnboardingStep = _currentStep.value
    fun isActive(): Boolean = _isOnboardingActive.value
}

/** Kroki onboarding */
enum class OnboardingStep {
    WELCOME,              // Powitanie
    PLAN_SELECTION,       // Wybór planu
    FEATURES_OVERVIEW,    // Przegląd funkcji
    COMPLETED             // Ukończony
}
