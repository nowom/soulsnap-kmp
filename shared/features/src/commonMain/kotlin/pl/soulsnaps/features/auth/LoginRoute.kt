package pl.soulsnaps.features.auth

import androidx.compose.runtime.Composable
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
    onLoginSuccess: () -> Unit, ) {
    composable<LoginRoute> {
        LoginScreen()
    }
}