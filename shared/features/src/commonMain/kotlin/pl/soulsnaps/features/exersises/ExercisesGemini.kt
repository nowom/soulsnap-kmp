package pl.soulsnaps.features.exersises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Duration.Companion.minutes

// File: src/commonMain/kotlin/com/soulunity/data/Emotion.kt
/**
 * Reprezentuje pojedynczą emocję w kole emocji.
 * Represents a single emotion in the emotion wheel.
 */
data class Emotion(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String,
    val category: EmotionCategory
)

/**
 * Kategoria emocji, używana do grupowania ćwiczeń i quizów.
 * Emotion category, used for grouping exercises and quizzes.
 */
enum class EmotionCategory {
    JOY, SADNESS, ANGER, FEAR, SURPRISE, DISGUST, CALM, NEUTRAL
}

// File: src/commonMain/kotlin/com/soulunity/data/Exercise.kt

/**
 * Reprezentuje ćwiczenie wspierające autoregulację emocjonalną.
 * Represents an exercise supporting emotional self-regulation.
 */
data class Exercise(
    val id: String,
    val title: String,
    val description: String,
    val category: EmotionCategory,
    var isCompleted: Boolean = false
)

/**
 * Reprezentuje rytuał dzienny (poranny lub wieczorny).
 * Represents a daily ritual (morning or evening).
 */
data class Ritual(
    val id: String,
    val title: String,
    val description: String,
    val exercises: List<Exercise> // Przykładowe ćwiczenia w rytuale
)

// File: src/commonMain/kotlin/com/soulunity/data/BreathingPhase.kt

/**
 * Fazy sesji oddechowej.
 * Phases of a breathing session.
 */
enum class BreathingPhase {
    INHALE, HOLD, EXHALE, PAUSE
}

/**
 * Interfejs repozytorium dla ćwiczeń.
 * Repository interface for exercises.
 */
interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun markExerciseCompleted(exerciseId: String, completed: Boolean)
    fun getCompletedExerciseIds(): Flow<Set<String>>
}


/**
 * Implementacja repozytorium ćwiczeń w pamięci (mock).
 * In-memory implementation of the exercise repository (mock).
 */
class InMemoryExerciseRepository : ExerciseRepository {

    private val _exercises = MutableStateFlow(
        listOf(
            Exercise("ex1", "Oddech 4-7-8", "Technika oddechowa pomagająca w redukcji stresu i relaksacji.", EmotionCategory.CALM),
            Exercise("ex2", "Dziennik Wdzięczności", "Zapisuj 3 rzeczy, za które jesteś wdzięczny każdego dnia.", EmotionCategory.JOY),
            Exercise("ex3", "Skan Ciała", "Ćwiczenie uważności, które pomaga zidentyfikować napięcia w ciele.", EmotionCategory.CALM),
            Exercise("ex4", "Afirmacje Poranne", "Powtarzaj pozytywne stwierdzenia, aby wzmocnić poczucie własnej wartości.", EmotionCategory.JOY),
            Exercise("ex5", "Uwalnianie Złości", "Bezpieczne metody wyrażania i uwalniania nagromadzonej złości.", EmotionCategory.ANGER),
            Exercise("ex6", "Akceptacja Smutku", "Ćwiczenie pozwalające na przyjęcie i przetworzenie uczucia smutku.", EmotionCategory.SADNESS),
        )
    )

    private val _completedExerciseIds = MutableStateFlow(emptySet<String>())

    override fun getAllExercises(): Flow<List<Exercise>> = _exercises

    override suspend fun markExerciseCompleted(exerciseId: String, completed: Boolean) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(isCompleted = completed)
                } else {
                    exercise
                }
            }
        }
        _completedExerciseIds.update { currentSet ->
            if (completed) {
                currentSet + exerciseId
            } else {
                currentSet - exerciseId
            }
        }
    }

    override fun getCompletedExerciseIds(): Flow<Set<String>> = _completedExerciseIds
}

// File: src/commonMain/kotlin/com/soulunity/domain/usecase/GetCompletedExercisesUseCase.kt

