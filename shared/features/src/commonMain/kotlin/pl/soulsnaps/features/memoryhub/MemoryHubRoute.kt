package pl.soulsnaps.features.memoryhub

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object MemoryHubRoute

fun NavController.navigateToMemoryHub(navOptions: NavOptions? = null) =
    navigate(MemoryHubRoute, navOptions)

fun NavGraphBuilder.memoryHubTab(
    onMemoryClick: (Int) -> Unit,
) {
    composable<MemoryHubRoute> {
        MemoryHubRoute(onMemoryClick)
    }
}
