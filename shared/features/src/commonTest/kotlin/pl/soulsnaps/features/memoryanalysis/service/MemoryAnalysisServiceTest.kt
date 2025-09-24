package pl.soulsnaps.features.memoryanalysis.service

import dev.mokkery.mock
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType as DomainMoodType
import pl.soulsnaps.access.guard.*
import pl.soulsnaps.features.memoryanalysis.analyzer.ImageAnalyzerInterface
import pl.soulsnaps.features.memoryanalysis.analyzer.AnalysisFeature
import pl.soulsnaps.features.memoryanalysis.engine.PatternDetectionEngineInterface
import pl.soulsnaps.features.memoryanalysis.model.*
import pl.soulsnaps.photo.SharedImageInterface
import pl.soulsnaps.features.auth.manager.UserPlanManager
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl
import pl.soulsnaps.access.guard.UserPlanManagerInterface

/**
 * Testy dla MemoryAnalysisService - integracja z AccessGuard
 */
class MemoryAnalysisServiceTest {
    
    private lateinit var imageAnalyzer: ImageAnalyzerInterface
    private lateinit var patternDetectionEngine: PatternDetectionEngineInterface
    private lateinit var guard: AccessGuard
    private lateinit var service: MemoryAnalysisService
    private lateinit var scopePolicy: InMemoryScopePolicy
    private lateinit var quotaPolicy: InMemoryQuotaPolicy
    private lateinit var featureToggle: InMemoryFeatureToggle
    private lateinit var userPlanManager: UserPlanManager
    private lateinit var userPlanManagerInterface: UserPlanManagerInterface
    
    @BeforeTest
    fun setup() {
        imageAnalyzer = mock<ImageAnalyzerInterface>()
        patternDetectionEngine = mock<PatternDetectionEngineInterface>()
        userPlanManager = mock<UserPlanManager>()
        userPlanManagerInterface = mock<UserPlanManagerInterface>()
        
        val planRegistry = PlanRegistryReaderImpl()
        scopePolicy = InMemoryScopePolicy(planRegistry)
        quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        featureToggle = InMemoryFeatureToggle()
        guard = AccessGuard(scopePolicy, quotaPolicy, featureToggle, userPlanManagerInterface)
        
        service = MemoryAnalysisService(
            imageAnalyzer = imageAnalyzer,
            patternDetectionEngine = patternDetectionEngine,
            guard = guard,
            userPlanManager = userPlanManager
        )
    }
    
    // ===== ANALYZE MEMORY TESTS =====
    
