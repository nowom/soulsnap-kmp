package pl.soulsnaps.features.affirmation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object AffirmationsRoute

fun NavController.navigateToAffirmations(navOptions: NavOptions? = null) =
    navigate(AffirmationsRoute, navOptions)

fun NavGraphBuilder.affirmationsScreen() {
    composable<AffirmationsRoute> {
        AffirmationsScreen()
    }
}