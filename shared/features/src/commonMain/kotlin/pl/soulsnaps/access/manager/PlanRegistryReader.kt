package pl.soulsnaps.access.manager

import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.domain.UserPlanRepository
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * Interfejs do odczytu planów użytkowników
 */
interface PlanRegistryReader {
    
    /**
     * Pobierz plan użytkownika
     */
    suspend fun getPlan(userId: String): PlanDefinition?
    
    /**
     * Pobierz plan po typie
     */
    fun getPlanByType(type: PlanType): PlanDefinition

    /**
     * Sprawdź czy użytkownik ma plan
     */
    suspend fun hasPlan(userId: String): Boolean
    
    /**
     * Pobierz zalecany plan dla akcji
     */
    fun getRecommendedPlanForAction(action: String): PlanType?
    
    /**
     * Pobierz wszystkie dostępne plany
     */
    fun getAllPlans(): List<PlanDefinition>
}

/**
 * Implementacja PlanRegistryReader
 */
class PlanRegistryReaderImpl(
    private val userPlanRepository: UserPlanRepository,
    private val crashlyticsManager: CrashlyticsManager
) : PlanRegistryReader {
    
    override suspend fun getPlan(userId: String): PlanDefinition? {
        return try {
            crashlyticsManager.log("Getting plan for user: $userId")

            // Try to get user plan from database
            val userPlan = userPlanRepository.getUserPlan(userId)

            if (userPlan != null) {
                crashlyticsManager.log("Found user plan: ${userPlan.planType}")

                // Check if plan is active
                if (userPlan.isActive && userPlanRepository.hasActivePlan(userId)) {
                    val planDefinition = DefaultPlans.getPlan(userPlan.planType)
                    crashlyticsManager.log("Returning active plan: ${planDefinition.name}")
                    planDefinition
                } else {
                    crashlyticsManager.log("User plan is inactive, returning GUEST plan")
                    DefaultPlans.GUEST
                }
            } else {
                crashlyticsManager.log("No user plan found, returning GUEST plan")
                DefaultPlans.GUEST
            }
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error getting user plan: ${e.message}")
            // Fallback to GUEST plan on error
            DefaultPlans.GUEST
        }
    }
    
    override fun getPlanByType(type: PlanType): PlanDefinition {
        return DefaultPlans.getPlan(type)
    }
    
    override suspend fun hasPlan(userId: String): Boolean {
        return try {
            crashlyticsManager.log("Checking if user has plan: $userId")
            val userPlan = userPlanRepository.getUserPlan(userId)
            val hasPlan = userPlan != null && userPlan.isActive
            crashlyticsManager.log("User has plan: $hasPlan")
            hasPlan
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error checking if user has plan: ${e.message}")
            false
        }
    }
    
    override fun getRecommendedPlanForAction(action: String): PlanType? {
        return when {
            action.startsWith("ai.") -> PlanType.PREMIUM_USER
            action.startsWith("export.") -> PlanType.FREE_USER
            action.startsWith("memory.") -> PlanType.GUEST
            else -> null
        }
    }
    
    override fun getAllPlans(): List<PlanDefinition> {
        return DefaultPlans.getAllPlans()
    }
}



