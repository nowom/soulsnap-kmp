package pl.soulsnaps.features.exersises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soulunity.ui.components.EmotionCategory
import com.soulunity.ui.components.EmotionQuizSection
import com.soulunity.ui.components.EmotionWheelCircular
import com.soulunity.ui.components.EmotionWheelSection
import com.soulunity.ui.components.EmotionWheelSection1
import com.soulunity.ui.components.Exercise
import com.soulunity.ui.components.ExerciseListSection
import com.soulunity.ui.components.RitualSection

@Composable
internal fun ExercisesRoute() {
    App()
}

@Composable
fun ExercisesScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Ćwiczenia i Nauka",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zadbaj o swój nastrój i emocjonalną równowagę",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            EmotionWheelSection()
        }

        item {
            ExerciseListSection(
                title = "Polecane ćwiczenia",
                exercises = sampleExercises
            )
        }

        item {
            EmotionQuizSection(onQuizComplete = { emotion ->
                // TODO: Obsłuż wynik quizu – np. pokaż afirmację
            })
        }

        item {
            RitualSection()
        }
    }
}

// Przykładowe dane
val sampleExercises = listOf(
    Exercise("1", "Oddech 4-7-8", "Ćwiczenie uspokajające oddech", EmotionCategory.CALM, listOf("Wdech 4 sekundy", "Wstrzymaj oddech 7 sekund", "Wydech 8 sekund")),
    Exercise("2", "Wdzięczność", "Zapisz 3 rzeczy, za które jesteś wdzięczny", EmotionCategory.JOY, listOf("Zastanów się nad dniem", "Wypisz 3 pozytywne rzeczy"))
)
