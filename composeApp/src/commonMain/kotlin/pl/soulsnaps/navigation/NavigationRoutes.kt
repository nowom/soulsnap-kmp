package pl.soulsnaps.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import pl.soulsnaps.features.affirmation.AffirmationsRoute
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.exersises.ExerciseRoute
import pl.soulsnaps.features.memoryhub.MemoryHubRoute
import kotlin.reflect.KClass

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Home", "🏠")
    object SoulSnaps : Screen("snaps", "Snapy", "📷")
    object Affirmations : Screen("affirmations", "Afirmacje", "🎧")
    object Profile : Screen("profile", "Profil", "⚙️")
    object Coach : Screen("coach", "Coach", "🧠")

    companion object {
        val bottomNav = listOf(Dashboard, SoulSnaps, Affirmations, Profile)
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onItemSelected: (Screen) -> Unit
) {
    NavigationBar {
        Screen.bottomNav.filterNotNull().forEach { screen ->
            NavigationBarItem(
                selected = screen == currentScreen,
                onClick = { onItemSelected(screen) },
                icon = {
                    //Icon(DsIcons.Add(), contentDescription = "Dodaj Snap")
                },
                label = { Text(screen.label) }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: KClass<*>
)

val bottomNavItems = listOf(
    BottomNavItem("Home", DashboardRoute::class),
    BottomNavItem("Soul Snap", MemoryHubRoute::class),
    BottomNavItem("Affirmations", AffirmationsRoute::class),
    BottomNavItem(" Exercise", ExerciseRoute::class),
)
