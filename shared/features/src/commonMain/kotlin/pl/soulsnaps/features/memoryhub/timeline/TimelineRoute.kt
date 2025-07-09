package pl.soulsnaps.features.memoryhub.timeline

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TimelineRoute

fun NavController.navigateToTimeline(navOptions: NavOptions? = null) =
    navigate(TimelineRoute, navOptions)

fun NavGraphBuilder.timelineScreen(
    onMemoryDetailsClick: (Int) -> Unit,
) {
    // TODO: Handle back stack for each top-level destination. At the moment each top-level
    // destination may have own search screen's back stack.
    composable<TimelineRoute> {
        TimelineRoute(onMemoryDetailsClick)
    }
}