/**
 * Use case do pobierania ID ukończonych ćwiczeń.
 * Use case for getting IDs of completed exercises.
 */
class GetCompletedExercisesUseCase(private val repository: ExerciseRepository) {
    operator fun invoke(): Flow<Set<String>> {
        return repository.getCompletedExerciseIds()
    }
}


/**
 * Use case do oznaczania ćwiczenia jako ukończonego.
 * Use case for marking an exercise as completed.
 */
class MarkExerciseCompletedUseCase(private val repository: ExerciseRepository) {
    suspend operator fun invoke(exerciseId: String, completed: Boolean) {
        repository.markExerciseCompleted(exerciseId, completed)
    }
}

// File: src/commonMain/kotlin/com/soulunity/presentation/viewmodel/ExercisesViewModel.kt



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
        // Inicjalizacja danych emocji
        // Initialize emotion data
        _state.update {
            it.copy(
                emotions = listOf(
                    Emotion("joy", "Radość", "😊", Color(0xFFFFF0B3), "Uczucie szczęścia i zadowolenia.", EmotionCategory.JOY),
                    Emotion("sadness", "Smutek", "😢", Color(0xFFB3D9FF), "Uczucie żalu i przygnębienia.", EmotionCategory.SADNESS),
                    Emotion("anger", "Złość", "😠", Color(0xFFFFB3B3), "Silne uczucie irytacji lub wrogości.", EmotionCategory.ANGER),
                    Emotion("fear", "Strach", "😨", Color(0xFFC2B3FF), "Niepokój lub lęk wywołany zagrożeniem.", EmotionCategory.FEAR),
                    Emotion("surprise", "Zaskoczenie", "😮", Color(0xFFB3FFD9), "Nagłe uczucie wywołane czymś nieoczekiwanym.", EmotionCategory.SURPRISE),
                    Emotion("disgust", "Wstręt", "🤢", Color(0xFFD9FFB3), "Silne uczucie odrazy.", EmotionCategory.DISGUST),
                    Emotion("calm", "Spokój", "😌", Color(0xFFB3FFB3), "Uczucie ciszy i relaksu.", EmotionCategory.CALM),
                    Emotion("neutral", "Neutralność", "😐", Color(0xFFE0E0E0), "Brak wyraźnych emocji.", EmotionCategory.NEUTRAL)
                )
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
                            Exercise("ex1", "Oddech 4-7-8", "", EmotionCategory.CALM),
                            Exercise("ex4", "Afirmacje Poranne", "", EmotionCategory.JOY)
                        )
                    ),
                    Ritual(
                        "evening_ritual",
                        "🌙 Wieczorny rytuał",
                        "Zakończ dzień spokojem i wdzięcznością.",
                        listOf(
                            Exercise("ex2", "Dziennik Wdzięczności", "", EmotionCategory.JOY),
                            Exercise("ex3", "Skan Ciała", "", EmotionCategory.CALM)
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
    fun onQuizEmotionSelected(emotionCategory: EmotionCategory) {
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
}

/**
 * Stan UI dla zakładki "Ćwiczenia i Nauka".
 * UI State for the "Exercises and Learning" tab.
 */
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

// File: src/commonMain/kotlin/com/soulunity/presentation/ui/common/Tooltip.kt



/**
 * Prosty komponent Tooltip.
 * Simple Tooltip component.
 */
@Composable
fun Tooltip(
    text: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Popup(
            properties = PopupProperties(
                focusable = false,
                clippingEnabled = false
            )
        ) {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Komponent Koła Emocji z możliwością wyboru wielu emocji i tooltipami.
 * Emotion Wheel component with multi-selection and tooltips.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun EmotionWheelCircular(
    emotions: List<Emotion>,
    selectedEmotionIds: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var circleRadius by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    var tooltipVisible by remember { mutableStateOf(false) }
    var tooltipText by remember { mutableStateOf("") }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onSizeChanged { size ->
                circleRadius = min(size.width.toFloat(), size.height.toFloat()) / 2f * 0.8f // 80% of min dimension
                center = Offset(size.width / 2f, size.height / 2f)
            }
            .pointerInput(emotions) {
                detectTapGestures(
                    onPress = { offset ->
                        tooltipVisible = false // Hide tooltip on press
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance <= circleRadius) {
                            val angle = (atan2(dy, dx) + 2 * PI) % (2 * PI) // Angle from 0 to 2PI

                            val segmentAngle = 2 * PI / emotions.size
                            val segmentIndex = (angle / segmentAngle).toInt()

                            if (segmentIndex < emotions.size) {
                                val clickedEmotion = emotions[segmentIndex]
                                val newSelection = if (selectedEmotionIds.contains(clickedEmotion.id)) {
                                    selectedEmotionIds - clickedEmotion.id
                                } else {
                                    selectedEmotionIds + clickedEmotion.id
                                }
                                onSelectionChanged(newSelection)

                                // Show tooltip
                                tooltipText = clickedEmotion.description
                                tooltipOffset = offset
                                tooltipVisible = true
                            }
                        }
                    },
                    onTap = {
                        // Keep tooltip visible for a short duration after tap
                        // This might require a delayed hiding mechanism if not handled by Popup's auto-dismiss
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (circleRadius == 0f) return@Canvas

            val segmentAngle = 360f / emotions.size
            val innerRadius = circleRadius * 0.3f // Inner circle for text/center

            emotions.forEachIndexed { index, emotion ->
                val startAngle = (index * segmentAngle) - 90f // Start from top
                val sweepAngle = segmentAngle

                val isSelected = selectedEmotionIds.contains(emotion.id)
                val color = if (isSelected) emotion.color.copy(alpha = 0.8f) else emotion.color.copy(alpha = 0.5f)

                // Rysowanie segmentu koła
                // Draw wheel segment
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - circleRadius, center.y - circleRadius),
                    size = Size(circleRadius * 2, circleRadius * 2)
                )

                // Rysowanie obramowania dla wybranych emocji
                // Draw border for selected emotions
                if (isSelected) {
                    drawArc(
                        color = Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - circleRadius, center.y - circleRadius),
                        size = Size(circleRadius * 2, circleRadius * 2),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Obliczanie pozycji dla emoji i nazwy emocji
                // Calculate position for emoji and emotion name
                val midAngleRad =  ((startAngle + sweepAngle / 2f) * (PI / 180f)).toFloat()
                val textRadius = innerRadius + (circleRadius - innerRadius) / 2
                val textX = center.x + textRadius * cos(midAngleRad)
                val textY = center.y + textRadius * sin(midAngleRad)

                // Rysowanie emoji
                // Draw emoji
                val emojiText = emotion.emoji
                val emojiStyle = TextStyle(fontSize = 24.sp)
                val emojiResult = textMeasurer.measure(emojiText, emojiStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = emojiText,
                    topLeft = Offset(textX - emojiResult.size.width / 2, textY - emojiResult.size.height / 2 - 15.dp.toPx()),
                    style = emojiStyle
                )

                // Rysowanie nazwy emocji
                // Draw emotion name
                val nameText = emotion.name
                val nameStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
                val nameResult = textMeasurer.measure(nameText, nameStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = nameText,
                    topLeft = Offset(textX - nameResult.size.width / 2, textY - nameResult.size.height / 2 + 10.dp.toPx()),
                    style = nameStyle
                )
            }

            // Rysowanie centralnego okręgu
            // Draw central circle
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = Color.Cyan,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Tooltip
        if (tooltipVisible) {
            Tooltip(
                text = tooltipText,
                isVisible = tooltipVisible,
                modifier = Modifier
                    .padding(
                        start = (tooltipOffset.x / 5).dp,
                        top = (tooltipOffset.y / 5).dp
                    )
            )
        }
    }
}


// File: src/commonMain/kotlin/com/soulunity/presentation/ui/EmotionQuizSection.kt

/**
 * Sekcja Quizu Emocjonalnego.
 * Emotion Quiz Section.
 */
@Composable
fun EmotionQuizSection(
    emotions: List<Emotion>,
    selectedQuizEmotion: EmotionCategory?,
    onQuizEmotionSelected: (EmotionCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuizDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sprawdź swój nastrój",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Szybki quiz, aby zrozumieć, jak się dziś czujesz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { showQuizDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jak się dziś czujesz?")
            }

            selectedQuizEmotion?.let { category ->
                val selectedEmotion = emotions.find { it.category == category }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dziś czujesz się: ${selectedEmotion?.name ?: category.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        selectedEmotion?.let {
                            Text(
                                text = it.emoji,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQuizDialog) {
        EmotionSelectionDialog(
            emotions = emotions,
            onEmotionSelected = { emotion ->
                onQuizEmotionSelected(emotion.category)
                showQuizDialog = false
            },
            onDismissRequest = { showQuizDialog = false }
        )
    }
}

/**
 * Dialog do wyboru jednej emocji.
 * Dialog for selecting a single emotion.
 */
@Composable
fun EmotionSelectionDialog(
    emotions: List<Emotion>,
    onEmotionSelected: (Emotion) -> Unit,
    onDismissRequest: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz emocję, która najlepiej opisuje, jak się dziś czujesz:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotions.chunked(3).forEach { rowEmotions ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowEmotions.forEach { emotion ->
                                EmotionChip(emotion = emotion) {
                                    onEmotionSelected(emotion)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismissRequest) {
                    Text("Anuluj")
                }
            }
        }
    }
}

/**
 * Pojedynczy chip emocji do wyboru w quizie.
 * Single emotion chip for quiz selection.
 */
@Composable
fun EmotionChip(
    emotion: Emotion,
    onClick: (Emotion) -> Unit
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(emotion.color.copy(alpha = 0.6f))
            .clickable { onClick(emotion) }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = emotion.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = emotion.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

/**
 * Sekcja listy ćwiczeń.
 * Exercise List Section.
 */
@Composable
fun ExerciseListSection(
    exercises: List<Exercise>,
    onExerciseCompleted: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Ćwiczenia na dziś",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            exercises.forEach { exercise->
                ExerciseCard(
                    exercise = exercise,
                    onToggleComplete = { completed -> onExerciseCompleted(exercise.id, completed) }
                )
            }
        }
    }
}

/**
 * Karta pojedynczego ćwiczenia.
 * Single exercise card.
 */
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onToggleComplete: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(!exercise.isCompleted) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kategoria: ${exercise.category.name.lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = if (exercise.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (exercise.isCompleted) "Ukończone" else "Nieukończone",
                tint = if (exercise.isCompleted) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


/**
 * Podgląd sesji oddechowej z przyciskiem "Rozpocznij".
 * Breathing session preview with "Start" button.
 */
@Composable
fun BreathingSessionPreview(
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sesja Oddechu 4-7-8",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Zrelaksuj się i uspokój swój umysł.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rozpocznij Sesję")
        }
    }
}

/**
 * Ekran pełnej sesji oddechowej z animacją i timerem.
 * Full breathing session screen with animation and timer.
 */
@Composable
fun BreathingSessionScreen(
    phase: BreathingPhase,
    timeRemaining: Int,
    phaseProgress: Float,
    onSessionEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val inhaleColor = Color(0xFF81C784) // Light Green
    val holdColor = Color(0xFF64B5F6) // Light Blue
    val exhaleColor = Color(0xFFE57373) // Light Red

    val targetColor = when (phase) {
        BreathingPhase.INHALE -> inhaleColor
        BreathingPhase.HOLD -> holdColor
        BreathingPhase.EXHALE -> exhaleColor
        BreathingPhase.PAUSE -> Color.Gray // Should not be visible during actual phases
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    val animatedRadius by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (phase) {
                    BreathingPhase.INHALE -> 4000
                    BreathingPhase.HOLD -> 7000
                    BreathingPhase.EXHALE -> 8000
                    BreathingPhase.PAUSE -> 0
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val currentRadius = when (phase) {
        BreathingPhase.INHALE -> 0.3f + (0.5f - 0.3f) * phaseProgress
        BreathingPhase.HOLD -> 0.5f
        BreathingPhase.EXHALE -> 0.5f - (0.5f - 0.3f) * phaseProgress
        BreathingPhase.PAUSE -> 0.3f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedBackgroundColor.copy(alpha = 0.4f),
                        animatedBackgroundColor.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animowany okrąg oddechu
        // Animated breathing circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * currentRadius
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = radius,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = when (phase) {
                    BreathingPhase.INHALE -> "Wdech"
                    BreathingPhase.HOLD -> "Wstrzymaj"
                    BreathingPhase.EXHALE -> "Wydech"
                    BreathingPhase.PAUSE -> "Przygotuj się"
                },
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "test", //"${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}"
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 64.dp)
            )

            if (timeRemaining <= 0) {
                Text(
                    text = "Sesja zakończona 🌟",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 32.dp)
                )
                Button(
                    onClick = onSessionEnd,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Zakończ")
                }
            } else {
                Button(
                    onClick = onSessionEnd,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Zakończ Sesję")
                }
            }
        }
    }
}




