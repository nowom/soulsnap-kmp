package pl.soulsnaps.access.manager

import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test dla OnboardingManager używającego DataStore
 */
class OnboardingManagerDataStoreTest {
    
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var userPlanManager: UserPlanManager

    @BeforeTest
    fun setup() {
        userPlanManager = UserPlanManager(mock())
        onboardingManager = OnboardingManager(userPlanManager)
    }

    @Test
    fun `should start with welcome step and onboarding not active`() = runTest {
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should show onboarding when not completed`() = runTest {
        assertTrue(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should not show onboarding when completed`() = runTest {
        userPlanManager.setUserPlan("FREE_USER")
        assertFalse(onboardingManager.shouldShowOnboarding())
    }
    
    @Test
    fun `should start onboarding`() = runTest {
        onboardingManager.startOnboarding()
        
        assertEquals(OnboardingStep.WELCOME, onboardingManager.getCurrentStep())
        assertTrue(onboardingManager.isActive())
    }
    
    @Test
    fun `should navigate through onboarding steps`() = runTest {
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
    fun `should navigate back through onboarding steps`() = runTest {
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
    fun `should select plan and advance to next step`() = runTest {
        onboardingManager.startOnboarding()
        onboardingManager.nextStep() // Move to PLAN_SELECTION
        
        onboardingManager.selectPlan("PREMIUM_USER")
        
        assertEquals("PREMIUM_USER", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertEquals(OnboardingStep.FEATURES_OVERVIEW, onboardingManager.getCurrentStep())
    }
    
    @Test
    fun `should complete onboarding`() = runTest {
        onboardingManager.startOnboarding()
        onboardingManager.completeOnboarding()
        
        assertEquals(OnboardingStep.COMPLETED, onboardingManager.getCurrentStep())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should skip onboarding`() = runTest {
        onboardingManager.startOnboarding()
        onboardingManager.skipOnboarding()
        
        assertEquals("GUEST", userPlanManager.getUserPlan())
        assertTrue(userPlanManager.isOnboardingCompleted())
        assertFalse(onboardingManager.isActive())
    }
    
    @Test
    fun `should reset onboarding`() = runTest {
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
