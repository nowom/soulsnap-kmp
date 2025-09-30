package pl.soulsnaps.features.startup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import pl.soulsnaps.access.manager.OnboardingManager
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.data.StartupRepositoryImpl
import pl.soulsnaps.domain.MemoryMaintenance
import pl.soulsnaps.domain.model.StartupState
import pl.soulsnaps.domain.model.StartupUiState
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.network.SupabaseAuthService

@OptIn(ExperimentalCoroutinesApi::class)
class StartupRepositoryTest {
    
    @Mock
    private lateinit var userPlanManager: UserPlanManager
    
    @Mock
    private lateinit var onboardingManager: OnboardingManager
    
    @Mock
    private lateinit var authService: SupabaseAuthService
    
    @Mock
    private lateinit var memoryMaintenance: MemoryMaintenance
    
    private lateinit var startupRepository: StartupRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(StandardTestDispatcher())
        
        startupRepository = StartupRepositoryImpl(
            userPlanManager = userPlanManager,
            onboardingManager = onboardingManager,
            authService = authService,
            memoryMaintenance = memoryMaintenance
        )
    }
    
    @Test
    fun `initialize - authenticated user - should go to dashboard`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(true)
        whenever(userPlanManager.getUserPlan()).thenReturn("PREMIUM")
        whenever(authService.isAuthenticated()).thenReturn(true)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(false)
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.shouldShowOnboarding)
        assert(state.userPlan == "PREMIUM")
        assert(!state.isLoading)
        assert(state.error == null)
    }
    
    @Test
    fun `initialize - guest user - should go to dashboard`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(true)
        whenever(userPlanManager.getUserPlan()).thenReturn("GUEST")
        whenever(authService.isAuthenticated()).thenReturn(false)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(false)
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.shouldShowOnboarding)
        assert(state.userPlan == "GUEST")
    }
    
    @Test
    fun `initialize - completed onboarding but not authenticated - should go to auth`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(true)
        whenever(userPlanManager.getUserPlan()).thenReturn("PREMIUM")
        whenever(authService.isAuthenticated()).thenReturn(false)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(false)
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_AUTH)
        assert(!state.shouldShowOnboarding)
        assert(state.userPlan == "PREMIUM")
    }
    
    @Test
    fun `initialize - not completed onboarding - should go to onboarding`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(false)
        whenever(userPlanManager.getUserPlan()).thenReturn("FREE")
        whenever(authService.isAuthenticated()).thenReturn(false)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(false)
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_ONBOARDING)
        assert(state.shouldShowOnboarding)
        assert(state.userPlan == "FREE")
    }
    
    @Test
    fun `startOnboarding - should set onboarding active state`() = runTest {
        // When
        startupRepository.startOnboarding()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.ONBOARDING_ACTIVE)
        assert(!state.isLoading)
    }
    
    @Test
    fun `completeOnboarding - should go to dashboard`() = runTest {
        // When
        startupRepository.completeOnboarding()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.shouldShowOnboarding)
        assert(!state.isLoading)
    }
    
    @Test
    fun `skipOnboarding - should go to dashboard as guest`() = runTest {
        // When
        startupRepository.skipOnboarding()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.shouldShowOnboarding)
        assert(state.userPlan == "GUEST")
        assert(!state.isLoading)
    }
    
    @Test
    fun `goToDashboard - should set dashboard state`() = runTest {
        // When
        startupRepository.goToDashboard()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.isLoading)
    }
    
    @Test
    fun `goToAuth - should set auth state`() = runTest {
        // When
        startupRepository.goToAuth()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_AUTH)
        assert(!state.isLoading)
    }
    
    @Test
    fun `resetState - should reset to checking and reinitialize`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(true)
        whenever(userPlanManager.getUserPlan()).thenReturn("PREMIUM")
        whenever(authService.isAuthenticated()).thenReturn(true)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(false)
        
        // When
        startupRepository.recheck()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.shouldShowOnboarding)
        assert(state.userPlan == "PREMIUM")
    }
    
    
    @Test
    fun `initialize with maintenance needed - should perform cleanup`() = runTest {
        // Given
        whenever(userPlanManager.isOnboardingCompleted()).thenReturn(true)
        whenever(userPlanManager.getUserPlan()).thenReturn("PREMIUM")
        whenever(authService.isAuthenticated()).thenReturn(true)
        whenever(memoryMaintenance.isMaintenanceNeeded()).thenReturn(true)
        whenever(memoryMaintenance.cleanupLargeMemories()).thenReturn(5)
        whenever(memoryMaintenance.cleanupOrphanedFiles()).thenReturn(3)
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.state == StartupState.READY_FOR_DASHBOARD)
        assert(!state.isLoading)
    }
    
    @Test
    fun `initialize with error - should set error state`() = runTest {
        // Given
        whenever(userPlanManager.waitForInitialization()).thenThrow(RuntimeException("Service error"))
        
        // When
        startupRepository.initialize()
        
        // Then
        val state = startupRepository.state.first()
        assert(state.error == "Service error")
        assert(!state.isLoading)
    }
}
