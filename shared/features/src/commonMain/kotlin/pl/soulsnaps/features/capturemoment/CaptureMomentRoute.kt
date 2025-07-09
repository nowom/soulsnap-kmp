package pl.soulsnaps.features.capturemoment

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object CaptureMomentRoute

fun NavController.navigateToCaptureMoment(navOptions: NavOptions? = null) =
    navigate(CaptureMomentRoute, navOptions)

fun NavGraphBuilder.captureMomentScreen() {
    composable<CaptureMomentRoute> {
        AddMemoryScreen()
    }
}