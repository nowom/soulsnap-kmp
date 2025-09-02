package pl.soulsnaps.access.guard

import kotlin.test.*
import kotlinx.coroutines.test.runTest

/**
 * Testy dla GuardFactory - Factory Pattern
 */
class GuardFactoryTest {
    
    // ===== DEFAULT GUARD TESTS =====
    
    @Test
    fun `createDefaultGuard should create guard with in-memory implementations`() {
        // When
        val guard = GuardFactory.createDefaultGuard()
        
        // Then
        assertNotNull(guard)
        assertTrue(guard is AccessGuard)
        
        // Sprawdź czy ma dostęp do podstawowych funkcji
        assertTrue(guard.isFeatureEnabled("feature.memories"))
        assertTrue(guard.isFeatureEnabled("feature.analysis"))
    }
    
    @Test
    fun `createDefaultGuard should create working guard instance`() {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When & Then - sprawdź czy działa
        val features = guard.getAllFeatures()
        assertTrue(features.isNotEmpty())
        assertTrue(features.containsKey("feature.memories"))
    }
    
    // ===== CUSTOM GUARD TESTS =====
    
    @Test
    fun `createCustomGuard should create guard with custom implementations`() {
        // Given
        val customScopePolicy = MockScopePolicy(
            userScopes = mapOf("user123" to listOf("memory.create", "analysis.run.single"))
        )
        val customQuotaPolicy = MockQuotaPolicy(
            userQuotas = mapOf("user123" to mapOf("analysis.day" to 5))
        )
        val customFeatureToggle = MockFeatureToggle(
            features = mapOf("feature.analysis" to true)
        )
        
        // When
        val guard = GuardFactory.createCustomGuard(
            scopePolicy = customScopePolicy,
            quotaPolicy = customQuotaPolicy,
            featureToggle = customFeatureToggle
        )
        
        // Then
        assertNotNull(guard)
        assertTrue(guard is AccessGuard)
    }
    
    @Test
    fun `createCustomGuard should use provided implementations`() = runTest {
        // Given
        val customScopePolicy = MockScopePolicy(
            userScopes = mapOf("user123" to listOf("memory.create"))
        )
        val customQuotaPolicy = MockQuotaPolicy(
            userQuotas = mapOf("user123" to mapOf("memories.month" to 10))
        )
        val customFeatureToggle = MockFeatureToggle(
            features = mapOf("feature.memories" to true)
        )
        
        // When
        val guard = GuardFactory.createCustomGuard(
            scopePolicy = customScopePolicy,
            quotaPolicy = customQuotaPolicy,
            featureToggle = customFeatureToggle
        )
        
        // Then
        assertNotNull(guard)
        
        // Sprawdź czy używa custom implementacji
        val scopes = guard.getUserScopes("user123")
        assertTrue(scopes.contains("memory.create"))
        assertFalse(scopes.contains("analysis.run.single"))
    }
    
    // ===== TEST GUARD TESTS =====
    
    @Test
    fun `createTestGuard should create guard for testing`() {
        // When
        val guard = GuardFactory.createTestGuard()
        
        // Then
        assertNotNull(guard)
        assertTrue(guard is AccessGuard)
    }
    
    @Test
    fun `createTestGuard should be functional`() = runTest {
        // Given
        val guard = GuardFactory.createTestGuard()
        
        // When & Then
        val features = guard.getAllFeatures()
        assertTrue(features.isNotEmpty())
        
        // Sprawdź czy może sprawdzać scopes
        val scopes = guard.getUserScopes("test_user")
        assertTrue(scopes.isNotEmpty())
    }
    
    // ===== GUARD CONFIGURATION TESTS =====
    
    @Test
    fun `default guard should have all features enabled`() {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When
        val features = guard.getAllFeatures()
        
        // Then
        assertTrue(features["feature.memories"] == true)
        assertTrue(features["feature.analysis"] == true)
        assertTrue(features["feature.patterns"] == true)
        assertTrue(features["feature.insights"] == true)
        assertTrue(features["feature.sharing"] == true)
        assertTrue(features["feature.collaboration"] == true)
        assertTrue(features["feature.export"] == true)
        assertTrue(features["feature.backup"] == true)
        assertTrue(features["feature.ai"] == true)
        assertTrue(features["feature.api"] == true)
    }
    
    @Test
    fun `default guard should have emergency features disabled`() {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When
        val features = guard.getAllFeatures()
        
        // Then
        assertFalse(features["emergency.analysis.off"] == true)
        assertFalse(features["emergency.sharing.off"] == true)
    }
    
    // ===== GUARD BEHAVIOR TESTS =====
    
