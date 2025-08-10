package pl.soulsnaps.features.exersises.plutchikwheel

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ModernEmotionWheelRoute

fun NavController.navigateToModernEmotionWheel(navOptions: NavOptions? = null) =
    navigate(ModernEmotionWheelRoute, navOptions)

fun NavGraphBuilder.modernEmotionWheelScreen(
    onDone: () -> Unit = {}
) {
    composable<ModernEmotionWheelRoute> {
        ModernEmotionWheelScreen(onBackClick = onDone)
    }
}


