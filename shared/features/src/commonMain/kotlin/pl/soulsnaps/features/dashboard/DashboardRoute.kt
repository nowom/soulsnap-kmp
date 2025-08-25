package pl.soulsnaps.features.dashboard

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable data object DashboardRouteBaseRoute // route to base navigation graph

fun NavController.navigateToBaseDashboard(navOptions: NavOptions? = null) =
    navigate(DashboardRouteBaseRoute, navOptions)

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    navigate(DashboardRoute, navOptions)

fun NavGraphBuilder.dashboardScreen(
    onAddNewSnap: () -> Unit = {},
    onNavigateToSoulSnaps: () -> Unit = {},
    onNavigateToAffirmations: () -> Unit = {},
    onNavigateToExercises: () -> Unit = {},
    onNavigateToVirtualMirror: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onUpgradePlan: () -> Unit = {}
) {
    composable<DashboardRoute> {
        DashboardScreen(
            onAddNewSnap = onAddNewSnap,
            onNavigateToSoulSnaps = onNavigateToSoulSnaps,
            onNavigateToAffirmations = onNavigateToAffirmations,
            onNavigateToExercises = onNavigateToExercises,
            onNavigateToVirtualMirror = onNavigateToVirtualMirror,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onUpgradePlan = onUpgradePlan
        )
    }
}