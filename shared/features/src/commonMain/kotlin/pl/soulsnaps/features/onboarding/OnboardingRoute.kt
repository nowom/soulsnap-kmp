package pl.soulsnaps.features.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.soulsnaps.features.auth.navigateToLogin
import pl.soulsnaps.features.auth.navigateToRegistration

@Serializable
data object OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) =
    navigate(OnboardingRoute, navOptions)

fun NavGraphBuilder.onboardingScreen(
    onComplete: () -> Unit,
    onLogin: ()-> Unit,
    onRegister: ()-> Unit,
) {
    composable<OnboardingRoute> {
        OnboardingScreen(onComplete = onComplete, onLogin = onLogin)
    }
} 