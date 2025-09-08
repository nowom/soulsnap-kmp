package pl.soulsnaps.access.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.network.SupabaseAuthService

/**
 * AppStartupManager - zarządza startem aplikacji i routingiem
 * 
 * Funkcjonalności:
 * - Sprawdzanie czy użytkownik przeszedł onboarding
 * - Sprawdzanie stanu uwierzytelnienia
 * - Automatyczne przekierowanie do odpowiedniego ekranu
 * - Obsługa pierwszego uruchomienia aplikacji
 * - Zapamiętywanie stanu aplikacji
 */
class AppStartupManager(
    private val userPlanManager: UserPlanManager,
    private val onboardingManager: OnboardingManager,
    private val authService: SupabaseAuthService
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
    suspend fun checkAppState() {
        println("DEBUG: AppStartupManager.checkAppState() called")
        
        // Czekaj na zakończenie inicjalizacji UserPlanManager
        userPlanManager.waitForInitialization()
        
        val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
        val currentPlan = userPlanManager.getUserPlan()
        val isAuthenticated = authService.isAuthenticated()
        
        println("DEBUG: AppStartupManager.checkAppState() - userPlan: $currentPlan, hasCompletedOnboarding: $hasCompletedOnboarding, isAuthenticated: $isAuthenticated")
        
        _userPlan.value = currentPlan
        _shouldShowOnboarding.value = !hasCompletedOnboarding
        
        // Nowa logika uwzględniająca stan uwierzytelnienia - priorytet dla uwierzytelnienia
        when {
            isAuthenticated -> {
                // Jeśli użytkownik jest uwierzytelniony, idź do dashboard niezależnie od onboarding
                println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_DASHBOARD (authenticated)")
                _startupState.value = StartupState.READY_FOR_DASHBOARD
            }
            hasCompletedOnboarding && !isAuthenticated -> {
                println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_AUTH")
                _startupState.value = StartupState.READY_FOR_AUTH
            }
            else -> {
                println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_ONBOARDING")
                _startupState.value = StartupState.READY_FOR_ONBOARDING
            }
        }
    }
    
    /**
     * Rozpocznij onboarding
     */
    fun startOnboarding() {
        println("DEBUG: AppStartupManager.startOnboarding() called")
        onboardingManager.startOnboarding()
        _startupState.value = StartupState.ONBOARDING_ACTIVE
    }
    
    /**
     * Ukończ onboarding i przejdź do dashboard
     */
    fun completeOnboarding() {
        println("DEBUG: AppStartupManager.completeOnboarding() called")
        onboardingManager.completeOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
    }
    
    /**
     * Pomiń onboarding i przejdź do dashboard
     */
    fun skipOnboarding() {
        println("DEBUG: AppStartupManager.skipOnboarding() called")
        onboardingManager.skipOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
        _userPlan.value = "GUEST"
    }
    
    /**
     * Przejdź do dashboard (po udanym zalogowaniu)
     */
    fun goToDashboard() {
        println("DEBUG: AppStartupManager.goToDashboard() called - changing state to READY_FOR_DASHBOARD")
        _startupState.value = StartupState.READY_FOR_DASHBOARD
    }
    
    /**
     * Przejdź do ekranu logowania
     */
    fun goToAuth() {
        println("DEBUG: AppStartupManager.goToAuth() called - changing state to READY_FOR_AUTH")
        _startupState.value = StartupState.READY_FOR_AUTH
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
     * Sprawdź czy użytkownik jest uwierzytelniony
     */
    suspend fun isAuthenticated(): Boolean {
        return authService.isAuthenticated()
    }
    
    /**
     * Inicjalizuj aplikację
     */
    suspend fun initializeApp() {
        println("DEBUG: AppStartupManager.initializeApp() called")
        checkAppState()
    }
    
    /**
     * Resetuj stan startup (dla testów)
     */
    suspend fun resetStartupState() {
        println("DEBUG: AppStartupManager.resetStartupState() called")
        _startupState.value = StartupState.CHECKING
        _shouldShowOnboarding.value = false
        _userPlan.value = null
        // Sprawdź stan aplikacji po resecie
        checkAppState()
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
    READY_FOR_AUTH,             // Gotowy do pokazania logowania
    READY_FOR_DASHBOARD         // Gotowy do pokazania dashboard
}