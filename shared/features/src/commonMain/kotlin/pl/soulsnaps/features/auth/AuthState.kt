package pl.soulsnaps.features.auth

import androidx.compose.runtime.Immutable

@Immutable
data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUp: Boolean = true, // true for sign up, false for login
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

sealed class AuthIntent {
    data class UpdateEmail(val email: String) : AuthIntent()
    data class UpdatePassword(val password: String) : AuthIntent()
    data class UpdateConfirmPassword(val confirmPassword: String) : AuthIntent()
    data class ToggleMode(val isSignUp: Boolean) : AuthIntent()
    object SignUp : AuthIntent()
    object SignIn : AuthIntent()
    object SignInWithGoogle : AuthIntent()
    object SignInAnonymously : AuthIntent()
    object ClearError : AuthIntent()
} 