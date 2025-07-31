package pl.soulsnaps.features.exersises

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ExerciseRoute

fun NavController.navigateToExercises(navOptions: NavOptions? = null) =
    navigate(ExerciseRoute, navOptions)

fun NavGraphBuilder.exercisesScreen(
    onOpenBreathing: () -> Unit,
    onOpenGratitude: () -> Unit
) {
    composable<ExerciseRoute> {
        ExercisesRoute(onOpenBreathing, onOpenGratitude)
    }
}