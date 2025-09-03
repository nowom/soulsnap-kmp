package pl.soulsnaps.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
data object AuthenticationGraph

@Serializable
data object HomeGraph

// Navigation functions for switching between graphs
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    navigate(OnboardingGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
}

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(AuthenticationGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
}

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeGraph, navOptions ?: navOptions {
        popUpTo(0) { inclusive = true }
    })
}

// Helper function to determine start destination based on app state
fun getStartDestination(hasCompletedOnboarding: Boolean, isAuthenticated: Boolean): Any {
    return when {
        !hasCompletedOnboarding -> OnboardingGraph
        !isAuthenticated -> AuthenticationGraph
        else -> HomeGraph
    }
} 