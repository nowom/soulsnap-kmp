package pl.soulsnaps.features.affirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.domain.model.ThemeType
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.features.analytics.CapacityAnalytics

class AffirmationsViewModel(
    private val affirmationRepository: AffirmationRepository,
    private val accessGuard: pl.soulsnaps.access.guard.AccessGuard
): ViewModel() {

    private val _uiState = MutableStateFlow(AffirmationsUiState())
    val uiState: StateFlow<AffirmationsUiState> = _uiState
    
    // CapacityAnalytics for tracking usage
    private val capacityAnalytics = CapacityAnalytics(accessGuard)

    init {
        onEvent(AffirmationsEvent.LoadInitial)
    }

    fun onEvent(event: AffirmationsEvent) {
        when (event) {
            is AffirmationsEvent.LoadInitial -> loadAffirmations()
            is AffirmationsEvent.Play -> playAffirmation(event.affirmation)
            is AffirmationsEvent.ToggleFavorite -> toggleFavorite(event.affirmation.id)
            is AffirmationsEvent.SelectFilter -> {
                _uiState.value = _uiState.value.copy(selectedFilter = event.filter)
                loadAffirmations()
            }
            is AffirmationsEvent.ToggleFavoritesOnly -> {
                _uiState.value = _uiState.value.copy(
                    showOnlyFavorites = !_uiState.value.showOnlyFavorites
                )
                loadAffirmations()
            }
            
            // New capacity management events
            is AffirmationsEvent.CheckCapacity -> checkCapacity()
            is AffirmationsEvent.ShowPaywall -> showPaywall()
            is AffirmationsEvent.NavigateToPaywall -> {
                _uiState.value = _uiState.value.copy(
                    showPaywall = true,
                    paywallReason = event.reason,
                    recommendedPlan = event.recommendedPlan
                )
            }
            
            // New analytics events
            is AffirmationsEvent.ShowAnalytics -> showAnalytics()
            is AffirmationsEvent.UpdateAnalytics -> updateAnalytics()
        }
    }

    private fun loadAffirmations() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val emotionFilter = if (_uiState.value.selectedFilter == "Emotion") null else null
                val all = affirmationRepository.getAffirmations(emotionFilter)
                
                val filtered = if (_uiState.value.showOnlyFavorites) {
                    all.filter { it.isFavorite }
                } else {
                    all
                }
                
                _uiState.value = _uiState.value.copy(
                    affirmations = filtered,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load affirmations: ${e.message}"
                )
            }
        }
    }

    private fun toggleFavorite(id: String) {
        viewModelScope.launch {
            try {
                affirmationRepository.updateIsFavorite(id)
                // Reload affirmations to get updated state
                loadAffirmations()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update favorite: ${e.message}"
                )
            }
        }
    }

    private fun playAffirmation(affirmation: Affirmation) {
        viewModelScope.launch {
            // Check AI analysis limits before playing affirmation
            val aiResult = accessGuard.canPerformAction("current_user", "analysis.run.single", "feature.analysis")
            
            if (!aiResult.allowed) {
                // AI limit exceeded - show paywall
                val recommendedPlan = accessGuard.getUpgradeRecommendation("analysis.run.single")
                _uiState.value = _uiState.value.copy(
                    showPaywall = true,
                    paywallReason = aiResult.message ?: "Limit analiz AI przekroczony",
                    recommendedPlan = recommendedPlan
                )
                return@launch
            }
            
            // AI limit OK - play affirmation and update analytics
            affirmationRepository.playAffirmation(affirmation.text)
            
            // Update analytics after successful operation
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
        }
    }
    
    /**
     * Check current capacity status
     */
    private fun checkCapacity() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingCapacity = true)
            
            try {
                val capacityInfo = accessGuard.getQuotaInfo("current_user", "analysis.day")
                _uiState.value = _uiState.value.copy(
                    capacityInfo = capacityInfo,
                    isCheckingCapacity = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingCapacity = false,
                    error = "Nie udało się sprawdzić pojemności: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Show paywall
     */
    private fun showPaywall() {
        _uiState.value = _uiState.value.copy(showPaywall = true)
    }
    
    /**
     * Hide paywall
     */
    fun hidePaywall() {
        _uiState.value = _uiState.value.copy(showPaywall = false)
    }
    
    /**
     * Show analytics
     */
    private fun showAnalytics() {
        viewModelScope.launch {
            // Update analytics data first
            capacityAnalytics.updateUsageStats("current_user") // TODO: get real user ID
            _uiState.value = _uiState.value.copy(showAnalytics = true)
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
        _uiState.value = _uiState.value.copy(showAnalytics = false)
    }
    
    /**
     * Get analytics instance
     */
    fun getAnalytics(): CapacityAnalytics {
        return capacityAnalytics
    }


}

// UI state

data class AffirmationsUiState(
    val affirmations: List<Affirmation> = emptyList(),
    val selectedFilter: String = "Emotion",
    val showOnlyFavorites: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    
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

sealed class AffirmationsEvent {
    data object LoadInitial : AffirmationsEvent()
    data class Play(val affirmation: Affirmation) : AffirmationsEvent()
    data class ToggleFavorite(val affirmation: Affirmation) : AffirmationsEvent()
    data class SelectFilter(val filter: String) : AffirmationsEvent()
    data object ToggleFavoritesOnly : AffirmationsEvent()
    
    // New events for capacity management
    data object CheckCapacity : AffirmationsEvent()
    data object ShowPaywall : AffirmationsEvent()
    data class NavigateToPaywall(val reason: String, val recommendedPlan: String?) : AffirmationsEvent()
    
    // New events for analytics
    data object ShowAnalytics : AffirmationsEvent()
    data object UpdateAnalytics : AffirmationsEvent()
}
