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
    val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        val currentUser = userSessionManager.getCurrentUser()
        _state.value = _state.value.copy(
            currentPlan = userPlanManager.getUserPlan(),
            userEmail = currentUser?.email,
            userDisplayName = currentUser?.displayName,
            appVersion = getAppVersion()
        )
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
}
