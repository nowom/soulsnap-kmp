package pl.soulsnaps.features.auth.controller

import pl.soulsnaps.features.auth.guard.ScopeGuard
import pl.soulsnaps.features.auth.manager.UserScopeManager
import pl.soulsnaps.features.auth.model.*
import pl.soulsnaps.features.auth.repository.UserScopeRepository

/**
 * AppScopeController - Centralny kontroler dostępu do funkcji w całej aplikacji SoulSnaps
 * Integruje system user scopes ze wszystkimi modułami aplikacji
 */
class AppScopeController(
    private val scopeManager: UserScopeManager,
    private val scopeGuard: ScopeGuard,
    private val scopeRepository: UserScopeRepository
) {
    
    /**
     * Inicjalizacja scope dla nowego użytkownika
     */
    suspend fun initializeUserScope(userId: String, initialPlan: SubscriptionPlan = SubscriptionPlan.FREE): Boolean {
        val existingScope = scopeManager.getUserScope(userId)
        if (existingScope != null) return true
        
        val newScope = when (initialPlan) {
            SubscriptionPlan.FREE -> DefaultScopes.getFreeUserScope(userId)
            SubscriptionPlan.BASIC -> createBasicUserScope(userId)
            SubscriptionPlan.PREMIUM -> DefaultScopes.getPremiumUserScope(userId)
            SubscriptionPlan.FAMILY -> DefaultScopes.getFamilyUserScope(userId)
            SubscriptionPlan.ENTERPRISE -> createEnterpriseUserScope(userId)
            SubscriptionPlan.LIFETIME -> createLifetimeUserScope(userId)
        }
        
        return scopeRepository.createUserScope(newScope)
    }
    
    /**
     * Sprawdzenie dostępu do funkcji w całej aplikacji
     */
    suspend fun canAccessAppFeature(
        userId: String,
        appFeature: AppFeature,
        onRestricted: (FeatureRestrictionInfo) -> Unit = {}
    ): Boolean {
        val userScope = scopeManager.getUserScope(userId) ?: return false
        
        // Sprawdź czy scope jest aktywny
        if (!userScope.isActive) {
            onRestricted(FeatureRestrictionInfo(
                feature = appFeature.featureCategory,
                restrictionType = RestrictionType.FEATURE_DISABLED,
                message = "Twoja subskrypcja wygasła. Odnów plan, aby kontynuować.",
                requiredAction = RequiredAction.RENEW_SUBSCRIPTION
            ))
            return false
        }
        
        // Sprawdź czy użytkownik ma dostęp do funkcji
        val canAccess = scopeGuard.guardFeature(
            userId = userId,
            feature = appFeature.featureCategory
        ) { message ->
            onRestricted(FeatureRestrictionInfo(
                feature = appFeature.featureCategory,
                restrictionType = RestrictionType.UPGRADE_REQUIRED,
                message = message,
                requiredAction = RequiredAction.UPGRADE_PLAN
            ))
        }
        
        if (!canAccess) return false
        
        // Sprawdź dodatkowe ograniczenia specyficzne dla funkcji
        return checkFeatureSpecificRestrictions(userId, appFeature, onRestricted)
    }
    
    /**
     * Sprawdzenie możliwości wykonania akcji w aplikacji
     */
    suspend fun canPerformAppAction(
        userId: String,
        appAction: AppAction,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        val userScope = scopeManager.getUserScope(userId) ?: return false
        
        // Sprawdź podstawowe uprawnienia
        val canPerform = scopeGuard.guardAction(
            userId = userId,
            action = appAction.toUserAction()
        ) { message ->
            onRestricted(ActionRestrictionInfo(
                action = appAction,
                message = message,
                requiredAction = RequiredAction.UPGRADE_PLAN
            ))
        }
        
        if (!canPerform) return false
        
        // Sprawdź dodatkowe ograniczenia specyficzne dla akcji
        return checkActionSpecificRestrictions(userId, appAction, onRestricted)
    }
    
    /**
     * Pobranie pełnego statusu użytkownika w aplikacji
     */
    suspend fun getUserAppStatus(userId: String): UserAppStatus {
        val userScope = scopeManager.getUserScope(userId) ?: return UserAppStatus.NotFound
        
        val featureSummary = scopeGuard.getFeatureAccessSummary(userId)
        val upgradeRecommendations = scopeManager.getUpgradeRecommendations(userId)
        
        return UserAppStatus.Active(
            userScope = userScope,
            featureSummary = featureSummary,
            upgradeRecommendations = upgradeRecommendations,
            usageStats = getUserUsageStats(userId),
            nextBillingDate = getNextBillingDate(userScope)
        )
    }
    
    /**
     * Upgrade planu użytkownika
     */
    suspend fun upgradeUserPlan(
        userId: String,
        newPlan: SubscriptionPlan,
        onSuccess: (UserScope) -> Unit = {},
        onError: (String) -> Unit = {}
    ): Boolean {
        try {
            val success = scopeManager.upgradeUserScope(userId, newPlan)
            if (success) {
                val newScope = scopeManager.getUserScope(userId)
                newScope?.let { onSuccess(it) }
            } else {
                onError("Nie udało się uaktualnić planu")
            }
            return success
        } catch (e: Exception) {
            onError("Błąd podczas uaktualniania planu: ${e.message}")
            return false
        }
    }
    
    /**
     * Sprawdzenie limitów użytkownika
     */
    suspend fun checkUserLimits(userId: String): UserLimitsStatus {
        val userScope = scopeManager.getUserScope(userId) ?: return UserLimitsStatus.NotFound
        
        val currentUsage = getUserCurrentUsage(userId)
        val limits = userScope.limits
        
        return UserLimitsStatus.Active(
            memories = LimitStatus(
                current = currentUsage.memoriesCount,
                limit = limits.maxMemories,
                percentage = (currentUsage.memoriesCount.toFloat() / limits.maxMemories) * 100
            ),
            storage = LimitStatus(
                current = currentUsage.storageUsedGB.toInt(),
                limit = limits.maxStorageGB,
                percentage = (currentUsage.storageUsedGB / limits.maxStorageGB) * 100
            ),
            dailyAnalysis = LimitStatus(
                current = currentUsage.dailyAnalysisCount,
                limit = limits.maxAnalysisPerDay,
                percentage = (currentUsage.dailyAnalysisCount.toFloat() / limits.maxAnalysisPerDay) * 100
            ),
            monthlyExports = LimitStatus(
                current = currentUsage.monthlyExportsCount,
                limit = limits.maxExportsPerMonth,
                percentage = (currentUsage.monthlyExportsCount.toFloat() / limits.maxExportsPerMonth) * 100
            )
        )
    }
    
    /**
     * Pobranie rekomendacji upgrade'u dla konkretnych funkcji
     */
    suspend fun getFeatureUpgradeRecommendations(
        userId: String,
        features: List<FeatureCategory>
    ): List<FeatureUpgradeRecommendation> {
        val recommendations = scopeManager.getUpgradeRecommendations(userId)
        
        return features.mapNotNull { feature ->
            val recommendation = recommendations.find { it.feature == feature }
            recommendation?.let {
                FeatureUpgradeRecommendation(
                    feature = feature,
                    currentPlan = it.currentPlan,
                    recommendedPlan = it.recommendedPlan,
                    reason = it.reason,
                    benefits = it.benefits,
                    estimatedCost = getPlanEstimatedCost(it.recommendedPlan)
                )
            }
        }
    }
    
    // Helper functions
    private suspend fun checkFeatureSpecificRestrictions(
        userId: String,
        appFeature: AppFeature,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean {
        // Implementacja specyficznych ograniczeń dla funkcji
        return when (appFeature) {
            is AppFeature.MemoryCapture -> checkMemoryCaptureRestrictions(userId, onRestricted)
            is AppFeature.MemoryAnalysis -> checkMemoryAnalysisRestrictions(userId, onRestricted)
            is AppFeature.PatternDetection -> checkPatternDetectionRestrictions(userId, onRestricted)
            is AppFeature.Insights -> checkInsightsRestrictions(userId, onRestricted)
            is AppFeature.Sharing -> checkSharingRestrictions(userId, onRestricted)
            is AppFeature.Collaboration -> checkCollaborationRestrictions(userId, onRestricted)
            is AppFeature.Export -> checkExportRestrictions(userId, onRestricted)
            is AppFeature.Backup -> checkBackupRestrictions(userId, onRestricted)
            is AppFeature.Customization -> checkCustomizationRestrictions(userId, onRestricted)
            is AppFeature.AdvancedAI -> checkAdvancedAIRestrictions(userId, onRestricted)
            is AppFeature.APIAccess -> checkAPIAccessRestrictions(userId, onRestricted)
            is AppFeature.Support -> checkSupportRestrictions(userId, onRestricted)
        }
    }
    
    private suspend fun checkActionSpecificRestrictions(
        userId: String,
        appAction: AppAction,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean {
        // Implementacja specyficznych ograniczeń dla akcji
        return when (appAction) {
            is AppAction.CreateMemory -> checkCreateMemoryRestrictions(userId, onRestricted)
            is AppAction.AnalyzeMemory -> checkAnalyzeMemoryRestrictions(userId, onRestricted)
            is AppAction.ShareMemory -> checkShareMemoryRestrictions(userId, onRestricted)
            is AppAction.ExportData -> checkExportDataRestrictions(userId, onRestricted)
            is AppAction.BackupData -> checkBackupDataRestrictions(userId, onRestricted)
            is AppAction.CustomizeApp -> checkCustomizeAppRestrictions(userId, onRestricted)
        }
    }
    
    // Placeholder implementations for restriction checks
    private suspend fun checkMemoryCaptureRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkMemoryAnalysisRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkInsightsRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkPatternDetectionRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkSharingRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkCollaborationRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkExportRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkBackupRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkCustomizationRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkAdvancedAIRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkAPIAccessRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkSupportRestrictions(
        userId: String,
        onRestricted: (FeatureRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkCreateMemoryRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkAnalyzeMemoryRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkShareMemoryRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkExportDataRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkBackupDataRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun checkCustomizeAppRestrictions(
        userId: String,
        onRestricted: (ActionRestrictionInfo) -> Unit
    ): Boolean = true
    
    private suspend fun getUserUsageStats(userId: String): UserUsageStats {
        // TODO: Implement actual usage statistics
        return UserUsageStats(
            memoriesCount = 0,
            storageUsedGB = 0.0f,
            dailyAnalysisCount = 0,
            monthlyExportsCount = 0
        )
    }
    
    private suspend fun getUserCurrentUsage(userId: String): UserUsageStats {
        // TODO: Implement actual current usage
        return UserUsageStats(
            memoriesCount = 0,
            storageUsedGB = 0.0f,
            dailyAnalysisCount = 0,
            monthlyExportsCount = 0
        )
    }
    
    private fun getNextBillingDate(userScope: UserScope): Long? {
        // TODO: Implement billing date calculation
        return null
    }
    
    private fun getPlanEstimatedCost(plan: SubscriptionPlan): String {
        return when (plan) {
            SubscriptionPlan.FREE -> "Darmowy"
            SubscriptionPlan.BASIC -> "9.99 zł/miesiąc"
            SubscriptionPlan.PREMIUM -> "19.99 zł/miesiąc"
            SubscriptionPlan.FAMILY -> "29.99 zł/miesiąc"
            SubscriptionPlan.ENTERPRISE -> "99.99 zł/miesiąc"
            SubscriptionPlan.LIFETIME -> "299.99 zł jednorazowo"
        }
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
