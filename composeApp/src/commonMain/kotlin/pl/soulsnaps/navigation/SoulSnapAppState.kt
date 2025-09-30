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
import pl.soulsnaps.features.settings.SettingsRoute
import pl.soulsnaps.features.settings.navigateToSettings
import pl.soulsnaps.navigation.HomeGraph
import kotlin.reflect.KClass

@Stable
internal class SoulSnapAppState(
    val navController: NavHostController
) {
    private val previousDestination = mutableStateOf<NavDestination?>(null)

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

    val shouldShowBottomBar: Boolean
        @Composable get() {
            // Show bottom bar only when we're in the HomeGraph (main app)
            // Check if we can find a top level destination
            val currentRoute = currentDestination?.route
            return bottomNavItems.any { topLevelDestination ->
                currentRoute == topLevelDestination.route.qualifiedName
            }
        }

    val currentTopLevelDestination: BottomNavItem?
        @Composable get() {
            // Only show top level destinations when we're in the HomeGraph
            if (!shouldShowBottomBar) return null
            
            val currentRoute = currentDestination?.route
            return bottomNavItems.firstOrNull { topLevelDestination ->
                currentRoute == topLevelDestination.route.qualifiedName
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
            // Pop up to the HomeGraph start destination to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            // This prevents bottom menu items from being stacked
            popUpTo(HomeGraph) {
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
            SettingsRoute::class -> navController.navigateToSettings()
        }
    }

    fun navigateToAddSnap() {
        navOptions {
            // Pop up to the HomeGraph start destination to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(HomeGraph) {
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

    fun navigateToMemoryHub() {
        navOptions {
            // Pop up to the HomeGraph start destination to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(HomeGraph) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        navController.navigateToMemoryHub()
    }

    val shouldShowFab: Boolean
        @Composable get() {
            // Show FAB only when we're in the HomeGraph and on appropriate screens
            if (!shouldShowBottomBar) return false
            
            val currentRoute = currentDestination?.route
            // Show FAB on main screens but not on settings
            return currentRoute == DashboardRoute::class.qualifiedName ||
                    currentRoute == AffirmationsRoute::class.qualifiedName ||
                    currentRoute == MemoryHubRoute::class.qualifiedName ||
                    currentRoute == ExerciseRoute::class.qualifiedName
        }
}

@Composable
internal fun rememberAppState(): SoulSnapAppState {
    val navController = rememberNavController()
    return remember { SoulSnapAppState(navController) }
}


