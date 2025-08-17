package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.serialization.Serializable

/**
 * SOLID Principle: Single Responsibility
 * PlanRegistry - tylko zarządza planami, nie logiką biznesową
 */

@Serializable
data class PlanRegistry(
    val plans: Map<String, PlanDefinition>,
    val metadata: PlanMetadata = PlanMetadata()
)

@Serializable
data class PlanDefinition(
    val scopes: List<String>,           // What actions are allowed
    val quotas: Map<String, Int>,       // What limits apply
    val features: Map<String, Boolean> = emptyMap(), // Feature flags per plan
    val pricing: PricingInfo? = null    // Pricing information
)

@Serializable
data class PlanMetadata(
    val version: String = "1.0",
    val lastUpdated: Long = System.currentTimeMillis(),
    val environment: String = "production"
)

@Serializable
data class PricingInfo(
    val monthlyPrice: Double? = null,
    val yearlyPrice: Double? = null,
    val lifetimePrice: Double? = null,
    val currency: String = "PLN"
)

/**
 * Plan Registry Interface - SOLID: Interface Segregation
 */
interface PlanRegistryReader {
    fun getPlan(planName: String): PlanDefinition?
    fun getAllPlans(): List<String>
    fun getRecommendedPlanForAction(action: String): String?
    fun getPlanMetadata(): PlanMetadata
}

interface PlanRegistryWriter {
    fun updatePlan(planName: String, definition: PlanDefinition): Boolean
    fun addPlan(planName: String, definition: PlanDefinition): Boolean
    fun removePlan(planName: String): Boolean
}

/**
 * Default Plans Implementation - SOLID: Open/Closed Principle
 * Łatwo rozszerzyć o nowe plany bez modyfikacji istniejącego kodu
 */
object DefaultPlans : PlanRegistryReader {
    
    private val registry = PlanRegistry(
        plans = mapOf(
            "FREE_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.create",
                    "memory.read", 
                    "analysis.run.single"
                ),
                quotas = mapOf(
                    "memories.month" to 50,
                    "analysis.day" to 5
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to false,
                    "pattern_detection" to false
                ),
                pricing = PricingInfo(monthlyPrice = 0.0)
            ),
            
            "BASIC_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.single", 
                    "insights.read",
                    "export.basic"
                ),
                quotas = mapOf(
                    "memories.month" to 500,
                    "analysis.day" to 50,
                    "export.month" to 5
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to false
                ),
                pricing = PricingInfo(monthlyPrice = 9.99)
            ),
            
            "PREMIUM_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.single", 
                    "analysis.run.patterns",
                    "insights.read",
                    "insights.export",
                    "sharing.basic",
                    "export.basic",
                    "backup.create"
                ),
                quotas = mapOf(
                    "memories.month" to 1000,
                    "analysis.day" to 100,
                    "analysis.patterns.day" to 20,
                    "export.month" to 10,
                    "backup.month" to 5
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true,
                    "advanced_ai" to false
                ),
                pricing = PricingInfo(monthlyPrice = 19.99, yearlyPrice = 199.99)
            ),
            
            "FAMILY_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "collaboration.basic", 
                    "sharing.basic", 
                    "analysis.run.single",
                    "analysis.run.patterns",
                    "insights.read",
                    "insights.export"
                ),
                quotas = mapOf(
                    "memories.month" to 5000,
                    "analysis.day" to 200,
                    "collaborators.max" to 10,
                    "shares.month" to 100
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true,
                    "family_features" to true
                ),
                pricing = PricingInfo(monthlyPrice = 29.99, yearlyPrice = 299.99)
            ),
            
            "ENTERPRISE_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.*", 
                    "insights.*", 
                    "advanced_ai.*", 
                    "api.access",
                    "export.*",
                    "backup.*"
                ),
                quotas = mapOf(
                    "memories.month" to 100000,
                    "analysis.day" to 10000,
                    "api.calls.month" to 100000,
                    "export.month" to 1000
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true,
                    "advanced_ai" to true,
                    "api_access" to true,
                    "priority_support" to true
                ),
                pricing = PricingInfo(monthlyPrice = 99.99, yearlyPrice = 999.99)
            ),
            
            "LIFETIME" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.single", 
                    "analysis.run.patterns",
                    "insights.read",
                    "insights.export",
                    "export.basic"
                ),
                quotas = mapOf(
                    "memories.month" to 1000,
                    "analysis.day" to 100,
                    "export.month" to 10
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true
                ),
                pricing = PricingInfo(lifetimePrice = 299.99)
            )
        ),
        metadata = PlanMetadata(
            version = "1.0",
            lastUpdated = System.currentTimeMillis(),
            environment = "production"
        )
    )
    
    override fun getPlan(planName: String): PlanDefinition? = registry.plans[planName]
    
    override fun getAllPlans(): List<String> = registry.plans.keys.toList()
    
    override fun getRecommendedPlanForAction(action: String): String? {
        return registry.plans.entries.find { (_, plan) ->
            plan.scopes.any { scope ->
                scope == action || scope.endsWith(".*") && action.startsWith(scope.dropLast(2))
            }
        }?.key
    }
    
    override fun getPlanMetadata(): PlanMetadata = registry.metadata
    
    /**
     * SOLID: Open/Closed - łatwo dodać nowe funkcje bez modyfikacji
     */
    fun getPlanFeatures(planName: String): Map<String, Boolean> {
        return getPlan(planName)?.features ?: emptyMap()
    }
    
    fun getPlanPricing(planName: String): PricingInfo? {
        return getPlan(planName)?.pricing
    }
    
    fun getPlanQuotas(planName: String): Map<String, Int> {
        return getPlan(planName)?.quotas ?: emptyMap()
    }
}
