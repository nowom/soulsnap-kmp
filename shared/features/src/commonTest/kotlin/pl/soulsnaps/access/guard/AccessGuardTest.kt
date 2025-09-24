package pl.soulsnaps.access.guard

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.access.guard.AccessGuard
import pl.soulsnaps.access.guard.InMemoryScopePolicy
import pl.soulsnaps.access.guard.InMemoryQuotaPolicy
import pl.soulsnaps.access.guard.InMemoryFeatureToggle
import pl.soulsnaps.access.manager.DefaultPlans
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl
import pl.soulsnaps.access.manager.PlanDefinition
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.model.PlanType
import pl.soulsnaps.access.guard.DenyReason

/**
 * Testy dla AccessGuard - Dependency Inversion Principle
 */
class AccessGuardTest {
    
    private lateinit var scopePolicy: InMemoryScopePolicy
    private lateinit var quotaPolicy: InMemoryQuotaPolicy
    private lateinit var featureToggle: InMemoryFeatureToggle
    private lateinit var guard: AccessGuard
    private lateinit var planRegistry: PlanRegistryReader
    private lateinit var userPlanManager: MockUserPlanManager
    
    @BeforeTest
    fun setup() {
        planRegistry = MockPlanRegistryReader()
        scopePolicy = InMemoryScopePolicy(planRegistry)
        quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        featureToggle = InMemoryFeatureToggle()
        userPlanManager = MockUserPlanManager()
        guard = AccessGuard(scopePolicy, quotaPolicy, featureToggle, userPlanManager)
    }
    
    // ===== BASIC ACCESS CONTROL TESTS =====
    
