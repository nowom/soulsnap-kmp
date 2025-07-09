
package com.soulunity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

// Prosty model emocji i ćwiczeń
data class Exercise(
    val id: String,
    val title: String,
    val description: String,
    val category: EmotionCategory,
    val steps: List<String>
)

enum class EmotionCategory {
    SADNESS, ANGER, JOY, FEAR, CALM
}

// Sekcja: Koło emocji (prosta reprezentacja listowa)
@Composable
fun EmotionWheelSection() {
    Column {
        Text("Koło emocji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EmotionCategory.values().forEach { emotion ->
                Chip(text = emotion.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .padding(end = 4.dp)
            .clickable { /* TODO: wybór emocji */ }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Sekcja: Lista ćwiczeń
@Composable
fun ExerciseListSection(title: String, exercises: List<Exercise>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        exercises.forEach { exercise ->
            ExerciseCard(exercise)
        }
    }
}

// Karta pojedynczego ćwiczenia
@Composable
fun ExerciseCard(exercise: Exercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(exercise.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Sekcja: Quiz emocji (prosty placeholder)
@Composable
fun EmotionQuizSection(onQuizComplete: (EmotionCategory) -> Unit) {
    Column {
        Text("Quiz emocji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onQuizComplete(EmotionCategory.JOY) }) {
            Text("Jak się dziś czujesz?")
        }
    }
}

// Sekcja: Rytuały poranny i wieczorny
@Composable
fun RitualSection() {
    Column {
        Text("Rytuały dnia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RitualCard("🌅 Poranny rytuał", listOf("Oddech", "Afirmacja"))
            RitualCard("🌙 Wieczorny rytuał", listOf("Wdzięczność", "Refleksja"))
        }
    }
}

@Composable
fun RitualCard(title: String, steps: List<String>) {
    Card(
        modifier = Modifier
            .padding(end = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            steps.forEach { step ->
                Text("• $step", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
