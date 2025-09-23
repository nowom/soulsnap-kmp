package pl.soulsnaps.access.manager

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test for OnboardingManagerSimple using DataStore
 * Based on official KMP DataStore patterns
 */
class OnboardingManagerSimpleTest {
    
    private lateinit var onboardingManager: OnboardingManagerSimple
    private lateinit var userPlanManager: UserPlanManagerSimple
    private lateinit var mockPreferencesStore: MockSimplePreferencesStore
    
    @BeforeTest
    fun setup() {
        mockPreferencesStore = MockSimplePreferencesStore()
        userPlanManager = UserPlanManagerSimple(mockPreferencesStore)
        onboardingManager = OnboardingManagerSimple(userPlanManager)
    }
    
    @Test
    fun `should start with welcome step and onboarding not active`() {
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should show onboarding when not completed`() {
        assertTrue(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should not show onboarding when completed`() {
        userPlanManager.setUserPlan("FREE_USER")
        userPlanManager.setOnboardingCompleted(true)
        assertFalse(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should start onboarding`() {
        onboardingManager.startOnboarding()
        
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertTrue(onboardingManager.isActive())
    }
    
    @Test
    fun `should navigate through onboarding steps`() {
        onboardingManager.startOnboarding()
        
        // WELCOME -> PLAN_SELECTION
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.PLAN_SELECTION, onboardingManager.getCurrentStep())
        
        // PLAN_SELECTION -> FEATURES_OVERVIEW
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.FEATURES_OVERVIEW, onboardingManager.getCurrentStep())
        
        // FEATURES_OVERVIEW -> COMPLETED
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        
        // COMPLETED -> COMPLETED (no change)
        onboardingManager.nextStep()
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should navigate back through onboarding steps`() {
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // WELCOME -> PLAN_SELECTION
        onboardingManager.nextStep() // PLAN_SELECTION -> FEATURES_OVERVIEW
        
        // FEATURES_OVERVIEW -> PLAN_SELECTION
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.PLAN_SELECTION, onboardingManager.getCurrentStep())
        
        // PLAN_SELECTION -> WELCOME
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        
        // WELCOME -> WELCOME (no change)
        onboardingManager.previousStep()
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should select plan and advance to next step`() {
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // Move to PLAN_SELECTION
        
        onboardingManager.selectPlan("PREMIUM_USER")
        
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals(OnboardingStep.FEATURES_OVERVIEW, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should complete onboarding`() {
        onboardingManager.startOnboarding()
        onboardingManager.completeOnboarding()
        
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should skip onboarding`() {
        onboardingManager.startOnboarding()
        onboardingManager.skipOnboarding()
        
        assertEquals("GUEST", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should reset onboarding`() {
        onboardingManager.startOnboarding()
        onboardingManager.nextStep()
        userPlanManager.setUserPlan("PREMIUM_USER")
        
        onboardingManager.resetOnboarding()
        
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
        assertNull(userPlanManager.getUserPlan())
        assertFalse(userPlanManager.isOnboardingCompleted())
    }
}

/**
 * Simple mock SimplePreferencesStore for tests
 */
class MockSimplePreferencesStore : pl.soulsnaps.access.storage.SimplePreferencesStore {
    private val storage = mutableMapOf<String, Any>()
    
    override suspend fun saveUserPlan(planName: String) {
        storage["user_plan"] = planName
    }
    
    override suspend fun getUserPlan(): String? {
        return storage["user_plan"] as? String
    }
    
    override fun getUserPlanFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage["user_plan"] as? String)
    }
    
    override suspend fun saveOnboardingCompleted(completed: Boolean) {
        storage["onboarding_completed"] = completed
    }
    
    override suspend fun isOnboardingCompleted(): Boolean {
        return storage["onboarding_completed"] as? Boolean ?: false
    }
    
    override fun isOnboardingCompletedFlow(): kotlinx.coroutines.flow.Flow<Boolean> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage["onboarding_completed"] as? Boolean ?: false)
    }
    
    override suspend fun putString(key: String, value: String) {
        storage[key] = value
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return storage[key] as? String ?: defaultValue
    }
    
    override fun getStringFlow(key: String, defaultValue: String?): kotlinx.coroutines.flow.Flow<String?> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage[key] as? String ?: defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        storage[key] = value
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defaultValue
    }
    
    override fun getBooleanFlow(key: String, defaultValue: Boolean): kotlinx.coroutines.flow.Flow<Boolean> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage[key] as? Boolean ?: defaultValue)
    }
    
    override suspend fun clearAllData() {
        storage.clear()
    }
    
    override suspend fun hasStoredData(): Boolean {
        return storage.isNotEmpty()
    }
}
