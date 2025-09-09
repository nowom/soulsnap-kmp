package pl.soulsnaps.features.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import pl.soulsnaps.features.onboarding.OnboardingRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    navigate(SettingsRoute, navOptions)

fun NavGraphBuilder.settingsScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToUpgrade = onNavigateToUpgrade,
            onNavigateToAuth = onNavigateToAuth
        )
    }
}