    @Test
    fun `default guard should allow basic actions for free user`() = runTest {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When & Then - sprawdź czy domyślny guard działa poprawnie
        val scopes = guard.getUserScopes("free_user")
        assertTrue(scopes.contains("memory.create"))
        assertTrue(scopes.contains("analysis.run.single"))
    }
    
    @Test
    fun `default guard should deny premium actions for free user`() = runTest {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When & Then
        val scopes = guard.getUserScopes("free_user")
        assertFalse(scopes.contains("analysis.run.patterns"))
        assertFalse(scopes.contains("sharing.basic"))
    }
    
    // ===== FACTORY PATTERN TESTS =====
    
    @Test
    fun `guard factory should create different guard instances`() {
        // When
        val guard1 = GuardFactory.createDefaultGuard()
        val guard2 = GuardFactory.createDefaultGuard()
        
        // Then
        assertNotNull(guard1)
        assertNotNull(guard2)
        // Każde wywołanie powinno tworzyć nową instancję
        assertNotSame(guard1, guard2)
    }
    
    @Test
    fun `guard factory should create guards with same behavior`() {
        // Given
        val guard1 = GuardFactory.createDefaultGuard()
        val guard2 = GuardFactory.createDefaultGuard()
        
        // When
        val features1 = guard1.getAllFeatures()
        val features2 = guard2.getAllFeatures()
        
        // Then
        assertEquals(features1.size, features2.size)
        features1.forEach { (key, value) ->
            assertEquals(value, features2[key], "Feature $key should have same value")
        }
    }
    
    // ===== ERROR HANDLING TESTS =====
    
    @Test
    fun `guard factory should handle null implementations gracefully`() {
        // Given
        val nullScopePolicy: ScopePolicy? = null
        val nullQuotaPolicy: QuotaPolicy? = null
        val nullFeatureToggle: FeatureToggle? = null
        
        // When & Then - powinno rzucić exception przy próbie utworzenia
        assertFailsWith<NullPointerException> {
            GuardFactory.createCustomGuard(
                scopePolicy = nullScopePolicy!!,
                quotaPolicy = nullQuotaPolicy!!,
                featureToggle = nullFeatureToggle!!
            )
        }
    }
    
    // ===== INTEGRATION TESTS =====
    
    @Test
    fun `factory created guards should work with memory analysis service`() = runTest {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When & Then - sprawdź czy guard może być użyty w service
        val scopes = guard.getUserScopes("test_user")
        assertTrue(scopes.isNotEmpty())
        
        val features = guard.getAllFeatures()
        assertTrue(features.isNotEmpty())
        
        // Sprawdź czy może sprawdzać quota
        val quotaStatus = guard.getQuotaStatus("test_user", "analysis.day")
        assertTrue(quotaStatus >= 0)
    }
    
    @Test
    fun `factory created guards should support all required operations`() = runTest {
        // Given
        val guard = GuardFactory.createDefaultGuard()
        
        // When & Then - sprawdź wszystkie wymagane operacje
        assertNotNull(guard.getFeatureInfo("feature.analysis"))
        assertNotNull(guard.getAllFeatures())
        assertTrue(guard.isFeatureEnabled("feature.memories"))
        
        // Sprawdź upgrade recommendations
        assertNotNull(guard.getUpgradeRecommendation("memory.create"))
        assertNotNull(guard.getUpgradeRecommendation("analysis.run.patterns"))
    }
}

// Mock implementations for testing
class MockScopePolicy(
    private val userScopes: Map<String, List<String>>
) : ScopePolicy {
    override fun hasScope(userId: String, action: String): Boolean = 
        userScopes[userId]?.contains(action) ?: false
    override fun getUserScopes(userId: String): List<String> = 
        userScopes[userId] ?: emptyList()
    override fun getRequiredPlanForAction(action: String): String? = null
}

class MockQuotaPolicy(
    private val userQuotas: Map<String, Map<String, Int>>
) : QuotaPolicy {
    override fun checkAndConsume(userId: String, key: String, amount: Int): Boolean = true
    override fun getRemaining(userId: String, key: String): Int = 
        userQuotas[userId]?.get(key) ?: 100
    override fun resetQuota(userId: String, key: String): Boolean = true
    override fun getQuotaInfo(userId: String, key: String): QuotaInfo? = null
}

class MockFeatureToggle(
    private val features: Map<String, Boolean>
) : FeatureToggle {
    override fun isOn(key: String): Boolean = features[key] ?: false
    override fun getFeatureInfo(key: String): FeatureInfo? = null
    override fun getAllFeatures(): Map<String, Boolean> = features
}
