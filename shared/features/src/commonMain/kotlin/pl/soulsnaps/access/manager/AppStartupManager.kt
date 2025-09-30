package pl.soulsnaps.access.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.domain.MemoryRepository

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
    private val authService: SupabaseAuthService,
    private val memoryRepository: MemoryRepository
) {

    private val _startupState = MutableStateFlow(StartupState.CHECKING)
    private val _shouldShowOnboarding = MutableStateFlow(false)
    private val _userPlan = MutableStateFlow<String?>(null)

    val startupState: Flow<StartupState> = _startupState.asStateFlow()
    val shouldShowOnboarding: Flow<Boolean> = _shouldShowOnboarding.asStateFlow()
    val userPlan: Flow<String?> = _userPlan.asStateFlow()

    private val stateMutex = Mutex()

    /**
     * Sprawdź stan aplikacji przy starcie
     */
    suspend fun checkAppState() = stateMutex.withLock {
        println("DEBUG: AppStartupManager.checkAppState() called")

        userPlanManager.waitForInitialization()

        val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
        val currentPlan = userPlanManager.getUserPlan()
        val isAuthenticated = authService.isAuthenticated()

        println("DEBUG: AppStartupManager.checkAppState() - userPlan: $currentPlan, hasCompletedOnboarding: $hasCompletedOnboarding, isAuthenticated: $isAuthenticated")

        _userPlan.value = currentPlan
        _shouldShowOnboarding.value = !hasCompletedOnboarding

        when {
            isAuthenticated -> {
                println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_DASHBOARD (authenticated)")
                _startupState.value = StartupState.READY_FOR_DASHBOARD
            }
            hasCompletedOnboarding && !isAuthenticated -> {
                if (currentPlan == "GUEST") {
                    println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_DASHBOARD (guest user)")
                    _startupState.value = StartupState.READY_FOR_DASHBOARD
                } else {
                    println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_AUTH")
                    _startupState.value = StartupState.READY_FOR_AUTH
                }
            }
            else -> {
                println("DEBUG: AppStartupManager.checkAppState() - READY_FOR_ONBOARDING")
                _startupState.value = StartupState.READY_FOR_ONBOARDING
            }
        }
    }

    fun startOnboarding() {
        println("DEBUG: AppStartupManager.startOnboarding() called")
        onboardingManager.startOnboarding()
        _startupState.value = StartupState.ONBOARDING_ACTIVE
    }

    suspend fun completeOnboarding() = stateMutex.withLock {
        println("DEBUG: AppStartupManager.completeOnboarding() called")
        onboardingManager.completeOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
    }

    fun skipOnboarding() {
        println("DEBUG: AppStartupManager.skipOnboarding() called")
        onboardingManager.skipOnboarding()
        _startupState.value = StartupState.READY_FOR_DASHBOARD
        _shouldShowOnboarding.value = false
        _userPlan.value = "GUEST"
        // Opcjonalnie: userPlanManager.setUserPlan("GUEST") jeśli chcesz wymusić zapis źródła prawdy tutaj.
    }

    fun goToDashboard() {
        println("DEBUG: AppStartupManager.goToDashboard() called - changing state to READY_FOR_DASHBOARD")
        _startupState.value = StartupState.READY_FOR_DASHBOARD
    }

    fun goToAuth() {
        println("DEBUG: AppStartupManager.goToAuth() called - changing state to READY_FOR_AUTH")
        _startupState.value = StartupState.READY_FOR_AUTH
    }

    fun getCurrentUserPlan(): String = userPlanManager.getPlanOrDefault()
    fun hasCompletedOnboarding(): Boolean = userPlanManager.isOnboardingCompleted()
    suspend fun isAuthenticated(): Boolean = authService.isAuthenticated()

    /**
     * Inicjalizuj aplikację
     */
    suspend fun initializeApp() = stateMutex.withLock {
        println("DEBUG: AppStartupManager.initializeApp() called")
        checkAppState()
    }

    suspend fun resetStartupState() = stateMutex.withLock {
        println("DEBUG: AppStartupManager.resetStartupState() called")
        _startupState.value = StartupState.CHECKING
        _shouldShowOnboarding.value = false
        _userPlan.value = null
        checkAppState()
    }

    fun resetAppState() {
        userPlanManager.resetUserPlan()
        onboardingManager.resetOnboarding()
        _startupState.value = StartupState.CHECKING
        _shouldShowOnboarding.value = false
        _userPlan.value = null
    }

    fun getStartupState(): StartupState = _startupState.value
    fun shouldShowOnboardingNow(): Boolean = _shouldShowOnboarding.value
}

/** Stany startu aplikacji */
enum class StartupState {
    CHECKING,
    READY_FOR_ONBOARDING,
    ONBOARDING_ACTIVE,
    READY_FOR_AUTH,
    READY_FOR_DASHBOARD
}