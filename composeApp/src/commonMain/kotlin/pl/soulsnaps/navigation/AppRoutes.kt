package pl.soulsnaps.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
data object AuthGraph

@Serializable
data object MainAppGraph

// Navigation functions for switching between graphs
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    navigate(OnboardingGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
}

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(AuthGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
}

fun NavController.navigateToMainApp(navOptions: NavOptions? = null) {
    navigate(MainAppGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
} 