package pl.soulsnaps.access.manager

import pl.soulsnaps.access.model.PlanType

/**
 * Domyślne plany dostępne w aplikacji
 */
object DefaultPlans {
    
    val GUEST = PlanDefinition(
        type = PlanType.GUEST,
        name = "Guest",
        description = "Darmowy plan z ograniczonymi funkcjami",
        scopes = setOf("memory.basic", "view.basic"),
        quotas = mapOf(
            "snaps.capacity" to 5,
            "storage.gb" to 1,
            "ai.daily" to 1
        ),
        features = setOf("basic.capture", "basic.view"),
        pricing = PlanPricing(
            monthlyPrice = 0f,
            yearlyPrice = 0f,
            lifetimePrice = 0f
        )
    )
    
    val FREE_USER = PlanDefinition(
        type = PlanType.FREE_USER,
        name = "Free User",
        description = "Darmowy plan z podstawowymi funkcjami",
        scopes = setOf("memory.basic", "view.basic", "export.basic"),
        quotas = mapOf(
            "snaps.capacity" to 50,
            "storage.gb" to 10,
            "ai.daily" to 5,
            "export.data" to 10
        ),
        features = setOf("basic.capture", "basic.view", "basic.export", "basic.analysis"),
        pricing = PlanPricing(
            monthlyPrice = 0f,
            yearlyPrice = 0f,
            lifetimePrice = 0f
        )
    )
    
    val PREMIUM_USER = PlanDefinition(
        type = PlanType.PREMIUM_USER,
        name = "Premium",
        description = "Pełny dostęp do wszystkich funkcji",
        scopes = setOf("memory.*", "view.*", "export.*", "ai.*"),
        quotas = mapOf(
            "snaps.capacity" to -1, // unlimited
            "storage.gb" to -1,     // unlimited
            "ai.daily" to -1,       // unlimited
            "export.data" to -1     // unlimited
        ),
        features = setOf("*"), // all features
        pricing = PlanPricing(
            monthlyPrice = 9.99f,
            yearlyPrice = 99.99f,
            lifetimePrice = 299.99f
        )
    )
    
    /**
     * Pobierz plan po typie
     */
    fun getPlan(type: PlanType): PlanDefinition {
        return when (type) {
            PlanType.GUEST -> GUEST
            PlanType.FREE_USER -> FREE_USER
            PlanType.PREMIUM_USER -> PREMIUM_USER
            PlanType.ENTERPRISE_USER -> PREMIUM_USER // Fallback to premium
        }
    }
    
    /**
     * Pobierz wszystkie dostępne plany
     */
    fun getAllPlans(): List<PlanDefinition> {
        return listOf(GUEST, FREE_USER, PREMIUM_USER)
    }
}

/**
 * Definicja planu użytkownika
 */
data class PlanDefinition(
    val type: PlanType,
    val name: String,
    val description: String,
    val scopes: Set<String>,
    val quotas: Map<String, Int>, // -1 = unlimited
    val features: Set<String>,
    val pricing: PlanPricing? = null
)

/**
 * Cennik planu
 */
data class PlanPricing(
    val monthlyPrice: Float? = null,
    val yearlyPrice: Float? = null,
    val lifetimePrice: Float? = null,
    val currency: String = "USD"
)
