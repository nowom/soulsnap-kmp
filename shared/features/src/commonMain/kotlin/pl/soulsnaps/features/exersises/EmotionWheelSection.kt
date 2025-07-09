
package com.soulunity.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.PopupProperties

data class Emotion(
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String
)

val baseEmotions = listOf(
    Emotion("Radość", "😊", Color(0xFFFFF176), "Stan szczęścia, spełnienia i satysfakcji."),
    Emotion("Smutek", "😢", Color(0xFF90CAF9), "Stan przygnębienia, straty lub melancholii."),
    Emotion("Złość", "😠", Color(0xFFE57373), "Stan frustracji, złości lub niesprawiedliwości."),
    Emotion("Strach", "😨", Color(0xFFCE93D8), "Stan zagrożenia, niepokoju lub niepewności."),
    Emotion("Zaskoczenie", "😲", Color(0xFFA5D6A7), "Reakcja na coś niespodziewanego."),
    Emotion("Wstręt", "🤢", Color(0xFFB0BEC5), "Stan odrazy lub niechęci."),
    Emotion("Zaufanie", "🤝", Color(0xFFFFCC80), "Poczucie bezpieczeństwa i akceptacji."),
    Emotion("Oczekiwanie", "🤔", Color(0xFF80CBC4), "Stan ciekawości lub napięcia przed czymś.")
)

@Composable
fun EmotionWheelSection1(
    onEmotionSelected: (Emotion) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Koło emocji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            baseEmotions.forEach { emotion ->
                EmotionChip(emotion, onEmotionSelected)
            }
        }
    }
}

@Composable
fun EmotionChip(
    emotion: Emotion,
    onEmotionSelected: (Emotion) -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(60.dp)
            .background(color = emotion.color.copy(alpha = 0.3f), shape = CircleShape)
            .clickable {
                showTooltip = !showTooltip
                onEmotionSelected(emotion)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(emotion.emoji, style = MaterialTheme.typography.headlineSmall)
        if (showTooltip) {
            TooltipBox(text = emotion.description)
        }
    }
}

@Composable
fun TooltipBox(text: String) {
    Box(
        modifier = Modifier
            .offset(y = (-70).dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
