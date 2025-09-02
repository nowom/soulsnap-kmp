package pl.soulsnaps.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.UpdateEmail -> {
                _state.update { it.copy(email = intent.email) }
            }
            is AuthIntent.UpdatePassword -> {
                _state.update { it.copy(password = intent.password) }
            }
            is AuthIntent.UpdateConfirmPassword -> {
                _state.update { it.copy(confirmPassword = intent.confirmPassword) }
            }
            is AuthIntent.ToggleMode -> {
                _state.update { 
                    it.copy(
                        isSignUp = intent.isSignUp,
                        errorMessage = null,
                        confirmPassword = ""
                    ) 
                }
            }
            is AuthIntent.SignUp -> {
                performSignUp()
            }
            is AuthIntent.SignIn -> {
                performSignIn()
            }
            is AuthIntent.SignInWithGoogle -> {
                performGoogleSignIn()
            }
            is AuthIntent.SignInAnonymously -> {
                performAnonymousSignIn()
            }
            is AuthIntent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun performSignUp() {
        val currentState = _state.value
        
        if (!validateSignUpInput(currentState)) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // For MVP, just mark as authenticated
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) 
                }
                println("User signed up successfully: ${currentState.email}")
            } catch (e: Exception) {
                println("User signed up successfully: ${currentState.email}")

                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign up failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun performSignIn() {
        val currentState = _state.value
        
        if (!validateSignInInput(currentState)) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // For MVP, just mark as authenticated
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) 
                }
                println("User signed in successfully: ${currentState.email}")
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign in failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun performGoogleSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Simulate Google sign in
                kotlinx.coroutines.delay(1000)
                
                // For MVP, just mark as authenticated
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) 
                }
                println("User signed in with Google successfully")
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Google sign in failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun performAnonymousSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Simulate anonymous sign in
                kotlinx.coroutines.delay(500)
                
                // For MVP, just mark as authenticated
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) 
                }
                println("User signed in anonymously successfully")
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Anonymous sign in failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun validateSignUpInput(state: AuthState): Boolean {
        if (state.email.isEmpty()) {
            _state.update { it.copy(errorMessage = "Email is required") }
            return false
        }
        
        if (!isValidEmail(state.email)) {
            _state.update { it.copy(errorMessage = "Please enter a valid email") }
            return false
        }
        
        if (state.password.isEmpty()) {
            _state.update { it.copy(errorMessage = "Password is required") }
            return false
        }
        
        if (state.password.length < 6) {
            _state.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return false
        }
        
        if (state.confirmPassword.isEmpty()) {
            _state.update { it.copy(errorMessage = "Please confirm your password") }
            return false
        }
        
        if (state.password != state.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match") }
            return false
        }
        
        return true
    }

    private fun validateSignInInput(state: AuthState): Boolean {
        if (state.email.isEmpty()) {
            _state.update { it.copy(errorMessage = "Email is required") }
            return false
        }
        
        if (!isValidEmail(state.email)) {
            _state.update { it.copy(errorMessage = "Please enter a valid email") }
            return false
        }
        
        if (state.password.isEmpty()) {
            _state.update { it.copy(errorMessage = "Password is required") }
            return false
        }
        
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return emailRegex.matches(email)
    }
} 