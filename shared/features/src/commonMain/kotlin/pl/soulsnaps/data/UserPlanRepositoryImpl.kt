package pl.soulsnaps.data

import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.domain.SubscriptionStatus
import pl.soulsnaps.domain.UserPlan
import pl.soulsnaps.domain.UserPlanRepository
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Implementation of UserPlanRepository
 * Currently uses in-memory storage, can be extended to use Supabase
 */
class UserPlanRepositoryImpl(
    private val crashlyticsManager: CrashlyticsManager
) : UserPlanRepository {
    
    // In-memory storage for MVP
    private val userPlans = mutableMapOf<String, UserPlan>()
    
    override suspend fun getUserPlan(userId: String): UserPlan? {
        return try {
            crashlyticsManager.log("Getting user plan for user: $userId")
            val plan = userPlans[userId]
            crashlyticsManager.log("User plan found: ${plan?.planType}")
            plan
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plan: ${e.message}")
            null
        }
    }
    
    override suspend fun saveUserPlan(userPlan: UserPlan): Boolean {
        return try {
            crashlyticsManager.log("Saving user plan: ${userPlan.planType} for user: ${userPlan.userId}")
            userPlans[userPlan.userId] = userPlan.copy(updatedAt = getCurrentTimeMillis())
            crashlyticsManager.log("User plan saved successfully")
            true
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error saving user plan: ${e.message}")
            false
        }
    }
    
    override suspend fun updateUserPlanType(userId: String, planType: PlanType): Boolean {
        return try {
            crashlyticsManager.log("Updating user plan type to: $planType for user: $userId")
            val existingPlan = userPlans[userId]
            if (existingPlan != null) {
                val updatedPlan = existingPlan.copy(
                    planType = planType,
                    planName = getPlanName(planType),
                    updatedAt = getCurrentTimeMillis()
                )
                userPlans[userId] = updatedPlan
                crashlyticsManager.log("User plan type updated successfully")
                true
            } else {
                crashlyticsManager.log("User plan not found for update")
                false
            }
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating user plan type: ${e.message}")
            false
        }
    }
    
    override suspend fun updateSubscriptionStatus(userId: String, status: SubscriptionStatus): Boolean {
        return try {
            crashlyticsManager.log("Updating subscription status to: $status for user: $userId")
            val existingPlan = userPlans[userId]
            if (existingPlan != null) {
                val updatedPlan = existingPlan.copy(
                    subscriptionStatus = status,
                    updatedAt = getCurrentTimeMillis()
                )
                userPlans[userId] = updatedPlan
                crashlyticsManager.log("Subscription status updated successfully")
                true
            } else {
                crashlyticsManager.log("User plan not found for status update")
                false
            }
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating subscription status: ${e.message}")
            false
        }
    }
    
    override suspend fun hasActivePlan(userId: String): Boolean {
        return try {
            val plan = getUserPlan(userId)
            val hasActive = plan?.isActive == true && 
                           plan.subscriptionStatus in listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)
            crashlyticsManager.log("User $userId has active plan: $hasActive")
            hasActive
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error checking active plan: ${e.message}")
            false
        }
    }
    
    override suspend fun getAllUserPlans(): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting all user plans, count: ${userPlans.size}")
            userPlans.values.toList()
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting all user plans: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun deleteUserPlan(userId: String): Boolean {
        return try {
            crashlyticsManager.log("Deleting user plan for user: $userId")
            val removed = userPlans.remove(userId) != null
            crashlyticsManager.log("User plan deleted: $removed")
            removed
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error deleting user plan: ${e.message}")
            false
        }
    }
    
    override suspend fun getUserPlansByType(planType: PlanType): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting user plans by type: $planType")
            val plans = userPlans.values.filter { it.planType == planType }
            crashlyticsManager.log("Found ${plans.size} plans of type $planType")
            plans
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plans by type: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getUserPlansByStatus(status: SubscriptionStatus): List<UserPlan> {
        return try {
            crashlyticsManager.log("Getting user plans by status: $status")
            val plans = userPlans.values.filter { it.subscriptionStatus == status }
            crashlyticsManager.log("Found ${plans.size} plans with status $status")
            plans
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plans by status: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun isUserInTrial(userId: String): Boolean {
        return try {
            val plan = getUserPlan(userId)
            val inTrial = plan?.subscriptionStatus == SubscriptionStatus.TRIALING &&
                         plan.trialEndsAt?.let { it > getCurrentTimeMillis() } == true
            crashlyticsManager.log("User $userId is in trial: $inTrial")
            inTrial
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error checking trial status: ${e.message}")
            false
        }
    }
    
    override suspend fun getTrialEndDate(userId: String): Long? {
        return try {
            val plan = getUserPlan(userId)
            val trialEnd = plan?.trialEndsAt
            crashlyticsManager.log("User $userId trial ends at: $trialEnd")
            trialEnd
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting trial end date: ${e.message}")
            null
        }
    }
    
    override suspend fun updateTrialInfo(userId: String, trialEndsAt: Long?): Boolean {
        return try {
            crashlyticsManager.log("Updating trial info for user: $userId, trial ends at: $trialEndsAt")
            val existingPlan = userPlans[userId]
            if (existingPlan != null) {
                val updatedPlan = existingPlan.copy(
                    trialEndsAt = trialEndsAt,
                    subscriptionStatus = if (trialEndsAt != null) SubscriptionStatus.TRIALING else SubscriptionStatus.ACTIVE,
                    updatedAt = getCurrentTimeMillis()
                )
                userPlans[userId] = updatedPlan
                crashlyticsManager.log("Trial info updated successfully")
                true
            } else {
                crashlyticsManager.log("User plan not found for trial update")
                false
            }
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error updating trial info: ${e.message}")
            false
        }
    }
    
    /**
     * Helper method to get plan name from plan type
     */
    private fun getPlanName(planType: PlanType): String {
        return when (planType) {
            PlanType.GUEST -> "Guest"
            PlanType.FREE_USER -> "Free User"
            PlanType.PREMIUM_USER -> "Premium"
            PlanType.ENTERPRISE_USER -> "Enterprise"
        }
    }
    
    /**
     * Clear all user plans (for testing)
     */
    fun clearAllPlans() {
        crashlyticsManager.log("Clearing all user plans")
        userPlans.clear()
    }
    
    /**
     * Get user plan count (for statistics)
     */
    fun getUserPlanCount(): Int {
        return userPlans.size
    }
}
