package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.datetime.Clock

/**
 * SOLID Principle: Open/Closed
 * Polityki są otwarte na rozszerzenie, zamknięte na modyfikację
 */

/**
 * Scope Policy - sprawdza uprawnienia użytkownika
 * Łatwo rozszerzyć o nowe typy uprawnień
 */
interface ScopePolicy {
    fun hasScope(userId: String, action: String): Boolean
    fun getUserScopes(userId: String): List<String>
    fun getRequiredPlanForAction(action: String): String?
}

/**
 * Quota Policy - zarządza limitami użytkownika
 * Łatwo rozszerzyć o nowe typy limitów
 */
interface QuotaPolicy {
    fun checkAndConsume(userId: String, key: String, amount: Int = 1): Boolean
    fun getRemaining(userId: String, key: String): Int
    fun resetQuota(userId: String, key: String): Boolean
    fun getQuotaInfo(userId: String, key: String): QuotaInfo?
}

/**
 * Feature Toggle - globalne przełączniki funkcji
 * Łatwo rozszerzyć o nowe typy flag
 */
interface FeatureToggle {
    fun isOn(key: String): Boolean
    fun getFeatureInfo(key: String): FeatureInfo?
    fun getAllFeatures(): Map<String, Boolean>
}

/**
 * User Context - kontekst użytkownika
 * Łatwo rozszerzyć o nowe atrybuty
 */
interface UserContext {
    fun getUserId(): String
    fun getUserPlan(): String
    fun getUserRole(): String?
    fun getUserAttributes(): Map<String, Any>
}

/**
 * Data classes dla rozszerzalności
 */
data class QuotaInfo(
    val key: String,
    val current: Int,
    val limit: Int,
    val resetTime: Long,
    val resetType: ResetType
)

enum class ResetType {
    DAILY, WEEKLY, MONTHLY, YEARLY, NEVER
}

data class FeatureInfo(
    val key: String,
    val enabled: Boolean,
    val description: String? = null,
    val lastChanged: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * SOLID: Liskov Substitution Principle
 * Implementacje mogą być zastąpione bez zmiany zachowania
 */

/**
 * In-Memory Scope Policy - dla MVP
 * W przyszłości łatwo zastąpić bazą danych
 */
class InMemoryScopePolicy(
    private val planRegistry: PlanRegistryReader
) : ScopePolicy {
    
    private val userPlans = mutableMapOf<String, String>()
    
    override fun hasScope(userId: String, action: String): Boolean {
        val userPlan = userPlans[userId] ?: "FREE_USER"
        val plan = planRegistry.getPlan(userPlan) ?: return false
        
        return plan.scopes.any { scope ->
            scope == action || scope.endsWith(".*") && action.startsWith(scope.dropLast(2))
        }
    }
    
    override fun getUserScopes(userId: String): List<String> {
        val userPlan = userPlans[userId] ?: "FREE_USER"
        return planRegistry.getPlan(userPlan)?.scopes ?: emptyList()
    }
    
    override fun getRequiredPlanForAction(action: String): String? {
        return planRegistry.getRecommendedPlanForAction(action)
    }
    
    // Helper methods
    fun setUserPlan(userId: String, plan: String) {
        userPlans[userId] = plan
    }
    
    fun getUserPlan(userId: String): String {
        return userPlans[userId] ?: "FREE_USER"
    }
}

/**
 * In-Memory Quota Policy - dla MVP
 * W przyszłości łatwo zastąpić Redisem lub bazą danych
 */
class InMemoryQuotaPolicy(
    private val planRegistry: PlanRegistryReader,
    private val scopePolicy: ScopePolicy
) : QuotaPolicy {
    
    private val userQuotas = mutableMapOf<String, MutableMap<String, Int>>()
    private val quotaResetTimes = mutableMapOf<String, MutableMap<String, Long>>()
    
    override fun checkAndConsume(userId: String, key: String, amount: Int): Boolean {
        val remaining = getRemaining(userId, key)
        if (remaining < amount) return false
        
        val userQuota = userQuotas.getOrPut(userId) { mutableMapOf() }
        userQuota[key] = (userQuota[key] ?: 0) + amount
        return true
    }
    
    override fun getRemaining(userId: String, key: String): Int {
        val userPlan = (scopePolicy as? InMemoryScopePolicy)?.getUserPlan(userId) ?: "FREE_USER"
        val plan = planRegistry.getPlan(userPlan) ?: return 0
        val limit = plan.quotas[key] ?: return 0
        
        val userQuota = userQuotas[userId] ?: return limit
        val used = userQuota[key] ?: 0
        
        return maxOf(0, limit - used)
    }
    
    override fun resetQuota(userId: String, key: String): Boolean {
        val userQuota = userQuotas[userId]
        userQuota?.remove(key)
        
        val userResetTimes = quotaResetTimes[userId]
        userResetTimes?.remove(key)
        
        return true
    }
    
    override fun getQuotaInfo(userId: String, key: String): QuotaInfo? {
        val userPlan = (scopePolicy as? InMemoryScopePolicy)?.getUserPlan(userId) ?: "FREE_USER"
        val plan = planRegistry.getPlan(userPlan) ?: return null
        val limit = plan.quotas[key] ?: return null
        
        val current = userQuotas[userId]?.get(key) ?: 0
        val resetTime = getNextResetTime(key)
        
        return QuotaInfo(
            key = key,
            current = current,
            limit = limit,
            resetTime = resetTime,
            resetType = getResetType(key)
        )
    }
    
    private fun getNextResetTime(key: String): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        return when {
            key.endsWith(".day") -> now + (24 * 60 * 60 * 1000) // +1 day
            key.endsWith(".month") -> now + (30L * 24 * 60 * 60 * 1000) // +30 days
            key.endsWith(".year") -> now + (365L * 24 * 60 * 60 * 1000) // +1 year
            else -> now + (24 * 60 * 60 * 1000) // default: daily
        }
    }
    
    private fun getResetType(key: String): ResetType {
        return when {
            key.endsWith(".day") -> ResetType.DAILY
            key.endsWith(".month") -> ResetType.MONTHLY
            key.endsWith(".year") -> ResetType.YEARLY
            else -> ResetType.DAILY
        }
    }
}

