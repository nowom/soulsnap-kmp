package pl.soulsnaps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import pl.soulsnaps.features.affirmation.affirmationsScreen
import pl.soulsnaps.features.capturemoment.captureMomentScreen
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.dashboard.dashboardScreen
import pl.soulsnaps.features.exersises.exercisesScreen
import pl.soulsnaps.features.memoryhub.memoryHubTab

@Composable
internal fun SoulSnapNavHost(
    appState: SoulSnapAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier,
    ) {
        dashboardScreen()
        captureMomentScreen()
        affirmationsScreen()
        memoryHubTab({

        })
        exercisesScreen()
    }
}
