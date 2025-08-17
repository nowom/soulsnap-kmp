package pl.soulsnaps.features.auth.guard

import pl.soulsnaps.features.auth.manager.UserScopeManager
import pl.soulsnaps.features.auth.model.*

/**
 * Scope Guard - Easy-to-use protection for features
 * Provides convenient methods to check permissions and show upgrade prompts
 */
class ScopeGuard(
    private val scopeManager: UserScopeManager
) {
    
    /**
     * Guard a feature with scope check
     * Returns true if user can access, false if restricted
     */
    suspend fun guardFeature(
        userId: String,
        feature: FeatureCategory,
        onRestricted: (String) -> Unit = {}
    ): Boolean {
        val canAccess = scopeManager.canAccessFeature(userId, feature)
        
        if (!canAccess) {
            val message = scopeManager.getRestrictionMessage(userId, feature) 
                ?: "Ta funkcja nie jest dostępna w Twoim planie."
            onRestricted(message)
        }
        
        return canAccess
    }
    
    /**
     * Guard a feature with specific permission level
     */
    suspend fun guardFeatureWithLevel(
        userId: String,
        feature: FeatureCategory,
        requiredLevel: PermissionLevel,
        onRestricted: (String) -> Unit = {}
    ): Boolean {
        val hasLevel = scopeManager.hasPermissionLevel(userId, feature, requiredLevel)
        
        if (!hasLevel) {
            val message = scopeManager.getRestrictionMessage(userId, feature)
                ?: "Ta funkcja wymaga wyższego poziomu uprawnień."
            onRestricted(message)
        }
        
        return hasLevel
    }
    
    /**
     * Guard an action with scope check
     */
    suspend fun guardAction(
        userId: String,
        action: UserAction,
        onRestricted: (String) -> Unit = {}
    ): Boolean {
        val canPerform = scopeManager.canPerformAction(userId, action)
        
        if (!canPerform) {
            val message = when (action) {
                is UserAction.CreateMemory -> "Osiągnięto limit wspomnień. Uaktualnij plan, aby dodać więcej."
                is UserAction.AnalyzeMemory -> "Dzienny limit analiz został przekroczony. Uaktualnij plan."
                is UserAction.ExportData -> "Miesięczny limit eksportów został przekroczony. Uaktualnij plan."
                is UserAction.ShareMemory -> "Udostępnianie nie jest dostępne w Twoim planie. Uaktualnij plan."
                is UserAction.Collaborate -> "Osiągnięto limit współpracowników. Uaktualnij plan."
            }
            onRestricted(message)
        }
        
        return canPerform
    }
    
    /**
     * Check if user needs upgrade for feature
     */
    suspend fun checkUpgradeNeeded(
        userId: String,
        feature: FeatureCategory,
        onUpgradeNeeded: (UpgradeInfo) -> Unit = {}
    ): Boolean {
        val needsUpgrade = scopeManager.needsUpgrade(userId, feature)
        
        if (needsUpgrade) {
            val recommendations = scopeManager.getUpgradeRecommendations(userId)
            val relevantRecommendation = recommendations.find { it.feature == feature }
            
            relevantRecommendation?.let { recommendation ->
                val upgradeInfo = UpgradeInfo(
                    feature = feature,
                    currentPlan = recommendation.currentPlan,
                    recommendedPlan = recommendation.recommendedPlan,
                    reason = recommendation.reason,
                    benefits = recommendation.benefits
                )
                onUpgradeNeeded(upgradeInfo)
            }
        }
        
        return needsUpgrade
    }
    
    /**
     * Get user's current plan info
     */
    suspend fun getCurrentPlanInfo(userId: String): PlanInfo? {
        val userScope = scopeManager.getUserScope(userId) ?: return null
        
        return PlanInfo(
            planName = userScope.subscriptionPlan.name,
            displayName = userScope.subscriptionPlan.name,
            description = "Plan for user ${userScope.userId}",
            monthlyPrice = null,
            yearlyPrice = null,
            features = emptyList(),
            limits = mapOf(
                "memories" to userScope.limits.maxMemories,
                "storage" to userScope.limits.maxStorageGB,
                "analysis" to userScope.limits.maxAnalysisPerDay,
                "exports" to userScope.limits.maxExportsPerMonth
            )
        )
    }
    
    /**
     * Get feature access summary for user
     */
    suspend fun getFeatureAccessSummary(userId: String): FeatureAccessSummary {
        val userScope = scopeManager.getUserScope(userId) ?: return FeatureAccessSummary(
            accessibleFeatures = emptyList(),
            restrictedFeatures = emptyList(),
            upgradeRecommendations = emptyList()
        )
        
        val accessibleFeatures = mutableListOf<FeatureCategory>()
        val restrictedFeatures = mutableListOf<FeatureCategory>()
        val upgradeFeatures = mutableListOf<FeatureCategory>()
        
        FeatureCategory.values().forEach { feature ->
            when {
                scopeManager.canAccessFeature(userId, feature) -> {
                    accessibleFeatures.add(feature)
                }
                scopeManager.needsUpgrade(userId, feature) -> {
                    upgradeFeatures.add(feature)
                }
                else -> {
                    restrictedFeatures.add(feature)
                }
            }
        }
        
        return FeatureAccessSummary(
            accessibleFeatures = accessibleFeatures,
            restrictedFeatures = restrictedFeatures,
            upgradeRecommendations = scopeManager.getUpgradeRecommendations(userId)
        )
    }
    
    /**
     * Check if user can access multiple features
     */
    suspend fun canAccessMultipleFeatures(
        userId: String,
        features: List<FeatureCategory>
    ): Map<FeatureCategory, Boolean> {
        return features.associateWith { feature ->
            scopeManager.canAccessFeature(userId, feature)
        }
    }
    
    /**
     * Get upgrade recommendations for multiple features
     */
    suspend fun getUpgradeRecommendationsForFeatures(
        userId: String,
        features: List<FeatureCategory>
    ): List<UpgradeRecommendation> {
        val allRecommendations = scopeManager.getUpgradeRecommendations(userId)
        return allRecommendations.filter { it.feature in features }
    }
}

// Data classes for scope guard responses
data class UpgradeInfo(
    val feature: FeatureCategory,
    val currentPlan: SubscriptionPlan,
    val recommendedPlan: SubscriptionPlan,
    val reason: String,
    val benefits: List<String>
)

// Using PlanInfo from model package

// Using FeatureAccessSummary from model package
