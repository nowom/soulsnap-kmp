package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

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
    val lastUpdated: Long = Clock.System.now().toEpochMilliseconds(),
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
            // Guest User - Offline-first basic access
            "GUEST" to PlanDefinition(
                scopes = listOf(
                    "memory.create",
                    "memory.read",
                    "map.basic",
                    "export.pdf"
                ),
                quotas = mapOf(
                    "snaps.capacity" to 10,
                    "ai.daily" to 1,  // changed from 0 to 1 for validation
                    "storage.gb" to 1 // changed from 0 to 1 for validation (minimum 1GB)
                ),
                features = mapOf(
                    "memory_capture" to true,
                    "map_basic" to true,
                    "export_pdf" to true,
                    "offline_sync" to true,
                    "memory_sync" to false,
                    "map_advanced" to false,
                    "filters_advanced" to false,
                    "export_video" to false,
                    "backup_cloud" to false,
                    "share_link" to false,
                    "audio_attach" to false,
                    "ai_generate" to false,
                    "ai_insights" to false
                ),
                pricing = PricingInfo(monthlyPrice = 0.0)
            ),
            
            // Free User - Cloud sync with basic limits
            "FREE_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.create",
                    "memory.read",
                    "memory.sync",
                    "analysis.run.single",
                    "map.basic",
                    "export.pdf",
                    "share.link",
                    "ai.generate"
                ),
                quotas = mapOf(
                    "memories.month" to 50,
                    "analysis.day" to 5,
                    "snaps.capacity" to 50,
                    "ai.daily" to 5,
                    "storage.gb" to 1
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to false,
                    "pattern_detection" to false,
                    "memory_capture" to true,
                    "memory_sync" to true,
                    "map_basic" to true,
                    "export_pdf" to true,
                    "share_link" to true,
                    "ai_generate" to true,
                    "map_advanced" to false,
                    "filters_advanced" to false,
                    "export_video" to false,
                    "backup_cloud" to false,
                    "audio_attach" to false,
                    "ai_insights" to false,
                    "team_collaboration" to false,
                    "analytics_dashboard" to false,
                    "api_access" to false
                ),
                pricing = PricingInfo(monthlyPrice = 0.0)
            ),
            
            // Premium User - Full features with advanced limits
            "PREMIUM_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.single", 
                    "analysis.run.patterns",
                    "insights.read",
                    "insights.export",
                    "sharing.basic",
                    "export.basic",
                    "backup.create",
                    "map.*",
                    "filters.*",
                    "export.*",
                    "backup.*",
                    "share.*",
                    "audio.*",
                    "ai.*"
                ),
                quotas = mapOf(
                    "memories.month" to 1000,
                    "analysis.day" to 100,
                    "analysis.patterns.day" to 20,
                    "export.month" to 10,
                    "backup.month" to 5,
                    "snaps.capacity" to -1, // unlimited
                    "ai.daily" to 100,
                    "storage.gb" to 10
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true,
                    "advanced_ai" to false,
                    "memory_capture" to true,
                    "memory_sync" to true,
                    "map_basic" to true,
                    "map_advanced" to true,
                    "filters_advanced" to true,
                    "export_pdf" to true,
                    "export_video" to true,
                    "backup_cloud" to true,
                    "share_link" to true,
                    "audio_attach" to true,
                    "ai_generate" to true,
                    "ai_insights" to true,
                    "team_collaboration" to false,
                    "analytics_dashboard" to false,
                    "api_access" to false,
                    "night_mode" to true,
                    "playlist_features" to true,
                    "advanced_stats" to true
                ),
                pricing = PricingInfo(monthlyPrice = 19.99, yearlyPrice = 199.99)
            ),
            
            // Enterprise User - All features with enterprise limits
            "ENTERPRISE_USER" to PlanDefinition(
                scopes = listOf(
                    "memory.*",
                    "analysis.run.*", 
                    "insights.*", 
                    "advanced_ai.*", 
                    "api.access",
                    "export.*",
                    "backup.*",
                    "map.*",
                    "filters.*",
                    "share.*",
                    "audio.*",
                    "ai.*",
                    "team.*",
                    "analytics.*"
                ),
                quotas = mapOf(
                    "memories.month" to 100000,
                    "analysis.day" to 10000,
                    "analysis.batch.day" to 1000,
                    "api.calls.month" to 100000,
                    "export.month" to 1000,
                    "snaps.capacity" to -1, // unlimited
                    "ai.daily" to 1000,
                    "storage.gb" to 100,
                    "backup.month" to -1, // unlimited
                    "team.members" to 50
                ),
                features = mapOf(
                    "basic_insights" to true,
                    "photo_analysis" to true,
                    "pattern_detection" to true,
                    "advanced_ai" to true,
                    "api_access" to true,
                    "priority_support" to true,
                    "memory_capture" to true,
                    "memory_sync" to true,
                    "map_basic" to true,
                    "map_advanced" to true,
                    "filters_advanced" to true,
                    "export_pdf" to true,
                    "export_video" to true,
                    "backup_cloud" to true,
                    "share_link" to true,
                    "audio_attach" to true,
                    "ai_generate" to true,
                    "ai_insights" to true,
                    "team_collaboration" to true,
                    "analytics_dashboard" to true,
                    "white_label" to true,
                    "sso_integration" to true,
                    "audit_logs" to true,
                    "custom_themes" to true
                ),
                pricing = PricingInfo(monthlyPrice = 99.99, yearlyPrice = 999.99)
            ),
            
            // Keep backward compatibility with old plans for tests
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
            version = "2.0", // Updated to reflect new plan structure
            lastUpdated = Clock.System.now().toEpochMilliseconds(),
            environment = "production"
        )
    )
    
    override fun getPlan(planName: String): PlanDefinition? = registry.plans[planName]
    
    override fun getAllPlans(): List<String> = registry.plans.keys.toList()
    
    override fun getRecommendedPlanForAction(action: String): String? {
        // Plan hierarchy order: FREE_USER is preferred for basic actions over GUEST
        val planOrder = listOf("FREE_USER", "BASIC_USER", "PREMIUM_USER", "ENTERPRISE_USER", "FAMILY_USER", "LIFETIME", "GUEST")
        
        // Find all plans that support this action
        val supportingPlans = registry.plans.entries.filter { (_, plan) ->
            plan.scopes.any { scope ->
                scope == action || (scope.endsWith(".*") && action.startsWith(scope.dropLast(2)))
            }
        }
        
        // Return the plan with highest priority (earliest in planOrder)
        return planOrder.firstOrNull { planName ->
            supportingPlans.any { it.key == planName }
        }
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
