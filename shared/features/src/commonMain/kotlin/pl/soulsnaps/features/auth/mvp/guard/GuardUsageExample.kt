@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package pl.soulsnaps.features.auth.mvp.guard

import pl.soulsnaps.features.auth.model.*

/**
 * Przykład użycia AccessGuard
 * Pokazuje prostotę MVP + możliwość rozszerzenia
 */

/**
 * Przykład 1: Podstawowe użycie (MVP)
 */
suspend fun exampleBasicUsage() {
    // Twórz domyślny guard (in-memory dla MVP)
    val guard = GuardFactory.createDefaultGuard()
    
    // Ustaw plan użytkownika
    val scopePolicy = (guard as? AccessGuard)?.let { 
        // W MVP: prosty dostęp do scope policy
        // W przyszłości: dependency injection
    }
    
    // Sprawdź dostęp
    val result = guard.allowAction(
        userId = "user123",
        action = "analysis.run.single",
        quotaKey = "analysis.day"
    )
    
    when {
        result.allowed -> performAnalysis()
        result.reason == DenyReason.MISSING_SCOPE -> showUpgradeDialog(result.recommendedPlan)
        result.reason == DenyReason.QUOTA_EXCEEDED -> showQuotaDialog(result.quotaInfo)
        result.reason == DenyReason.FEATURE_OFF -> showFeatureDisabledDialog(result.featureInfo)
    }
}

/**
 * Przykład 2: Rozszerzenie o nowe funkcjonalności (Future)
 */
suspend fun exampleFutureExtension() {
    // W przyszłości: łatwo zastąpić in-memory implementacje
    
    // Database Scope Policy (placeholder)
    val dbScopePolicy = InMemoryScopePolicy(DefaultPlans) // TODO: Implement DatabaseScopePolicy
    
    // Redis Quota Policy (placeholder)
    val redisQuotaPolicy = InMemoryQuotaPolicy(DefaultPlans, dbScopePolicy) // TODO: Implement RedisQuotaPolicy
    
    // Remote Config Feature Toggle
    val remoteFeatureToggle = InMemoryFeatureToggle() // TODO: Implement RemoteFeatureToggle
    
    // Twórz custom guard
    val enterpriseGuard = GuardFactory.createCustomGuard(
        scopePolicy = dbScopePolicy,
        quotaPolicy = redisQuotaPolicy,
        featureToggle = remoteFeatureToggle
    )
    
    // Użycie bez zmian w kodzie!
    val result = enterpriseGuard.allowAction(
        userId = "enterprise_user",
        action = "api.access",
        quotaKey = "api.calls.month"
    )
}

/**
 * Przykład 3: Decorator Pattern - dodawanie funkcjonalności
 */
suspend fun exampleDecoratorUsage() {
    val baseGuard = GuardFactory.createDefaultGuard()
    
    // Dodaj logowanie
    val loggingGuard = LoggingGuardDecorator(baseGuard) { message ->
        println("[ACCESS_LOG] $message")
    }
    
    // Dodaj metryki (placeholder)
    val metricsGuard = loggingGuard // TODO: Implement MetricsGuardDecorator
    
    // Dodaj cache (placeholder)
    val cachedGuard = metricsGuard // TODO: Implement CachedGuardDecorator
    
    // Użycie z wszystkimi funkcjonalnościami
    val result = cachedGuard.allowAction(
        userId = "user123",
        action = "memory.create",
        quotaKey = "memories.month",
        flagKey = "feature.memories"
    )
}

/**
 * Przykład 4: Rozszerzenie o nowe plany (Open/Closed)
 */
suspend fun examplePlanExtension() {
    // Łatwo dodać nowy plan bez modyfikacji istniejącego kodu
    
    val extendedPlans = ExtendedPlans() // Nowa implementacja
    val scopePolicy = InMemoryScopePolicy(extendedPlans)
    
    val guard = AccessGuard(
        scopePolicy = scopePolicy,
        quotaPolicy = InMemoryQuotaPolicy(extendedPlans, scopePolicy),
        featureToggle = InMemoryFeatureToggle()
    )
    
    // Nowy plan działa bez zmian!
    val result = guard.allowAction(
        userId = "student_user",
        action = "analysis.run.patterns",
        quotaKey = "analysis.student.month",
        flagKey = "feature.analysis"
    )
}

/**
 * Przykład 5: Rozszerzenie o nowe polityki (Interface Segregation)
 */
suspend fun examplePolicyExtension() {
    // Łatwo dodać nowe typy polityk
    
    // Rate Limiting Policy (placeholder)
    val rateLimitPolicy = object { // TODO: Implement RateLimitPolicy
        fun checkRateLimit(userId: String, key: String): Boolean = true
    }
    
    // Geographic Policy (placeholder)
    val geoPolicy = object { // TODO: Implement GeographicPolicy
        fun isAllowed(location: String): Boolean = true
    }
    
    // Extended Guard z nowymi politykami
    val extendedGuard = ExtendedAccessGuard(
        scopePolicy = InMemoryScopePolicy(DefaultPlans),
        quotaPolicy = InMemoryQuotaPolicy(DefaultPlans, InMemoryScopePolicy(DefaultPlans)),
        featureToggle = InMemoryFeatureToggle(),
        rateLimitPolicy = rateLimitPolicy,
        geoPolicy = geoPolicy
    )
    
    // Użycie z nowymi politykami
    val result = extendedGuard.allowAction(
        userId = "user123",
        action = "api.access",
        quotaKey = "api.calls.month",
        flagKey = "feature.api"
    )
}

