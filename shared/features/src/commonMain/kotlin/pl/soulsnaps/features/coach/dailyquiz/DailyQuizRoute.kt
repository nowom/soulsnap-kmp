package pl.soulsnaps.features.coach.dailyquiz

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/**
 * Navigation route for Daily Emotion Quiz
 */
@Serializable
data object DailyQuizRoute

/**
 * Navigation function for Daily Quiz
 */
fun NavController.navigateToDailyQuiz(navOptions: NavOptions? = null) =
    navigate(DailyQuizRoute, navOptions)

/**
 * Navigation extension for Daily Quiz screen
 */
fun NavGraphBuilder.dailyQuizScreen(
    onBack: () -> Unit = {},
    onCompleted: () -> Unit = {}
) {
    composable<DailyQuizRoute> {
        DailyQuizScreen(
            onBack = onBack,
            onCompleted = onCompleted
        )
    }
}
