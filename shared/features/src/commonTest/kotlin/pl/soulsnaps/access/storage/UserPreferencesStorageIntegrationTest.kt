package pl.soulsnaps.access.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Testy integracyjne dla UserPreferencesStorage
 * Sprawdza czy dane są rzeczywiście zapisywane i odczytywane
 */
class UserPreferencesStorageIntegrationTest {
    
    private fun createTestDataStore(): DataStore<Preferences> {
        return createDataStore { "test_preferences.pb" }
    }
    
    @Test
    fun `should persist data across multiple operations`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When - zapisz dane
        storage.saveUserPlan("FREE_USER")
        storage.saveOnboardingCompleted(true)
        
        // Then - sprawdź czy dane są zapisane
        val retrievedPlan = storage.getUserPlan()
        val retrievedOnboarding = storage.isOnboardingCompleted()
        val hasData = storage.hasStoredData()
        
        assertEquals("FREE_USER", retrievedPlan)
        assertTrue(retrievedOnboarding)
        assertTrue(hasData)
    }
    
    @Test
    fun `should handle plan changes`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When - zmień plan
        storage.saveUserPlan("GUEST")
        storage.saveUserPlan("PREMIUM_USER")
        
        // Then - sprawdź czy ostatni plan jest zapisany
        val retrievedPlan = storage.getUserPlan()
        assertEquals("PREMIUM_USER", retrievedPlan)
    }
    
    @Test
    fun `should handle onboarding status changes`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When - zmień status onboarding
        storage.saveOnboardingCompleted(false)
        storage.saveOnboardingCompleted(true)
        
        // Then - sprawdź czy ostatni status jest zapisany
        val retrievedOnboarding = storage.isOnboardingCompleted()
        assertTrue(retrievedOnboarding)
    }
    
    @Test
    fun `should clear all data`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        storage.saveUserPlan("FREE_USER")
        storage.saveOnboardingCompleted(true)
        
        // When - wyczyść dane
        storage.clearAllData()
        
        // Then - sprawdź czy dane są wyczyszczone
        assertNull(storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertFalse(storage.hasStoredData())
    }
    
    @Test
    fun `should handle empty storage correctly`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When & Then - sprawdź domyślne wartości
        assertNull(storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertFalse(storage.hasStoredData())
    }
    
    @Test
    fun `should handle partial data`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When - zapisz tylko plan
        storage.saveUserPlan("GUEST")
        
        // Then - sprawdź czy tylko plan jest zapisany
        assertEquals("GUEST", storage.getUserPlan())
        assertFalse(storage.isOnboardingCompleted())
        assertTrue(storage.hasStoredData())
    }
    
    @Test
    fun `should handle only onboarding status`() = runTest {
        // Given
        val dataStore = createTestDataStore()
        val storage = UserPreferencesStorageImpl(dataStore)
        
        // Clean up first
        storage.clearAllData()
        
        // When - zapisz tylko status onboarding
        storage.saveOnboardingCompleted(true)
        
        // Then - sprawdź czy tylko status jest zapisany
        assertNull(storage.getUserPlan())
        assertTrue(storage.isOnboardingCompleted())
        assertTrue(storage.hasStoredData())
    }
}
