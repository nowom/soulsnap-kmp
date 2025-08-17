package pl.soulsnaps.features.memoryanalysis.service

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.features.auth.mvp.guard.*
import pl.soulsnaps.features.memoryanalysis.analyzer.ImageAnalyzer
import pl.soulsnaps.features.memoryanalysis.engine.PatternDetectionEngine
import pl.soulsnaps.features.memoryanalysis.model.*

/**
 * Testy dla MemoryAnalysisService - integracja z AccessGuard
 */
class MemoryAnalysisServiceTest {
    
    private lateinit var imageAnalyzer: MockImageAnalyzer
    private lateinit var patternDetectionEngine: MockPatternDetectionEngine
    private lateinit var guard: AccessGuard
    private lateinit var service: MemoryAnalysisServiceSOLID
    private lateinit var scopePolicy: InMemoryScopePolicy
    private lateinit var quotaPolicy: InMemoryQuotaPolicy
    private lateinit var featureToggle: InMemoryFeatureToggle
    
    @BeforeTest
    fun setup() {
        imageAnalyzer = MockImageAnalyzer()
        patternDetectionEngine = MockPatternDetectionEngine()
        
        val planRegistry = DefaultPlans
        scopePolicy = InMemoryScopePolicy(planRegistry)
        quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        featureToggle = InMemoryFeatureToggle()
        guard = AccessGuard(scopePolicy, quotaPolicy, featureToggle)
        
        service = MemoryAnalysisServiceSOLID(
            imageAnalyzer = imageAnalyzer,
            patternDetectionEngine = patternDetectionEngine,
            guard = guard
        )
    }
    
    // ===== ANALYZE MEMORY TESTS =====
    
    @Test
    fun `analyzeMemory should allow analysis for user with scope`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
    fun `analyzeMemory should deny analysis for user without scope`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Restricted)
        val restrictedResult = result as MemoryAnalysisResult.Restricted
        assertEquals("1", restrictedResult.memoryId)
        assertEquals("UPGRADE_PLAN", restrictedResult.requiredAction)
        assertEquals("PREMIUM_USER", restrictedResult.recommendedPlan)
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
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
            createTestMemory(1, "Memory 1", MoodType.HAPPY),
            createTestMemory(2, "Memory 2", MoodType.SAD)
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
            createTestMemory(1, "Memory 1", MoodType.HAPPY),
            createTestMemory(2, "Memory 2", MoodType.SAD)
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
            createTestMemory(1, "Memory 1", MoodType.HAPPY),
            createTestMemory(2, "Memory 2", MoodType.SAD)
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
            MockSharedImage("image1"),
            MockSharedImage("image2")
        )
        
        // When
        val result = service.analyzeImagesBatch(userId, images)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Success)
        val successResult = result as MemoriesAnalysisResult.Success
        assertNotNull(successResult.imageAnalyses)
        assertTrue(successResult.processingTime >= 0)
    }
    
    @Test
    fun `analyzeImagesBatch should deny batch analysis for basic user`() = runTest {
        // Given
        val userId = "basic_user"
        scopePolicy.setUserPlan(userId, "BASIC_USER")
        val images = listOf(
            MockSharedImage("image1"),
            MockSharedImage("image2")
        )
        
        // When
        val result = service.analyzeImagesBatch(userId, images)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Restricted)
        val restrictedResult = result as MemoriesAnalysisResult.Restricted
        assertEquals("UPGRADE_PLAN", restrictedResult.requiredAction)
    }
    
    // ===== ANALYSIS CAPABILITIES TESTS =====
    
    @Test
    fun `getAnalysisCapabilities should return correct capabilities for user`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        
        // When
        val capabilities = service.getAnalysisCapabilities(userId)
        
        // Then
        assertTrue(capabilities.basicAnalysis)
        assertTrue(capabilities.patternAnalysis)
        assertFalse(capabilities.batchAnalysis) // Premium nie ma batch
        assertTrue(capabilities.insightsAccess)
        assertTrue(capabilities.exportAccess)
    }
    
    @Test
    fun `getAnalysisCapabilities should return limited capabilities for free user`() = runTest {
        // Given
        val userId = "free_user"
        scopePolicy.setUserPlan(userId, "FREE_USER")
        
        // When
        val capabilities = service.getAnalysisCapabilities(userId)
        
        // Then
        assertTrue(capabilities.basicAnalysis)
        assertFalse(capabilities.patternAnalysis)
        assertFalse(capabilities.batchAnalysis)
        assertFalse(capabilities.insightsAccess)
        assertFalse(capabilities.exportAccess)
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
        assertEquals(1000, quotaStatus["memories.month"])
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
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
        // Simulate error in pattern detection engine
        patternDetectionEngine.shouldThrowError = true
        
        // When
        val result = service.analyzeMemory(userId, memory)
        
        // Then
        assertTrue(result is MemoryAnalysisResult.Error)
        val errorResult = result as MemoryAnalysisResult.Error
        assertEquals("1", errorResult.memoryId)
        assertNotNull(errorResult.error)
        assertTrue(errorResult.processingTime >= 0)
    }
    
    @Test
    fun `analyzeMemories should handle exceptions gracefully`() = runTest {
        // Given
        val userId = "premium_user"
        scopePolicy.setUserPlan(userId, "PREMIUM_USER")
        val memories = listOf(
            createTestMemory(1, "Memory 1", MoodType.HAPPY)
        )
        
        // Simulate error in pattern detection engine
        patternDetectionEngine.shouldThrowError = true
        
        // When
        val result = service.analyzeMemories(userId, memories)
        
        // Then
        assertTrue(result is MemoriesAnalysisResult.Error)
        val errorResult = result as MemoriesAnalysisResult.Error
        assertNotNull(errorResult.error)
        assertTrue(errorResult.processingTime >= 0)
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
        
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
        val memory = createTestMemory(1, "Test memory", MoodType.HAPPY)
        
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
    
    private fun createTestMemory(id: Int, description: String, mood: MoodType): Memory {
        return Memory(
            id = id,
            createdAt = System.currentTimeMillis(),
            locationName = "Test Location",
            mood = mood,
            description = description
        )
    }
}

