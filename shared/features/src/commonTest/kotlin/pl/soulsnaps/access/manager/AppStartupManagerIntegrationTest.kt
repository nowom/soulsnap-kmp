package pl.soulsnaps.access.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import pl.soulsnaps.access.storage.UserPreferencesStorageImpl
import pl.soulsnaps.access.storage.createDataStore
import kotlin.test.*

/**
 * Integration tests for AppStartupManager components
 * 
 * Tests the UserPlanManager and OnboardingManager integration:
 * - User plan management and persistence
 * - Onboarding flow and state management
 * - Data persistence across app restarts
 * 
 * Note: These tests use real implementations with minimal dependencies
 * to avoid issues with final classes that cannot be extended.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupManagerIntegrationTest {
    
    private lateinit var userPlanManager: UserPlanManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var dataStore: DataStore<Preferences>
    
    @BeforeTest
    fun setup() {
        // Set up test dispatcher for Main
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Create unique DataStore for each test to avoid conflicts
        val testId = (0..1000).random() // generates random from 0 to 10 (inclusive)

        dataStore = createDataStore { "test_app_startup_$testId.preferences_pb" }
        
                // Create real UserPlanManager with real dependencies
                val storage = UserPreferencesStorageImpl(dataStore)
                userPlanManager = UserPlanManagerImpl(
                    storage = storage,
                    userPlanRepository = createFakeUserPlanRepository(),
                    userPlanUseCase = createFakeUserPlanUseCase(),
                    crashlyticsManager = createFakeCrashlyticsManager(),
                    userSessionManager = createFakeUserSessionManager(),
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                )
        
        // Create real OnboardingManager
        onboardingManager = OnboardingManager(userPlanManager)
    }
    
    @AfterTest
    fun tearDown() {
        // Reset Main dispatcher after test
        Dispatchers.resetMain()
        
        // Clean up DataStore
        try {
            runTest {
                dataStore.data.first()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    fun `should start with no plan and onboarding not completed`() = runTest {
        // Given - fresh app state
        
        // When & Then - check initial state
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        assertFalse(userPlanManager.hasPlanSet())
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should set user plan and mark onboarding as completed`() = runTest {
        // Given - fresh app state
        userPlanManager.waitForInitialization()
        
        // When - set user plan
        userPlanManager.setUserPlanAndWait("FREE_USER")
        
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
        
        // Then - should be active and on welcome step
        assertTrue(onboardingManager.isActive())
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should select plan and advance to next step`() = runTest {
        // Given - onboarding is started
        onboardingManager.startOnboarding()
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        
        // When - select plan
        onboardingManager.selectPlan("PREMIUM_USER")
        
        // Then - should advance to next step
        assertEquals(OnboardingStep.PLAN_SELECTION, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should complete onboarding`() = runTest {
        // Given - onboarding in progress
        onboardingManager.startOnboarding()
        onboardingManager.selectPlan("PREMIUM_USER")
        
        // When - complete onboarding
        onboardingManager.completeOnboarding()
        
        // Then - should be completed
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
        assertFalse(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should skip onboarding`() = runTest {
        // Given - fresh state
        assertTrue(onboardingManager.shouldShowOnboarding())
        
        // When - skip onboarding
        onboardingManager.skipOnboarding()
        
        // Then - should not show onboarding
        assertFalse(onboardingManager.shouldShowOnboarding())
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should reset onboarding`() = runTest {
        // Given - completed onboarding
        onboardingManager.startOnboarding()
        onboardingManager.selectPlan("PREMIUM_USER")
        onboardingManager.completeOnboarding()
        assertFalse(onboardingManager.shouldShowOnboarding())
        
        // When - reset onboarding
        onboardingManager.resetOnboarding()
        
        // Then - should show onboarding again
        assertTrue(onboardingManager.shouldShowOnboarding())
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should handle multiple plan changes`() = runTest {
        // Given - user has a plan
        userPlanManager.waitForInitialization()
        userPlanManager.setUserPlanAndWait("FREE_USER")
        assertEquals("FREE_USER", userPlanManager.getUserPlan())
        
        // When - change plan multiple times
        userPlanManager.setUserPlanAndWait("PREMIUM_USER")
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        
        userPlanManager.setUserPlanAndWait("ENTERPRISE_USER")
        assertEquals("ENTERPRISE_USER", userPlanManager.getUserPlan())
        
        // Then - should end up in correct final state
        assertEquals("ENTERPRISE_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
    }
    
    @Test
    fun `should reset user plan completely`() = runTest {
        // Given - user has completed onboarding with plan
        userPlanManager.waitForInitialization()
        userPlanManager.setUserPlanAndWait("PREMIUM_USER")
        assertTrue(userPlanManager.hasPlanSet())
        assertTrue(userPlanManager.isOnboardingCompleted())
        
        // When - reset everything
        userPlanManager.resetUserPlan()
        onboardingManager.resetOnboarding()
        
        // Then - should be back to initial state
        userPlanManager.waitForInitialization() // Wait for reset to propagate
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
        assertFalse(userPlanManager.hasPlanSet())
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
        assertTrue(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should handle setDefaultPlanIfNeeded`() = runTest {
        // Given - no plan set
        assertNull(userPlanManager.getUserPlan())
        
        // When - set default plan if needed
        userPlanManager.setDefaultPlanIfNeeded()
        
        // Then - should have guest plan as default
        assertEquals("GUEST", userPlanManager.getPlanOrDefault())
    }
    
    @Test
    fun `should not override existing plan when setting default`() = runTest {
        // Given - user already has a plan
        userPlanManager.waitForInitialization()
        userPlanManager.setUserPlanAndWait("PREMIUM_USER")
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        
        // When - try to set default plan
        userPlanManager.setDefaultPlanIfNeeded()
        
        // Then - should keep existing plan
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.hasPlanSet())
    }
    
    @Test
    fun `should handle concurrent operations correctly`() = runTest {
        // Given - multiple operations happening
        userPlanManager.waitForInitialization()
        userPlanManager.setUserPlanAndWait("FREE_USER")
        onboardingManager.startOnboarding()
        
        // When - multiple state changes
        userPlanManager.setUserPlanAndWait("PREMIUM_USER")
        onboardingManager.completeOnboarding()
        
        // Then - should end up in correct final state
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should persist user plan across app restarts`() = runTest {
        // Given - user completes onboarding with a plan
        userPlanManager.setUserPlanAndWait("PREMIUM_USER") // Wait for data to be saved
        assertTrue(userPlanManager.hasPlanSet())
        
                // When - create new UserPlanManager instance (simulating app restart)
                // Use the same DataStore to simulate persistence
                val newStorage = UserPreferencesStorageImpl(dataStore)
                val newUserPlanManager = UserPlanManagerImpl(
                    storage = newStorage,
                    userPlanRepository = createFakeUserPlanRepository(),
                    userPlanUseCase = createFakeUserPlanUseCase(),
                    crashlyticsManager = createFakeCrashlyticsManager(),
                    userSessionManager = createFakeUserSessionManager(),
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                )
        
        // Wait for initialization to complete
        newUserPlanManager.waitForInitialization()
        
        // Then - should load persisted plan
        assertEquals("PREMIUM_USER", newUserPlanManager.getUserPlan())
        assertTrue(newUserPlanManager.hasPlanSet())
        assertTrue(newUserPlanManager.isOnboardingCompleted())
    }
    
    // Helper functions to create fake implementations
    private fun createFakeUserPlanRepository(): pl.soulsnaps.domain.UserPlanRepository {
        return object : pl.soulsnaps.domain.UserPlanRepository {
            override suspend fun getUserPlan(userId: String) = null
            override suspend fun saveUserPlan(userPlan: pl.soulsnaps.domain.UserPlan) = true
            override suspend fun updateUserPlanType(userId: String, planType: pl.soulsnaps.access.model.PlanType) = true
            override suspend fun updateSubscriptionStatus(userId: String, status: pl.soulsnaps.domain.SubscriptionStatus) = true
            override suspend fun hasActivePlan(userId: String) = false
            override suspend fun getAllUserPlans() = emptyList<pl.soulsnaps.domain.UserPlan>()
            override suspend fun deleteUserPlan(userId: String) = true
            override suspend fun getUserPlansByType(planType: pl.soulsnaps.access.model.PlanType) = emptyList<pl.soulsnaps.domain.UserPlan>()
            override suspend fun getUserPlansByStatus(status: pl.soulsnaps.domain.SubscriptionStatus) = emptyList<pl.soulsnaps.domain.UserPlan>()
            override suspend fun isUserInTrial(userId: String) = false
            override suspend fun getTrialEndDate(userId: String) = null
            override suspend fun updateTrialInfo(userId: String, trialEndsAt: Long?) = true
        }
    }
    
    private fun createFakeUserPlanUseCase(): pl.soulsnaps.domain.interactor.UserPlanUseCase {
        return pl.soulsnaps.domain.interactor.UserPlanUseCase(
            createFakeUserPlanRepository(),
            createFakeCrashlyticsManager()
        )
    }
    
    private fun createFakeCrashlyticsManager(): pl.soulsnaps.crashlytics.CrashlyticsManager {
        return object : pl.soulsnaps.crashlytics.CrashlyticsManager {
            override fun log(message: String) {}
            override fun recordException(throwable: Throwable) {}
            override fun setUserId(userId: String) {}
            override fun setCustomKey(key: String, value: String) {}
            override fun setCustomKey(key: String, value: Boolean) {}
            override fun setCustomKey(key: String, value: Int) {}
            override fun setCustomKey(key: String, value: Float) {}
            override fun setCustomKey(key: String, value: Double) {}
            override fun resetAnalyticsData() {}
            override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {}
            override fun testCrash() {}
        }
    }
    
    private fun createFakeUserSessionManager(): pl.soulsnaps.features.auth.UserSessionManager {
        return pl.soulsnaps.features.auth.UserSessionManagerImpl(
            createFakeSessionDataStore(),
            createFakeCrashlyticsManager(),
            createFakeLocalStorageManager()
        )
    }
    
    private fun createFakeSessionDataStore(): pl.soulsnaps.features.auth.SessionDataStore {
        return object : pl.soulsnaps.features.auth.SessionDataStore {
            override suspend fun saveSession(userSession: pl.soulsnaps.domain.model.UserSession) {}
            override suspend fun getStoredSession(): pl.soulsnaps.domain.model.UserSession? = null
            override val currentSession = kotlinx.coroutines.flow.flowOf(null)
            override val isAuthenticated = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun clearSession() {}
        }
    }
    
    private fun createFakeLocalStorageManager(): pl.soulsnaps.storage.LocalStorageManager {
        return pl.soulsnaps.storage.LocalStorageManager(
            createFakeMemoryRepository(),
            createFakeAffirmationRepository(),
            createFakeUserPreferencesStorage(),
            createFakeSessionDataStore(),
            createFakeAccessGuard(),
            createFakeCrashlyticsManager()
        )
    }
    
    private fun createFakeMemoryRepository(): pl.soulsnaps.domain.MemoryRepository {
        return object : pl.soulsnaps.domain.MemoryRepository {
            override fun getMemories() = kotlinx.coroutines.flow.flowOf(emptyList<pl.soulsnaps.domain.model.Memory>())
            override suspend fun addMemory(memory: pl.soulsnaps.domain.model.Memory) = 1
            override suspend fun getMemoryById(id: Int) = null
            override suspend fun deleteMemory(id: Int) {}
            override suspend fun updateMemory(memory: pl.soulsnaps.domain.model.Memory) {}
            override suspend fun markAsFavorite(id: Int, isFavorite: Boolean) {}
            override suspend fun cleanupInvalidMemories() = 0
            override suspend fun clearAllMemories() = 0
        }
    }
    
    private fun createFakeAffirmationRepository(): pl.soulsnaps.domain.AffirmationRepository {
        return object : pl.soulsnaps.domain.AffirmationRepository {
            override suspend fun getAffirmations(emotionFilter: String?) = emptyList<pl.soulsnaps.domain.model.Affirmation>()
            override suspend fun saveAffirmationForMemory(memoryId: Int, text: String, mood: String) {}
            override suspend fun getAffirmationByMemoryId(memoryId: Int) = null
            override suspend fun getFavoriteAffirmations() = emptyList<pl.soulsnaps.domain.model.Affirmation>()
            override suspend fun updateIsFavorite(id: String) {}
            override suspend fun clearAllFavorites() {}
            override fun playAffirmation(text: String) {}
            override fun stopAudio() {}
            override fun getAffirmationsFlow() = kotlinx.coroutines.flow.flowOf(emptyList<pl.soulsnaps.domain.model.Affirmation>())
        }
    }
    
    private fun createFakeUserPreferencesStorage(): pl.soulsnaps.access.storage.UserPreferencesStorage {
        return object : pl.soulsnaps.access.storage.UserPreferencesStorage {
            override suspend fun getUserPlan(): String? = null
            override suspend fun saveUserPlan(planName: String) {}
            override suspend fun isOnboardingCompleted(): Boolean = false
            override suspend fun saveOnboardingCompleted(completed: Boolean) {}
            override suspend fun clearAllData() {}
            override suspend fun saveNotificationPermissionDecided(decided: Boolean) {}
            override suspend fun isNotificationPermissionDecided(): Boolean = false
            override suspend fun saveNotificationPermissionGranted(granted: Boolean) {}
            override suspend fun isNotificationPermissionGranted(): Boolean = false
            override suspend fun hasStoredData(): Boolean = false
        }
    }
    
    private fun createFakeAccessGuard(): pl.soulsnaps.access.guard.AccessGuard {
        return pl.soulsnaps.access.guard.AccessGuard(
            createFakeScopePolicy(),
            createFakeQuotaPolicy(),
            createFakeFeatureToggle(),
            createMockUserPlanManager()
        )
    }
    
    private fun createFakeScopePolicy(): pl.soulsnaps.access.guard.ScopePolicy {
        return object : pl.soulsnaps.access.guard.ScopePolicy {
            override fun hasScope(userId: String, action: String): Boolean = true
            override fun getUserScopes(userId: String): List<String> = emptyList()
            override fun getRequiredPlanForAction(action: String): String? = null
        }
    }
    
    private fun createFakeQuotaPolicy(): pl.soulsnaps.access.guard.QuotaPolicy {
        return object : pl.soulsnaps.access.guard.QuotaPolicy {
            override fun getQuotaInfo(userId: String, key: String): pl.soulsnaps.access.guard.QuotaInfo? = null
            override fun resetQuota(userId: String, key: String): Boolean = true
            override fun checkAndConsume(userId: String, key: String, amount: Int): Boolean = true
            override fun getRemaining(userId: String, key: String): Int = 100
        }
    }
    
    private fun createFakeFeatureToggle(): pl.soulsnaps.access.guard.FeatureToggle {
        return object : pl.soulsnaps.access.guard.FeatureToggle {
            override fun isOn(key: String): Boolean = true
            override fun getFeatureInfo(key: String): pl.soulsnaps.access.guard.FeatureInfo? = null
            override fun getAllFeatures(): Map<String, Boolean> = emptyMap()
        }
    }

    fun createMockUserPlanManager(): UserPlanManager {
        return mock<UserPlanManager> {
            // property Flow
            // (Mokkery pozwala stubować property; jeśli Twoja wersja wymaga innej składni,
            // zamień na odpowiednią 'every { currentPlan } returns ...')
            every { currentPlan } returns flowOf<String?>(null)

            // metody niesuspendujące – zwracają te same wartości co Twój fake
            every { getUserPlan() } returns null
            every { isOnboardingCompleted() } returns false
            every { getPlanOrDefault() } returns "GUEST"
            every { hasPlanSet() } returns false
            every { getCurrentPlan() } returns null

            // „void” (Unit) – nic nie robią
            every { setUserPlan("GUEST") } calls { /* no-op */ }
            every { resetUserPlan() } calls { /* no-op */ }
            every { setDefaultPlanIfNeeded() } calls { /* no-op */ }

            // suspend
        }
    }

}