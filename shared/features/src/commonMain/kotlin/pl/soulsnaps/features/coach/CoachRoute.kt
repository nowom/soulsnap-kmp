package pl.soulsnaps.features.coach

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object CoachRoute

fun NavController.navigateToCoach(navOptions: NavOptions? = null) =
    navigate(CoachRoute, navOptions)

fun NavGraphBuilder.coachScreen(
    onStartQuiz: () -> Unit = {},
    onOpenWheel: () -> Unit = {},
    onOpenBreathing: () -> Unit = {},
    onOpenGratitude: () -> Unit = {},
    quizCompletedToday: Boolean = false,
    streak: Int = 0
) {
    composable<CoachRoute> {
        CoachScreen(
            onStartQuiz = onStartQuiz,
            onOpenWheel = onOpenWheel,
            onOpenBreathing = onOpenBreathing,
            onOpenGratitude = onOpenGratitude,
            quizCompletedToday = quizCompletedToday,
            streak = streak
        )
    }
}

@Serializable
data object CoachQuizRoute

fun NavController.navigateToCoachQuiz(navOptions: NavOptions? = null) =
    navigate(CoachQuizRoute, navOptions)

fun NavGraphBuilder.coachQuizScreen(
    onDone: () -> Unit = {}
) {
    composable<CoachQuizRoute> {
        CoachQuizScreen(onDone = onDone)
    }
}

@Serializable
data object BreathingSessionRoute

fun NavController.navigateToBreathingSession(navOptions: NavOptions? = null) =
    navigate(BreathingSessionRoute, navOptions)

fun NavGraphBuilder.breathingSessionScreen(onDone: () -> Unit = {}) {
    composable<BreathingSessionRoute> {
        BreathingSessionScreenPolished(onBack = onDone)
    }
}

@Serializable
data object CoachEmotionWheelRoute

fun NavController.navigateToCoachEmotionWheel(navOptions: NavOptions? = null) =
    navigate(CoachEmotionWheelRoute, navOptions)

fun NavGraphBuilder.emotionWheelScreen(onDone: () -> Unit = {}) {
    composable<CoachEmotionWheelRoute> {
        EmotionWheelScreen(onBack = onDone)
    }
}

@Serializable
data object GratitudeRoute

fun NavController.navigateToGratitude(navOptions: NavOptions? = null) =
    navigate(GratitudeRoute, navOptions)

fun NavGraphBuilder.gratitudeScreen(onDone: () -> Unit = {}) {
    composable<GratitudeRoute> {
        GratitudeScreen(onBack = onDone)
    }
}