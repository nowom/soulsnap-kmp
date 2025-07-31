package pl.soulsnaps.features.coach

import androidx.compose.runtime.Immutable

@Immutable
data class CoachQuizState(
    val selectedEmotion: String? = null,
    val reflection: String? = null,
    val affirmation: String? = null,
    val completedToday: Boolean = false
) 