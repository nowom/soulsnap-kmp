package pl.soulsnaps.features.exersises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.data.InMemoryExerciseRepository
import pl.soulsnaps.domain.interactor.GetCompletedExercisesUseCase
import pl.soulsnaps.domain.interactor.MarkExerciseCompletedUseCase
import pl.soulsnaps.domain.model.Ritual
import pl.soulsnaps.features.exersises.SimplePlutchikWheel

@Composable
internal fun ExercisesRoute(
    onOpenBreathing: () -> Unit,
    onOpenGratitude: () -> Unit,
    onOpenEmotionWheel: () -> Unit,
    onOpenDailyQuiz: () -> Unit = {}
) {
    ExercisesScreen(onOpenBreathing, onOpenGratitude, onOpenEmotionWheel, onOpenDailyQuiz)
}

@Composable
fun ExercisesScreen(
    onOpenBreathing: () -> Unit = {},
    onOpenGratitude: () -> Unit = {},
    onOpenEmotionWheel: () -> Unit = {},
    onOpenDailyQuiz: () -> Unit = {}
) {
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
                .padding(16.dp)
        ) {
            // Simple Plutchik Wheel Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Koło Emocji",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                SimplePlutchikWheel(
                    onWheelClick = onOpenEmotionWheel,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emotion Quiz Section
            EmotionQuizSection(
                emotions = state.emotions,
                selectedQuizEmotion = state.selectedQuizEmotion,
                onQuizEmotionSelected = { viewModel.onQuizEmotionSelected(it) },
                onOpenDailyQuiz = onOpenDailyQuiz,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise List Section
            ExerciseListSection(
                exercises = state.exercises,
                onExerciseCompleted = { id, completed ->
                    viewModel.onExerciseCompleted(id, completed)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Breathing Session Preview
            BreathingSessionPreview(
                onStartSession = { viewModel.startBreathingSession() },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Gratitude Section ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Wdzięczność", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Zapisz 1-3 rzeczy, za które jesteś dziś wdzięczny.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenGratitude,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Przejdź do dziennika wdzięczności")
                    }
                }
            }
            // --- End Gratitude Section ---

            // Daily Rituals Section
            RitualSection(
                rituals = state.rituals,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

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