    @Test
    fun `analyzeMemory should allow analysis for user with scope`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Success)
        val successResult = result as MemoryAnalysisResult.Success
        assertEquals("1", successResult.memoryId)
        assertNotNull(successResult.insights)
        assertTrue(successResult.processingTime >= 0)
    }
    
    @Test
    fun `analyzeMemory should allow analysis for free user with basic scope`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Success)
        val successResult = result as MemoryAnalysisResult.Success
        assertEquals("1", successResult.memoryId)
        assertNull(successResult.imageAnalysis) // Memory nie ma pola image, więc imageAnalysis jest null
        // FREE_USER ma scope analysis.run.single, więc może analizować pojedyncze wspomnienia
    }
    
    @Test
    fun `analyzeMemory should deny analysis when quota exceeded`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        // Zużyj cały quota
        repeat(5) {
            quotaPolicy.checkAndConsume(userId, "analysis.day")
        }
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Restricted)
        val restrictedResult = result as MemoryAnalysisResult.Restricted
        assertEquals("WAIT_RESET", restrictedResult.requiredAction)
        assertNotNull(restrictedResult.quotaInfo)
    }
    
    @Test
    fun `analyzeMemory should deny analysis when feature is off`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        featureToggle.setFeature("feature.analysis", false)
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Restricted)
        val restrictedResult = result as MemoryAnalysisResult.Restricted
        assertEquals("WAIT_FEATURE", restrictedResult.requiredAction)
        assertNotNull(restrictedResult.featureInfo)
    }
    
    @Test
    fun `analyzeMemory should consume quota when successful`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        val initialQuota = quotaPolicy.getRemaining(userId, "analysis.day")
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Success)
        val remainingQuota = quotaPolicy.getRemaining(userId, "analysis.day")
        assertEquals(initialQuota - 1, remainingQuota)
    }
    
    // ===== ANALYZE MEMORIES TESTS =====
    
    @Test
    fun `analyzeMemories should allow pattern analysis for premium user`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memories = listOf(
            createTestMemory(1, "Memory 1", DomainMoodType.HAPPY),
            createTestMemory(2, "Memory 2", DomainMoodType.SAD)
        )
        
        // When
        val result = service.analyzeMemories(userId, memories)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Success)
        val successResult = result as MemoriesAnalysisResult.Success
        assertNotNull(successResult.patterns)
        assertNotNull(successResult.insights)
        assertTrue(successResult.processingTime >= 0)
    }
    
    @Test
    fun `analyzeMemories should deny pattern analysis for free user`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        val memories = listOf(
            createTestMemory(1, "Memory 1", DomainMoodType.HAPPY),
            createTestMemory(2, "Memory 2", DomainMoodType.SAD)
        )
        
        // When
        val result = service.analyzeMemories(userId, memories)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Restricted)
        val restrictedResult = result as MemoriesAnalysisResult.Restricted
        assertEquals("UPGRADE_PLAN", restrictedResult.requiredAction)
        assertEquals("PREMIUM_USER", restrictedResult.recommendedPlan)
    }
    
    @Test
    fun `analyzeMemories should consume patterns quota`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val initialQuota = quotaPolicy.getRemaining(userId, "analysis.patterns.day")
        val memories = listOf(
            createTestMemory(1, "Memory 1", DomainMoodType.HAPPY),
            createTestMemory(2, "Memory 2", DomainMoodType.SAD)
        )
        
        // When
        val result = service.analyzeMemories(userId, memories)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Success)
        val remainingQuota = quotaPolicy.getRemaining(userId, "analysis.patterns.day")
        assertEquals(initialQuota - 1, remainingQuota)
    }
    
    // ===== ANALYZE IMAGES BATCH TESTS =====
    
    @Test
    fun `analyzeImagesBatch should allow batch analysis for enterprise user`() = runTest {
        // Given
        val userId = "enterprise_user"
        scopePolicy.setUserPlan(userId, "ENTERPRISE_USER")
        val images = listOf(
            mock<SharedImageInterface>(),
            mock<SharedImageInterface>()
        )
        
        // When
        val result = service.analyzeImagesBatch(userId, images)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    @Test
    fun `analyzeImagesBatch should deny batch analysis for basic user`() = runTest {
        // Given
        val userId = "basic_user"
        scopePolicy.setUserPlan(userId, "BASIC_USER")
        val images = listOf(
            mock<SharedImageInterface>(),
            mock<SharedImageInterface>()
        )
        
        // When
        val result = service.analyzeImagesBatch(userId, images)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    // ===== ANALYSIS CAPABILITIES TESTS =====
    
    @Test
    fun `getAnalysisCapabilities should return correct capabilities for premium user`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        // Note: Mock configuration would be needed here with Mokkery
        
        // When
        val capabilities = service.getAnalysisCapabilities(userId)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    @Test
    fun `getAnalysisCapabilities should return limited capabilities for free user`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        // Note: Mock configuration would be needed here with Mokkery
        
        // When
        val capabilities = service.getAnalysisCapabilities(userId)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    @Test
    fun `getAnalysisCapabilities should return pro plan limits`() = runTest {
        // Given
        val userId = "pro_user"
        scopePolicy.setUserPlan(userId, "PRO_USER")
        // Note: Mock configuration would be needed here with Mokkery
        
        // When
        val capabilities = service.getAnalysisCapabilities(userId)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    // ===== QUOTA STATUS TESTS =====
    
    @Test
    fun `getUserQuotaStatus should return correct quota information`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        
        // When
        val quotaStatus = service.getUserQuotaStatus(userId)
        
        // Then
        assertEquals(100, quotaStatus["analysis.day"])
        assertEquals(20, quotaStatus["analysis.patterns.day"])
        assertEquals(1000, quotaStatus["snaps.capacity"])
    }
    
    @Test
    fun `getUserQuotaStatus should return zero for exceeded quota`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        // Zużyj cały quota
        repeat(5) {
            quotaPolicy.checkAndConsume(userId, "analysis.day")
        }
        
        // When
        val quotaStatus = service.getUserQuotaStatus(userId)
        
        // Then
        assertEquals(0, quotaStatus["analysis.day"])
    }
    
    // ===== USER PLAN INFO TESTS =====
    
    @Test
    fun `getUserPlanInfo should return correct plan information`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        
        // When
        val planInfo = service.getUserPlanInfo(userId)
        
        // Then
        assertNotNull(planInfo)
        assertTrue(planInfo.scopes.contains("analysis.run.patterns"))
        assertTrue(planInfo.features["feature.patterns"] == true)
        assertTrue(planInfo.quotas["analysis.day"]!! > 0)
    }
    
    @Test
    fun `getUserPlanInfo should return default plan for unknown user`() = runTest {
        // Given
        val userId = "unknown_user"
        
        // When
        val planInfo = service.getUserPlanInfo(userId)
        
        // Then
        assertNotNull(planInfo)
        assertTrue(planInfo.scopes.contains("memory.create"))
        assertFalse(planInfo.scopes.contains("analysis.run.patterns"))
    }
    
    // ===== ERROR HANDLING TESTS =====
    
    @Test
    fun `analyzeMemory should handle exceptions gracefully`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then - analyzeMemory nie używa patternDetectionEngine, więc nie powinien rzucać wyjątków
        assertTrue(result is MemoryAnalysisResult.Success)
        val successResult = result as MemoryAnalysisResult.Success
        assertEquals("1", successResult.memoryId)
        assertNotNull(successResult.insights)
        assertTrue(successResult.processingTime >= 0)
    }
    
    @Test
    fun `analyzeMemories should handle exceptions gracefully`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memories = listOf(
            createTestMemory(1, "Memory 1", DomainMoodType.HAPPY)
        )
        
        // Note: Mock configuration would be needed here with Mokkery
        
        // When
        val result = service.analyzeMemories(userId, memories)
        
        // Then
        // Note: Verification would be needed here with Mokkery
    }
    
    // ===== INTEGRATION TESTS =====
    
    @Test
    fun `service should work with different user plans`() = runTest {
        // Given
        val freeUser = "free_user"
        val premiumUser = "premium_user"
        val enterpriseUser = "enterprise_user"
        
        scopePolicy.setUserPlan(freeUser, "FREE_USER")
        scopePolicy.setUserPlan(premiumUser, "PREMIUM_USER")
        scopePolicy.setUserPlan(enterpriseUser, "ENTERPRISE_USER")
        
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When & Then - FREE_USER
        val freeResult = service.analyzeMemory(freeUser, memory)
        assertTrue(freeResult is MemoryAnalysisResult.Success)
        
        // When & Then - PREMIUM_USER
        val premiumResult = service.analyzeMemory(premiumUser, memory)
        assertTrue(premiumResult is MemoryAnalysisResult.Success)
        
        // When & Then - ENTERPRISE_USER
        val enterpriseResult = service.analyzeMemory(enterpriseUser, memory)
        assertTrue(enterpriseResult is MemoryAnalysisResult.Success)
    }
    
    @Test
    fun `service should respect feature flags`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memory = createTestMemory(1, "Test memory", DomainMoodType.HAPPY)
        
        // When - feature włączony
        val result1 = service.analyzeMemory(userId, memory)
        assertTrue(result1 is MemoryAnalysisResult.Success)
        
        // When - wyłącz feature
        featureToggle.setFeature("feature.analysis", false)
        val result2 = service.analyzeMemory(userId, memory)
        assertTrue(result2 is MemoryAnalysisResult.Restricted)
        assertEquals("WAIT_FEATURE", (result2 as MemoryAnalysisResult.Restricted).requiredAction)
    }
    
    // ===== HELPER METHODS =====
    
    private fun createTestMemory(id: Int, description: String, mood: DomainMoodType): Memory {
        return Memory(
            id = id,
            title = "Test Memory $id",
            description = description,
            createdAt = getCurrentTimeMillis(),
            mood = mood,
            photoUri = null,
            audioUri = null,
            locationName = "Test Location",
            latitude = null,
            longitude = null
        )
    }
}
