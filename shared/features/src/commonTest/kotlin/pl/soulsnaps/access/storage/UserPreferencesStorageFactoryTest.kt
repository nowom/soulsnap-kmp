package pl.soulsnaps.access.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Testy jednostkowe dla UserPreferencesStorage
 */
class UserPreferencesStorageUnitTest {
    
    private fun createTestDataStore(): DataStore<Preferences> {
        return createDataStore { "test_preferences_unit.pb" }
    }
    
    @Test
    fun `should create UserPreferencesStorage instance`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        
        // When
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Then
        assertNotNull(storage)
        assertTrue(storage is UserPreferencesStorage)
    }
    
    @Test
    fun `should have all required methods`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // When & Then
        // Sprawdź czy wszystkie metody są dostępne
        assertNotNull(storage::saveUserPlan)
        assertNotNull(storage::getUserPlan)
        assertNotNull(storage::saveOnboardingCompleted)
        assertNotNull(storage::isOnboardingCompleted)
        assertNotNull(storage::clearAllData)
        assertNotNull(storage::hasStoredData)
        assertNotNull(storage::saveNotificationPermissionDecided)
        assertNotNull(storage::isNotificationPermissionDecided)
        assertNotNull(storage::saveNotificationPermissionGranted)
        assertNotNull(storage::isNotificationPermissionGranted)
    }
    
    @Test
    fun `should handle basic storage operations`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
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
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
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
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // When & Then
        assertNull(storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertFalse(storage.hasStoredData())
    }
    
    @Test
    fun `should handle multiple operations`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
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
    
    @Test
    fun `should handle notification permission methods`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // When
        storage.saveNotificationPermissionDecided(true)
        storage.saveNotificationPermissionGranted(false)
        
        // Then
        assertTrue(storage.isNotificationPermissionDecided())
        assertFalse(storage.isNotificationPermissionGranted())
        assertTrue(storage.hasStoredData())
    }
}