/**
 * Przykład 6: Testowanie (Liskov Substitution)
 */
suspend fun exampleTesting() {
    // Przykład testowania z rzeczywistymi implementacjami
    
    val testGuard = GuardFactory.createDefaultGuard()
    
    // Testy bez zmian w kodzie!
    val result = testGuard.allowAction(
        userId = "user123",
        action = "memory.create",
        quotaKey = "memories.month",
        flagKey = "feature.memories"
    )
    
    // Sprawdź czy guard działa poprawnie
    // result.allowed może być true lub false w zależności od uprawnień użytkownika
}

// Placeholder implementations for future extensions
class DatabaseScopePolicy(
    private val database: Any, // TODO: Implement DatabaseConnection
    private val cache: Any // TODO: Implement RedisCache
) : ScopePolicy {
    override fun hasScope(userId: String, action: String): Boolean = true
    override fun getUserScopes(userId: String): List<String> = emptyList()
    override fun getRequiredPlanForAction(action: String): String? = null
}

class RedisQuotaPolicy(
    private val redis: Any, // TODO: Implement RedisConnection
    private val ttl: Long // TODO: Use Duration from model
) : QuotaPolicy {
    override fun checkAndConsume(userId: String, key: String, amount: Int): Boolean = true
    override fun getRemaining(userId: String, key: String): Int = 100
    override fun resetQuota(userId: String, key: String): Boolean = true
    override fun getQuotaInfo(userId: String, key: String): QuotaInfo? = null
}

class RemoteFeatureToggle(
    private val configService: Any, // TODO: Implement ConfigService
    private val fallback: FeatureToggle
) : FeatureToggle {
    override fun isOn(key: String): Boolean = true
    override fun getFeatureInfo(key: String): FeatureInfo? = null
    override fun getAllFeatures(): Map<String, Boolean> = emptyMap()
}

class ExtendedPlans : PlanRegistryReader {
    override fun getPlan(planName: String): PlanDefinition? = null
    override fun getAllPlans(): List<String> = emptyList()
    override fun getRecommendedPlanForAction(action: String): String? = null
    override fun getPlanMetadata(): PlanMetadata = PlanMetadata()
}

class RateLimitPolicy(
    private val redis: Any, // TODO: Implement RedisConnection
    private val limits: Any // TODO: Implement RateLimits
) {
    fun checkRateLimit(userId: String, key: String): Boolean = true
}

class GeographicPolicy(
    private val allowedCountries: List<String>,
    private val blockedRegions: List<String>
) {
    fun isAllowed(location: String): Boolean = true
}

class ExtendedAccessGuard(
    scopePolicy: ScopePolicy,
    quotaPolicy: QuotaPolicy,
    featureToggle: FeatureToggle,
    private val rateLimitPolicy: Any, // TODO: Proper type
    private val geoPolicy: Any // TODO: Proper type
) : AccessGuard(scopePolicy, quotaPolicy, featureToggle) {
    
    suspend fun allowAction(
        userId: String,
        action: String,
        quotaKey: String? = null,
        flagKey: String? = null,
        rateLimitKey: String? = null,
        geoLocation: String? = null
    ): AccessResult {
        // Sprawdź rate limiting (placeholder)
        if (rateLimitKey != null) {
            // TODO: Implement proper rate limiting check
            val rateLimitExceeded = false // Placeholder
            if (rateLimitExceeded) {
                return AccessResult(
                    allowed = false,
                    reason = DenyReason.QUOTA_EXCEEDED, // Using existing reason
                    message = "Rate limit exceeded"
                )
            }
        }
        
        // Sprawdź geolokalizację (placeholder)
        if (geoLocation != null) {
            // TODO: Implement proper geo check
            val geoAllowed = true // Placeholder
            if (!geoAllowed) {
                return AccessResult(
                    allowed = false,
                    reason = DenyReason.MISSING_SCOPE,
                    message = "Service not available in your region"
                )
            }
        }
        
        // Standardowe sprawdzenie
        return super.allowAction(userId, action, quotaKey, flagKey)
    }
}

// Mock implementations moved to test files to avoid duplication

// Placeholder classes (TODO: Implement when needed)
class LocalFeatureToggle : FeatureToggle {
    override fun isOn(key: String): Boolean = true
    override fun getFeatureInfo(key: String): FeatureInfo? = null
    override fun getAllFeatures(): Map<String, Boolean> = emptyMap()
}

// Placeholder functions
private fun performAnalysis() {}
private fun showUpgradeDialog(plan: String?) {}
private fun showQuotaDialog(quotaInfo: QuotaInfo?) {}
private fun showFeatureDisabledDialog(featureInfo: FeatureInfo?) {}
