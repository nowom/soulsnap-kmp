package pl.soulsnaps.features.auth

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.domain.interactor.RegisterUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegistrationViewModelTest {

    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var startupRepository: StartupRepository
    private lateinit var viewModel: RegistrationViewModel

    @BeforeTest
    fun setup() {
        registerUseCase = mock<RegisterUseCase>()
        startupRepository = mock<StartupRepository>()
        viewModel = RegistrationViewModel(registerUseCase, startupRepository)
    }

    @Test
    fun `initial state should be correct`() {
        val state = viewModel.state.value
        
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.passwordVisible)
        assertFalse(state.confirmPasswordVisible)
        assertFalse(state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `update email should update state`() {
        viewModel.handleIntent(RegistrationIntent.UpdateEmail("test@example.com"))
        
        assertEquals("test@example.com", viewModel.state.value.email)
    }

    @Test
    fun `update password should update state`() {
        viewModel.handleIntent(RegistrationIntent.UpdatePassword("password123"))
        
        assertEquals("password123", viewModel.state.value.password)
    }

    @Test
    fun `update confirm password should update state`() {
        viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword("password123"))
        
        assertEquals("password123", viewModel.state.value.confirmPassword)
    }

    @Test
    fun `toggle password visible should update state`() {
        viewModel.handleIntent(RegistrationIntent.TogglePasswordVisible)
        
        assertTrue(viewModel.state.value.passwordVisible)
        
        viewModel.handleIntent(RegistrationIntent.TogglePasswordVisible)
        
        assertFalse(viewModel.state.value.passwordVisible)
    }

    @Test
    fun `toggle confirm password visible should update state`() {
        viewModel.handleIntent(RegistrationIntent.ToggleConfirmPasswordVisible)
        
        assertTrue(viewModel.state.value.confirmPasswordVisible)
        
        viewModel.handleIntent(RegistrationIntent.ToggleConfirmPasswordVisible)
        
        assertFalse(viewModel.state.value.confirmPasswordVisible)
    }

    @Test
    fun `submit with empty email should show error`() {
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals("Email is required", viewModel.state.value.errorMessage)
    }

    @Test
    fun `submit with invalid email should show error`() {
        viewModel.handleIntent(RegistrationIntent.UpdateEmail("invalid-email"))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals("Invalid email", viewModel.state.value.errorMessage)
    }

    @Test
    fun `submit with short password should show error`() {
        viewModel.handleIntent(RegistrationIntent.UpdateEmail("test@example.com"))
        viewModel.handleIntent(RegistrationIntent.UpdatePassword("123"))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals("Password must be at least 6 characters", viewModel.state.value.errorMessage)
    }

    @Test
    fun `submit with mismatched passwords should show error`() {
        viewModel.handleIntent(RegistrationIntent.UpdateEmail("test@example.com"))
        viewModel.handleIntent(RegistrationIntent.UpdatePassword("password123"))
        viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword("different123"))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals("Passwords do not match", viewModel.state.value.errorMessage)
    }

    @Test
    fun `submit with valid data should call register use case`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        
        everySuspend { registerUseCase(email, password) } returns Unit
        everySuspend { startupRepository.completeOnboarding() } returns Unit
        
        viewModel.handleIntent(RegistrationIntent.UpdateEmail(email))
        viewModel.handleIntent(RegistrationIntent.UpdatePassword(password))
        viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword(password))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        verify { registerUseCase(email, password) }
        verify { startupRepository.completeOnboarding() }
    }

    @Test
    fun `submit with valid data should emit navigation event`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        
        everySuspend { registerUseCase(email, password) } returns Unit
        everySuspend { startupRepository.completeOnboarding() } returns Unit
        
        viewModel.handleIntent(RegistrationIntent.UpdateEmail(email))
        viewModel.handleIntent(RegistrationIntent.UpdatePassword(password))
        viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword(password))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals(RegistrationNavigationEvent.NavigateToDashboard, viewModel.navigationEvents.value)
    }

    @Test
    fun `submit with registration error should show error message`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Registration failed"
        
        everySuspend { registerUseCase(email, password) } throws Exception(errorMessage)
        
        viewModel.handleIntent(RegistrationIntent.UpdateEmail(email))
        viewModel.handleIntent(RegistrationIntent.UpdatePassword(password))
        viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword(password))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `continue as guest should call startup repository`() = runTest {
        everySuspend { startupRepository.skipOnboarding() } returns Unit
        
        viewModel.handleIntent(RegistrationIntent.ContinueAsGuest)
        
        verify { startupRepository.skipOnboarding() }
    }

    @Test
    fun `continue as guest should emit navigation event`() = runTest {
        everySuspend { startupRepository.skipOnboarding() } returns Unit
        
        viewModel.handleIntent(RegistrationIntent.ContinueAsGuest)
        
        assertEquals(RegistrationNavigationEvent.NavigateToDashboard, viewModel.navigationEvents.value)
    }

    @Test
    fun `continue as guest with error should show error message`() = runTest {
        val errorMessage = "Guest login failed"
        everySuspend { startupRepository.skipOnboarding() } throws Exception(errorMessage)
        
        viewModel.handleIntent(RegistrationIntent.ContinueAsGuest)
        
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `clear error should remove error message`() {
        // First set an error
        viewModel.handleIntent(RegistrationIntent.UpdateEmail(""))
        viewModel.handleIntent(RegistrationIntent.Submit)
        
        assertTrue(viewModel.state.value.errorMessage != null)
        
        // Then clear it
        viewModel.handleIntent(RegistrationIntent.ClearError)
        
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun `clear navigation event should remove navigation event`() {
        // First trigger a navigation event
        viewModel.handleIntent(RegistrationIntent.ContinueAsGuest)
        
        assertTrue(viewModel.navigationEvents.value != null)
        
        // Then clear it
        viewModel.handleIntent(RegistrationIntent.ClearNavigationEvent)
        
        assertEquals(null, viewModel.navigationEvents.value)
    }
}
