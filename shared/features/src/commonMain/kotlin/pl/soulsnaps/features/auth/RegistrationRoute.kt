package pl.soulsnaps.features.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.domain.interactor.RegisterUseCase
import org.koin.compose.getKoin
import pl.soulsnaps.access.manager.AppStartupManager

@Serializable
data object RegistrationRoute

fun NavController.navigateToRegistration(navOptions: NavOptions? = null) =
    navigate(RegistrationRoute, navOptions)

fun NavGraphBuilder.registrationScreen(
    onRegistrationSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
) {
    composable<RegistrationRoute> {
        val register: RegisterUseCase = getKoin().get()
        val appStartupManager: AppStartupManager = getKoin().get()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        RegistrationScreen(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            passwordVisible = passwordVisible,
            confirmPasswordVisible = confirmPasswordVisible,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onTogglePasswordVisible = { passwordVisible = !passwordVisible },
            onToggleConfirmPasswordVisible = { confirmPasswordVisible = !confirmPasswordVisible },
            onRegisterClick = {
                // Simple validation
                when {
                    email.isBlank() -> errorMessage = "Email is required"
                    !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email) -> errorMessage = "Invalid email"
                    password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                    password != confirmPassword -> errorMessage = "Passwords do not match"
                    else -> {
                        errorMessage = null
                        isLoading = true
                        // launch coroutine in composition scope
                        scope.launch {
                            try {
                                register(email, password)
                                // Complete onboarding after successful registration
                                appStartupManager.completeOnboarding()
                                onRegistrationSuccess()
                            } catch (e: Throwable) {
                                errorMessage = e.message ?: "Registration failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            onBack = onBack,
            onNavigateToLogin = onNavigateToLogin,
            onContinueAsGuest = {
                // Handle guest continuation with onboarding completion
                scope.launch {
                    try {
                        appStartupManager.skipOnboarding()
                        onContinueAsGuest()
                    } catch (e: Throwable) {
                        errorMessage = e.message ?: "Guest login failed"
                    }
                }
            },
        )
    }
}