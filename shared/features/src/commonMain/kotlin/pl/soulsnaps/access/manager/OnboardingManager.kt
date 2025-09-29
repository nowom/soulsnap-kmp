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
    
    /**
     * Sprawdź czy użytkownik powinien przejść onboarding
     */
    fun shouldShowOnboarding(): Boolean {
        return !userPlanManager.isOnboardingCompleted()
    }
    
    /**
     * Rozpocznij onboarding
     */
    fun startOnboarding() {
        _isOnboardingActive.value = true
        _currentStep.value = OnboardingStep.WELCOME
    }
    
    /**
     * Przejdź do następnego kroku
     */
    fun nextStep() {
        val currentStepValue = _currentStep.value
        val nextStep = when (currentStepValue) {
            OnboardingStep.WELCOME -> OnboardingStep.PLAN_SELECTION
            OnboardingStep.PLAN_SELECTION -> OnboardingStep.FEATURES_OVERVIEW
            OnboardingStep.FEATURES_OVERVIEW -> OnboardingStep.COMPLETED
            OnboardingStep.COMPLETED -> OnboardingStep.COMPLETED
        }
        _currentStep.value = nextStep
    }
    
    /**
     * Przejdź do poprzedniego kroku
     */
    fun previousStep() {
        val currentStepValue = _currentStep.value
        val previousStep = when (currentStepValue) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.PLAN_SELECTION -> OnboardingStep.WELCOME
            OnboardingStep.FEATURES_OVERVIEW -> OnboardingStep.PLAN_SELECTION
            OnboardingStep.COMPLETED -> OnboardingStep.FEATURES_OVERVIEW
        }
        _currentStep.value = previousStep
    }
    
    /**
     * Wybierz plan użytkownika
     */
    fun selectPlan(planName: String) {
        userPlanManager.setUserPlan(planName)
        nextStep()
    }
    
    /**
     * Ukończ onboarding
     */
    suspend fun completeOnboarding() {
        _currentStep.value = OnboardingStep.COMPLETED
        _isOnboardingActive.value = false
        // Ustaw plan użytkownika (domyślnie GUEST jeśli nie wybrano)
        if (!userPlanManager.hasPlanSet()) {
            userPlanManager.setUserPlanAndWait("GUEST")
        }
    }
    
    /**
     * Pomiń onboarding (ustaw domyślny plan)
     */
    fun skipOnboarding() {
        userPlanManager.setUserPlan("GUEST")
        _isOnboardingActive.value = false
    }
    
    /**
     * Resetuj onboarding (dla testów)
     */
    fun resetOnboarding() {
        userPlanManager.resetUserPlan()
        _currentStep.value = OnboardingStep.WELCOME
        _isOnboardingActive.value = false
    }
    
    /**
     * Pobierz aktualny krok
     */
    fun getCurrentStep(): OnboardingStep {
        return _currentStep.value
    }
    
    /**
     * Sprawdź czy onboarding jest aktywny
     */
    fun isActive(): Boolean {
        return _isOnboardingActive.value
    }
}

/**
 * Kroki onboarding
 */
enum class OnboardingStep {
    WELCOME,              // Powitanie
    PLAN_SELECTION,       // Wybór planu
    FEATURES_OVERVIEW,    // Przegląd funkcji
    COMPLETED            // Ukończony
}

