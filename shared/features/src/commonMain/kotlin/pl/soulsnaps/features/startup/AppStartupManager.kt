package pl.soulsnaps.features.startup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.features.auth.mvp.guard.UserPlanManager

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
    
    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
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
                    StartupState.ShowOnboarding
                }
                !hasCompletedOnboarding -> {
                    println("DEBUG: AppStartupManager - onboarding not completed, showing onboarding")
                    StartupState.ShowOnboarding
                }
                else -> {
                    println("DEBUG: AppStartupManager - user ready, showing dashboard")
                    StartupState.ShowDashboard
                }
            }
            
            _startupState.value = state
            
        } catch (e: Exception) {
            println("DEBUG: AppStartupManager - error during initialization: ${e.message}")
            _startupState.value = StartupState.Error(e.message ?: "Unknown error")
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
        _startupState.value = StartupState.Loading
    }
}

/**
 * Stany startu aplikacji
 */
sealed class StartupState {
    object Loading : StartupState()
    object ShowOnboarding : StartupState()
    object ShowDashboard : StartupState()
    data class Error(val message: String) : StartupState()
}