/**
 * Sekcja Rytuałów Dnia.
 * Daily Rituals Section.
 */
@Composable
fun RitualSection(
    rituals: List<Ritual>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Rytuały Dnia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            rituals.forEach { ritual ->
                RitualCard(ritual = ritual)
            }
        }
    }
}

/**
 * Karta pojedynczego rytuału.
 * Single ritual card.
 */
@Composable
fun RitualCard(
    ritual: Ritual
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ritual.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ritual.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Zawiera: ${ritual.exercises.joinToString { it.title }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // TODO: Dodać przycisk "Rozpocznij Rytuał" i integrację z AI Coachem
            // TODO: Add "Start Ritual" button and integration with AI Coach
        }
    }
}


/**
 * Główna kompozycja aplikacji SoulUnity.
 * Main composable for the SoulUnity app.
 */
@Composable
fun App() {
    // Inicjalizacja zależności (można użyć DI frameworka w większej aplikacji)
    // Dependency initialization (can use DI framework in a larger app)
    val exerciseRepository = InMemoryExerciseRepository()
    val markExerciseCompletedUseCase = MarkExerciseCompletedUseCase(exerciseRepository)
    val getCompletedExercisesUseCase = GetCompletedExercisesUseCase(exerciseRepository)
    val viewModel: ExercisesViewModel = koinViewModel()

    val state by viewModel.state.collectAsState()

    if (state.isBreathingSessionActive) {
        BreathingSessionScreen(
            phase = state.breathingPhase,
            timeRemaining = state.breathingTimeRemaining,
            phaseProgress = state.breathingPhaseProgress,
            onSessionEnd = { viewModel.endBreathingSession() }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp) // Ogólny padding dla całej zakładki
        ) {
            // Sekcja Koła Emocji
            // Emotion Wheel Section
            EmotionWheelCircular(
                emotions = state.emotions,
                selectedEmotionIds = state.selectedEmotionWheelIds,
                onSelectionChanged = { viewModel.onEmotionSelectionChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Ustaw wysokość dla koła
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sekcja Quizu Emocjonalnego
            // Emotion Quiz Section
            EmotionQuizSection(
                emotions = state.emotions,
                selectedQuizEmotion = state.selectedQuizEmotion,
                onQuizEmotionSelected = { viewModel.onQuizEmotionSelected(it) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sekcja Listy Ćwiczeń
            // Exercise List Section
            ExerciseListSection(
                exercises = state.exercises,
                onExerciseCompleted = { id, completed ->
                    viewModel.onExerciseCompleted(
                        id,
                        completed
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Podgląd Sesji Oddechu
            // Breathing Session Preview
            BreathingSessionPreview(
                onStartSession = { viewModel.startBreathingSession() },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sekcja Rytuałów Dnia
            // Daily Rituals Section
            RitualSection(
                rituals = state.rituals,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// Minimalny plik main.kt dla uruchomienia na JVM (Desktop)
// Minimal main.kt file for JVM (Desktop) execution
/*
package com.soulunity

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "SoulUnity") {
        App()
    }
}
*/