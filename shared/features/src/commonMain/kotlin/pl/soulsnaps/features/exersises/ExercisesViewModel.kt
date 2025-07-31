package pl.soulsnaps.features.exersises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.soulsnaps.domain.ExerciseRepository
import pl.soulsnaps.domain.interactor.GetCompletedExercisesUseCase
import pl.soulsnaps.domain.interactor.MarkExerciseCompletedUseCase
import pl.soulsnaps.domain.model.BreathingPhase
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.EmotionData
import pl.soulsnaps.domain.model.Exercise
import pl.soulsnaps.domain.model.Ritual
import kotlin.time.Duration.Companion.minutes

/**
 * ViewModel dla zakładki "Ćwiczenia i Nauka".
 * ViewModel for the "Exercises and Learning" tab.
 */
class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val markExerciseCompletedUseCase: MarkExerciseCompletedUseCase,
    private val getCompletedExercisesUseCase: GetCompletedExercisesUseCase
): ViewModel() {
    private val _state = MutableStateFlow(EmotionExerciseState())
    val state: StateFlow<EmotionExerciseState> = _state.asStateFlow()

    private var breathingTimerJob: Job? = null

    init {
        // Inicjalizacja danych emocji z jednego źródła
        // Initialize emotion data from single source
        _state.update {
            it.copy(
                emotions = EmotionData.getAllBasicEmotions()
            )
        }

        // Obserwowanie wszystkich ćwiczeń i ukończonych ćwiczeń
        // Observe all exercises and completed exercises
        viewModelScope.launch {
            combine(
                exerciseRepository.getAllExercises(),
                getCompletedExercisesUseCase()
            ) { allExercises, completedIds ->
                allExercises.map { exercise ->
                    exercise.copy(isCompleted = completedIds.contains(exercise.id))
                }
            }.collect { updatedExercises ->
                _state.update { it.copy(exercises = updatedExercises) }
            }
        }

        // Inicjalizacja rytuałów
        // Initialize rituals
        _state.update {
            it.copy(
                rituals = listOf(
                    Ritual(
                        "morning_ritual",
                        "🌅 Poranny rytuał",
                        "Rozpocznij dzień z pozytywną energią.",
                        listOf(
                            Exercise("ex1", "Oddech 4-7-8", "", EmotionCategory.SURPRISE),
                            Exercise("ex4", "Afirmacje Poranne", "", EmotionCategory.JOY)
                        )
                    ),
                    Ritual(
                        "evening_ritual",
                        "🌙 Wieczorny rytuał",
                        "Zakończ dzień spokojem i wdzięcznością.",
                        listOf(
                            Exercise("ex2", "Dziennik Wdzięczności", "", EmotionCategory.JOY),
                            Exercise("ex3", "Skan Ciała", "", EmotionCategory.SURPRISE)
                        )
                    )
                )
            )
        }
    }

    /**
     * Aktualizuje wybrane emocje w kole emocji.
     * Updates selected emotions in the emotion wheel.
     */
    fun onEmotionSelectionChanged(selectedIds: Set<String>) {
        _state.update { it.copy(selectedEmotionWheelIds = selectedIds) }
    }

    /**
     * Ustawia wybraną emocję z quizu.
     * Sets the selected emotion from the quiz.
     */
    fun onQuizEmotionSelected(emotionCategory: EmotionCategory?) {
        _state.update { it.copy(selectedQuizEmotion = emotionCategory) }
        // Tutaj można dodać logikę do sugerowania ćwiczeń na podstawie wybranej emocji
        // Here you can add logic to suggest exercises based on the selected emotion
    }

    /**
     * Oznacza ćwiczenie jako ukończone/nieukończone.
     * Marks an exercise as completed/incomplete.
     */
    fun onExerciseCompleted(exerciseId: String, completed: Boolean) {
        viewModelScope.launch {
            markExerciseCompletedUseCase(exerciseId, completed)
        }
    }

    /**
     * Rozpoczyna sesję oddechową.
     * Starts the breathing session.
     */
    fun startBreathingSession() {
        _state.update { it.copy(isBreathingSessionActive = true, breathingTimeRemaining = 2.minutes.inWholeSeconds.toInt()) }
        breathingTimerJob?.cancel()
        breathingTimerJob = viewModelScope.launch {
            val totalDuration = 2.minutes.inWholeSeconds
            var elapsed = 0L

            while (elapsed < totalDuration) {
                val phaseDuration = when (_state.value.breathingPhase) {
                    BreathingPhase.INHALE -> 4L
                    BreathingPhase.HOLD -> 7L
                    BreathingPhase.EXHALE -> 8L
                    BreathingPhase.PAUSE -> 0L // Pause is instant transition
                }
                _state.update { it.copy(breathingPhaseProgress = 0f) } // Reset progress for new phase

                for (i in 0..phaseDuration) {
                    if (elapsed >= totalDuration) break
                    _state.update { it.copy(breathingTimeRemaining = (totalDuration - elapsed).toInt()) }
                    delay(1000) // Delay for 1 second
                    elapsed++
                    _state.update { it.copy(breathingPhaseProgress = (i + 1).toFloat() / phaseDuration) }
                }

                // Przejście do następnej fazy
                // Transition to the next phase
                _state.update { currentState ->
                    val nextPhase = when (currentState.breathingPhase) {
                        BreathingPhase.INHALE -> BreathingPhase.HOLD
                        BreathingPhase.HOLD -> BreathingPhase.EXHALE
                        BreathingPhase.EXHALE -> BreathingPhase.INHALE // Loop back
                        BreathingPhase.PAUSE -> BreathingPhase.INHALE // Start from inhale
                    }
                    currentState.copy(breathingPhase = nextPhase)
                }
            }
            _state.update { it.copy(isBreathingSessionActive = false, breathingTimeRemaining = 0, breathingPhase = BreathingPhase.INHALE) }
        }
    }

    /**
     * Kończy sesję oddechową.
     * Ends the breathing session.
     */
    fun endBreathingSession() {
        breathingTimerJob?.cancel()
        _state.update { it.copy(isBreathingSessionActive = false, breathingTimeRemaining = 0, breathingPhase = BreathingPhase.INHALE) }
    }

    data class EmotionExerciseState(
        val emotions: List<Emotion> = emptyList(),
        val selectedEmotionWheelIds: Set<String> = emptySet(),
        val selectedQuizEmotion: EmotionCategory? = null,
        val exercises: List<Exercise> = emptyList(),
        val rituals: List<Ritual> = emptyList(),
        val isBreathingSessionActive: Boolean = false,
        val breathingPhase: BreathingPhase = BreathingPhase.INHALE,
        val breathingTimeRemaining: Int = 0,
        val breathingPhaseProgress: Float = 0f // Progress within current phase (0.0 to 1.0)
    )
}


