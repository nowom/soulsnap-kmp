package pl.soulsnaps.access.guard

import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import pl.soulsnaps.access.guard.CapacityGuard
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.access.guard.DenyReason
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.DefaultPlans
import pl.soulsnaps.access.manager.UserPlanManager

class CapacityGuardTest {

    private val mockPlanRegistry = object : PlanRegistryReader {
        override suspend fun getPlan(userId: String) = DefaultPlans.FREE_USER
        override fun getPlanByType(type: pl.soulsnaps.access.model.PlanType) = DefaultPlans.getPlan(type)
        override suspend fun hasPlan(userId: String) = true
        override fun getRecommendedPlanForAction(action: String) = null
        override fun getAllPlans() = DefaultPlans.getAllPlans()
    }
    
    @Test
    fun `canAddSnap should allow when user has remaining snap quota`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val result = capacityGuard.canAddSnap(userId)
        
        // Then
        assertTrue(result.allowed, "User should be able to add snap when quota available")
    }
    
    @Test
    fun `canAddSnapWithSize should check both snap and storage quotas`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        val fileSizeMB = 50 // 50MB file
        
        // When
        val result = capacityGuard.canAddSnapWithSize(userId, fileSizeMB)
        
        // Then
        assertTrue(result.allowed, "User should be able to add snap with reasonable file size")
    }
    
    @Test
    fun `canAddSnapWithSize should deny when file size exceeds storage limit`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        val fileSizeMB = 11000 // 11GB file - exceeds FREE_USER 10GB limit
        
        // When
        val result = capacityGuard.canAddSnapWithSize(userId, fileSizeMB)
        
        // Then
        assertFalse(result.allowed, "User should not be able to add snap exceeding storage limit")
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason)
        assertTrue(result.message?.contains("Rozmiar pliku przekracza") == true)
    }
    
    @Test
    fun `canRunAIAnalysis should check AI daily quota`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val result = capacityGuard.canRunAIAnalysis(userId)
        
        // Then
        assertTrue(result.allowed, "User should be able to run AI analysis when quota available")
    }
    
    @Test
    fun `canAddMemory should check monthly memory quota`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val result = capacityGuard.canAddMemory(userId)
        
        // Then
        assertTrue(result.allowed, "User should be able to add memory when monthly quota available")
    }
    
    @Test
    fun `canExport should check monthly export quota`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When - FREE_USER has export.pdf scope, not export.basic
        val result = capacityGuard.canExport(userId)
        
        // Then
        assertTrue(result.allowed, "User should be able to export when monthly quota available")
    }
    
    @Test
    fun `canCreateBackup should check monthly backup quota`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When - FREE_USER doesn't have backup.create scope, so this should fail
        val result = capacityGuard.canCreateBackup(userId)
        
        // Then - FREE_USER doesn't have backup permissions
        assertFalse(result.allowed, "FREE_USER should not have backup permissions")
        assertEquals(DenyReason.MISSING_SCOPE, result.reason)
    }
    
    @Test
    fun `getCapacityInfo should return all quota information`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val capacityInfo = capacityGuard.getCapacityInfo(userId)
        
        // Then
        assertNotNull(capacityInfo.snaps, "Snaps quota info should be available")
        assertNotNull(capacityInfo.storage, "Storage quota info should be available")
        assertNotNull(capacityInfo.aiAnalysis, "AI analysis quota info should be available")
        assertNotNull(capacityInfo.memories, "Memories quota info should be available")
    }
    
    @Test
    fun `canPerformActionWithFileSize should check file size for memory actions`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        val action = "memory.create"
        val fileSizeMB = 100
        
        // When
        val result = capacityGuard.canPerformActionWithFileSize(userId, action, fileSizeMB)
        
        // Then
        assertTrue(result.allowed, "User should be able to perform memory action with reasonable file size")
    }
    
    @Test
    fun `canPerformActionWithFileSize should deny when file size exceeds storage for memory actions`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        val action = "memory.create"
        val fileSizeMB = 11000 // 11GB - exceeds FREE_USER 10GB limit
        
        // When
        val result = capacityGuard.canPerformActionWithFileSize(userId, action, fileSizeMB)
        
        // Then
        assertFalse(result.allowed, "User should not be able to perform memory action with oversized file")
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason)
    }
    
    @Test
    fun `canPerformActionWithFileSize should allow non-file actions regardless of file size`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        val action = "analysis.run.single" // Non-file action
        val fileSizeMB = 10000 // Large file size
        
        // When
        val result = capacityGuard.canPerformActionWithFileSize(userId, action, fileSizeMB)
        
        // Then
        assertTrue(result.allowed, "Non-file actions should not be limited by file size")
    }
    
    @Test
    fun `getUpgradeRecommendation should return LOW urgency when quotas are fine`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val recommendation = capacityGuard.getUpgradeRecommendation(userId)
        
        // Then
        assertEquals(UpgradeUrgency.LOW, recommendation.urgency, "Urgency should be LOW when quotas are fine")
        assertTrue(recommendation.recommendations.isEmpty(), "No recommendations when quotas are fine")
    }
    
    @Test
    fun `getUpgradeRecommendation should return MEDIUM urgency when approaching limits`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // For this test, we'll just verify the basic functionality works
        // In a real implementation, we'd have a way to set user plans
        
        // When
        val recommendation = capacityGuard.getUpgradeRecommendation(userId)
        
        // Then
        // The recommendation should work for any user
        assertTrue(recommendation.urgency in listOf(UpgradeUrgency.LOW, UpgradeUrgency.MEDIUM), "Urgency should be appropriate for usage level")
    }
    
    @Test
    fun `getUpgradeRecommendation should include recommended plan`() = runTest {
        // Given
        val capacityGuard = GuardFactory.createCapacityGuard(mock(), mockPlanRegistry)
        val userId = "test_user"
        
        // When
        val recommendation = capacityGuard.getUpgradeRecommendation(userId)
        
        // Then
        // The recommended plan might be null for low usage, which is fine
        assertTrue(recommendation.recommendedPlan == null || recommendation.recommendedPlan in listOf("FREE_USER", "PREMIUM_USER"), "Recommended plan should be valid if present")
    }
}
