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
import pl.soulsnaps.features.auth.mvp.guard.UserPlanManager
import pl.soulsnaps.data.MemoryRepositoryImpl
import pl.soulsnaps.domain.MemoryRepository
import kotlin.getValue

data class SettingsState(
    val currentPlan: String? = null,
    val isLoading: Boolean = false
)

class SettingsViewModel(val userPlanManager: UserPlanManager,
                        val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        _state.value = _state.value.copy(
            currentPlan = userPlanManager.getUserPlan()
        )
    }
    
    fun logout() {
        println("DEBUG: SettingsViewModel.logout() - starting logout process")
        
        // Clean up invalid memories before logout
        viewModelScope.launch {
            try {
                println("DEBUG: SettingsViewModel.logout() - cleaning up invalid memories")
                memoryRepository.cleanupInvalidMemories()
                println("DEBUG: SettingsViewModel.logout() - cleanup completed")
            } catch (e: Exception) {
                println("ERROR: SettingsViewModel.logout() - cleanup failed: ${e.message}")
            }
        }
        
        // Reset user plan (this will clear all user data)
        userPlanManager.resetUserPlan()
        println("DEBUG: SettingsViewModel.logout() - user plan reset completed")
        
        // Update state
        _state.value = _state.value.copy(
            currentPlan = null
        )
        
        println("DEBUG: SettingsViewModel.logout() - logout process completed")
    }
}
