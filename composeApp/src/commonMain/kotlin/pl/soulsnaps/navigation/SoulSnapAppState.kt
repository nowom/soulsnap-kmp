package pl.soulsnaps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import pl.soulsnaps.features.affirmation.AffirmationsRoute
import pl.soulsnaps.features.affirmation.navigateToAffirmations
import pl.soulsnaps.features.capturemoment.CaptureMomentRoute
import pl.soulsnaps.features.capturemoment.navigateToCaptureMoment
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.dashboard.navigateToDashboard
import pl.soulsnaps.features.exersises.ExerciseRoute
import pl.soulsnaps.features.exersises.navigateToExercises
import pl.soulsnaps.features.memoryhub.MemoryHubRoute
import pl.soulsnaps.features.memoryhub.navigateToMemoryHub
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.auth.LoginRoute
import pl.soulsnaps.features.coach.BreathingSessionRoute
import pl.soulsnaps.features.coach.GratitudeRoute
import pl.soulsnaps.features.exersises.plutchikwheel.ModernEmotionWheelRoute
import kotlin.reflect.KClass

@Stable
internal class SoulSnapAppState(
    val navController: NavHostController
) {
    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val shouldShowBottomBar: Boolean
        @Composable get() = currentTopLevelDestination?.route?.let {
            currentDestination?.hasRoute(route = it) == true
        } ?: false

    val currentDestination: NavDestination?
        @Composable get() {
            // Collect the currentBackStackEntryFlow as a state
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            // Fallback to previousDestination if currentEntry is null
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val currentTopLevelDestination: BottomNavItem?
        @Composable get() {
            return bottomNavItems.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(route = topLevelDestination.route) == true
            }
        }

    val topLevelDestinations: List<BottomNavItem> = bottomNavItems

//    val currentTopLevelDestination: TabsDestinations?
//        @Composable get() = when (currentDestination?.route) {
//            dashboardNavigationRoute -> TabsDestinations.Dashboard
//            else -> null
//        }


    fun navigateToTopLevelDestination(navItem: BottomNavItem) {
        navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        when (navItem.route) {
            DashboardRoute::class -> navController.navigateToDashboard()
            AffirmationsRoute::class -> navController.navigateToAffirmations()
            CaptureMomentRoute::class -> navController.navigateToCaptureMoment()
            MemoryHubRoute::class -> navController.navigateToMemoryHub()
            ExerciseRoute::class -> navController.navigateToExercises()
        }
    }

    fun navigateToAddSnap() {
        navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        navController.navigateToCaptureMoment()
    }

    val shouldShowFab: Boolean
        @Composable get() {
            val currentRoute = currentDestination?.route
            return currentRoute !in listOf(
                CaptureMomentRoute::class.qualifiedName,
                OnboardingRoute::class.qualifiedName,
                LoginRoute::class.qualifiedName,
                ModernEmotionWheelRoute::class.qualifiedName,
                BreathingSessionRoute::class.qualifiedName,
                GratitudeRoute::class.qualifiedName,
            )
        }
}

@Composable
internal fun rememberAppState(): SoulSnapAppState {
    val navController = rememberNavController()
    return remember { SoulSnapAppState(navController) }
}


