package pl.soulsnaps.features.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

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
        val viewModel: RegistrationViewModel = koinViewModel()
        val uiState by viewModel.state.collectAsStateWithLifecycle()
        val navigationEvents by viewModel.navigationEvents.collectAsStateWithLifecycle()

        // Handle navigation events
        LaunchedEffect(navigationEvents) {
            when (navigationEvents) {
                RegistrationNavigationEvent.NavigateToDashboard -> {
                    onRegistrationSuccess()
                }
                null -> { /* No action needed */ }
            }
        }

        RegistrationScreen(
            email = uiState.email,
            password = uiState.password,
            confirmPassword = uiState.confirmPassword,
            passwordVisible = uiState.passwordVisible,
            confirmPasswordVisible = uiState.confirmPasswordVisible,
            errorMessage = uiState.errorMessage,
            isLoading = uiState.isLoading,
            onEmailChange = { viewModel.handleIntent(RegistrationIntent.UpdateEmail(it)) },
            onPasswordChange = { viewModel.handleIntent(RegistrationIntent.UpdatePassword(it)) },
            onConfirmPasswordChange = { viewModel.handleIntent(RegistrationIntent.UpdateConfirmPassword(it)) },
            onTogglePasswordVisible = { viewModel.handleIntent(RegistrationIntent.TogglePasswordVisible) },
            onToggleConfirmPasswordVisible = { viewModel.handleIntent(RegistrationIntent.ToggleConfirmPasswordVisible) },
            onRegisterClick = { viewModel.handleIntent(RegistrationIntent.Submit) },
            onBack = onBack,
            onNavigateToLogin = onNavigateToLogin,
            onContinueAsGuest = { viewModel.handleIntent(RegistrationIntent.ContinueAsGuest) },
        )
    }
}