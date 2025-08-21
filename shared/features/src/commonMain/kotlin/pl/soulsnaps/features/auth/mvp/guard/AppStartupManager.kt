package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AppStartupManager - zarządza startem aplikacji i routingiem
 * 
 * Funkcjonalności:
 * - Sprawdzanie czy użytkownik przeszedł onboarding
 * - Automatyczne przekierowanie do odpowiedniego ekranu
 * - Obsługa pierwszego uruchomienia aplikacji
 * - Zapamiętywanie stanu aplikacji
 */
class AppStartupManager(
    private val userPlanManager: UserPlanManager = UserPlanManagerInstance.getInstance(),
    private val onboardingManager: OnboardingManager = OnboardingManagerInstance.getInstance()
) {
    
    private val _startupState = MutableStateFlow(StartupState.CHECKING)
    private val _shouldShowOnboarding = MutableStateFlow(false)
    private val _userPlan = MutableStateFlow<String?>(null)
    
    val startupState: Flow<StartupState> = _startupState.asStateFlow()
    val shouldShowOnboarding: Flow<Boolean> = _shouldShowOnboarding.asStateFlow()
    val userPlan: Flow<String?> = _userPlan.asStateFlow()
    
    /**
     * Sprawdź stan aplikacji przy starcie
     */
    fun checkAppState() {
        println("DEBUG: AppStartupManager.checkAppState() called")
        val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
        val currentPlan = userPlanManager.getUserPlan()
        
        println("DEBUG: AppStartupManager.checkAppState() - userPlan: $currentPlan, hasCompletedOnboarding: $hasCompletedOnboarding")
        
        _userPlan.value = currentPlan
        _shouldShowOnboarding.value = !hasCompletedOnboarding
        
        if (hasCompletedOnboarding) {
            println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_DASHBOARD")
            _startupState.value = StartupState.READY_FOR_DASHBOARD
        } else {
            println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_ONBOARDING")
            _startupState.value = StartupState.READY_FOR_ONBOARDING
        }
    }
    
    /**
     * Rozpocznij onboarding
     */
    fun startOnboarding() {
        onboardingManager.startOnboarding()
        _startupState.value = StartupState.ONBOARDING_ACTIVE
    }
    
    /**
     * Ukończ onboarding i przejdź do dashboard
     */
    fun completeOnboarding() {
        onboardingManager.completeOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
    }
    
    /**
     * Pomiń onboarding i przejdź do dashboard
     */
    fun skipOnboarding() {
        onboardingManager.skipOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
        _userPlan.value = "GUEST"
    }
    
    /**
     * Przejdź do dashboard
     */
    fun goToDashboard() {
        _startupState.value = StartupState.READY_FOR_DASHBOARD
    }
    
    /**
     * Pobierz aktualny plan użytkownika
     */
    fun getCurrentUserPlan(): String {
        return userPlanManager.getPlanOrDefault()
    }
    
    /**
     * Sprawdź czy użytkownik ukończył onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return userPlanManager.isOnboardingCompleted()
    }
    
    /**
     * Resetuj stan aplikacji (dla testów)
     */
    fun resetAppState() {
        userPlanManager.resetUserPlan()
        onboardingManager.resetOnboarding()
        _startupState.value = StartupState.CHECKING
        _shouldShowOnboarding.value = false
        _userPlan.value = null
    }
    
    /**
     * Pobierz aktualny stan startup
     */
    fun getStartupState(): StartupState {
        return _startupState.value
    }
    
    /**
     * Sprawdź czy powinien pokazać onboarding
     */
    fun shouldShowOnboardingNow(): Boolean {
        return _shouldShowOnboarding.value
    }
}

/**
 * Stany startu aplikacji
 */
enum class StartupState {
    CHECKING,                    // Sprawdzanie stanu aplikacji
    READY_FOR_ONBOARDING,       // Gotowy do pokazania onboarding
    ONBOARDING_ACTIVE,          // Onboarding aktywny
    READY_FOR_DASHBOARD         // Gotowy do pokazania dashboard
}

/**
 * Singleton instance dla łatwego dostępu
 */
object AppStartupManagerInstance {
    private var instance: AppStartupManager? = null
    
    fun getInstance(): AppStartupManager {
        if (instance == null) {
            instance = AppStartupManager()
        }
        return instance!!
    }
    
    fun reset() {
        instance = null
    }
}
