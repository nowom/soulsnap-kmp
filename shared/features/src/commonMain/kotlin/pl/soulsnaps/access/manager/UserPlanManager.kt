package pl.soulsnaps.access.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.domain.UserPlanRepository
import pl.soulsnaps.domain.interactor.UserPlanUseCase
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.features.auth.UserSessionManager

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

interface UserPlanManager {
    val currentPlan: Flow<String?>
    fun setUserPlan(planName: String)
    suspend fun setUserPlanAndWait(planName: String)
    fun getUserPlan(): String?
    fun isOnboardingCompleted(): Boolean
    fun getPlanOrDefault(): String
    fun hasPlanSet(): Boolean
    fun getCurrentPlan(): String?
    suspend fun waitForInitialization()
    fun resetUserPlan()
    fun setDefaultPlanIfNeeded()
}

class UserPlanManagerImpl(
    private val storage: UserPreferencesStorage,
    private val userPlanRepository: UserPlanRepository,
    private val userPlanUseCase: UserPlanUseCase,
    private val crashlyticsManager: CrashlyticsManager,
    private val userSessionManager: UserSessionManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : UserPlanManager {

    private val _currentPlan = MutableStateFlow<String?>(null)
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    
    override val currentPlan: Flow<String?> = _currentPlan.asStateFlow()
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
    override fun setUserPlan(planName: String) {
        println("DEBUG: UserPlanManager.setUserPlan($planName)")
        _currentPlan.value = planName
        _hasCompletedOnboarding.value = true
        
        // Zapisz na dysk i w bazie danych
        coroutineScope.launch {
            try {
                // Save to local storage
                storage.saveUserPlan(planName)
                storage.saveOnboardingCompleted(true)

                // Save to database if user is authenticated
                val currentUserId = getCurrentUserId()
                if (currentUserId != null) {
                    val planType = getPlanTypeFromName(planName)
                    val userPlan = pl.soulsnaps.domain.UserPlan(
                        userId = currentUserId,
                        planType = planType,
                        planName = planName,
                        isActive = true
                    )
                    userPlanUseCase.saveUserPlan(userPlan)
                    crashlyticsManager.log("User plan saved to database: $planName")
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e)
                crashlyticsManager.log("Error saving user plan: ${e.message}")
            }
        }
    }
    
    /**
     * Ustaw plan użytkownika i poczekaj na zapisanie danych
     */
    override suspend fun setUserPlanAndWait(planName: String) {
        println("DEBUG: UserPlanManager.setUserPlanAndWait($planName)")
        
        try {
            // Save to local storage FIRST
            storage.saveUserPlan(planName)
            storage.saveOnboardingCompleted(true)
            
            // Save to database if user is authenticated
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val planType = getPlanTypeFromName(planName)
                val userPlan = pl.soulsnaps.domain.UserPlan(
                    userId = currentUserId,
                    planType = planType,
                    planName = planName,
                    isActive = true
                )
                userPlanUseCase.saveUserPlan(userPlan)
                crashlyticsManager.log("User plan saved to database: $planName")
            }
            
            // Only set StateFlow values AFTER successful save
            _currentPlan.value = planName
            _hasCompletedOnboarding.value = true
            
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error saving user plan: ${e.message}")
            // Don't set StateFlow values if save failed
        }
    }
    
    /**
     * Pobierz aktualny plan użytkownika
     */
    override fun getUserPlan(): String? {
        return _currentPlan.value
    }
    
    /**
     * Sprawdź czy użytkownik ukończył onboarding
     */
    override fun isOnboardingCompleted(): Boolean {
        return _hasCompletedOnboarding.value
    }
    
    /**
     * Ustaw domyślny plan (GUEST) jeśli żaden nie jest ustawiony
     */
    override fun setDefaultPlanIfNeeded() {
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
    override fun resetUserPlan() {
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
    override fun hasPlanSet(): Boolean {
        return _currentPlan.value != null
    }
    
    /**
     * Pobierz plan lub domyślny
     */
    override fun getPlanOrDefault(): String {
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
    override suspend fun waitForInitialization() {
        while (_isLoading.value) {
            kotlinx.coroutines.delay(10) // Krótkie opóźnienie
        }
    }
    
    /**
     * Pobierz aktualny plan (synchronicznie)
     */
    override fun getCurrentPlan(): String? {
        return _currentPlan.value
    }
    
    /**
     * Helper method to get current user ID from UserSessionManager
     */
    private fun getCurrentUserId(): String? {
        return userSessionManager.currentUser.value?.userId
    }
    
    /**
     * Helper method to convert plan name to PlanType
     */
    private fun getPlanTypeFromName(planName: String): PlanType {
        return when (planName.uppercase()) {
            "GUEST" -> PlanType.GUEST
            "FREE_USER" -> PlanType.FREE_USER
            "PREMIUM_USER" -> PlanType.PREMIUM_USER
            "ENTERPRISE_USER" -> PlanType.ENTERPRISE_USER
            else -> PlanType.GUEST
        }
    }
}
