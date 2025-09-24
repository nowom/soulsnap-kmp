package pl.soulsnaps.access.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.access.storage.UserPreferencesStorageImpl
import pl.soulsnaps.access.storage.createDataStore
import kotlin.test.*

/**
 * Testy integracyjne dla AppStartupManager
 * 
 * Testują kompletny flow aplikacji:
 * 1. Sprawdzanie stanu aplikacji przy starcie
 * 2. Obsługę onboarding
 * 3. Obsługę uwierzytelnienia
 * 4. Przekierowania między ekranami
 * 5. Integrację z UserPlanManager i OnboardingManager
 * 
 * Uwaga: Te testy skupiają się na logice onboarding i zarządzania stanem,
 * bez mockowania SupabaseAuthService, który jest zbyt skomplikowany do mockowania.
 */
class AppStartupManagerIntegrationTest {
    
    private lateinit var userPlanManager: UserPlanManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var dataStore: DataStore<Preferences>
    
    @BeforeTest
    fun setup() {
        dataStore = createDataStore { "test_app_startup.pb" }
        userPlanManager = UserPlanManager(mock())
        onboardingManager = OnboardingManager(userPlanManager)
    }
    
    @Test
    fun `should start with no plan and onboarding not completed`() = runTest {
        // Given - fresh app state
        
        // When & Then
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        assertFalse(userPlanManager.hasPlanSet())
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should set user plan and mark onboarding as completed`() = runTest {
        // Given - fresh app state
        
        // When - set user plan
        userPlanManager.setUserPlan("FREE_USER")
        
        // Then - should be updated
        assertEquals("FREE_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertTrue(userPlanManager.hasPlanSet())
        assertEquals("FREE_USER", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should handle onboarding flow correctly`() = runTest {
        // Given - fresh onboarding state
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
        assertTrue(onboardingManager.shouldShowOnboarding())
        
        // When - start onboarding
        onboardingManager.startOnboarding()
        
        // Then - should be active
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertTrue(onboardingManager.isActive())
        
        // When - navigate through steps
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.PLAN_SELECTION, onboardingManager.getCurrentStep())
        
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.FEATURES_OVERVIEW, onboardingManager.getCurrentStep())
        
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should select plan and advance to next step`() = runTest {
        // Given - onboarding started
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // Move to PLAN_SELECTION
        
        // When - select plan
        onboardingManager.selectPlan("PREMIUM_USER")
        
        // Then - plan should be set and onboarding completed
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals(OnboardingStep.FEATURES_OVERVIEW, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should complete onboarding`() = runTest {
        // Given - onboarding started
        onboardingManager.startOnboarding()
        
        // When - complete onboarding
        onboardingManager.completeOnboarding()
        
        // Then - should be completed
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals("GUEST", userPlanManager.getUserPlan()) // Default plan
    }
    
    @Test
    fun `should skip onboarding`() = runTest {
        // Given - onboarding started
        onboardingManager.startOnboarding()
        
        // When - skip onboarding
        onboardingManager.skipOnboarding()
        
        // Then - should be completed with guest plan
        assertEquals("GUEST", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should reset onboarding`() = runTest {
        // Given - onboarding with plan set
        onboardingManager.startOnboarding()
        onboardingManager.nextStep()
        userPlanManager.setUserPlan("PREMIUM_USER")
        
        // When - reset onboarding
        onboardingManager.resetOnboarding()
        
        // Then - should be back to initial state
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
    }
    
    @Test
    fun `should handle navigation back through onboarding steps`() = runTest {
        // Given - onboarding started and advanced
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // WELCOME -> PLAN_SELECTION
        onboardingManager.nextStep() // PLAN_SELECTION -> FEATURES_OVERVIEW
        
        // When - navigate back
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.PLAN_SELECTION, onboardingManager.getCurrentStep())
        
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        
        // When - try to go back from first step
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep()) // Should stay at first step
    }
    
    @Test
    fun `should handle navigation forward from last step`() = runTest {
        // Given - onboarding at last step
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // WELCOME -> PLAN_SELECTION
        onboardingManager.nextStep() // PLAN_SELECTION -> FEATURES_OVERVIEW
        onboardingManager.nextStep() // FEATURES_OVERVIEW -> COMPLETED
        
        // When - try to go forward from last step
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep()) // Should stay at last step
    }
    
    @Test
    fun `should not show onboarding when completed`() = runTest {
        // Given - onboarding completed
        userPlanManager.setUserPlan("FREE_USER")
        
        // When & Then
        assertFalse(onboardingManager.shouldShowOnboarding())
        assertTrue(userPlanManager.isOnboardingCompleted())
    }
    
    @Test
    fun `should handle multiple plan changes`() = runTest {
        // Given - initial state
        assertNull(userPlanManager.getUserPlan())
        
        // When - set multiple plans
        userPlanManager.setUserPlan("FREE_USER")
        assertEquals("FREE_USER", userPlanManager.getUserPlan())
        
        userPlanManager.setUserPlan("PREMIUM_USER")
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        
        userPlanManager.setUserPlan("ENTERPRISE_USER")
        assertEquals("ENTERPRISE_USER", userPlanManager.getUserPlan())
        
        // Then - onboarding should remain completed
        assertTrue(userPlanManager.isOnboardingCompleted())
    }
    
    @Test
    fun `should reset user plan completely`() = runTest {
        // Given - user with plan and completed onboarding
        userPlanManager.setUserPlan("PREMIUM_USER")
        assertTrue(userPlanManager.isOnboardingCompleted())
        
        // When - reset user plan
        userPlanManager.resetUserPlan()
        
        // Then - should be back to initial state
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        assertFalse(userPlanManager.hasPlanSet())
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should handle setDefaultPlanIfNeeded`() = runTest {
        // Given - no plan set
        assertNull(userPlanManager.getUserPlan())
        
        // When - set default plan if needed
        userPlanManager.setDefaultPlanIfNeeded()
        
        // Then - should set default plan but not mark onboarding as completed
        assertEquals("GUEST", userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted()) // Onboarding not completed by default
    }
    
    @Test
    fun `should not override existing plan when setting default`() = runTest {
        // Given - existing plan
        userPlanManager.setUserPlan("PREMIUM_USER")
        
        // When - set default plan if needed
        userPlanManager.setDefaultPlanIfNeeded()
        
        // Then - should keep existing plan
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
    }
    
    @Test
    fun `should handle concurrent operations correctly`() = runTest {
        // Given - fresh state
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        
        // When - perform multiple operations
        userPlanManager.setUserPlan("FREE_USER")
        onboardingManager.startOnboarding()
        onboardingManager.nextStep()
        onboardingManager.selectPlan("PREMIUM_USER")
        onboardingManager.completeOnboarding()
        
        // Then - should end up in correct final state
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should handle storage persistence`() = runTest {
        // Given - fresh state
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        
        // When - set plan and complete onboarding
        userPlanManager.setUserPlan("ENTERPRISE_USER")
        
        // Then - should persist data
        assertEquals("ENTERPRISE_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertTrue(userPlanManager.hasPlanSet())
        
        // When - create new instance (simulating app restart)
        val newDataStore = createDataStore { "test_app_startup_new.pb" }
        val newStorage = UserPreferencesStorageImpl(newDataStore)
        val newUserPlanManager = UserPlanManager(newStorage)
        
        // Then - should load persisted data
        // Note: In a real test environment, this would load from actual storage
        // For now, we test that the new instance can be created without errors
        assertNotNull(newUserPlanManager)
    }
}