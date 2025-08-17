package pl.soulsnaps.features.auth.manager

import kotlinx.datetime.Clock
import pl.soulsnaps.features.auth.model.*
import pl.soulsnaps.features.auth.repository.UserScopeRepository

/**
 * User Scope Manager
 * Handles user scope validation, upgrades, and dynamic permission management
 */
class UserScopeManager(
    private val scopeRepository: UserScopeRepository
) {
    
    /**
     * Get current user scope
     */
    suspend fun getUserScope(userId: String): UserScope? {
        return scopeRepository.getUserScope(userId)
    }
    
    /**
     * Validate if user can access feature
     */
    suspend fun canAccessFeature(userId: String, feature: FeatureCategory): Boolean {
        val userScope = getUserScope(userId) ?: return false
        
        // Check if scope is active and not expired
        if (!isScopeValid(userScope)) return false
        
        return FeatureAccessControl.hasAccess(userScope, feature)
    }
    
    /**
     * Check if user has specific permission level
     */
    suspend fun hasPermissionLevel(userId: String, feature: FeatureCategory, requiredLevel: PermissionLevel): Boolean {
        val userScope = getUserScope(userId) ?: return false
        
        if (!isScopeValid(userScope)) return false
        
        return FeatureAccessControl.hasPermissionLevel(userScope, feature, requiredLevel)
    }
    
    /**
     * Check if user can perform specific action
     */
    suspend fun canPerformAction(userId: String, action: UserAction): Boolean {
        val userScope = getUserScope(userId) ?: return false
        
        if (!isScopeValid(userScope)) return false
        
        return FeatureAccessControl.canPerformAction(userScope, action)
    }
    
    /**
     * Get restriction message for feature
     */
    suspend fun getRestrictionMessage(userId: String, feature: FeatureCategory): String? {
        val userScope = getUserScope(userId) ?: return null
        
        if (!isScopeValid(userScope)) return "Twój plan wygasł. Odnów subskrypcję, aby kontynuować."
        
        return FeatureAccessControl.getRestrictionMessage(userScope, feature)
    }
    
    /**
     * Check if user needs upgrade for feature
     */
    suspend fun needsUpgrade(userId: String, feature: FeatureCategory): Boolean {
        val userScope = getUserScope(userId) ?: return true
        
        if (!isScopeValid(userScope)) return true
        
        return FeatureAccessControl.needsUpgrade(userScope, feature)
    }
    
    /**
     * Get upgrade recommendations for user
     */
    suspend fun getUpgradeRecommendations(userId: String): List<UpgradeRecommendation> {
        val userScope = getUserScope(userId) ?: return getDefaultUpgradeRecommendations()
        
        val recommendations = mutableListOf<UpgradeRecommendation>()
        
        // Check which features are restricted
        FeatureCategory.values().forEach { feature ->
            if (needsUpgrade(userId, feature)) {
                val currentPlan = userScope.subscriptionPlan
                val recommendedPlan = getRecommendedPlanForFeature(feature)
                
                if (recommendedPlan.ordinal > currentPlan.ordinal) {
                                    recommendations.add(
                    UpgradeRecommendation(
                        feature = feature,
                        currentPlan = currentPlan,
                        recommendedPlan = recommendedPlan,
                        reason = getUpgradeReason(feature, currentPlan),
                        benefits = getPlanBenefits(recommendedPlan),
                        estimatedCost = getPlanEstimatedCost(recommendedPlan)
                    )
                )
                }
            }
        }
        
        return recommendations.sortedBy { it.recommendedPlan.ordinal }
    }
    
    /**
     * Upgrade user scope
     */
    suspend fun upgradeUserScope(userId: String, newPlan: SubscriptionPlan): Boolean {
        val currentScope = getUserScope(userId)
        val newScope = createScopeForPlan(userId, newPlan)
        
        return scopeRepository.updateUserScope(newScope)
    }
    
    /**
     * Downgrade user scope
     */
    suspend fun downgradeUserScope(userId: String, newPlan: SubscriptionPlan): Boolean {
        val newScope = createScopeForPlan(userId, newPlan)
        
        return scopeRepository.updateUserScope(newScope)
    }
    
    /**
     * Check scope validity
     */
    private fun isScopeValid(userScope: UserScope): Boolean {
        if (!userScope.isActive) return false
        
        // Check if scope has expired
        userScope.validUntil?.let { expiryTime ->
            if (Clock.System.now().toEpochMilliseconds() > expiryTime) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Create scope for specific plan
     */
    private fun createScopeForPlan(userId: String, plan: SubscriptionPlan): UserScope {
        return when (plan) {
            SubscriptionPlan.FREE -> DefaultScopes.getFreeUserScope(userId)
            SubscriptionPlan.BASIC -> createBasicUserScope(userId)
            SubscriptionPlan.PREMIUM -> DefaultScopes.getPremiumUserScope(userId)
            SubscriptionPlan.FAMILY -> DefaultScopes.getFamilyUserScope(userId)
            SubscriptionPlan.ENTERPRISE -> createEnterpriseUserScope(userId)
            SubscriptionPlan.LIFETIME -> createLifetimeUserScope(userId)
        }
    }
    
    /**
     * Get recommended plan for feature
     */
    private fun getRecommendedPlanForFeature(feature: FeatureCategory): SubscriptionPlan {
        return when (feature) {
            FeatureCategory.MEMORY_CAPTURE -> SubscriptionPlan.FREE
            FeatureCategory.MEMORY_ANALYSIS -> SubscriptionPlan.BASIC
            FeatureCategory.PATTERN_DETECTION -> SubscriptionPlan.PREMIUM
            FeatureCategory.INSIGHTS -> SubscriptionPlan.PREMIUM
            FeatureCategory.SHARING -> SubscriptionPlan.PREMIUM
            FeatureCategory.COLLABORATION -> SubscriptionPlan.FAMILY
            FeatureCategory.EXPORT -> SubscriptionPlan.PREMIUM
            FeatureCategory.BACKUP -> SubscriptionPlan.PREMIUM
            FeatureCategory.CUSTOMIZATION -> SubscriptionPlan.PREMIUM
            FeatureCategory.ADVANCED_AI -> SubscriptionPlan.ENTERPRISE
            FeatureCategory.API_ACCESS -> SubscriptionPlan.ENTERPRISE
            FeatureCategory.SUPPORT -> SubscriptionPlan.PREMIUM
        }
    }
    
    /**
     * Get upgrade reason
     */
    private fun getUpgradeReason(feature: FeatureCategory, currentPlan: SubscriptionPlan): String {
        return when (feature) {
            FeatureCategory.PATTERN_DETECTION -> "Wykrywanie wzorców dostępne od planu Premium"
            FeatureCategory.ADVANCED_AI -> "Zaawansowane funkcje AI dostępne w planie Enterprise"
            FeatureCategory.COLLABORATION -> "Współpraca rodzinna dostępna od planu Family"
            FeatureCategory.API_ACCESS -> "Dostęp do API dostępny w planie Enterprise"
            else -> "Ta funkcja wymaga uaktualnienia planu"
        }
    }
    
    /**
     * Get plan benefits
     */
    private fun getPlanBenefits(plan: SubscriptionPlan): List<String> {
        return when (plan) {
            SubscriptionPlan.FREE -> listOf("Podstawowe przechwytywanie wspomnień", "Ograniczona analiza")
            SubscriptionPlan.BASIC -> listOf("Rozszerzona analiza wspomnień", "Więcej miejsca na dane")
            SubscriptionPlan.PREMIUM -> listOf("Wykrywanie wzorców", "Szczegółowe insights", "Eksport danych")
            SubscriptionPlan.FAMILY -> listOf("Współpraca rodzinna", "Udostępnianie wspomnień", "Więcej miejsca")
            SubscriptionPlan.ENTERPRISE -> listOf("Zaawansowane AI", "Dostęp do API", "Priorytetowe wsparcie")
            SubscriptionPlan.LIFETIME -> listOf("Dożywotni dostęp", "Wszystkie funkcje", "Bez miesięcznych opłat")
        }
    }
    
    /**
     * Get plan estimated cost
     */
    private fun getPlanEstimatedCost(plan: SubscriptionPlan): String {
        return when (plan) {
            SubscriptionPlan.FREE -> "Darmowy"
            SubscriptionPlan.BASIC -> "9.99 PLN/miesiąc"
            SubscriptionPlan.PREMIUM -> "19.99 PLN/miesiąc"
            SubscriptionPlan.FAMILY -> "29.99 PLN/miesiąc"
            SubscriptionPlan.ENTERPRISE -> "99.99 PLN/miesiąc"
            SubscriptionPlan.LIFETIME -> "299.99 PLN jednorazowo"
        }
    }
    
    /**
     * Get default upgrade recommendations
     */
    private fun getDefaultUpgradeRecommendations(): List<UpgradeRecommendation> {
        return listOf(
            UpgradeRecommendation(
                feature = FeatureCategory.MEMORY_ANALYSIS,
                currentPlan = SubscriptionPlan.FREE,
                recommendedPlan = SubscriptionPlan.BASIC,
                reason = "Rozpocznij z podstawowym planem, aby uzyskać dostęp do analizy wspomnień",
                benefits = getPlanBenefits(SubscriptionPlan.BASIC),
                estimatedCost = getPlanEstimatedCost(SubscriptionPlan.BASIC)
            )
        )
    }
    
    // Helper functions for creating scopes
    private fun createBasicUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.PREMIUM_USER,
            subscriptionPlan = SubscriptionPlan.BASIC,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.FULL,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.STANDARD,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.NONE,
                FeatureCategory.INSIGHTS to PermissionLevel.STANDARD,
                FeatureCategory.SHARING to PermissionLevel.NONE,
                FeatureCategory.COLLABORATION to PermissionLevel.NONE,
                FeatureCategory.EXPORT to PermissionLevel.NONE,
                FeatureCategory.BACKUP to PermissionLevel.BASIC,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.STANDARD,
                FeatureCategory.ADVANCED_AI to PermissionLevel.NONE,
                FeatureCategory.API_ACCESS to PermissionLevel.NONE,
                FeatureCategory.SUPPORT to PermissionLevel.BASIC
            ),
            limits = UserLimits(
                maxMemories = 500,
                maxStorageGB = 5,
                maxAnalysisPerDay = 50,
                maxCollaborators = 0,
                maxExportsPerMonth = 5,
                maxBackups = 2,
                retentionDays = 180
            ),
            restrictions = listOf(
                FeatureRestriction(
                    feature = FeatureCategory.PATTERN_DETECTION,
                    restrictionType = RestrictionType.UPGRADE_REQUIRED,
                    value = null,
                    message = "Wykrywanie wzorców dostępne od planu Premium"
                )
            ),
            validUntil = null,
            isActive = true
        )
    }
    
    private fun createEnterpriseUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.ENTERPRISE_USER,
            subscriptionPlan = SubscriptionPlan.ENTERPRISE,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.FULL,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.FULL,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.FULL,
                FeatureCategory.INSIGHTS to PermissionLevel.FULL,
                FeatureCategory.SHARING to PermissionLevel.FULL,
                FeatureCategory.COLLABORATION to PermissionLevel.FULL,
                FeatureCategory.EXPORT to PermissionLevel.FULL,
                FeatureCategory.BACKUP to PermissionLevel.FULL,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.FULL,
                FeatureCategory.ADVANCED_AI to PermissionLevel.FULL,
                FeatureCategory.API_ACCESS to PermissionLevel.FULL,
                FeatureCategory.SUPPORT to PermissionLevel.FULL
            ),
            limits = UserLimits(
                maxMemories = 100000,
                maxStorageGB = 1000,
                maxAnalysisPerDay = 10000,
                maxCollaborators = 100,
                maxExportsPerMonth = 1000,
                maxBackups = 100,
                retentionDays = 3650
            ),
            restrictions = emptyList(),
            validUntil = null,
            isActive = true
        )
    }
    
    private fun createLifetimeUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.PREMIUM_USER,
            subscriptionPlan = SubscriptionPlan.LIFETIME,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.FULL,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.FULL,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.FULL,
                FeatureCategory.INSIGHTS to PermissionLevel.FULL,
                FeatureCategory.SHARING to PermissionLevel.FULL,
                FeatureCategory.COLLABORATION to PermissionLevel.FULL,
                FeatureCategory.EXPORT to PermissionLevel.FULL,
                FeatureCategory.BACKUP to PermissionLevel.FULL,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.FULL,
                FeatureCategory.ADVANCED_AI to PermissionLevel.FULL,
                FeatureCategory.API_ACCESS to PermissionLevel.FULL,
                FeatureCategory.SUPPORT to PermissionLevel.FULL
            ),
            limits = UserLimits(
                maxMemories = 100000,
                maxStorageGB = 1000,
                maxAnalysisPerDay = 10000,
                maxCollaborators = 100,
                maxExportsPerMonth = 1000,
                maxBackups = 100,
                retentionDays = 36500 // 100 years
            ),
            restrictions = emptyList(),
            validUntil = null,
            isActive = true
        )
    }
}

// Using UpgradeRecommendation from model package
