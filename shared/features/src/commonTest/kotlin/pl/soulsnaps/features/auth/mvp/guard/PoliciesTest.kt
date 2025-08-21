package pl.soulsnaps.features.auth.mvp.guard

import kotlin.test.*

/**
 * Testy dla Policies - SOLID: Open/Closed + Interface Segregation
 */
class PoliciesTest {
    
    private lateinit var scopePolicy: InMemoryScopePolicy
    private lateinit var quotaPolicy: InMemoryQuotaPolicy
    private lateinit var featureToggle: InMemoryFeatureToggle
    private lateinit var planRegistry: PlanRegistryReader
    
    @BeforeTest
    fun setup() {
        planRegistry = DefaultPlans
        scopePolicy = InMemoryScopePolicy(planRegistry)
        quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        featureToggle = InMemoryFeatureToggle()
    }
    
    // ===== SCOPE POLICY TESTS =====
    
    @Test
    fun `scope policy should allow basic actions for free user`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When & Then
        assertTrue(scopePolicy.hasScope("user123", "memory.create"))
        assertTrue(scopePolicy.hasScope("user123", "memory.read"))
        assertTrue(scopePolicy.hasScope("user123", "analysis.run.single"))
    }
    
    @Test
    fun `scope policy should deny premium actions for free user`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When & Then
        assertFalse(scopePolicy.hasScope("user123", "analysis.run.patterns"))
        assertFalse(scopePolicy.hasScope("user123", "sharing.basic"))
        assertFalse(scopePolicy.hasScope("user123", "insights.export"))
    }
    
    @Test
    fun `scope policy should allow all actions for premium user`() {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When & Then
        assertTrue(scopePolicy.hasScope("user123", "memory.create"))
        assertTrue(scopePolicy.hasScope("user123", "analysis.run.patterns"))
        assertTrue(scopePolicy.hasScope("user123", "sharing.basic"))
        assertTrue(scopePolicy.hasScope("user123", "insights.export"))
    }
    
    @Test
    fun `scope policy should handle wildcard scopes correctly`() {
        // Given
        scopePolicy.setUserPlan("user123", "BASIC_USER")
        
        // When & Then
        // memory.* scope powinien pozwolić na wszystkie memory actions
        assertTrue(scopePolicy.hasScope("user123", "memory.create"))
        assertTrue(scopePolicy.hasScope("user123", "memory.read"))
        assertTrue(scopePolicy.hasScope("user123", "memory.update"))
        assertTrue(scopePolicy.hasScope("user123", "memory.delete"))
    }
    
    @Test
    fun `scope policy should return default plan for unknown user`() {
        // Given
        // user123 nie ma ustawionego planu
        
        // When
        val scopes = scopePolicy.getUserScopes("user123")
        
        // Then
        assertEquals("FREE_USER", scopePolicy.getUserPlan("user123"))
        assertTrue(scopes.contains("memory.create"))
        assertTrue(scopes.contains("analysis.run.single"))
    }
    
    @Test
    fun `scope policy should return correct required plan for action`() {
        // When & Then
        assertEquals("FREE_USER", scopePolicy.getRequiredPlanForAction("memory.create"))
        assertEquals("PREMIUM_USER", scopePolicy.getRequiredPlanForAction("analysis.run.patterns"))
        assertEquals("ENTERPRISE_USER", scopePolicy.getRequiredPlanForAction("api.access"))
    }
    
    @Test
    fun `scope policy should return null for unknown action`() {
        // When
        val requiredPlan = scopePolicy.getRequiredPlanForAction("unknown.action")
        
        // Then
        assertNull(requiredPlan)
    }
    
    // ===== QUOTA POLICY TESTS =====
    
    @Test
    fun `quota policy should allow action when quota available`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val allowed = quotaPolicy.checkAndConsume("user123", "analysis.day")
        
        // Then
        assertTrue(allowed)
        assertEquals(4, quotaPolicy.getRemaining("user123", "analysis.day")) // 5 - 1 = 4
    }
    
    @Test
    fun `quota policy should deny action when quota exceeded`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When - zużyj cały quota
        repeat(5) {
            quotaPolicy.checkAndConsume("user123", "analysis.day")
        }
        
        // Then - próba użycia po wyczerpaniu
        val allowed = quotaPolicy.checkAndConsume("user123", "analysis.day")
        assertFalse(allowed)
        assertEquals(0, quotaPolicy.getRemaining("user123", "analysis.day"))
    }
    
    @Test
    fun `quota policy should handle multiple quota keys independently`() {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When
        quotaPolicy.checkAndConsume("user123", "analysis.day", 10) // zużyj 10 z 100
        quotaPolicy.checkAndConsume("user123", "memories.month", 50) // zużyj 50 z 1000
        
        // Then
        assertEquals(90, quotaPolicy.getRemaining("user123", "analysis.day"))
        assertEquals(950, quotaPolicy.getRemaining("user123", "memories.month"))
    }
    
    @Test
    fun `quota policy should return correct remaining quota`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When
        val initialRemaining = quotaPolicy.getRemaining("user123", "analysis.day")
        quotaPolicy.checkAndConsume("user123", "analysis.day", 2)
        val afterConsumption = quotaPolicy.getRemaining("user123", "analysis.day")
        
        // Then
        assertEquals(5, initialRemaining)
        assertEquals(3, afterConsumption)
    }
    
    @Test
    fun `quota policy should reset quota correctly`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        quotaPolicy.checkAndConsume("user123", "analysis.day", 3)
        
        // When
        val beforeReset = quotaPolicy.getRemaining("user123", "analysis.day")
        quotaPolicy.resetQuota("user123", "analysis.day")
        val afterReset = quotaPolicy.getRemaining("user123", "analysis.day")
        
        // Then
        assertEquals(2, beforeReset)
        assertEquals(5, afterReset) // zresetowane do limitu planu
    }
    
    @Test
    fun `quota policy should return correct quota info`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        quotaPolicy.checkAndConsume("user123", "analysis.day", 2)
        
        // When
        val quotaInfo = quotaPolicy.getQuotaInfo("user123", "analysis.day")
        
        // Then
        assertNotNull(quotaInfo)
        assertEquals("analysis.day", quotaInfo.key)
        assertEquals(2, quotaInfo.current) // zużyte
        assertEquals(5, quotaInfo.limit) // limit planu
        assertEquals(ResetType.DAILY, quotaInfo.resetType)
    }
    
    @Test
    fun `quota policy should handle different reset types correctly`() {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When & Then
        val dailyQuota = quotaPolicy.getQuotaInfo("user123", "analysis.day")
        val monthlyQuota = quotaPolicy.getQuotaInfo("user123", "memories.month")
        
        assertEquals(ResetType.DAILY, dailyQuota?.resetType)
        assertEquals(ResetType.MONTHLY, monthlyQuota?.resetType)
    }
    
    // ===== FEATURE TOGGLE TESTS =====
    
    @Test
    fun `feature toggle should return correct feature status`() {
        // When & Then
        assertTrue(featureToggle.isOn("feature.memories"))
        assertTrue(featureToggle.isOn("feature.analysis"))
        assertTrue(featureToggle.isOn("feature.patterns"))
        assertFalse(featureToggle.isOn("emergency.analysis.off"))
        assertFalse(featureToggle.isOn("emergency.sharing.off"))
    }
    
    @Test
    fun `feature toggle should return null for unknown feature`() {
        // When
        val featureInfo = featureToggle.getFeatureInfo("unknown.feature")
        
        // Then
        assertNull(featureInfo)
    }
    
    @Test
    fun `feature toggle should return correct feature info`() {
        // When
        val analysisInfo = featureToggle.getFeatureInfo("feature.analysis")
        val emergencyInfo = featureToggle.getFeatureInfo("emergency.analysis.off")
        
        // Then
        assertNotNull(analysisInfo)
        assertEquals("feature.analysis", analysisInfo.key)
        assertTrue(analysisInfo.enabled)
        assertEquals("Analiza wspomnień", analysisInfo.description)
        
        assertNotNull(emergencyInfo)
        assertEquals("emergency.analysis.off", emergencyInfo.key)
        assertFalse(emergencyInfo.enabled)
        assertEquals("Emergency: wyłączenie analizy", emergencyInfo.description)
    }
    
    @Test
    fun `feature toggle should return all features`() {
        // When
        val allFeatures = featureToggle.getAllFeatures()
        
        // Then
        assertTrue(allFeatures.size >= 12) // wszystkie zdefiniowane features
        assertTrue(allFeatures.containsKey("feature.memories"))
        assertTrue(allFeatures.containsKey("feature.analysis"))
        assertTrue(allFeatures.containsKey("emergency.analysis.off"))
    }
    
    @Test
    fun `feature toggle should allow setting features`() {
        // Given
        val testFeature = "test.feature"
        
        // When
        featureToggle.setFeature(testFeature, true)
        val enabled = featureToggle.isOn(testFeature)
        
        // Then
        assertTrue(enabled)
    }
    
    @Test
    fun `feature toggle should allow toggling features`() {
        // Given
        val testFeature = "test.feature"
        featureToggle.setFeature(testFeature, false)
        
        // When
        featureToggle.toggleFeature(testFeature)
        val enabled = featureToggle.isOn(testFeature)
        
        // Then
        assertTrue(enabled)
        
        // When - toggle again
        featureToggle.toggleFeature(testFeature)
        val disabled = featureToggle.isOn(testFeature)
        
        // Then
        assertFalse(disabled)
    }
    
    // ===== INTEGRATION TESTS =====
    
    @Test
    fun `policies should work together correctly`() {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When - sprawdź scope i quota
        val hasScope = scopePolicy.hasScope("user123", "analysis.run.patterns")
        val quotaRemaining = quotaPolicy.getRemaining("user123", "analysis.patterns.day")
        val featureEnabled = featureToggle.isOn("feature.patterns")
        
        // Then
        assertTrue(hasScope)
        assertEquals(20, quotaRemaining) // limit z planu PREMIUM
        assertTrue(featureEnabled)
    }
    
    @Test
    fun `quota policy should respect plan limits`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When - spróbuj zużyć więcej niż limit planu
        val allowed = quotaPolicy.checkAndConsume("user123", "memories.month", 100)
        
        // Then
        assertFalse(allowed) // FREE_USER ma limit 50 wspomnień
        assertEquals(50, quotaPolicy.getRemaining("user123", "memories.month"))
    }
    
    @Test
    fun `scope policy should handle plan changes`() {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        
        // When - sprawdź scope dla FREE_USER
        val canCreateMemory = scopePolicy.hasScope("user123", "memory.create")
        val canRunPatterns = scopePolicy.hasScope("user123", "analysis.run.patterns")
        
        // Then
        assertTrue(canCreateMemory)
        assertFalse(canRunPatterns)
        
        // When - zmień plan na PREMIUM
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        val canRunPatternsAfterUpgrade = scopePolicy.hasScope("user123", "analysis.run.patterns")
        
        // Then
        assertTrue(canRunPatternsAfterUpgrade)
    }
    
    @Test
    fun `feature toggle should work with emergency flags`() {
        // Given
        val emergencyFeature = "emergency.analysis.off"
        
        // When - włącz emergency flag
        featureToggle.setFeature(emergencyFeature, true)
        
        // Then
        assertTrue(featureToggle.isOn(emergencyFeature))
        
        // When - wyłącz emergency flag
        featureToggle.setFeature(emergencyFeature, false)
        
        // Then
        assertFalse(featureToggle.isOn(emergencyFeature))
    }
}
