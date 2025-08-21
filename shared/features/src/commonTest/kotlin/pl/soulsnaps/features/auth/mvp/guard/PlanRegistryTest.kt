package pl.soulsnaps.features.auth.mvp.guard

import kotlin.test.*

/**
 * Testy dla PlanRegistry - SOLID: Single Responsibility
 */
class PlanRegistryTest {
    
    @Test
    fun `getPlan should return correct plan for valid plan name`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val freePlan = planRegistry.getPlan("FREE_USER")
        val premiumPlan = planRegistry.getPlan("PREMIUM_USER")
        
        // Then
        assertNotNull(freePlan)
        assertNotNull(premiumPlan)
        assertTrue(freePlan.scopes.contains("memory.create"), "FREE_USER should contain memory.create scope")
        assertTrue(premiumPlan.scopes.contains("analysis.run.patterns"), "PREMIUM_USER should contain analysis.run.patterns scope")
    }
    
    @Test
    fun `getPlan should return null for invalid plan name`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val invalidPlan = planRegistry.getPlan("INVALID_PLAN")
        
        // Then
        assertNull(invalidPlan)
    }
    
    @Test
    fun `getAllPlans should return all available plans`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val allPlans = planRegistry.getAllPlans()
        
        // Then
        assertTrue(allPlans.size >= 6) // FREE, BASIC, PREMIUM, FAMILY, ENTERPRISE, LIFETIME
        assertTrue(allPlans.contains("FREE_USER"))
        assertTrue(allPlans.contains("PREMIUM_USER"))
        assertTrue(allPlans.contains("ENTERPRISE_USER"))
    }
    
    @Test
    fun `getRecommendedPlanForAction should return correct plan for basic action`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val planForMemoryCreate = planRegistry.getRecommendedPlanForAction("memory.create")
        val planForAnalysis = planRegistry.getRecommendedPlanForAction("analysis.run.single")
        
        // Then
        assertEquals("FREE_USER", planForMemoryCreate)
        assertEquals("FREE_USER", planForAnalysis)
    }
    
    @Test
    fun `getRecommendedPlanForAction should return correct plan for premium action`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val planForPatterns = planRegistry.getRecommendedPlanForAction("analysis.run.patterns")
        val planForSharing = planRegistry.getRecommendedPlanForAction("sharing.basic")
        
        // Then
        assertEquals("PREMIUM_USER", planForPatterns)
        assertEquals("PREMIUM_USER", planForSharing)
    }
    
    @Test
    fun `getRecommendedPlanForAction should return correct plan for enterprise action`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val planForApi = planRegistry.getRecommendedPlanForAction("api.access")
        val planForAdvancedAI = planRegistry.getRecommendedPlanForAction("advanced_ai.patterns")
        
        // Then
        assertEquals("ENTERPRISE_USER", planForApi)
        assertEquals("ENTERPRISE_USER", planForAdvancedAI)
    }
    
    @Test
    fun `getRecommendedPlanForAction should handle wildcard scopes correctly`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val planForMemoryRead = planRegistry.getRecommendedPlanForAction("memory.read")
        val planForMemoryDelete = planRegistry.getRecommendedPlanForAction("memory.delete")
        val planForAnalysisBatch = planRegistry.getRecommendedPlanForAction("analysis.run.batch")
        
        // Then
        // memory.read jest w FREE_USER, ale memory.* w BASIC_USER powinno pasować
        // Jednak FREE_USER ma pierwszeństwo bo ma dokładne dopasowanie
        assertEquals("FREE_USER", planForMemoryRead) // Dokładne dopasowanie w FREE_USER
        assertEquals("BASIC_USER", planForMemoryDelete) // memory.* scope w BASIC_USER
        assertEquals("ENTERPRISE_USER", planForAnalysisBatch) // analysis.run.* scope
    }
    
    @Test
    fun `getPlanFeatures should return correct features for plan`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val freeFeatures = planRegistry.getPlanFeatures("FREE_USER")
        val premiumFeatures = planRegistry.getPlanFeatures("PREMIUM_USER")
        
        // Then
        assertTrue(freeFeatures["basic_insights"] == true)
        assertTrue(freeFeatures["photo_analysis"] == false)
        assertTrue(freeFeatures["pattern_detection"] == false)
        
        assertTrue(premiumFeatures["basic_insights"] == true)
        assertTrue(premiumFeatures["photo_analysis"] == true)
        assertTrue(premiumFeatures["pattern_detection"] == true)
        assertTrue(premiumFeatures["advanced_ai"] == false)
    }
    
    @Test
    fun `getPlanPricing should return correct pricing for plan`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val freePricing = planRegistry.getPlanPricing("FREE_USER")
        val premiumPricing = planRegistry.getPlanPricing("PREMIUM_USER")
        val lifetimePricing = planRegistry.getPlanPricing("LIFETIME")
        
        // Then
        assertEquals(0.0, freePricing?.monthlyPrice)
        assertEquals(19.99, premiumPricing?.monthlyPrice)
        assertEquals(199.99, premiumPricing?.yearlyPrice)
        assertEquals(299.99, lifetimePricing?.lifetimePrice)
        assertEquals("PLN", freePricing?.currency)
    }
    
    @Test
    fun `getPlanQuotas should return correct quotas for plan`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val freeQuotas = planRegistry.getPlanQuotas("FREE_USER")
        val premiumQuotas = planRegistry.getPlanQuotas("PREMIUM_USER")
        
        // Then
        assertEquals(50, freeQuotas["memories.month"])
        assertEquals(5, freeQuotas["analysis.day"])
        
        assertEquals(1000, premiumQuotas["memories.month"])
        assertEquals(100, premiumQuotas["analysis.day"])
        assertEquals(20, premiumQuotas["analysis.patterns.day"])
    }
    
    @Test
    fun `plan definitions should have valid structure`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val allPlans = planRegistry.getAllPlans()
        
        // Then
        allPlans.forEach { planName ->
            val plan = planRegistry.getPlan(planName)
            assertNotNull(plan, "Plan $planName should exist")
            assertTrue(plan.scopes.isNotEmpty(), "Plan $planName should have scopes")
            assertTrue(plan.quotas.isNotEmpty(), "Plan $planName should have quotas")
            
            // Sprawdź czy wszystkie scopes są unikalne
            assertEquals(plan.scopes.size, plan.scopes.toSet().size, "Plan $planName should have unique scopes")
            
            // Sprawdź czy wszystkie quotas są dodatnie lub unlimited (-1)
            plan.quotas.values.forEach { quota ->
                assertTrue(quota > 0 || quota == -1, "Plan $planName should have positive quotas or unlimited (-1)")
            }
        }
    }
    
    @Test
    fun `plan hierarchy should be logical`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val freePlan = planRegistry.getPlan("FREE_USER")
        val basicPlan = planRegistry.getPlan("BASIC_USER")
        val premiumPlan = planRegistry.getPlan("PREMIUM_USER")
        
        // Then
        assertNotNull(freePlan, "FREE_USER plan should exist")
        assertNotNull(basicPlan, "BASIC_USER plan should exist")
        assertNotNull(premiumPlan, "PREMIUM_USER plan should exist")
        
        // FREE < BASIC < PREMIUM (ilość wspomnień)
        assertTrue(freePlan.quotas["memories.month"]!! < basicPlan.quotas["memories.month"]!!)
        assertTrue(basicPlan.quotas["memories.month"]!! < premiumPlan.quotas["memories.month"]!!)
        
        // FREE < BASIC < PREMIUM (ilość analiz)
        assertTrue(freePlan.quotas["analysis.day"]!! < basicPlan.quotas["analysis.day"]!!)
        assertTrue(basicPlan.quotas["analysis.day"]!! < premiumPlan.quotas["analysis.day"]!!)
    }
    
    @Test
    fun `enterprise plan should have highest limits`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val enterprisePlan = planRegistry.getPlan("ENTERPRISE_USER")
        
        // Then
        assertNotNull(enterprisePlan, "ENTERPRISE_USER plan should exist")
        
        assertTrue(enterprisePlan.quotas["memories.month"]!! >= 100000)
        assertTrue(enterprisePlan.quotas["analysis.day"]!! >= 10000)
        assertTrue(enterprisePlan.quotas["api.calls.month"]!! >= 100000)
        
        // Sprawdź czy ma wszystkie funkcje
        assertTrue(enterprisePlan.features["basic_insights"] == true)
        assertTrue(enterprisePlan.features["photo_analysis"] == true)
        assertTrue(enterprisePlan.features["pattern_detection"] == true)
        assertTrue(enterprisePlan.features["advanced_ai"] == true)
        assertTrue(enterprisePlan.features["api_access"] == true)
    }
    
    @Test
    fun `lifetime plan should have reasonable limits`() {
        // Given
        val planRegistry = DefaultPlans
        
        // When
        val lifetimePlan = planRegistry.getPlan("LIFETIME")
        
        // Then
        assertNotNull(lifetimePlan, "LIFETIME plan should exist")
        
        // Lifetime powinien mieć limity podobne do Premium
        assertTrue(lifetimePlan.quotas["memories.month"]!! >= 1000)
        assertTrue(lifetimePlan.quotas["analysis.day"]!! >= 100)
        
        // Ale nie tak wysokie jak Enterprise
        assertTrue(lifetimePlan.quotas["memories.month"]!! < 100000)
        assertTrue(lifetimePlan.quotas["analysis.day"]!! < 10000)
    }
}
