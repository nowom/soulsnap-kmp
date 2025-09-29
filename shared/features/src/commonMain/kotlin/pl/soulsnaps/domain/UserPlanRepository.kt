package pl.soulsnaps.domain

import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Repository interface for user plan management
 */
interface UserPlanRepository {
    
    /**
     * Get user plan by user ID
     */
    suspend fun getUserPlan(userId: String): UserPlan?
    
    /**
     * Create or update user plan
     */
    suspend fun saveUserPlan(userPlan: UserPlan): Boolean
    
    /**
     * Update user plan type
     */
    suspend fun updateUserPlanType(userId: String, planType: PlanType): Boolean
    
    /**
     * Update subscription status
     */
    suspend fun updateSubscriptionStatus(userId: String, status: SubscriptionStatus): Boolean
    
    /**
     * Check if user has active plan
     */
    suspend fun hasActivePlan(userId: String): Boolean
    
    /**
     * Get all user plans (for admin purposes)
     */
    suspend fun getAllUserPlans(): List<UserPlan>
    
    /**
     * Delete user plan
     */
    suspend fun deleteUserPlan(userId: String): Boolean
    
    /**
     * Get user plans by plan type
     */
    suspend fun getUserPlansByType(planType: PlanType): List<UserPlan>
    
    /**
     * Get user plans by subscription status
     */
    suspend fun getUserPlansByStatus(status: SubscriptionStatus): List<UserPlan>
    
    /**
     * Check if user is in trial period
     */
    suspend fun isUserInTrial(userId: String): Boolean
    
    /**
     * Get trial end date for user
     */
    suspend fun getTrialEndDate(userId: String): Long?
    
    /**
     * Update trial information
     */
    suspend fun updateTrialInfo(userId: String, trialEndsAt: Long?): Boolean
}

/**
 * User plan data model
 */
data class UserPlan(
    val id: String? = null,
    val userId: String,
    val planType: PlanType,
    val planName: String,
    val isActive: Boolean = true,
    val subscriptionId: String? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val trialEndsAt: Long? = null,
    val currentPeriodStart: Long? = null,
    val currentPeriodEnd: Long? = null,
    val cancelAtPeriodEnd: Boolean = false,
    val canceledAt: Long? = null,
    val createdAt: Long = getCurrentTimeMillis(),
    val updatedAt: Long = getCurrentTimeMillis()
)

/**
 * Subscription status enum
 */
enum class SubscriptionStatus {
    ACTIVE,
    CANCELED,
    PAST_DUE,
    UNPAID,
    TRIALING
}
