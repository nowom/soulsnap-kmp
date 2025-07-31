package pl.soulsnaps.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.features.exersises.EmotionWheelCircular

@Composable
fun EmotionWheelScreen(onBack: () -> Unit) {
    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Koło emocji", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Kliknij na emocję, aby zobaczyć jej opis.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        EmotionWheelCircular(
            selectedEmotionIds = emptySet(),
            onSelectionChanged = {

            }
        )
        Spacer(Modifier.height(16.dp))
        selectedEmotion?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    //Text(it.emoji + " " + it.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(it.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Powrót") }
    }
} 