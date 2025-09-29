package pl.soulsnaps.domain.interactor

import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.domain.SubscriptionStatus
import pl.soulsnaps.domain.UserPlan
import pl.soulsnaps.domain.UserPlanRepository
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * Use case for user plan management
 */
class UserPlanUseCase(
    private val userPlanRepository: UserPlanRepository,
    private val crashlyticsManager: CrashlyticsManager
) {
    
    /**
     * Get user plan
     */
    suspend operator fun invoke(userId: String): UserPlan? {
        return try {
            crashlyticsManager.log("Getting user plan for: $userId")
            val plan = userPlanRepository.getUserPlan(userId)
            crashlyticsManager.log("User plan retrieved: ${plan?.planType}")
            plan
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plan: ${e.message}")
            null
        }
    }
    
    /**
     * Create or update user plan
     */
    suspend fun saveUserPlan(userPlan: UserPlan): Boolean {
        return try {
            crashlyticsManager.log("Saving user plan: ${userPlan.planType} for user: ${userPlan.userId}")
            val success = userPlanRepository.saveUserPlan(userPlan)
            crashlyticsManager.log("User plan saved: $success")
            success
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error saving user plan: ${e.message}")
            false
        }
    }
    
    /**
     * Update user plan type
     */
    suspend fun updatePlanType(userId: String, planType: PlanType): Boolean {
        return try {
            crashlyticsManager.log("Updating plan type to: $planType for user: $userId")
            val success = userPlanRepository.updateUserPlanType(userId, planType)
            crashlyticsManager.log("Plan type updated: $success")
            success
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating plan type: ${e.message}")
            false
        }
    }
    
    /**
     * Update subscription status
     */
    suspend fun updateSubscriptionStatus(userId: String, status: SubscriptionStatus): Boolean {
        return try {
            crashlyticsManager.log("Updating subscription status to: $status for user: $userId")
            val success = userPlanRepository.updateSubscriptionStatus(userId, status)
            crashlyticsManager.log("Subscription status updated: $success")
            success
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating subscription status: ${e.message}")
            false
        }
    }
    
    /**
     * Check if user has active plan
     */
    suspend fun hasActivePlan(userId: String): Boolean {
        return try {
            crashlyticsManager.log("Checking active plan for user: $userId")
            val hasActive = userPlanRepository.hasActivePlan(userId)
            crashlyticsManager.log("User has active plan: $hasActive")
            hasActive
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error checking active plan: ${e.message}")
            false
        }
    }
    
    /**
     * Check if user is in trial
     */
    suspend fun isUserInTrial(userId: String): Boolean {
        return try {
            crashlyticsManager.log("Checking trial status for user: $userId")
            val inTrial = userPlanRepository.isUserInTrial(userId)
            crashlyticsManager.log("User is in trial: $inTrial")
            inTrial
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error checking trial status: ${e.message}")
            false
        }
    }
    
    /**
     * Get trial end date
     */
    suspend fun getTrialEndDate(userId: String): Long? {
        return try {
            crashlyticsManager.log("Getting trial end date for user: $userId")
            val trialEnd = userPlanRepository.getTrialEndDate(userId)
            crashlyticsManager.log("Trial ends at: $trialEnd")
            trialEnd
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting trial end date: ${e.message}")
            null
        }
    }
    
    /**
     * Update trial information
     */
    suspend fun updateTrialInfo(userId: String, trialEndsAt: Long?): Boolean {
        return try {
            crashlyticsManager.log("Updating trial info for user: $userId, trial ends at: $trialEndsAt")
            val success = userPlanRepository.updateTrialInfo(userId, trialEndsAt)
            crashlyticsManager.log("Trial info updated: $success")
            success
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating trial info: ${e.message}")
            false
        }
    }
    
    /**
     * Delete user plan
     */
    suspend fun deleteUserPlan(userId: String): Boolean {
        return try {
            crashlyticsManager.log("Deleting user plan for user: $userId")
            val success = userPlanRepository.deleteUserPlan(userId)
            crashlyticsManager.log("User plan deleted: $success")
            success
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error deleting user plan: ${e.message}")
            false
        }
    }
    
    /**
     * Get all user plans
     */
    suspend fun getAllUserPlans(): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting all user plans")
            val plans = userPlanRepository.getAllUserPlans()
            crashlyticsManager.log("Retrieved ${plans.size} user plans")
            plans
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting all user plans: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get user plans by type
     */
    suspend fun getUserPlansByType(planType: PlanType): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting user plans by type: $planType")
            val plans = userPlanRepository.getUserPlansByType(planType)
            crashlyticsManager.log("Retrieved ${plans.size} plans of type $planType")
            plans
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plans by type: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get user plans by status
     */
    suspend fun getUserPlansByStatus(status: SubscriptionStatus): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting user plans by status: $status")
            val plans = userPlanRepository.getUserPlansByStatus(status)
            crashlyticsManager.log("Retrieved ${plans.size} plans with status $status")
            plans
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plans by status: ${e.message}")
            emptyList()
        }
    }
}

