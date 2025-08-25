package pl.soulsnaps.features.auth.mvp.guard.model

import kotlinx.serialization.Serializable

/**
 * PlanRestriction represents different types of restrictions that can trigger paywall
 */
@Serializable
sealed class PlanRestriction {
    
    /**
     * Restriction for snaps capacity limit
     */
    @Serializable
    data class SnapsCapacity(
        val current: Int,
        val limit: Int,
        val message: String
    ) : PlanRestriction()
    
    /**
     * Restriction for AI daily usage limit
     */
    @Serializable
    data class AIDailyLimit(
        val used: Int,
        val limit: Int,
        val message: String
    ) : PlanRestriction()
    
    /**
     * Restriction for storage limit
     */
    @Serializable
    data class StorageLimit(
        val usedGB: Double,
        val limitGB: Double,
        val message: String
    ) : PlanRestriction()
    
    /**
     * Restriction for specific feature access
     */
    @Serializable
    data class FeatureRestriction(
        val feature: String,
        val message: String,
        val upgradeRequired: Boolean
    ) : PlanRestriction()
    
    /**
     * Generic restriction for other cases
     */
    @Serializable
    data class GenericRestriction(
        val message: String,
        val upgradeRequired: Boolean
    ) : PlanRestriction()
}

