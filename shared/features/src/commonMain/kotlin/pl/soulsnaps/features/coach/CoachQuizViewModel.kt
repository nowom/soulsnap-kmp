package pl.soulsnaps.features.coach

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CoachQuizViewModel : ViewModel() {
    var state by mutableStateOf(CoachQuizState())
        private set

    private val reflections = mapOf(
        "Radość" to "Ciesz się tą chwilą i podziel się radością z innymi.",
        "Smutek" to "To w porządku czuć smutek. Daj sobie czas na regenerację.",
        "Złość" to "Spróbuj wyrazić swoją złość w zdrowy sposób.",
        "Strach" to "Zastanów się, co możesz zrobić, by poczuć się bezpieczniej.",
        "Spokój" to "Doceniaj ten stan i spróbuj go utrzymać."
    )
    private val affirmations = mapOf(
        "Radość" to "Zasługuję na szczęście.",
        "Smutek" to "Jestem wystarczający, nawet gdy jest mi smutno.",
        "Złość" to "Potrafię panować nad swoimi emocjami.",
        "Strach" to "Mam w sobie odwagę.",
        "Spokój" to "Oddycham spokojem."
    )

    fun selectEmotion(emotion: String) {
        state = state.copy(
            selectedEmotion = emotion,
            reflection = reflections[emotion] ?: "Zadbaj o siebie.",
            affirmation = affirmations[emotion] ?: "Jestem wartościowy.",
            completedToday = true
        )
    }

    fun resetQuiz() {
        state = CoachQuizState()
    }
} 