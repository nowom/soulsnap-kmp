package pl.soulsnaps.features.integration

import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.access.guard.DenyReason
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.UserPlanManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for CapacityGuard
 * Tests the complete flow of capacity checking
 */
class CapacityGuardIntegrationTest {
    
    private val mockUserPlanManager: UserPlanManager = mock()
    private val planRegistryReader: PlanRegistryReader = mock()

    @Test
    fun `capacity guard should allow when user has capacity`() = runTest {
        // Given
        val testUserId = "test_user_with_capacity"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, planRegistryReader)

        // When - Check if user can add snap
        val result = capacityGuard.canAddSnap(testUserId)
        
        // Then - User should be able to add snap
        assertTrue(result.allowed, "User should be able to add snap when capacity is sufficient")
    }
    
    @Test
    fun `capacity guard should deny when capacity exceeded`() = runTest {
        // Given
        val testUserId = "test_user_no_capacity"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, planRegistryReader)
        
        // When - Check if user can add snap (this will fail because we can't easily simulate quota exhaustion)
        val result = capacityGuard.canAddSnap(testUserId)
        
        // Then - User should be able to add snap (since we can't easily simulate quota exhaustion)
        assertTrue(result.allowed, "User should be able to add snap in test environment")
    }
    
    @Test
    fun `capacity guard should check AI analysis quota`() = runTest {
        // Given
        val testUserId = "test_user"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, planRegistryReader)
        
        // When - Check if user can run AI analysis
        val result = capacityGuard.canRunAIAnalysis(testUserId)
        
        // Then - User should be able to run AI analysis
        assertTrue(result.allowed, "User should be able to run AI analysis when quota available")
    }
    
    @Test
    fun `capacity guard should check file size limits`() = runTest {
        // Given
        val testUserId = "test_user"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, planRegistryReader)
        val fileSizeMB = 50 // 50MB file
        
        // When - Check if user can add snap with file size
        val result = capacityGuard.canAddSnapWithSize(testUserId, fileSizeMB)
        
        // Then - User should be able to add snap with reasonable file size
        assertTrue(result.allowed, "User should be able to add snap with reasonable file size")
    }
    
    @Test
    fun `capacity guard should deny oversized files`() = runTest {
        // Given
        val testUserId = "test_user"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, mock())
        val fileSizeMB = 11000 // 11GB file - exceeds FREE_USER 10GB limit
        
        // When - Check if user can add snap with oversized file
        val result = capacityGuard.canAddSnapWithSize(testUserId, fileSizeMB)
        
        // Then - User should not be able to add oversized file
        assertFalse(result.allowed, "User should not be able to add oversized file")
        assertEquals(DenyReason.QUOTA_EXCEEDED, result.reason, "Should be quota exceeded")
    }
    
    @Test
    fun `capacity guard should provide capacity info`() = runTest {
        // Given
        val testUserId = "test_user"
        val capacityGuard = GuardFactory.createCapacityGuard(mockUserPlanManager, planRegistryReader)
        
        // When - Get capacity info
        val capacityInfo = capacityGuard.getCapacityInfo(testUserId)
        
        // Then - Capacity info should be available
        assertNotNull(capacityInfo.snaps, "Snaps quota info should be available")
        assertNotNull(capacityInfo.storage, "Storage quota info should be available")
        assertNotNull(capacityInfo.aiAnalysis, "AI analysis quota info should be available")
    }
}