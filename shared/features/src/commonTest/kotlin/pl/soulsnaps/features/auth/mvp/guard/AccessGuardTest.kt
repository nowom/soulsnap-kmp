package pl.soulsnaps.features.auth.mvp.guard

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.features.auth.mvp.guard.AccessGuard
import pl.soulsnaps.features.auth.mvp.guard.InMemoryScopePolicy
import pl.soulsnaps.features.auth.mvp.guard.InMemoryQuotaPolicy
import pl.soulsnaps.features.auth.mvp.guard.InMemoryFeatureToggle
import pl.soulsnaps.features.auth.mvp.guard.DefaultPlans
import pl.soulsnaps.features.auth.mvp.guard.PlanRegistryReader
import pl.soulsnaps.features.auth.mvp.guard.DenyReason

/**
 * Testy dla AccessGuard - Dependency Inversion Principle
 */
class AccessGuardTest {
    
    private lateinit var scopePolicy: InMemoryScopePolicy
    private lateinit var quotaPolicy: InMemoryQuotaPolicy
    private lateinit var featureToggle: InMemoryFeatureToggle
    private lateinit var guard: AccessGuard
    private lateinit var planRegistry: PlanRegistryReader
    
    @BeforeTest
    fun setup() {
        planRegistry = DefaultPlans
        scopePolicy = InMemoryScopePolicy(planRegistry)
        quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        featureToggle = InMemoryFeatureToggle()
        guard = AccessGuard(scopePolicy, quotaPolicy, featureToggle)
    }
    
    // ===== BASIC ACCESS CONTROL TESTS =====
    
