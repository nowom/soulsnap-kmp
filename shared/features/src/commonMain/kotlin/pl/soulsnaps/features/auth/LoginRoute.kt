package pl.soulsnaps.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.onboarding.OnboardingScreen
import pl.soulsnaps.features.auth.LoginIntent

@Serializable
data object LoginRoute

fun NavController.navigateToLogin(navOptions: NavOptions? = null) =
    navigate(LoginRoute, navOptions)

fun NavGraphBuilder.loginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
) {
    composable<LoginRoute> {
        val vm: LoginViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()

        LoginScreen(
            email = state.email,
            password = state.password,
            passwordVisible = state.passwordVisible,
            onEmailChange = { vm.handleIntent(LoginIntent.UpdateEmail(it)) },
            onPasswordChange = { vm.handleIntent(LoginIntent.UpdatePassword(it)) },
            onTogglePasswordVisible = { vm.handleIntent(LoginIntent.TogglePasswordVisible) },
            onLoginClick = { vm.handleIntent(LoginIntent.Submit) },
            onBack = onBack,
            onNavigateToRegister = onNavigateToRegister,
            onContinueAsGuest = { vm.handleIntent(LoginIntent.ContinueAsGuest) },
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
        )

        val navigationEvent by vm.navigationEvents.collectAsStateWithLifecycle()
        
        LaunchedEffect(navigationEvent) {
            when (val event = navigationEvent) {
                is LoginNavigationEvent.NavigateToDashboard -> {
                    onLoginSuccess()
                    // Clear the navigation event after consuming it
                    vm.handleIntent(LoginIntent.ClearNavigationEvent)
                }
                null -> { /* No navigation event */ }
            }
        }
    }
}