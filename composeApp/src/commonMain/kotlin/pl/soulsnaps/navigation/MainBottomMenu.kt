package pl.soulsnaps.navigation


import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlin.reflect.KClass

@Composable
fun MainBottomMenu(
    destinations: List<BottomNavItem>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (BottomNavItem) -> Unit,
) {
    NavigationBar(
        tonalElevation = 3.dp,
        contentColor = Color.Red,
        containerColor = Color.White,
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination
                .isRouteInHierarchy(destination.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(destination.title)
                }
            )
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false