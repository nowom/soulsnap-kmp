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
import org.mockito.kotlin.verify
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.domain.model.StartupState
import pl.soulsnaps.domain.model.StartupUiState

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {
    
    @Mock
    private lateinit var startupRepository: StartupRepository
    
    private lateinit var startupViewModel: SplashViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(StandardTestDispatcher())
        
        startupViewModel = SplashViewModel(startupRepository)
    }
    
    @Test
    fun `initialize - should send Initialize intent`() = runTest {
        // When
        startupViewModel.initialize()
        
        // Then
        verify(startupRepository).initialize()
    }
    
    @Test
    fun `startOnboarding - should send StartOnboarding intent`() = runTest {
        // When
        startupViewModel.startOnboarding()
        
        // Then
        verify(startupRepository).startOnboarding()
    }
    
    @Test
    fun `completeOnboarding - should send CompleteOnboarding intent`() = runTest {
        // When
        startupViewModel.completeOnboarding()
        
        // Then
        verify(startupRepository).completeOnboarding()
    }
    
    @Test
    fun `skipOnboarding - should send SkipOnboarding intent`() = runTest {
        // When
        startupViewModel.skipOnboarding()
        
        // Then
        verify(startupRepository).skipOnboarding()
    }
    
    @Test
    fun `goToDashboard - should send GoToDashboard intent`() = runTest {
        // When
        startupViewModel.goToDashboard()
        
        // Then
        verify(startupRepository).goToDashboard()
    }
    
    @Test
    fun `goToAuth - should send GoToAuth intent`() = runTest {
        // When
        startupViewModel.goToAuth()
        
        // Then
        verify(startupRepository).goToAuth()
    }
    
    @Test
    fun `recheck - should call recheck`() = runTest {
        // When
        startupViewModel.recheck()
        
        // Then
        verify(startupRepository).recheck()
    }
    
    @Test
    fun `getCurrentState - should delegate to repository`() = runTest {
        // Given
        val expectedState = StartupUiState(
            state = StartupState.READY_FOR_DASHBOARD,
            shouldShowOnboarding = false,
            userPlan = "PREMIUM"
        )
        org.mockito.kotlin.whenever(startupRepository.state.value).thenReturn(expectedState)
        
        // When
        val result = startupViewModel.getCurrentState()
        
        // Then
        assert(result == expectedState)
    }
    
    @Test
    fun `uiState - should expose repository state`() = runTest {
        // Given
        val expectedState = StartupUiState(
            state = StartupState.READY_FOR_ONBOARDING,
            shouldShowOnboarding = true
        )
        org.mockito.kotlin.whenever(startupRepository.state).thenReturn(
            kotlinx.coroutines.flow.flowOf(expectedState)
        )
        
        // When
        val result = startupViewModel.state.first()
        
        // Then
        assert(result == expectedState)
    }
    
    @Test
    fun `isInitialized - should track initialization state`() = runTest {
        // Initially not initialized
        assert(!startupViewModel.isInitialized.first())
        
        // After initialize() call
        startupViewModel.initialize()
        assert(startupViewModel.isInitialized.first())
    }
}
