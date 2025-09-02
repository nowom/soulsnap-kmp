package pl.soulsnaps.features.startup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.features.auth.manager.UserPlanManager

/**
 * AppStartupManager - zarządza stanem startu aplikacji
 * 
 * Zapobiega mignięciu onboardingu poprzez:
 * 1. Loading state podczas ładowania danych
 * 2. Sprawdzanie stanu przed wyświetleniem UI
 * 3. Płynne przejścia między stanami
 */
class AppStartupManager(
    private val userPlanManager: UserPlanManager = UserPlanManager()
) {
    
    private val _startupState = MutableStateFlow<StartupState>(StartupState.CHECKING)
    val startupState: Flow<StartupState> = _startupState.asStateFlow()
    
    /**
     * Inicjalizuje aplikację i sprawdza stan użytkownika
     */
    suspend fun initializeApp() {
        println("DEBUG: AppStartupManager - initializing app")
        
        try {
            // Czekaj na załadowanie danych użytkownika
            userPlanManager.waitForInitialization()
            
            val currentPlan = userPlanManager.getCurrentPlan()
            val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
            
            println("DEBUG: AppStartupManager - loaded state: plan=$currentPlan, onboarding=$hasCompletedOnboarding")
            
            // Określ stan startu aplikacji
            val state = when {
                currentPlan == null -> {
                    println("DEBUG: AppStartupManager - no plan found, showing onboarding")
                    StartupState.READY_FOR_ONBOARDING
                }
                !hasCompletedOnboarding -> {
                    println("DEBUG: AppStartupManager - onboarding not completed, showing onboarding")
                    StartupState.READY_FOR_ONBOARDING
                }
                else -> {
                    println("DEBUG: AppStartupManager - user ready, showing dashboard")
                    StartupState.READY_FOR_DASHBOARD
                }
            }
            
            _startupState.value = state
            
        } catch (e: Exception) {
            println("DEBUG: AppStartupManager - error during initialization: ${e.message}")
            _startupState.value = StartupState.READY_FOR_ONBOARDING // Fallback to onboarding on error
        }
    }
    
    /**
     * Pobiera aktualny stan startu
     */
    fun getCurrentStartupState(): StartupState {
        return _startupState.value
    }
    
    /**
     * Resetuje stan startu (np. po logout)
     */
    fun resetStartupState() {
        _startupState.value = StartupState.CHECKING
    }
}

/**
 * Stany startu aplikacji
 */
enum class StartupState {
    CHECKING,
    READY_FOR_ONBOARDING,
    ONBOARDING_ACTIVE,
    READY_FOR_DASHBOARD
}
