package pl.soulsnaps.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            onContinueAsGuest = onContinueAsGuest,
        )

        if (state.isSuccess) {
            onLoginSuccess()
        }
    }
}