    @Test
    fun `guard should allow action when all conditions are met`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertTrue(result.allowed)
        assertNull(result.reason)
        assertNull(result.message)
    }
    
    @Test
    fun `guard should deny action when feature flag is off`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        featureToggle.setFeature("feature.patterns", false)
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.FEATURE_OFF, result.reason)
        assertEquals("Funkcja chwilowo niedostępna", result.message)
        assertNotNull(result.featureInfo)
    }
    
    @Test
    fun `guard should deny action when user lacks scope`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.MISSING_SCOPE, result.reason)
        assertTrue(result.message?.contains("PREMIUM_USER") == true)
        assertEquals("PREMIUM_USER", result.recommendedPlan)
    }
    
    @Test
    fun `guard should deny action when quota is exceeded`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        // Zużyj cały quota
        repeat(5) {
            quotaPolicy.checkAndConsume("user123", "analysis.day")
        }
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.single",
            quotaKey = "analysis.day",
            flagKey = "feature.analysis"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason)
        assertTrue(result.message?.contains("Limit dzienny wyczerpany") == true)
        assertNotNull(result.quotaInfo)
    }
    
    // ===== QUOTA CONSUMPTION TESTS =====
    
    @Test
    fun `guard should consume quota when action is allowed`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.single",
            quotaKey = "analysis.day"
        )
        
        // Then
        assertTrue(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        assertEquals(initialQuota - 1, remainingQuota)
    }
    
    @Test
    fun `guard should not consume quota when action is denied`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        
        // When - próba akcji bez scope
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns", // brak scope w FREE_USER
            quotaKey = "analysis.day"
        )
        
        // Then
        assertFalse(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        assertEquals(initialQuota, remainingQuota) // quota nie zostało skonsumowane
    }
    
    // ===== CAN PERFORM ACTION TESTS =====
    
    @Test
    fun `canPerformAction should check without consuming quota`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        
        // When
        val result = guard.canPerformAction(
            userId = "user123",
            action = "analysis.run.single",
            quotaKey = "analysis.day"
        )
        
        // Then
        assertTrue(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "analysis.day")
        assertEquals(initialQuota, remainingQuota) // quota nie zostało skonsumowane
    }
    
    @Test
    fun `canPerformAction should deny when quota is exceeded`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        // Zużyj cały quota
        repeat(5) {
            quotaPolicy.checkAndConsume("user123", "analysis.day")
        }
        
        // When
        val result = guard.canPerformAction(
            userId = "user123",
            action = "analysis.run.single",
            quotaKey = "analysis.day"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason)
    }
    
    // ===== UPGRADE RECOMMENDATION TESTS =====
    
    @Test
    fun `getUpgradeRecommendation should return correct plan for action`() {
        // When & Then
        assertEquals("FREE_USER", guard.getUpgradeRecommendation("memory.create"))
        assertEquals("PREMIUM_USER", guard.getUpgradeRecommendation("analysis.run.patterns"))
        assertEquals("ENTERPRISE_USER", guard.getUpgradeRecommendation("api.access"))
    }
    
    @Test
    fun `getUpgradeRecommendation should return null for unknown action`() {
        // When
        val recommendation = guard.getUpgradeRecommendation("unknown.action")
        
        // Then
        assertNull(recommendation)
    }
    
    // ===== QUOTA STATUS TESTS =====
    
    @Test
    fun `getQuotaStatus should return correct remaining quota`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        quotaPolicy.checkAndConsume("user123", "analysis.day", 2)
        
        // When
        val remaining = guard.getQuotaStatus("user123", "analysis.day")
        
        // Then
        assertEquals(3, remaining) // 5 - 2 = 3
    }
    
    @Test
    fun `getQuotaInfo should return correct quota information`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        quotaPolicy.checkAndConsume("user123", "analysis.day", 2)
        
        // When
        val quotaInfo = guard.getQuotaInfo("user123", "analysis.day")
        
        // Then
        assertNotNull(quotaInfo)
        assertEquals("analysis.day", quotaInfo.key)
        assertEquals(2, quotaInfo.current)
        assertEquals(5, quotaInfo.limit)
        assertEquals(ResetType.DAILY, quotaInfo.resetType)
    }
    
    // ===== USER SCOPES TESTS =====
    
    @Test
    fun `getUserScopes should return correct scopes for user`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When
        val scopes = guard.getUserScopes("user123")
        
        // Then - sprawdź rzeczywiste scopes z planu (włączając wildcard)
        assertTrue(scopes.contains("memory.*")) // Wildcard scope
        assertTrue(scopes.contains("analysis.run.patterns"))
        assertTrue(scopes.contains("sharing.basic"))
        assertTrue(scopes.contains("insights.export"))
        
        // Sprawdź czy wildcard scope pasuje do konkretnych akcji
        assertTrue(scopePolicy.hasScope("user123", "memory.create")) // memory.* powinno pasować
        assertTrue(scopePolicy.hasScope("user123", "memory.read"))   // memory.* powinno pasować
    }
    
    @Test
    fun `getUserScopes should return default scopes for unknown user`() = runTest {
        // When
        val scopes = guard.getUserScopes("unknown_user")
        
        // Then
        assertTrue(scopes.contains("memory.create"))
        assertTrue(scopes.contains("analysis.run.single"))
        assertFalse(scopes.contains("analysis.run.patterns"))
    }
    
    // ===== FEATURE TOGGLE TESTS =====
    
    @Test
    fun `getFeatureInfo should return correct feature information`() {
        // When
        val analysisInfo = guard.getFeatureInfo("feature.analysis")
        val emergencyInfo = guard.getFeatureInfo("emergency.analysis.off")
        
        // Then
        assertNotNull(analysisInfo)
        assertEquals("feature.analysis", analysisInfo.key)
        assertTrue(analysisInfo.enabled)
        
        assertNotNull(emergencyInfo)
        assertEquals("emergency.analysis.off", emergencyInfo.key)
        assertFalse(emergencyInfo.enabled)
    }
    
    @Test
    fun `getAllFeatures should return all available features`() {
        // When
        val allFeatures = guard.getAllFeatures()
        
        // Then
        assertTrue(allFeatures.size >= 12)
        assertTrue(allFeatures.containsKey("feature.memories"))
        assertTrue(allFeatures.containsKey("feature.analysis"))
        assertTrue(allFeatures.containsKey("emergency.analysis.off"))
    }
    
    @Test
    fun `isFeatureEnabled should return correct feature status`() {
        // When & Then
        assertTrue(guard.isFeatureEnabled("feature.memories"))
        assertTrue(guard.isFeatureEnabled("feature.analysis"))
        assertFalse(guard.isFeatureEnabled("emergency.analysis.off"))
    }
    
    // ===== COMPLEX SCENARIOS TESTS =====
    
    @Test
    fun `guard should handle multiple quota keys correctly`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When - sprawdź różne quota keys
        val analysisResult = guard.allowAction(
            userId = "user123",
            action = "analysis.run.single",
            quotaKey = "analysis.day"
        )
        
        val patternsResult = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day"
        )
        
        // Then
        assertTrue(analysisResult.allowed)
        assertTrue(patternsResult.allowed)
        
        // Sprawdź czy quota zostały skonsumowane niezależnie
        assertEquals(99, quotaPolicy.getRemaining("user123", "analysis.day")) // 100 - 1
        assertEquals(19, quotaPolicy.getRemaining("user123", "analysis.patterns.day")) // 20 - 1
    }
    
    @Test
    fun `guard should handle feature flag changes dynamically`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When - feature włączony
        val result1 = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertTrue(result1.allowed)
        
        // When - wyłącz feature
        featureToggle.setFeature("feature.patterns", false)
        val result2 = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertFalse(result2.allowed)
        assertEquals(DenyReason.FEATURE_OFF, result2.reason)
    }
    
    @Test
    fun `guard should handle plan upgrades correctly`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When - spróbuj premium action
        val resultBefore = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertFalse(resultBefore.allowed)
        assertEquals(DenyReason.MISSING_SCOPE, resultBefore.reason)
        
        // When - upgrade plan
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        val resultAfter = guard.allowAction(
            userId = "user123",
            action = "analysis.run.patterns",
            flagKey = "feature.patterns"
        )
        
        // Then
        assertTrue(resultAfter.allowed)
    }
    
    @Test
    fun `guard should handle emergency feature flags`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When - włącz emergency flag
        featureToggle.setFeature("emergency.analysis.off", true)
        
        val result = guard.allowAction(
            userId = "user123",
            action = "analysis.run.single",
            flagKey = "emergency.analysis.off"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.FEATURE_OFF, result.reason)
    }
    
    // ===== EDGE CASES TESTS =====
    
    @Test
    fun `guard should handle null quota key gracefully`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create" // bez quota key
        )
        
        // Then
        assertTrue(result.allowed)
    }
    
    @Test
    fun `guard should handle null flag key gracefully`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "memories.month" // bez flag key
        )
        
        // Then
        assertTrue(result.allowed)
    }
    
    @Test
    fun `guard should handle empty user ID gracefully`() = runTest {
        // Given
        scopePolicy.setUserPlan("", "FREE_USER")
        
        // When
        val result = guard.allowAction(
            userId = "",
            action = "memory.create"
        )
        
        // Then
        assertTrue(result.allowed)
    }
    
    @Test
    fun `guard should handle empty action gracefully`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = ""
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.MISSING_SCOPE, result.reason)
    }
}