/**
 * In-Memory Feature Toggle - dla MVP
 * W przyszłości łatwo zastąpić Remote Config lub bazą danych
 */
class InMemoryFeatureToggle : FeatureToggle {
    
    private val features = mutableMapOf(
        "feature.memories" to true,
        "feature.analysis" to true,
        "feature.patterns" to true,
        "feature.insights" to true,
        "feature.sharing" to true,
        "feature.collaboration" to true,
        "feature.export" to true,
        "feature.backup" to true,
        "feature.ai" to true,
        "feature.api" to true,
        "feature.batch_analysis" to true, // Dodano feature flag dla batch analysis
        "emergency.analysis.off" to false,
        "emergency.sharing.off" to false
    )
    
    override fun isOn(key: String): Boolean {
        return features[key] ?: false
    }
    
    override fun getFeatureInfo(key: String): FeatureInfo? {
        val enabled = features[key] ?: return null
        return FeatureInfo(
            key = key,
            enabled = enabled,
            description = getFeatureDescription(key)
        )
    }
    
    override fun getAllFeatures(): Map<String, Boolean> = features.toMap()
    
    // Helper methods
    fun setFeature(key: String, enabled: Boolean) {
        features[key] = enabled
    }
    
    fun toggleFeature(key: String) {
        features[key] = !(features[key] ?: false)
    }
    
    private fun getFeatureDescription(key: String): String {
        return when (key) {
            "feature.memories" -> "Podstawowe funkcje wspomnień"
            "feature.analysis" -> "Analiza wspomnień"
            "feature.patterns" -> "Wykrywanie wzorców"
            "feature.insights" -> "Szczegółowe insights"
            "feature.sharing" -> "Udostępnianie"
            "feature.collaboration" -> "Współpraca"
            "feature.export" -> "Eksport danych"
            "feature.backup" -> "Backup"
            "feature.ai" -> "Funkcje AI"
            "feature.api" -> "Dostęp do API"
            "feature.batch_analysis" -> "Analiza obrazów w batch"
            "emergency.analysis.off" -> "Emergency: wyłączenie analizy"
            "emergency.sharing.off" -> "Emergency: wyłączenie udostępniania"
            else -> "Nieznana funkcja"
        }
    }
}

/**
 * Simple User Context - dla MVP
 * W przyszłości łatwo rozszerzyć o role, atrybuty, etc.
 */
class SimpleUserContext(
    private val userId: String,
    private val userPlan: String = "FREE_USER"
) : UserContext {
    
    override fun getUserId(): String = userId
    
    override fun getUserPlan(): String = userPlan
    
    override fun getUserRole(): String? = null // MVP: brak ról
    
    override fun getUserAttributes(): Map<String, Any> = emptyMap() // MVP: brak atrybutów
}
