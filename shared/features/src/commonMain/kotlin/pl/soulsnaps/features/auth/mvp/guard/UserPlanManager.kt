package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.soulsnaps.features.auth.mvp.guard.storage.UserPreferencesStorage
import pl.soulsnaps.features.auth.mvp.guard.storage.UserPreferencesStorageFactory

/**
 * UserPlanManager - zarządza zapamiętywaniem planu użytkownika
 * 
 * Funkcjonalności:
 * - Zapamiętywanie wybranego planu użytkownika
 * - Sprawdzanie czy użytkownik przeszedł onboarding
 * - Automatyczne ustawianie planu domyślnego
 * - Obsługa zmian planu
 * - Persistent storage na dysku
 */
class UserPlanManager(
    private val storage: UserPreferencesStorage = UserPreferencesStorageFactory.create(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    
    private val _currentPlan = MutableStateFlow<String?>(null)
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    
    val currentPlan: Flow<String?> = _currentPlan.asStateFlow()
    val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding.asStateFlow()
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    init {
        println("DEBUG: UserPlanManager initialized")
        // Załaduj dane z persistent storage przy starcie
        loadDataFromStorage()
    }
    
    /**
     * Ustaw plan użytkownika i oznacz onboarding jako ukończony
     */
    fun setUserPlan(planName: String) {
        println("DEBUG: UserPlanManager.setUserPlan($planName)")
        _currentPlan.value = planName
        _hasCompletedOnboarding.value = true
        
        // Zapisz na dysk
        coroutineScope.launch {
            storage.saveUserPlan(planName)
            storage.saveOnboardingCompleted(true)
        }
    }
    
    /**
     * Pobierz aktualny plan użytkownika
     */
    fun getUserPlan(): String? {
        return _currentPlan.value
    }
    
    /**
     * Sprawdź czy użytkownik ukończył onboarding
     */
    fun isOnboardingCompleted(): Boolean {
        return _hasCompletedOnboarding.value
    }
    
    /**
     * Ustaw domyślny plan (GUEST) jeśli żaden nie jest ustawiony
     */
    fun setDefaultPlanIfNeeded() {
        if (_currentPlan.value == null) {
            _currentPlan.value = "GUEST"
        }
    }
    
    /**
     * Załaduj dane z persistent storage
     */
    private fun loadDataFromStorage() {
        coroutineScope.launch {
            try {
                println("DEBUG: UserPlanManager.loadDataFromStorage() - loading data...")
                val storedPlan = storage.getUserPlan()
                val storedOnboardingCompleted = storage.isOnboardingCompleted()
                
                println("DEBUG: UserPlanManager.loadDataFromStorage() - loaded plan: $storedPlan, onboarding: $storedOnboardingCompleted")
                
                _currentPlan.value = storedPlan
                _hasCompletedOnboarding.value = storedOnboardingCompleted
            } catch (e: Exception) {
                println("DEBUG: UserPlanManager.loadDataFromStorage() - error: ${e.message}")
                // W przypadku błędu, ustaw domyślne wartości
                _currentPlan.value = null
                _hasCompletedOnboarding.value = false
            } finally {
                _isLoading.value = false
                println("DEBUG: UserPlanManager.loadDataFromStorage() - loading completed")
            }
        }
    }
    
    /**
     * Resetuj plan użytkownika (dla testów lub wylogowania)
     */
    fun resetUserPlan() {
        _currentPlan.value = null
        _hasCompletedOnboarding.value = false
        
        // Wyczyść dane z dysku
        coroutineScope.launch {
            storage.clearAllData()
        }
    }
    
    /**
     * Sprawdź czy plan jest ustawiony
     */
    fun hasPlanSet(): Boolean {
        return _currentPlan.value != null
    }
    
    /**
     * Pobierz plan lub domyślny
     */
    fun getPlanOrDefault(): String {
        return _currentPlan.value ?: "GUEST"
    }
    
    /**
     * Sprawdź czy dane są zapisane na dysku
     */
    suspend fun hasStoredData(): Boolean {
        return storage.hasStoredData()
    }
    
    /**
     * Wymuś odświeżenie danych z storage
     */
    fun refreshFromStorage() {
        loadDataFromStorage()
    }
    
    /**
     * Czekaj na zakończenie inicjalizacji
     */
    suspend fun waitForInitialization() {
        while (_isLoading.value) {
            kotlinx.coroutines.delay(10) // Krótkie opóźnienie
        }
    }
    
    /**
     * Pobierz aktualny plan (synchronicznie)
     */
    fun getCurrentPlan(): String? {
        return _currentPlan.value
    }
}

/**
 * Singleton instance dla łatwego dostępu
 */
object UserPlanManagerInstance {
    private var instance: UserPlanManager? = null
    
    fun getInstance(): UserPlanManager {
        if (instance == null) {
            instance = UserPlanManager()
        }
        return instance!!
    }
    
    fun reset() {
        instance = null
    }
}
