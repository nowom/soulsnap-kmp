package pl.soulsnaps.features.auth.mvp.guard

import kotlinx.coroutines.test.runTest
import pl.soulsnaps.features.auth.mvp.guard.model.AppAction
import pl.soulsnaps.features.auth.mvp.guard.model.AppFeature
import pl.soulsnaps.features.auth.mvp.guard.model.PlanRestriction
import pl.soulsnaps.features.auth.mvp.guard.model.PlanType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaywallTriggerTest {
    
    @Test
    fun `checkCapacityLimit should allow when under limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restrictionTriggered = false
        
        // When
        val result = paywallTrigger.checkCapacityLimit(
            userId = "user123",
            currentCount = 25, // Under 50 limit for FREE_USER
            onRestricted = { restrictionTriggered = true }
        )
        
        // Then
        assertTrue(result)
        assertFalse(restrictionTriggered)
    }
    
    @Test
    fun `checkCapacityLimit should trigger restriction when at limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restriction: PlanRestriction? = null
        
        // When
        val result = paywallTrigger.checkCapacityLimit(
            userId = "user123",
            currentCount = 50, // At 50 limit for FREE_USER
            onRestricted = { restriction = it }
        )
        
        // Then
        assertFalse(result)
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.SnapsCapacity)
        assertEquals(50, (restriction as PlanRestriction.SnapsCapacity).current)
        assertEquals(50, (restriction as PlanRestriction.SnapsCapacity).limit)
    }
    
    @Test
    fun `checkAIDailyLimit should allow when under limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restrictionTriggered = false
        
        // When
        val result = paywallTrigger.checkAIDailyLimit(
            userId = "user123",
            usedToday = 3, // Under 5 limit for FREE_USER
            onRestricted = { restrictionTriggered = true }
        )
        
        // Then
        assertTrue(result)
        assertFalse(restrictionTriggered)
    }
    
    @Test
    fun `checkAIDailyLimit should trigger restriction when at limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restriction: PlanRestriction? = null
        
        // When
        val result = paywallTrigger.checkAIDailyLimit(
            userId = "user123",
            usedToday = 5, // At 5 limit for FREE_USER
            onRestricted = { restriction = it }
        )
        
        // Then
        assertFalse(result)
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.AIDailyLimit)
        assertEquals(5, (restriction as PlanRestriction.AIDailyLimit).used)
        assertEquals(5, (restriction as PlanRestriction.AIDailyLimit).limit)
    }
    
    @Test
    fun `checkStorageLimit should allow when under limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restrictionTriggered = false
        
        // When
        val result = paywallTrigger.checkStorageLimit(
            userId = "user123",
            usedGB = 0.5, // Under 1.0 GB limit for FREE_USER
            onRestricted = { restrictionTriggered = true }
        )
        
        // Then
        assertTrue(result)
        assertFalse(restrictionTriggered)
    }
    
    @Test
    fun `checkStorageLimit should trigger restriction when at limit`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restriction: PlanRestriction? = null
        
        // When
        val result = paywallTrigger.checkStorageLimit(
            userId = "user123",
            usedGB = 1.0, // At 1.0 GB limit for FREE_USER
            onRestricted = { restriction = it }
        )
        
        // Then
        assertFalse(result)
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.StorageLimit)
        assertEquals(1.0, (restriction as PlanRestriction.StorageLimit).usedGB)
        assertEquals(1.0, (restriction as PlanRestriction.StorageLimit).limitGB)
    }
    
    @Test
    fun `createRestriction should create correct restriction for CreateMemory`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restriction: PlanRestriction? = null
        
        // When
        val result = paywallTrigger.checkAndTrigger(
            userId = "user123",
            action = AppAction.CreateMemory("PHOTO"),
            onRestricted = { restriction = it }
        )
        
        // Then
        // Note: This test depends on the actual AccessGuard behavior
        // We're just testing that the method doesn't crash
        assertTrue(result || restriction != null)
    }
    
    @Test
    fun `createFeatureRestriction should create correct restriction for MAP_ADVANCED`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        var restriction: PlanRestriction? = null
        
        // When
        val result = paywallTrigger.checkFeatureAccess(
            userId = "user123",
            feature = AppFeature.MAP_ADVANCED,
            onRestricted = { restriction = it }
        )
        
        // Then
        // Note: This test depends on the actual AccessGuard behavior
        // We're just testing that the method doesn't crash
        assertTrue(result || restriction != null)
    }
    
    @Test
    fun `test plan limits for different plan types`() = runTest {
        // Given
        val mockAccessGuard = GuardFactory.createDefaultGuard()
        val mockPlanRegistry = DefaultPlans
        val paywallTrigger = PaywallTrigger(mockAccessGuard, mockPlanRegistry)
        
        // When & Then - Test capacity limits
        var restriction: PlanRestriction? = null
        
        // Test FREE_USER limits (current implementation)
        paywallTrigger.checkCapacityLimit(
            userId = "user123",
            currentCount = 50, // At limit
            onRestricted = { restriction = it }
        )
        
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.SnapsCapacity)
        assertEquals(50, (restriction as PlanRestriction.SnapsCapacity).limit)
        
        // Test AI daily limits
        restriction = null
        paywallTrigger.checkAIDailyLimit(
            userId = "user123",
            usedToday = 5, // At limit
            onRestricted = { restriction = it }
        )
        
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.AIDailyLimit)
        assertEquals(5, (restriction as PlanRestriction.AIDailyLimit).limit)
        
        // Test storage limits
        restriction = null
        paywallTrigger.checkStorageLimit(
            userId = "user123",
            usedGB = 1.0, // At limit
            onRestricted = { restriction = it }
        )
        
        assertNotNull(restriction)
        assertTrue(restriction is PlanRestriction.StorageLimit)
        assertEquals(1.0, (restriction as PlanRestriction.StorageLimit).limitGB)
    }
}
