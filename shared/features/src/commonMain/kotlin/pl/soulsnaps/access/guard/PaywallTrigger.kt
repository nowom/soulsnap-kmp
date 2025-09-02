package pl.soulsnaps.access.guard

import pl.soulsnaps.access.model.AppAction
import pl.soulsnaps.access.model.AppFeature
import pl.soulsnaps.access.model.PlanRestriction
import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.access.model.UserAction
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl

/**
 * PaywallTrigger handles plan restrictions and paywall UX
 * Triggers appropriate paywall modals when users hit plan limits
 */
class PaywallTrigger(
    private val accessGuard: AccessGuard,
    private val planRegistry: PlanRegistryReader = PlanRegistryReaderImpl()
) {
    
    /**
     * Check if user can perform action and trigger paywall if needed
     */
    suspend fun checkAndTrigger(
        userId: String,
        action: AppAction,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return try {
            val actionString = when (action) {
                is AppAction.CreateMemory -> "memory.create"
                is AppAction.AnalyzeMemory -> "analysis.run"
                is AppAction.ShareMemory -> "memory.share"
                is AppAction.ExportData -> "export.data"
                is AppAction.BackupData -> "backup.data"
                is AppAction.CustomizeApp -> "customize.app"
                is AppAction.UseAI -> "ai.use"
                is AppAction.ExportVideo -> "export.video"
                is AppAction.BackupToCloud -> "backup.cloud"
                is AppAction.AdvancedFilters -> "filters.advanced"
                is AppAction.AudioAttach -> "audio.attach"
            }
            
            val result = accessGuard.allowAction(userId, actionString)
            if (result.allowed) {
                true
            } else {
                // Get user's current plan to determine restriction type
                val userPlan = getUserPlan(userId)
                val restriction = createRestriction(action, userPlan, result.message)
                onRestricted(restriction)
                false
            }
        } catch (e: Exception) {
            // If there's an error, default to allowing the action
            // but log the error for debugging
            println("PaywallTrigger error: ${e.message}")
            true
        }
    }
    
    /**
     * Check specific feature access and trigger paywall if needed
     */
    suspend fun checkFeatureAccess(
        userId: String,
        feature: AppFeature,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return try {
            val featureString = when (feature) {
                AppFeature.MEMORY_CAPTURE -> "memory.capture"
                AppFeature.MEMORY_SYNC -> "memory.sync"
                AppFeature.MAP_BASIC -> "map.basic"
                AppFeature.MAP_ADVANCED -> "map.advanced"
                AppFeature.FILTERS_ADVANCED -> "filters.advanced"
                AppFeature.EXPORT_PDF -> "export.pdf"
                AppFeature.EXPORT_VIDEO -> "export.video"
                AppFeature.BACKUP_CLOUD -> "backup.cloud"
                AppFeature.SHARE_LINK -> "share.link"
                AppFeature.AUDIO_ATTACH -> "audio.attach"
                AppFeature.AI_GENERATE -> "ai.generate"
                AppFeature.AI_INSIGHTS -> "ai.insights"
                AppFeature.TEAM_COLLABORATION -> "team.collaboration"
                AppFeature.ANALYTICS_DASHBOARD -> "analytics.dashboard"
                AppFeature.API_ACCESS -> "api.access"
            }
            
            val result = accessGuard.allowAction(userId, featureString)
            if (result.allowed) {
                true
            } else {
                val userPlan = getUserPlan(userId)
                val restriction = createFeatureRestriction(feature, userPlan, result.message)
                onRestricted(restriction)
                false
            }
        } catch (e: Exception) {
            println("PaywallTrigger feature check error: ${e.message}")
            true
        }
    }
    
    /**
     * Check capacity limits (e.g., number of snaps)
     */
    suspend fun checkCapacityLimit(
        userId: String,
        currentCount: Int,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        val userPlan = getUserPlan(userId)
        val limit = getCapacityLimit(userPlan)
        
        if (limit == -1 || currentCount < limit) {
            return true
        }
        
        val restriction = PlanRestriction.SnapsCapacity(
            current = currentCount,
            limit = limit,
            message = "Osiągnięto limit $limit Snapów. Odblokuj nielimit w Premium!"
        )
        onRestricted(restriction)
        return false
    }
    
    /**
     * Check AI daily limit
     */
    suspend fun checkAIDailyLimit(
        userId: String,
        usedToday: Int,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        val userPlan = getUserPlan(userId)
        val limit = getAIDailyLimit(userPlan)
        
        if (usedToday < limit) {
            return true
        }
        
        val restriction = PlanRestriction.AIDailyLimit(
            used = usedToday,
            limit = limit,
            message = "Dzisiejszy limit AI wykorzystany ($usedToday/$limit). Premium zwiększa limit do 100/dzień!"
        )
        onRestricted(restriction)
        return false
    }
    
    /**
     * Check storage limit
     */
    suspend fun checkStorageLimit(
        userId: String,
        usedGB: Double,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        val userPlan = getUserPlan(userId)
        val limit = getStorageLimit(userPlan)
        
        if (usedGB < limit) {
            return true
        }
        
        val restriction = PlanRestriction.StorageLimit(
            usedGB = usedGB,
            limitGB = limit,
            message = "Osiągnięto limit miejsca ($usedGB GB/$limit GB). Premium daje 10 GB!"
        )
        onRestricted(restriction)
        return false
    }
    
    /**
     * Get user's current plan (simplified - in real app this would come from user service)
     */
    private fun getUserPlan(userId: String): PlanType {
        // TODO: In real implementation, get from user service
        // For now, return FREE_USER as default
        return PlanType.FREE_USER
    }
    
    /**
     * Get capacity limit for plan
     */
    private fun getCapacityLimit(plan: PlanType): Int {
        return when (plan) {
            PlanType.GUEST -> 10
            PlanType.FREE_USER -> 50
            PlanType.PREMIUM_USER -> -1 // unlimited
            PlanType.ENTERPRISE_USER -> -1 // unlimited
        }
    }
    
    /**
     * Get AI daily limit for plan
     */
    private fun getAIDailyLimit(plan: PlanType): Int {
        return when (plan) {
            PlanType.GUEST -> 0
            PlanType.FREE_USER -> 5
            PlanType.PREMIUM_USER -> 100
            PlanType.ENTERPRISE_USER -> 1000
        }
    }
    
    /**
     * Get storage limit for plan
     */
    private fun getStorageLimit(plan: PlanType): Double {
        return when (plan) {
            PlanType.GUEST -> 0.1
            PlanType.FREE_USER -> 1.0
            PlanType.PREMIUM_USER -> 10.0
            PlanType.ENTERPRISE_USER -> 100.0
        }
    }
    
    /**
     * Create restriction based on action and plan
     */
    private fun createRestriction(
        action: AppAction,
        plan: PlanType,
        reason: String?
    ): PlanRestriction {
        return when (action) {
            is AppAction.CreateMemory -> {
                val limit = getCapacityLimit(plan)
                PlanRestriction.SnapsCapacity(
                    current = 0, // TODO: Get actual current count
                    limit = limit,
                    message = reason ?: "Osiągnięto limit Snapów. Odblokuj nielimit w Premium!"
                )
            }
            is AppAction.UseAI -> {
                val limit = getAIDailyLimit(plan)
                PlanRestriction.AIDailyLimit(
                    used = 0, // TODO: Get actual usage
                    limit = limit,
                    message = reason ?: "Dzisiejszy limit AI wykorzystany. Premium zwiększa limit do 100/dzień!"
                )
            }
            is AppAction.ExportVideo -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Eksport wideo",
                    message = "Eksport wideo dostępny w Premium!",
                    upgradeRequired = true
                )
            }
            is AppAction.BackupToCloud -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Backup do chmury",
                    message = "Backup do chmury dostępny w Premium!",
                    upgradeRequired = true
                )
            }
            is AppAction.AdvancedFilters -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Zaawansowane filtry",
                    message = "Zaawansowane filtry dostępne w Premium!",
                    upgradeRequired = true
                )
            }
            is AppAction.AudioAttach -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Audio do Snapów",
                    message = "Audio do Snapów dostępne w Premium!",
                    upgradeRequired = true
                )
            }
            else -> {
                PlanRestriction.GenericRestriction(
                    message = reason ?: "Ta funkcja wymaga wyższego planu.",
                    upgradeRequired = true
                )
            }
        }
    }
    
    /**
     * Create restriction based on feature and plan
     */
    private fun createFeatureRestriction(
        feature: AppFeature,
        plan: PlanType,
        reason: String?
    ): PlanRestriction {
        return when (feature) {
            AppFeature.MAP_ADVANCED -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Zaawansowana mapa",
                    message = "Zaawansowana mapa dostępna w Premium!",
                    upgradeRequired = true
                )
            }
            AppFeature.FILTERS_ADVANCED -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Zaawansowane filtry",
                    message = "Zaawansowane filtry dostępne w Premium!",
                    upgradeRequired = true
                )
            }
            AppFeature.EXPORT_VIDEO -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Eksport wideo",
                    message = "Eksport wideo dostępny w Premium!",
                    upgradeRequired = true
                )
            }
            AppFeature.BACKUP_CLOUD -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Backup do chmury",
                    message = "Backup do chmury dostępny w Premium!",
                    upgradeRequired = true
                )
            }
            AppFeature.AUDIO_ATTACH -> {
                PlanRestriction.FeatureRestriction(
                    feature = "Audio do Snapów",
                    message = "Audio do Snapów dostępne w Premium!",
                    upgradeRequired = true
                )
            }
            AppFeature.AI_INSIGHTS -> {
                PlanRestriction.FeatureRestriction(
                    feature = "AI Insights",
                    message = "AI Insights dostępne w Premium!",
                    upgradeRequired = true
                )
            }
            else -> {
                PlanRestriction.GenericRestriction(
                    message = reason ?: "Ta funkcja wymaga wyższego planu.",
                    upgradeRequired = true
                )
            }
        }
    }
}
