package pl.soulsnaps.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.MemoryMaintenance
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.domain.model.StartupState
import pl.soulsnaps.domain.model.StartupUiState
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.OnboardingManager

/**
 * Implementation of StartupRepository
 * Simple command-based interface with sequential processing
 * No Mutex - uses single-threaded dispatcher for sequential processing
 */
class StartupRepositoryImpl(
    private val userPlanManager: UserPlanManager,
    private val onboardingManager: OnboardingManager,
    private val authService: SupabaseAuthService,
    private val memoryMaintenance: MemoryMaintenance
) : StartupRepository {
    
    // Single source of truth for startup state
    private val _state = MutableStateFlow(StartupUiState())
    override val state: StateFlow<StartupUiState> = _state.asStateFlow()
    
    // Sequential command processing with limited parallelism
    private val commandScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default.limitedParallelism(1)
    )
    
    override fun initialize() {
        commandScope.launch {
            try {
                updateState { copy(isLoading = true, error = null) }
                
                // Wait for user plan initialization
                userPlanManager.waitForInitialization()
                
                // Perform memory maintenance if needed
                if (memoryMaintenance.isMaintenanceNeeded()) {
                    val cleanedMemories = memoryMaintenance.cleanupLargeMemories()
                    val cleanedFiles = memoryMaintenance.cleanupOrphanedFiles()
                    println("DEBUG: StartupRepository - cleaned $cleanedMemories memories, $cleanedFiles files")
                }
                
                // Check app state
                val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
                val currentPlan = userPlanManager.getUserPlan()
                val isAuthenticated = authService.isAuthenticated()
                
                println("DEBUG: StartupRepository - userPlan: $currentPlan, hasCompletedOnboarding: $hasCompletedOnboarding, isAuthenticated: $isAuthenticated")
                
                val newState = when {
                    isAuthenticated -> StartupState.READY_FOR_DASHBOARD
                    hasCompletedOnboarding && !isAuthenticated -> {
                        if (currentPlan == "GUEST") {
                            StartupState.READY_FOR_DASHBOARD
                        } else {
                            StartupState.READY_FOR_AUTH
                        }
                    }
                    else -> StartupState.READY_FOR_ONBOARDING
                }
                
                updateState {
                    copy(
                        state = newState,
                        shouldShowOnboarding = !hasCompletedOnboarding,
                        userPlan = currentPlan,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    override fun recheck() {
        commandScope.launch {
            try {
                updateState { copy(isLoading = true, error = null) }
                
                val hasCompletedOnboarding = userPlanManager.isOnboardingCompleted()
                val currentPlan = userPlanManager.getUserPlan()
                val isAuthenticated = authService.isAuthenticated()
                
                val newState = when {
                    isAuthenticated -> StartupState.READY_FOR_DASHBOARD
                    hasCompletedOnboarding && !isAuthenticated -> {
                        if (currentPlan == "GUEST") {
                            StartupState.READY_FOR_DASHBOARD
                        } else {
                            StartupState.READY_FOR_AUTH
                        }
                    }
                    else -> StartupState.READY_FOR_ONBOARDING
                }
                
                updateState {
                    copy(
                        state = newState,
                        shouldShowOnboarding = !hasCompletedOnboarding,
                        userPlan = currentPlan,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    override fun startOnboarding() {
        commandScope.launch {
            try {
                updateState { copy(isLoading = true) }
                onboardingManager.startOnboarding()
                updateState {
                    copy(
                        state = StartupState.ONBOARDING_ACTIVE,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    override fun completeOnboarding() {
        commandScope.launch {
            try {
                updateState { copy(isLoading = true) }
                onboardingManager.completeOnboarding()
                updateState {
                    copy(
                        state = StartupState.READY_FOR_DASHBOARD,
                        shouldShowOnboarding = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    override fun skipOnboarding(forceGuestPlan: Boolean) {
        commandScope.launch {
            try {
                updateState { copy(isLoading = true) }
                onboardingManager.skipOnboarding()
                updateState {
                    copy(
                        state = StartupState.READY_FOR_DASHBOARD,
                        shouldShowOnboarding = false,
                        userPlan = if (forceGuestPlan) "GUEST" else _state.value.userPlan,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    override fun goToDashboard() {
        commandScope.launch {
            updateState {
                copy(
                    state = StartupState.READY_FOR_DASHBOARD,
                    isLoading = false
                )
            }
        }
    }
    
    override fun goToAuth() {
        commandScope.launch {
            updateState {
                copy(
                    state = StartupState.READY_FOR_AUTH,
                    isLoading = false
                )
            }
        }
    }
    
    private fun handleError(e: Exception) {
        println("ERROR: StartupRepository - ${e.message}")
        updateState {
            copy(
                error = e.message ?: "Unknown error",
                isLoading = false
            )
        }
    }
    
    private fun updateState(update: StartupUiState.() -> StartupUiState) {
        _state.value = _state.value.update()
    }
}
