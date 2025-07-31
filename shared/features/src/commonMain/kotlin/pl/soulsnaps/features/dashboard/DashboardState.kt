package pl.soulsnaps.features.dashboard

import pl.soulsnaps.domain.model.Memory

data class EmotionOfTheDay(
    val name: String,
    val emoji: String,
    val description: String
)

data class DashboardState(
    val affirmationOfTheDay: String = "Jestem spokojem i światłem.",
    val isAffirmationPlaying: Boolean = false,
    val lastSoulSnap: Memory? = null,
    val emotionOfTheDay: EmotionOfTheDay = EmotionOfTheDay(
        name = "Spokój",
        emoji = "😌",
        description = "Dzisiaj czujesz się spokojny i zrelaksowany."
    ),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class DashboardIntent {
    object LoadDashboard : DashboardIntent()
    object PlayAffirmation : DashboardIntent()
    object PauseAffirmation : DashboardIntent()
    object AddNewSnap : DashboardIntent()
    object NavigateToSoulSnaps : DashboardIntent()
    object NavigateToAffirmations : DashboardIntent()
    object NavigateToExercises : DashboardIntent()
    object NavigateToVirtualMirror : DashboardIntent()
} 