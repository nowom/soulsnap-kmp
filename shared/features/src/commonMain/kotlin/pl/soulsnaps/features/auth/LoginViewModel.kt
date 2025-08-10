package pl.soulsnaps.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.SignInUseCase

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

sealed interface LoginIntent {
    data class UpdateEmail(val email: String) : LoginIntent
    data class UpdatePassword(val password: String) : LoginIntent
    data object TogglePasswordVisible : LoginIntent
    data object Submit : LoginIntent
    data object ClearError : LoginIntent
}

class LoginViewModel(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> _state.update { it.copy(email = intent.email) }
            is LoginIntent.UpdatePassword -> _state.update { it.copy(password = intent.password) }
            is LoginIntent.TogglePasswordVisible -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is LoginIntent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = _state.value
        val email = s.email.trim()
        val password = s.password
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        when {
            email.isEmpty() -> {
                _state.update { it.copy(errorMessage = "Email is required") }
                return
            }
            !emailRegex.matches(email) -> {
                _state.update { it.copy(errorMessage = "Invalid email") }
                return
            }
            password.isEmpty() -> {
                _state.update { it.copy(errorMessage = "Password is required") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                signInUseCase(email, password)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Login failed") }
            }
        }
    }
}


