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
 * Zarządzanie planem użytkownika + onboarding persisted
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
        loadDataFromStorage()
    }

    /** Ustaw plan i zaznacz onboarding jako ukończony (async persist) */
    override fun setUserPlan(planName: String) {
        println("DEBUG: UserPlanManager.setUserPlan($planName)")
        _currentPlan.value = planName
        _hasCompletedOnboarding.value = true

        coroutineScope.launch {
            try {
                storage.saveUserPlan(planName)
                storage.saveOnboardingCompleted(true)

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

    /** Ustaw plan i poczekaj na zapis (sync persist) */
    override suspend fun setUserPlanAndWait(planName: String) {
        println("DEBUG: UserPlanManager.setUserPlanAndWait($planName)")
        try {
            storage.saveUserPlan(planName)
            storage.saveOnboardingCompleted(true)

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

            _currentPlan.value = planName
            _hasCompletedOnboarding.value = true
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error saving user plan: ${e.message}")
            // brak zmiany state jeśli zapis się nie udał
        }
    }

    override fun getUserPlan(): String? = _currentPlan.value
    override fun isOnboardingCompleted(): Boolean = _hasCompletedOnboarding.value

    /** Ustaw domyślny plan tylko w pamięci (opcjonalnie rozważ też persist) */
    override fun setDefaultPlanIfNeeded() {
        if (_currentPlan.value == null) _currentPlan.value = "GUEST"
    }

    /** Async load z persistent storage */
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
                _currentPlan.value = null
                _hasCompletedOnboarding.value = false
            } finally {
                _isLoading.value = false
                println("DEBUG: UserPlanManager.loadDataFromStorage() - loading completed")
            }
        }
    }

    /** Reset planu (np. testy/wylogowanie) */
    override fun resetUserPlan() {
        _currentPlan.value = null
        _hasCompletedOnboarding.value = false
        coroutineScope.launch { storage.clearAllData() }
    }

    override fun hasPlanSet(): Boolean = _currentPlan.value != null
    override fun getPlanOrDefault(): String = _currentPlan.value ?: "GUEST"
    suspend fun hasStoredData(): Boolean = storage.hasStoredData()
    fun refreshFromStorage() = loadDataFromStorage()

    /** Czekaj aż init się zakończy */
    override suspend fun waitForInitialization() {
        while (_isLoading.value) {
            kotlinx.coroutines.delay(10)
        }
    }

    override fun getCurrentPlan(): String? = _currentPlan.value

    private fun getCurrentUserId(): String? = userSessionManager.currentUser.value?.userId

    private fun getPlanTypeFromName(planName: String): PlanType =
        when (planName.uppercase()) {
            "GUEST" -> PlanType.GUEST
            "FREE_USER" -> PlanType.FREE_USER
            "PREMIUM_USER" -> PlanType.PREMIUM_USER
            "ENTERPRISE_USER" -> PlanType.ENTERPRISE_USER
            else -> PlanType.GUEST
        }
}
