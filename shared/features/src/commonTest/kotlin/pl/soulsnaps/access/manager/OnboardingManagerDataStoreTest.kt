package pl.soulsnaps.access.manager

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test dla OnboardingManagerDataStore używającego prostszego DataStore
 */
class OnboardingManagerDataStoreTest {
    
    private lateinit var onboardingManager: OnboardingManagerDataStore
    private lateinit var userPlanManager: UserPlanManagerDataStore
    private lateinit var mockDataStore: MockDataStore
    
    @BeforeTest
    fun setup() {
        mockDataStore = MockDataStore()
        userPlanManager = UserPlanManagerDataStore(mockDataStore)
        onboardingManager = OnboardingManagerDataStore(userPlanManager)
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
 * Prosty mock PreferencesDataStore dla testów
 */
class MockDataStore : pl.soulsnaps.access.storage.PreferencesDataStore {
    private val storage = mutableMapOf<String, Any>()
    
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
    
    override suspend fun putInt(key: String, value: Int) {
        storage[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return storage[key] as? Int ?: defaultValue
    }
    
    override fun getIntFlow(key: String, defaultValue: Int): kotlinx.coroutines.flow.Flow<Int> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage[key] as? Int ?: defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        storage[key] = value
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return storage[key] as? Long ?: defaultValue
    }
    
    override fun getLongFlow(key: String, defaultValue: Long): kotlinx.coroutines.flow.Flow<Long> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage[key] as? Long ?: defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        storage[key] = value
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return storage[key] as? Float ?: defaultValue
    }
    
    override fun getFloatFlow(key: String, defaultValue: Float): kotlinx.coroutines.flow.Flow<Float> {
        return kotlinx.coroutines.flow.MutableStateFlow(storage[key] as? Float ?: defaultValue)
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
    }
    
    override suspend fun clear() {
        storage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }
}
