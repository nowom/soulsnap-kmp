package pl.soulsnaps.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.interactor.SignInUseCase
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.features.auth.GuestToUserMigration
import pl.soulsnaps.features.auth.MigrationResult

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginIntent {
    data class UpdateEmail(val email: String) : LoginIntent
    data class UpdatePassword(val password: String) : LoginIntent
    data object TogglePasswordVisible : LoginIntent
    data object Submit : LoginIntent
    data object ContinueAsGuest : LoginIntent
    data object ClearError : LoginIntent
    data object ClearNavigationEvent : LoginIntent
}

sealed interface LoginNavigationEvent {
    data object NavigateToDashboard : LoginNavigationEvent
}

class LoginViewModel(
    private val signInUseCase: SignInUseCase,
    private val startupRepository: StartupRepository,
    private val guestToUserMigration: GuestToUserMigration
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _navigationEvents = MutableStateFlow<LoginNavigationEvent?>(null)
    val navigationEvents: StateFlow<LoginNavigationEvent?> = _navigationEvents.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> _state.update { it.copy(email = intent.email) }
            is LoginIntent.UpdatePassword -> _state.update { it.copy(password = intent.password) }
            is LoginIntent.TogglePasswordVisible -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is LoginIntent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is LoginIntent.ClearNavigationEvent -> _navigationEvents.update { null }
            is LoginIntent.Submit -> submit()
            is LoginIntent.ContinueAsGuest -> continueAsGuest()
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
                val userSession = signInUseCase(email, password)
                
                // Check if guest migration is needed (user was a guest before logging in)
                if (guestToUserMigration.isMigrationNeeded()) {
                    println("DEBUG: LoginViewModel - Guest migration needed, starting migration...")
                    
                    val migrationResult = guestToUserMigration.migrateGuestToUser(userSession)
                    
                    when (migrationResult) {
                        is MigrationResult.Success -> {
                            println("DEBUG: LoginViewModel - Migration successful: ${migrationResult.message}")
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "✓ ${migrationResult.message}"
                                ) 
                            }
                        }
                        is MigrationResult.Error -> {
                            println("ERROR: LoginViewModel - Migration failed: ${migrationResult.message}")
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Uwaga: ${migrationResult.message}"
                                ) 
                            }
                        }
                    }
                    
                    // Give user time to see the message
                    kotlinx.coroutines.delay(2000)
                }
                
                // Complete onboarding after successful login
                startupRepository.completeOnboarding()
                _state.update { it.copy(isLoading = false, errorMessage = null) }
                _navigationEvents.update { LoginNavigationEvent.NavigateToDashboard }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Login failed") }
            }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Użyj StartupRepository.skipOnboarding() tak jak w onboardingu
                startupRepository.skipOnboarding()
                _state.update { it.copy(isLoading = false) }
                _navigationEvents.update { LoginNavigationEvent.NavigateToDashboard }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Guest login failed") }
            }
        }
    }
}


