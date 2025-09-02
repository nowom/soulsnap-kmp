package pl.soulsnaps.features.auth.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Prosty UserPlanManager do zarządzania planami użytkownika
 */
class UserPlanManager {
    
    private val _currentPlan = MutableStateFlow<String?>(null)
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    
    val currentPlan: StateFlow<String?> = _currentPlan.asStateFlow()
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        println("DEBUG: UserPlanManager initialized")
        loadDataFromStorage()
    }
    
    /**
     * Załaduj dane z storage
     */
    private fun loadDataFromStorage() {
        println("DEBUG: UserPlanManager.loadDataFromStorage() - loading data...")
        
        // Symuluj ładowanie danych
        _isLoading.value = true
        
        // TODO: W rzeczywistej aplikacji tutaj byłoby ładowanie z SharedPreferences/Database
        // Na razie ustawiamy domyślne wartości
        _currentPlan.value = null
        _hasCompletedOnboarding.value = false
        
        println("DEBUG: UserPlanManager.loadDataFromStorage() - loaded plan: ${_currentPlan.value}, onboarding: ${_hasCompletedOnboarding.value}")
        
        _isLoading.value = false
        println("DEBUG: UserPlanManager.loadDataFromStorage() - loading completed")
    }
    
    /**
     * Ustaw plan użytkownika
     */
    fun setUserPlan(plan: String) {
        println("DEBUG: UserPlanManager.setUserPlan() - setting plan: $plan")
        _currentPlan.value = plan
        
        // Jeśli ustawiamy plan, oznacza to że onboarding został ukończony
        _hasCompletedOnboarding.value = true
        
        // TODO: Zapisz do storage
        println("DEBUG: UserPlanManager.setUserPlan() - plan set and onboarding marked as completed")
    }
    
    /**
     * Ustaw że onboarding został ukończony
     */
    fun setOnboardingCompleted() {
        println("DEBUG: UserPlanManager.setOnboardingCompleted() - marking onboarding as completed")
        _hasCompletedOnboarding.value = true
        
        // TODO: Zapisz do storage
    }
    
    /**
     * Pobierz aktualny plan
     */
    fun getCurrentPlan(): String? {
        return _currentPlan.value
    }
    
    /**
     * Sprawdź czy onboarding został ukończony
     */
    fun isOnboardingCompleted(): Boolean {
        return _hasCompletedOnboarding.value
    }
    
    /**
     * Czekaj na inicjalizację
     */
    suspend fun waitForInitialization() {
        // Symuluj opóźnienie inicjalizacji
        kotlinx.coroutines.delay(100)
        println("DEBUG: UserPlanManager.waitForInitialization() - completed")
    }
    
    /**
     * Resetuj plan użytkownika (np. po logout)
     */
    fun resetUserPlan() {
        println("DEBUG: UserPlanManager.resetUserPlan() - resetting user plan")
        _currentPlan.value = null
        _hasCompletedOnboarding.value = false
        
        // TODO: Wyczyść storage
    }
    
    /**
     * Pobierz plan z fallback do domyślnego
     */
    fun getPlanOrDefault(): String {
        return _currentPlan.value ?: "GUEST"
    }
}



