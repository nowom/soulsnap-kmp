package pl.soulsnaps.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoachQuizScreen(
    viewModel: CoachQuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onDone: () -> Unit
) {
    val state = viewModel.state

    if (state.selectedEmotion == null) {
        // Emotion selection
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Jak się dziś czujesz?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Radość", "Smutek", "Złość", "Strach", "Spokój").forEach { emotion ->
                    Button(onClick = { viewModel.selectEmotion(emotion) }) {
                        Text(emotion)
                    }
                }
            }
        }
    } else {
        // Result screen
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Twoja emocja: ${state.selectedEmotion}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text("Refleksja: ${state.reflection}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Text("Afirmacja: ${state.affirmation}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onDone) { Text("Zakończ") }
        }
    }
} 