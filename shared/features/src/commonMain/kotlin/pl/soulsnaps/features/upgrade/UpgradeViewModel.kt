package pl.soulsnaps.features.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.PlanDefinition
import pl.soulsnaps.access.manager.PlanPricing
import pl.soulsnaps.utils.getCurrentTimeMillis

data class UpgradeUiState(
    val currentPlan: String = "FREE_USER",
    val recommendations: List<UpgradeRecommendation> = emptyList(),
    val availablePlans: List<PlanDefinition> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isUpgrading: Boolean = false
)

sealed interface UpgradeIntent {
    data object LoadData : UpgradeIntent
    data class UpgradeToPlan(val planName: String) : UpgradeIntent
    data object ClearError : UpgradeIntent
}

class UpgradeViewModel(
    private val userPlanManager: UserPlanManager,
    private val planRegistry: PlanRegistryReader,
    private val upgradeRecommendationEngine: UpgradeRecommendationEngine
) : ViewModel() {

    private val _state = MutableStateFlow(UpgradeUiState())
    val state: StateFlow<UpgradeUiState> = _state.asStateFlow()

    init {
        handleIntent(UpgradeIntent.LoadData)
    }

    fun handleIntent(intent: UpgradeIntent) {
        when (intent) {
            is UpgradeIntent.LoadData -> loadData()
            is UpgradeIntent.UpgradeToPlan -> upgradeToPlan(intent.planName)
            is UpgradeIntent.ClearError -> clearError()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Get current plan
                val currentPlan = userPlanManager.getUserPlan() ?: "FREE_USER"
                
                // Load available plans from registry
                val availablePlans = planRegistry.getAllPlans()
                
                // Generate recommendations based on current usage
                val recommendations = generateRecommendations(currentPlan)
                
                _state.update { 
                    it.copy(
                        currentPlan = currentPlan,
                        availablePlans = availablePlans,
                        recommendations = recommendations,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load upgrade data: ${e.message}"
                    ) 
                }
            }
        }
    }

    private suspend fun generateRecommendations(currentPlan: String): List<UpgradeRecommendation> {
        return try {
            // Create mock usage statistics for now
            // TODO: Replace with actual usage data from analytics
            val usageStats = UsageStatistics(
                soulSnapsCount = 5,
                aiAnalysisCount = 2,
                affirmationsCount = 10,
                exercisesCount = 3,
                storageUsedGB = 0.5f,
                lastActivityDate = getCurrentTimeMillis()
            )
            
            // Create mock user behavior
            val userBehavior = UserBehavior(
                dailyActiveDays = 7,
                avgSessionDuration = 15,
                affirmationsUsage = 0.7f,
                analyticsInterest = 0.5f,
                featureUsagePattern = mapOf(
                    "affirmations" to 0.8f,
                    "exercises" to 0.4f,
                    "analytics" to 0.3f
                )
            )
            
            upgradeRecommendationEngine.analyzeUserAndGenerateRecommendations(
                currentPlan = currentPlan,
                usageStats = usageStats,
                userBehavior = userBehavior
            )
        } catch (e: Exception) {
            // Return empty list if recommendations fail
            emptyList()
        }
    }

    private fun upgradeToPlan(planName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpgrading = true, errorMessage = null) }
            
            try {
                // Update user plan
                userPlanManager.setUserPlanAndWait(planName)
                
                // Update state
                _state.update { 
                    it.copy(
                        currentPlan = planName,
                        isUpgrading = false
                    ) 
                }
                
                // Reload recommendations for new plan
                val newRecommendations = generateRecommendations(planName)
                _state.update { it.copy(recommendations = newRecommendations) }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isUpgrading = false,
                        errorMessage = "Failed to upgrade plan: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }


    fun isCurrentPlan(planName: String): Boolean {
        return _state.value.currentPlan == planName
    }

    fun getRecommendationsForPlan(planName: String): List<UpgradeRecommendation> {
        return _state.value.recommendations.filter { 
            it.recommendedPlan == planName 
        }
    }
}