    @Test
    fun `guard should allow action when all conditions are met`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity",
            flagKey = "feature.memories"
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
        featureToggle.setFeature("feature.memories", false)
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "memory.create",
            flagKey = "feature.memories"
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
            action = "ai.analysis", // This requires PREMIUM_USER scope
            quotaKey = "ai.analysis",
            flagKey = "feature.ai"
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
        // Zużyj cały quota dla snaps.capacity (który istnieje w FREE_USER plan)
        repeat(50) {
            quotaPolicy.checkAndConsume("user123", "snaps.capacity")
        }
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity",
            flagKey = "feature.memories"
        )
        
        // Then
        assertFalse(result.allowed)
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason)
        assertTrue(result.message?.contains("Limit") == true)
        assertNotNull(result.quotaInfo)
    }
    
    // ===== QUOTA CONSUMPTION TESTS =====
    
    @Test
    fun `guard should consume quota when action is allowed`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        
        // When
        val result = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
        
        // Then
        assertTrue(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        assertEquals(initialQuota - 1, remainingQuota)
    }
    
    @Test
    fun `guard should not consume quota when action is denied`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        
        // When - próba akcji bez scope
        val result = guard.allowAction(
            userId = "user123",
            action = "ai.analysis", // brak scope w FREE_USER
            quotaKey = "snaps.capacity"
        )
        
        // Then
        assertFalse(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        assertEquals(initialQuota, remainingQuota) // quota nie zostało skonsumowane
    }
    
    // ===== CAN PERFORM ACTION TESTS =====
    
    @Test
    fun `canPerformAction should check without consuming quota`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        
        // When
        val result = guard.canPerformAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
        
        // Then
        assertTrue(result.allowed)
        val remainingQuota = quotaPolicy.getRemaining("user123", "snaps.capacity")
        assertEquals(initialQuota, remainingQuota) // quota nie zostało skonsumowane
    }
    
    @Test
    fun `canPerformAction should deny when quota is exceeded`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        // Zużyj cały quota
        repeat(50) {
            quotaPolicy.checkAndConsume("user123", "snaps.capacity")
        }
        
        // When
        val result = guard.canPerformAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity"
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
        assertEquals("PREMIUM_USER", guard.getUpgradeRecommendation("ai.analysis"))
        assertEquals("PREMIUM_USER", guard.getUpgradeRecommendation("export.data"))
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
        quotaPolicy.checkAndConsume("user123", "snaps.capacity", 2)
        
        // When
        val remaining = guard.getQuotaStatus("user123", "snaps.capacity")
        
        // Then
        assertEquals(48, remaining) // 50 - 2 = 48
    }
    
    @Test
    fun `getQuotaInfo should return correct quota information`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "FREE_USER")
        quotaPolicy.checkAndConsume("user123", "snaps.capacity", 2)
        
        // When
        val quotaInfo = guard.getQuotaInfo("user123", "snaps.capacity")
        
        // Then
        assertNotNull(quotaInfo)
        assertEquals("snaps.capacity", quotaInfo.key)
        assertEquals(2, quotaInfo.current)
        assertEquals(50, quotaInfo.limit)
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
        assertTrue(scopes.contains("view.*"))
        assertTrue(scopes.contains("export.*"))
        assertTrue(scopes.contains("ai.*"))
        
        // Sprawdź czy wildcard scope pasuje do konkretnych akcji
        assertTrue(scopePolicy.hasScope("user123", "memory.create")) // memory.* powinno pasować
        assertTrue(scopePolicy.hasScope("user123", "memory.read"))   // memory.* powinno pasować
    }
    
    @Test
    fun `getUserScopes should return default scopes for unknown user`() = runTest {
        // When
        val scopes = guard.getUserScopes("unknown_user")
        
        // Then
        assertTrue(scopes.contains("memory.basic"))
        assertTrue(scopes.contains("view.basic"))
        assertFalse(scopes.contains("ai.*"))
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
        val memoryResult = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
        
        val exportResult = guard.allowAction(
            userId = "user123",
            action = "export.data",
            quotaKey = "export.data"
        )
        
        // Then
        assertTrue(memoryResult.allowed)
        assertTrue(exportResult.allowed)
        
        // Sprawdź czy quota zostały skonsumowane niezależnie
        assertEquals(Int.MAX_VALUE, quotaPolicy.getRemaining("user123", "snaps.capacity")) // unlimited
        assertEquals(Int.MAX_VALUE, quotaPolicy.getRemaining("user123", "export.data")) // unlimited
    }
    
    @Test
    fun `guard should handle feature flag changes dynamically`() = runTest {
        // Given
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        
        // When - feature włączony
        val result1 = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            flagKey = "feature.memories"
        )
        
        // Then
        assertTrue(result1.allowed)
        
        // When - wyłącz feature
        featureToggle.setFeature("feature.memories", false)
        val result2 = guard.allowAction(
            userId = "user123",
            action = "memory.create",
            flagKey = "feature.memories"
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
            action = "ai.analysis",
            flagKey = "feature.ai"
        )
        
        // Then
        assertFalse(resultBefore.allowed)
        assertEquals(DenyReason.MISSING_SCOPE, resultBefore.reason)
        
        // When - upgrade plan
        scopePolicy.setUserPlan("user123", "PREMIUM_USER")
        val resultAfter = guard.allowAction(
            userId = "user123",
            action = "ai.analysis",
            flagKey = "feature.ai"
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
            action = "memory.create",
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
            quotaKey = "snaps.capacity" // bez flag key
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

// Mock implementation for testing
class MockPlanRegistryReader : PlanRegistryReader {
    private val userPlans = mutableMapOf<String, PlanDefinition>()
    
    fun setUserPlan(userId: String, planType: String) {
        val plan = when (planType) {
            "GUEST" -> DefaultPlans.GUEST
            "FREE_USER" -> DefaultPlans.FREE_USER
            "PREMIUM_USER" -> DefaultPlans.PREMIUM_USER
            "ENTERPRISE_USER" -> DefaultPlans.PREMIUM_USER // Fallback to premium
            else -> DefaultPlans.GUEST
        }
        userPlans[userId] = plan
    }
    
    override suspend fun getPlan(userId: String): PlanDefinition? {
        return userPlans[userId] ?: DefaultPlans.GUEST
    }
    
    override fun getPlanByType(type: PlanType): PlanDefinition {
        return DefaultPlans.getPlan(type)
    }
    
    override suspend fun hasPlan(userId: String): Boolean {
        return userPlans.containsKey(userId)
    }
    
    override fun getRecommendedPlanForAction(action: String): PlanType? {
        return when {
            action.startsWith("ai.") -> PlanType.PREMIUM_USER
            action.startsWith("export.") -> PlanType.PREMIUM_USER
            action.startsWith("memory.") -> PlanType.FREE_USER
            else -> null
        }
    }
    
    override fun getAllPlans(): List<PlanDefinition> {
        return DefaultPlans.getAllPlans()
    }
}

// Mock UserPlanManager for testing
class MockUserPlanManager : UserPlanManagerInterface {
    private var currentPlan: String = "GUEST"
    private var onboardingCompleted: Boolean = false
    
    override fun setUserPlan(planName: String) {
        currentPlan = planName
        onboardingCompleted = true
    }
    
    override fun getUserPlan(): String? {
        return currentPlan
    }
    
    override fun isOnboardingCompleted(): Boolean {
        return onboardingCompleted
    }
    
    override fun getPlanOrDefault(): String {
        return currentPlan
    }
    
    override fun hasPlanSet(): Boolean {
        return currentPlan != "GUEST"
    }
    
    override fun getCurrentPlan(): String? {
        return currentPlan
    }
}


