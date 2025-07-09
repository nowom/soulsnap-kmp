package pl.soulsnaps.features.memoryhub.gallery

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlin.invoke

@Serializable
data object MomentsGalleryRoute

fun NavController.navigateToMomentsGallery(navOptions: NavOptions? = null) =
    navigate(MomentsGalleryRoute, navOptions)

fun NavGraphBuilder.momentsGalleryScreen(
    onMemoryClick: (Int) -> Unit,
    onAddMemoryClick: () -> Unit
) {
    composable<MomentsGalleryRoute> {
        MomentsGalleryRoute(onMemoryClick, onAddMemoryClick)
    }
}