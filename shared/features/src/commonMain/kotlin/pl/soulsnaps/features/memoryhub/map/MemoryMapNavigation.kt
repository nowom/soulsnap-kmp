package pl.soulsnaps.features.memoryhub.map

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object MemoryMapRoute

fun NavController.navigateToMemoryMap(navOptions: NavOptions? = null) =
    navigate(MemoryMapRoute, navOptions)

fun NavGraphBuilder.memoryMapScreen(
    onMemoryClick: (Int) -> Unit,
) {
    // TODO: Handle back stack for each top-level destination. At the moment each top-level
    // destination may have own search screen's back stack.
    composable<MemoryMapRoute> {
        MemoryMapRoute(onMemoryClick)
    }
}
