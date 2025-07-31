package pl.soulsnaps.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoachScreen(
    onStartQuiz: () -> Unit,
    onOpenWheel: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenGratitude: () -> Unit,
    quizCompletedToday: Boolean,
    streak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Progress visualization
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (quizCompletedToday) "Brawo! Dziś już sprawdziłeś swoje emocje 🎉" else "Jak się dziś czujesz?",
                    style = MaterialTheme.typography.titleMedium
                )
                if (streak > 0) {
                    Text("Streak: $streak dni", style = MaterialTheme.typography.bodySmall)
                }
                if (!quizCompletedToday) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onStartQuiz) { Text("Rozpocznij quiz") }
                }
            }
        }

        // Quick links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onOpenWheel) { Text("Koło emocji") }
            Button(onClick = onOpenBreathing) { Text("Oddychanie") }
            Button(onClick = onOpenGratitude) { Text("Wdzięczność") }
        }
    }
} 