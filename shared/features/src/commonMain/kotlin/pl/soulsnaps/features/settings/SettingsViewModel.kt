package pl.soulsnaps.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.data.MemoryRepositoryImpl
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.interactor.SignOutUseCase
import pl.soulsnaps.domain.interactor.ClearUserDataUseCase
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.domain.model.UserSession
import kotlin.getValue

data class SettingsState(
    val currentPlan: String? = null,
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)

class SettingsViewModel(
    val userPlanManager: UserPlanManager,
    val memoryRepository: MemoryRepository,
    val signOutUseCase: SignOutUseCase,
    val clearUserDataUseCase: ClearUserDataUseCase,
    val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadUserData()
        
        // Listen for user session changes
        viewModelScope.launch {
            userSessionManager.sessionState.collect { sessionState ->
                when (sessionState) {
                    is pl.soulsnaps.features.auth.SessionState.Authenticated -> {
                        println("DEBUG: SettingsViewModel - user authenticated, updating data")
                        loadUserData()
                    }
                    is pl.soulsnaps.features.auth.SessionState.Unauthenticated -> {
                        println("DEBUG: SettingsViewModel - user unauthenticated, clearing data")
                        _state.value = _state.value.copy(
                            userEmail = null,
                            userDisplayName = null,
                            currentPlan = "GUEST"
                        )
                    }
                    else -> {
                        // Loading or error states - keep current data
                    }
                }
            }
        }
        
        // Listen for user plan changes
        viewModelScope.launch {
            userPlanManager.currentPlan.collect { plan ->
                println("DEBUG: SettingsViewModel - user plan changed to: $plan")
                loadUserData()
            }
        }
    }
    
    private fun loadUserData() {
        val currentUser = userSessionManager.getCurrentUser()
        val userPlan = userPlanManager.getUserPlan()
        
        println("DEBUG: SettingsViewModel.loadUserData() - currentUser: ${currentUser?.email}")
        println("DEBUG: SettingsViewModel.loadUserData() - userPlan: $userPlan")
        println("DEBUG: SettingsViewModel.loadUserData() - userPlan is null: ${userPlan == null}")
        
        // If userPlan is null but user is authenticated, set to FREE_USER as fallback
        val finalUserPlan = if (userPlan == null && currentUser != null) {
            println("DEBUG: SettingsViewModel.loadUserData() - userPlan is null but user is authenticated, using FREE_USER as fallback")
            "FREE_USER"
        } else {
            userPlan
        }
        
        _state.value = _state.value.copy(
            currentPlan = finalUserPlan,
            userEmail = currentUser?.email,
            userDisplayName = currentUser?.displayName,
            appVersion = getAppVersion()
        )
        
        println("DEBUG: SettingsViewModel.loadUserData() - updated state: userEmail=${_state.value.userEmail}, currentPlan=${_state.value.currentPlan}")
    }
    
    private fun getAppVersion(): String {
        // For now, return hardcoded version. In a real app, this would come from BuildConfig
        return "1.0.0"
    }
    
    fun logout() {
        println("DEBUG: SettingsViewModel.logout() - starting logout process")
        
        viewModelScope.launch {
            try {
                // Use SignOutUseCase which handles all logout logic including clearing local memories
                signOutUseCase()
                
                // Reset user plan (this will clear all user data)
                userPlanManager.resetUserPlan()
                println("DEBUG: SettingsViewModel.logout() - user plan reset completed")
                
                // Update state
                _state.value = _state.value.copy(
                    currentPlan = null,
                    userEmail = null,
                    userDisplayName = null
                )
                
                println("DEBUG: SettingsViewModel.logout() - logout process completed")
            } catch (e: Exception) {
                println("ERROR: SettingsViewModel.logout() - logout failed: ${e.message}")
            }
        }
    }

    /**
     * Clear all user data (full GDPR compliance)
     */
    fun clearAllUserData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                clearUserDataUseCase()
                println("DEBUG: SettingsViewModel.clearAllUserData() - all user data cleared")
                
                // Reload user data after clearing
                loadUserData()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                println("ERROR: SettingsViewModel.clearAllUserData() - failed: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    

    /**
     * Clear only user-specific data (memories, preferences)
     * Keeps app settings and analytics
     */
    fun clearUserDataOnly() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                clearUserDataUseCase.clearUserDataOnly()
                println("DEBUG: SettingsViewModel.clearUserDataOnly() - user data cleared")
                
                // Reload user data after clearing
                loadUserData()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                println("ERROR: SettingsViewModel.clearUserDataOnly() - failed: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Clear only sensitive data (memories, session)
     * Keeps preferences and analytics
     */
    fun clearSensitiveDataOnly() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                clearUserDataUseCase.clearSensitiveDataOnly()
                println("DEBUG: SettingsViewModel.clearSensitiveDataOnly() - sensitive data cleared")
                
                // Reload user data after clearing
                loadUserData()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                println("ERROR: SettingsViewModel.clearSensitiveDataOnly() - failed: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Get storage statistics
     */
    suspend fun getStorageStats() = clearUserDataUseCase.getStorageStats()

    /**
     * Check if cleanup is needed
     */
    suspend fun isCleanupNeeded() = clearUserDataUseCase.isCleanupNeeded()
}
