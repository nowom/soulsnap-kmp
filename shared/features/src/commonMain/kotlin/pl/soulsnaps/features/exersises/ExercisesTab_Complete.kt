//
//package com.soulunity.ui.tabs
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.soulunity.ui.components.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//// ---------- MODEL ----------
//data class EmotionExerciseState(
//    val selectedEmotions: List<Emotion> = emptyList(),
//    val completedExercises: List<String> = emptyList(),
//    val sessionInProgress: Boolean = false,
//    val quizResult: EmotionCategory? = null
//)
//
//// ---------- REPOSITORY ----------
//interface ExerciseRepository {
//    suspend fun markExerciseCompleted(exerciseId: String)
//    suspend fun getCompletedExercises(): List<String>
//}
//
//class InMemoryExerciseRepository : ExerciseRepository {
//    private val completed = mutableListOf<String>()
//    override suspend fun markExerciseCompleted(exerciseId: String) {
//        completed.add(exerciseId)
//    }
//    override suspend fun getCompletedExercises(): List<String> = completed
//}
//
//// ---------- USE CASES ----------
//class MarkExerciseCompletedUseCase(private val repository: ExerciseRepository) {
//    suspend operator fun invoke(exerciseId: String) = repository.markExerciseCompleted(exerciseId)
//}
//
//class GetCompletedExercisesUseCase(private val repository: ExerciseRepository) {
//    suspend operator fun invoke(): List<String> = repository.getCompletedExercises()
//}
//
//// ---------- VIEWMODEL ----------
//class ExercisesViewModel(
//    private val markCompleted: MarkExerciseCompletedUseCase,
//    private val getCompleted: GetCompletedExercisesUseCase
//) {
//    private val _uiState = MutableStateFlow(EmotionExerciseState())
//    val uiState: StateFlow<EmotionExerciseState> = _uiState
//
//    fun onEmotionsSelected(emotions: List<Emotion>) {
//        _uiState.value = _uiState.value.copy(selectedEmotions = emotions)
//    }
//
//    fun completeExercise(exerciseId: String) {
//        _uiState.value = _uiState.value.copy(
//            completedExercises = _uiState.value.completedExercises + exerciseId
//        )
//    }
//
//    fun onQuizCompleted(emotion: EmotionCategory) {
//        _uiState.value = _uiState.value.copy(quizResult = emotion)
//    }
//
//    suspend fun loadCompleted() {
//        val completed = getCompleted()
//        _uiState.value = _uiState.value.copy(completedExercises = completed)
//    }
//
//    fun startSession() {
//        _uiState.value = _uiState.value.copy(sessionInProgress = true)
//    }
//
//    fun endSession() {
//        _uiState.value = _uiState.value.copy(sessionInProgress = false)
//    }
//}
//
//// ---------- SCREEN COMPOSABLE ----------
//@Composable
//fun ExercisesTab(
//    viewModel: ExercisesViewModel,
//    navigateToBreathingSession: () -> Unit
//) {
//    val state by viewModel.uiState.collectAsState()
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        viewModel.loadCompleted()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        Text("Ćwiczenia i Nauka", style = MaterialTheme.typography.headlineSmall)
//
//        EmotionWheelCircular(onSelectionChanged = { viewModel.onEmotionsSelected(it) })
//
//        EmotionQuizSection(onQuizComplete = {
//            viewModel.onQuizCompleted(it)
//        })
//
//        state.quizResult?.let {
//            Text("Quiz sugeruje: ${it.name}", style = MaterialTheme.typography.bodyMedium)
//        }
//
//        ExerciseListSection(
//            title = "Polecane ćwiczenia",
//            exercises = sampleExercises,
//            onExerciseClick = { exercise ->
//                scope.launch { viewModel.completeExercise(exercise.id) }
//            },
//            completedIds = state.completedExercises
//        )
//
//        BreathingSessionPreview(
//            sessionInProgress = state.sessionInProgress,
//            onStart = {
//                viewModel.startSession()
//                navigateToBreathingSession()
//            }
//        )
//
//        RitualSection()
//    }
//}
//
//@Composable
//fun BreathingSessionPreview(
//    sessionInProgress: Boolean,
//    onStart: () -> Unit
//) {
//    Column {
//        Text("Oddychanie 4-7-8", style = MaterialTheme.typography.titleMedium)
//        if (sessionInProgress) {
//            Text("Trwa sesja...", style = MaterialTheme.typography.bodySmall)
//        } else {
//            Button(onClick = onStart) {
//                Text("Rozpocznij sesję")
//            }
//        }
//    }
//}
//
//// ---------- SAMPLE ----------
//val sampleExercises = listOf(
//    Exercise("ex1", "Oddech 4-7-8", "Pomaga wyciszyć umysł i ciało", EmotionCategory.CALM, emptyList()),
//    Exercise("ex2", "Wdzięczność", "Zapisz 3 rzeczy, za które jesteś wdzięczny", EmotionCategory.JOY, emptyList()),
//    Exercise("ex3", "Grounding", "Ćwiczenie 5-4-3-2-1", EmotionCategory.FEAR, emptyList())
//)
