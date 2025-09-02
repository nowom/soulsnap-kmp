package pl.soulsnaps.access.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Testy dla UserPreferencesStorageFactory
 */
class UserPreferencesStorageFactoryTest {
    
    @Test
    fun `should create UserPreferencesStorage instance`() = runTest {
        // Given
        val factory = UserPreferencesStorageFactory
        
        // When
        val storage = factory.create()
        
        // Then
        assertNotNull(storage)
        assertTrue(storage is UserPreferencesStorage)
    }
    
    @Test
    fun `should have all required methods`() = runTest {
        // Given
        val storage = UserPreferencesStorageFactory.create()
        
        // When & Then
        // Sprawdź czy wszystkie metody są dostępne
        assertNotNull(storage::saveUserPlan)
        assertNotNull(storage::getUserPlan)
        assertNotNull(storage::saveOnboardingCompleted)
        assertNotNull(storage::isOnboardingCompleted)
        assertNotNull(storage::clearAllData)
        assertNotNull(storage::hasStoredData)
    }
    
    @Test
    fun `should handle basic storage operations`() = runTest {
        // Given
        val storage = UserPreferencesStorageFactory.create()
        
        // When
        storage.saveUserPlan("TEST_PLAN")
        storage.saveOnboardingCompleted(true)
        
        // Then
        val retrievedPlan = storage.getUserPlan()
        val retrievedOnboarding = storage.isOnboardingCompleted()
        
        assertEquals("TEST_PLAN", retrievedPlan)
        assertTrue(retrievedOnboarding)
        assertTrue(storage.hasStoredData())
    }
    
    @Test
    fun `should clear all data`() = runTest {
        // Given
        val storage = UserPreferencesStorageFactory.create()
        storage.saveUserPlan("TEST_PLAN")
        storage.saveOnboardingCompleted(true)
        
        // When
        storage.clearAllData()
        
        // Then
        assertNull(storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertFalse(storage.hasStoredData())
    }
    
    @Test
    fun `should handle empty storage`() = runTest {
        // Given
        val storage = UserPreferencesStorageFactory.create()
        
        // When & Then
        assertNull(storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertFalse(storage.hasStoredData())
    }
    
    @Test
    fun `should handle multiple operations`() = runTest {
        // Given
        val storage = UserPreferencesStorageFactory.create()
        
        // When
        storage.saveUserPlan("PLAN_1")
        storage.saveUserPlan("PLAN_2")
        storage.saveOnboardingCompleted(false)
        storage.saveOnboardingCompleted(true)
        
        // Then
        assertEquals("PLAN_2", storage.getUserPlan())
        assertTrue(storage.isOnboardingCompleted())
        assertTrue(storage.hasStoredData())
    }
}

