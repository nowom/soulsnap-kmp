package pl.soulsnaps.features.dashboard

import pl.soulsnaps.domain.model.Memory

data class EmotionOfTheDay(
    val name: String,
    val emoji: String,
    val description: String
)

data class BiofeedbackData(
    val heartRate: String? = null,
    val sleep: String? = null,
    val steps: String? = null
)

data class DashboardState(
    val userName: String = "Użytkowniku",
    val affirmationOfTheDay: String = "",
    val isAffirmationPlaying: Boolean = false,
    val isOffline: Boolean = false,
    val lastSoulSnap: Memory? = null,
    val emotionOfTheDay: EmotionOfTheDay = EmotionOfTheDay(
        name = "Spokój",
        emoji = "😌",
        description = "Dzisiaj czujesz się spokojny i zrelaksowany."
    ),
    val biofeedbackData: BiofeedbackData = BiofeedbackData(),
    val monthlyUsage: Int = 0,
    val monthlyLimit: Int = 30,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class DashboardIntent {
    object LoadDashboard : DashboardIntent()
    object PlayAffirmation : DashboardIntent()
    object PauseAffirmation : DashboardIntent()
    object ChangeAffirmation : DashboardIntent()
    object FavoriteAffirmation : DashboardIntent()
    object AddNewSnap : DashboardIntent()
    object NavigateToSoulSnaps : DashboardIntent()
    object NavigateToAffirmations : DashboardIntent()
    object NavigateToExercises : DashboardIntent()
    object NavigateToVirtualMirror : DashboardIntent()
    object TakeMoodQuiz : DashboardIntent()
    object ShowNotifications : DashboardIntent()
    object RefreshDashboard : DashboardIntent()
} 