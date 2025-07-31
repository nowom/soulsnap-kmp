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

fun NavGraphBuilder.coachQuizScreen(
    onDone: () -> Unit = {}
) {
    composable("coachQuiz") {
        CoachQuizScreen(onDone = onDone)
    }
}

fun NavGraphBuilder.breathingSessionScreen(onDone: () -> Unit = {}) {
    composable("breathingSession") {
        BreathingSessionScreenPolished(onBack = onDone)
    }
}

fun NavGraphBuilder.emotionWheelScreen(onDone: () -> Unit = {}) {
    composable("emotionWheel") {
        EmotionWheelScreen(onBack = onDone)
    }
}

fun NavGraphBuilder.gratitudeScreen(onDone: () -> Unit = {}) {
    composable("gratitude") {
        GratitudeScreen(onBack = onDone)
    }
} 