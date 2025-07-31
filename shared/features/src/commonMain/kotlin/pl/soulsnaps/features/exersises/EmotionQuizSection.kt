package pl.soulsnaps.features.exersises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionCategory

@Composable
fun EmotionQuizSection(
    emotions: List<Emotion>,
    selectedQuizEmotion: EmotionCategory?,
    onQuizEmotionSelected: (EmotionCategory?) -> Unit,
    modifier: Modifier = Modifier.Companion
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
            modifier = Modifier.Companion.padding(16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(
                text = "Sprawdź swój nastrój",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Companion.Bold,
                modifier = Modifier.Companion.padding(bottom = 8.dp)
            )
            Text(
                text = "Szybki quiz, aby zrozumieć, jak się dziś czujesz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.Companion.padding(bottom = 16.dp)
            )
            Button(
                onClick = { showQuizDialog = true },
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text("Jak się dziś czujesz?")
            }

            selectedQuizEmotion?.let { category ->
                val selectedEmotion = emotions.find { it.emotionCategory == category }
                Spacer(modifier = Modifier.Companion.height(16.dp))
                Card(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.Companion
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Companion.CenterHorizontally
                    ) {
                        Text(
                            text = "Dziś czujesz się: ${selectedEmotion?.name ?: category.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Companion.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        selectedEmotion?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.Companion.padding(top = 4.dp)
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
                onQuizEmotionSelected(emotion.emotionCategory)
                showQuizDialog = false
            },
            onDismissRequest = { showQuizDialog = false }
        )
    }
}

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
                text = emotion.name,
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