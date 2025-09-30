package pl.soulsnaps.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.RegisterUseCase
import pl.soulsnaps.domain.StartupRepository

data class RegistrationUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface RegistrationIntent {
    data class UpdateEmail(val email: String) : RegistrationIntent
    data class UpdatePassword(val password: String) : RegistrationIntent
    data class UpdateConfirmPassword(val confirmPassword: String) : RegistrationIntent
    data object TogglePasswordVisible : RegistrationIntent
    data object ToggleConfirmPasswordVisible : RegistrationIntent
    data object Submit : RegistrationIntent
    data object ContinueAsGuest : RegistrationIntent
    data object ClearError : RegistrationIntent
    data object ClearNavigationEvent : RegistrationIntent
}

sealed interface RegistrationNavigationEvent {
    data object NavigateToDashboard : RegistrationNavigationEvent
}

class RegistrationViewModel(
    private val registerUseCase: RegisterUseCase,
    private val startupRepository: StartupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    private val _navigationEvents = MutableStateFlow<RegistrationNavigationEvent?>(null)
    val navigationEvents: StateFlow<RegistrationNavigationEvent?> = _navigationEvents.asStateFlow()

    fun handleIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.UpdateEmail -> _state.update { it.copy(email = intent.email) }
            is RegistrationIntent.UpdatePassword -> _state.update { it.copy(password = intent.password) }
            is RegistrationIntent.UpdateConfirmPassword -> _state.update { it.copy(confirmPassword = intent.confirmPassword) }
            is RegistrationIntent.TogglePasswordVisible -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is RegistrationIntent.ToggleConfirmPasswordVisible -> _state.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            is RegistrationIntent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is RegistrationIntent.ClearNavigationEvent -> _navigationEvents.update { null }
            is RegistrationIntent.Submit -> submit()
            is RegistrationIntent.ContinueAsGuest -> continueAsGuest()
        }
    }

    private fun submit() {
        val s = _state.value
        val email = s.email.trim()
        val password = s.password
        val confirmPassword = s.confirmPassword
        
        // Validation
        when {
            email.isBlank() -> {
                _state.update { it.copy(errorMessage = "Email is required") }
                return
            }
            !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email) -> {
                _state.update { it.copy(errorMessage = "Invalid email") }
                return
            }
            password.length < 6 -> {
                _state.update { it.copy(errorMessage = "Password must be at least 6 characters") }
                return
            }
            password != confirmPassword -> {
                _state.update { it.copy(errorMessage = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                registerUseCase(email, password)
                // Complete onboarding after successful registration
                startupRepository.completeOnboarding()
                _state.update { it.copy(isLoading = false) }
                _navigationEvents.update { RegistrationNavigationEvent.NavigateToDashboard }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Registration failed") }
            }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                startupRepository.skipOnboarding()
                _state.update { it.copy(isLoading = false) }
                _navigationEvents.update { RegistrationNavigationEvent.NavigateToDashboard }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Guest login failed") }
            }
        }
    }
}
