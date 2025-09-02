package pl.soulsnaps.features.memoryhub.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.GetMemoryByIdUseCase
import pl.soulsnaps.domain.interactor.ToggleMemoryFavoriteUseCase
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.features.analytics.CapacityAnalytics

data class MemoryDetailsState(
    val memory: Memory? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // New fields for capacity management
    val capacityInfo: pl.soulsnaps.access.guard.QuotaInfo? = null,
    val showPaywall: Boolean = false,
    val paywallReason: String? = null,
    val recommendedPlan: String? = null,
    val isCheckingCapacity: Boolean = false,
    
    // New fields for analytics
    val showAnalytics: Boolean = false,
    val analyticsData: pl.soulsnaps.features.analytics.CapacityUsageStats? = null,
    val analyticsAlerts: List<pl.soulsnaps.features.analytics.CapacityAlert> = emptyList()
)

sealed interface MemoryDetailsIntent {
    data object LoadMemory : MemoryDetailsIntent
    data object ToggleFavorite : MemoryDetailsIntent
    data object DeleteMemory : MemoryDetailsIntent
    data object ShareMemory : MemoryDetailsIntent
    data object EditMemory : MemoryDetailsIntent
    
    // New intents for capacity management
    data object CheckCapacity : MemoryDetailsIntent
    data object ShowPaywall : MemoryDetailsIntent
    data class NavigateToPaywall(val reason: String, val recommendedPlan: String?) : MemoryDetailsIntent
    
    // New intents for analytics
    data object ShowAnalytics : MemoryDetailsIntent
    data object UpdateAnalytics : MemoryDetailsIntent
}

class MemoryDetailsViewModel(
    private val getMemoryByIdUseCase: GetMemoryByIdUseCase,
    private val toggleMemoryFavoriteUseCase: ToggleMemoryFavoriteUseCase,
    private val accessGuard: pl.soulsnaps.access.guard.AccessGuard
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryDetailsState())
    val state: StateFlow<MemoryDetailsState> = _state.asStateFlow()

    private var currentMemoryId: Int = 0
    
    // CapacityAnalytics for tracking usage
    private val capacityAnalytics = CapacityAnalytics(accessGuard)

    fun loadMemoryDetails(memoryId: Int) {
        currentMemoryId = memoryId
        handleIntent(MemoryDetailsIntent.LoadMemory)
    }

    fun handleIntent(intent: MemoryDetailsIntent) {
        when (intent) {
            is MemoryDetailsIntent.LoadMemory -> loadMemory()
            is MemoryDetailsIntent.ToggleFavorite -> toggleFavorite()
            is MemoryDetailsIntent.DeleteMemory -> deleteMemory()
            is MemoryDetailsIntent.ShareMemory -> shareMemory()
            is MemoryDetailsIntent.EditMemory -> editMemory()
            
            // New capacity management intents
            is MemoryDetailsIntent.CheckCapacity -> checkCapacity()
            is MemoryDetailsIntent.ShowPaywall -> showPaywall()
            is MemoryDetailsIntent.NavigateToPaywall -> {
                _state.update { 
                    it.copy(
                        showPaywall = true,
                        paywallReason = intent.reason,
                        recommendedPlan = intent.recommendedPlan
                    ) 
                }
            }
            
            // New analytics intents
            is MemoryDetailsIntent.ShowAnalytics -> showAnalytics()
            is MemoryDetailsIntent.UpdateAnalytics -> updateAnalytics()
        }
    }

    private fun loadMemory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val memory = getMemoryByIdUseCase(currentMemoryId)
                _state.update { it.copy(memory = memory, isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to load memory: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val currentMemory = _state.value.memory
                if (currentMemory != null) {
                    toggleMemoryFavoriteUseCase(currentMemory.id, !currentMemory.isFavorite)
                    // Update local state
                    _state.update { 
                        it.copy(
                            memory = currentMemory.copy(isFavorite = !currentMemory.isFavorite)
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to toggle favorite: ${e.message}")
                }
            }
        }
    }

    private fun deleteMemory() {
        // TODO: Implement delete functionality
        viewModelScope.launch {
            try {
                // Call delete use case
                _state.update { it.copy(errorMessage = "Delete functionality not implemented yet") }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to delete memory: ${e.message}")
                }
            }
        }
    }

    private fun shareMemory() {
        viewModelScope.launch {
            // Check export limits before sharing memory
            val exportResult = accessGuard.canPerformAction("current_user", "insights.export", "feature.export")
            
            if (!exportResult.allowed) {
                // Export limit exceeded - show paywall
                val recommendedPlan = accessGuard.getUpgradeRecommendation("insights.export")
                _state.update { 
                    it.copy(
                        showPaywall = true,
                        paywallReason = exportResult.message ?: "Limit eksportu przekroczony",
                        recommendedPlan = recommendedPlan
                    ) 
                }
                return@launch
            }
            
            // Export limit OK - proceed with share and update analytics
            try {
                // TODO: Implement actual share functionality
                _state.update { it.copy(errorMessage = "Share functionality not implemented yet") }
                
                // Update analytics after successful operation
                capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to share memory: ${e.message}")
                }
            }
        }
    }

    private fun editMemory() {
        // TODO: Implement edit functionality
        viewModelScope.launch {
            try {
                // Call edit use case
                _state.update { it.copy(errorMessage = "Edit functionality not implemented yet") }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = "Failed to edit memory: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Check current capacity status
     */
    private fun checkCapacity() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingCapacity = true) }
            
            try {
                val capacityInfo = accessGuard.getQuotaInfo("current_user", "memories.month")
                _state.update { 
                    it.copy(
                        capacityInfo = capacityInfo,
                        isCheckingCapacity = false
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isCheckingCapacity = false,
                        errorMessage = "Nie udało się sprawdzić pojemności: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Show paywall
     */
    private fun showPaywall() {
        _state.update { it.copy(showPaywall = true) }
    }
    
    /**
     * Hide paywall
     */
    fun hidePaywall() {
        _state.update { it.copy(showPaywall = false) }
    }
    
    /**
     * Show analytics
     */
    private fun showAnalytics() {
        viewModelScope.launch {
            // Update analytics data first
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
            _state.update { it.copy(showAnalytics = true) }
        }
    }
    
    /**
     * Update analytics
     */
    private fun updateAnalytics() {
        viewModelScope.launch {
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
        }
    }
    
    /**
     * Hide analytics
     */
    fun hideAnalytics() {
        _state.update { it.copy(showAnalytics = false) }
    }
    
    /**
     * Get analytics instance
     */
    fun getAnalytics(): CapacityAnalytics {
        return capacityAnalytics
    }
}