// Mock implementations for testing
class MockImageAnalyzer : ImageAnalyzer {
    override suspend fun analyzeImage(image: SharedImage): ImageAnalysis {
        return ImageAnalysis(
            dominantColors = listOf(ColorAnalysis("red", 0.5f)),
            brightness = 0.7f,
            contrast = 0.6f,
            faces = emptyList(),
            mood = MoodAnalysis("HAPPY", 0.8f, emptyList()),
            composition = CompositionAnalysis(0.7f, 0.6f, 0.5f),
            metadata = ImageMetadata(1920, 1080, "JPEG", null, null)
        )
    }
    
    override suspend fun analyzeColors(image: SharedImage): List<ColorAnalysis> = emptyList()
    override suspend fun detectFaces(image: SharedImage): List<FaceDetection> = emptyList()
    override suspend fun analyzeMood(image: SharedImage): MoodAnalysis? = null
    override suspend fun analyzeComposition(image: SharedImage): CompositionAnalysis? = null
    override suspend fun getDominantColors(image: SharedImage, count: Int): List<ColorAnalysis> = emptyList()
    override fun isAnalysisAvailable(): Boolean = true
    override fun getSupportedFeatures(): List<AnalysisFeature> = listOf(AnalysisFeature.COLOR_ANALYSIS)
}

class MockPatternDetectionEngine : PatternDetectionEngine {
    var shouldThrowError = false
    
    override fun detectPatterns(memories: List<Memory>): MemoryPatterns {
        if (shouldThrowError) throw RuntimeException("Test error")
        
        return MemoryPatterns(
            locationPatterns = emptyList(),
            timePatterns = emptyList(),
            activityPatterns = emptyList(),
            moodPatterns = emptyList()
        )
    }
    
    override fun generateInsights(memories: List<Memory>): MemoryInsights {
        if (shouldThrowError) throw RuntimeException("Test error")
        
        return MemoryInsights(
            weeklyStats = emptyList(),
            monthlyTrends = emptyList(),
            recommendations = emptyList()
        )
    }
}

class MockSharedImage(private val id: String) : SharedImage {
    override fun getId(): String = id
    override fun getSize(): Long = 1024L
    override fun getFormat(): String = "JPEG"
}
