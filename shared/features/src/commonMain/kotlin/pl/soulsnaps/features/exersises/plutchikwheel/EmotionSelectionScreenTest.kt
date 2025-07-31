// ui/EmotionSelectionScreenTest.kt
package pl.soulsnaps.features.exersises.plutchikwheel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionData
import pl.soulsnaps.domain.model.EmotionIntensity

@Composable
fun App() {
    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }
    var primaryEmotionsToHighlight by remember { mutableStateOf<List<String>>(emptyList()) }

    // NOWE STANY DLA TRYBU POŁĄCZ
    var connectionModeActive by remember { mutableStateOf(false) }
    val selectedEmotionsForConnectionCategory = remember { mutableStateListOf<EmotionCategory>() } // Lista do przechowywania wybranych BasicEmotion

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding() // Zapewnia, że UI nie wchodzi pod wycięcia systemowe
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Przycisk "Tryb Połącz"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        connectionModeActive = !connectionModeActive // Przełącz tryb
                        selectedEmotionsForConnectionCategory.clear() // Wyczyść wybrane emocje przy zmianie trybu
                        selectedEmotion = null // Wyczyść wybraną emocję na kole
                        primaryEmotionsToHighlight = emptyList() // Wyczyść podświetlenia
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(if (connectionModeActive) "Wyłącz Tryb Połącz" else "Włącz Tryb Połącz", fontSize = 16.sp)
                }
            }

            // Koło Plutchika
            PlutchikWheelCanvas(
                modifier = Modifier.fillMaxSize(), // Pozostała przestrzeń
                onEmotionSelected = { emotion ->
                    // Jeśli tryb łączenia jest aktywny, nie otwieraj od razu karty szczegółów
                    // Zamiast tego, obsłuż kliknięcie wewnętrznie dla trybu łączenia
                    if (connectionModeActive) {
                        if (emotion?.emotionCategory != null && emotion.intensity == EmotionIntensity.MEDIUM) { // Interesują nas tylko emocje podstawowe ze średnią intensywnością
                            if (selectedEmotionsForConnectionCategory.contains(emotion.emotionCategory)) {
                                selectedEmotionsForConnectionCategory.remove(emotion.emotionCategory)
                            } else {
                                if (selectedEmotionsForConnectionCategory.size < 2) { // Ogranicz do 2 wybranych emocji
                                    selectedEmotionsForConnectionCategory.add(emotion.emotionCategory)
                                } else {
                                    // Jeśli już dwie są wybrane i kliknięto na inną, zresetuj i dodaj nową
                                    selectedEmotionsForConnectionCategory.clear()
                                    selectedEmotionsForConnectionCategory.add(emotion.emotionCategory)
                                }
                            }

                            if (selectedEmotionsForConnectionCategory.size == 2) {
                                // Mamy dwie wybrane emocje podstawowe, spróbuj znaleźć diadę
                                val foundDiad = EmotionData.getCombinedEmotion(
                                    selectedEmotionsForConnectionCategory[0],
                                    selectedEmotionsForConnectionCategory[1]
                                ) ?: EmotionData.getCombinedEmotion(
                                    selectedEmotionsForConnectionCategory[1],
                                    selectedEmotionsForConnectionCategory[0]
                                )

                                if (foundDiad != null) {
                                    // Znaleziono diadę! Podświetl ją i otwórz kartę
                                    selectedEmotion = foundDiad
                                    primaryEmotionsToHighlight = listOfNotNull(
                                        EmotionData.getEmotion(
                                            foundDiad.primaryEmotion1!!,
                                            EmotionIntensity.MEDIUM
                                        )?.id,
                                        EmotionData.getEmotion(
                                            foundDiad.primaryEmotion2!!,
                                            EmotionIntensity.MEDIUM
                                        )?.id
                                    )
                                    // Po znalezieniu i wybraniu diady, tryb łączenia może zostać automatycznie wyłączony
                                    // lub użytkownik musi go wyłączyć ręcznie. Pozostawiam do decyzji.
                                    // connectionModeActive = false // Możesz odkomentować, aby automatycznie wyłączać tryb
                                } else {
                                    println("Nie znaleziono diady dla ${selectedEmotionsForConnectionCategory[0].name} i ${selectedEmotionsForConnectionCategory[1].name}")
                                    // Tutaj możesz pokazać Toast/Snackbar z informacją, że nie ma takiej diady
                                }
                                // selectedBasicEmotionsForConnection.clear() // Wyczyść wybór po próbie połączenia, nawet jeśli nic nie znaleziono
                                // Pamiętaj, że jeśli chcesz, aby wybrane emocje podstawowe pozostały podświetlone
                                // aż do zamknięcia karty szczegółów, nie czyść tej listy tutaj,
                                // ale w onClose EmotionDetailsCard.
                            }
                        } else {
                            // Kliknięto na emocję inną niż podstawowa MEDIUM w trybie łączenia (np. na inną intensywność, diadę lub poza kołem)
                            // Możesz wyczyścić wybór, aby zasygnalizować błąd lub po prostu zignorować
                            selectedEmotionsForConnectionCategory.clear()
                            selectedEmotion = null // Upewnij się, że żadna diada nie jest wybrana
                            primaryEmotionsToHighlight = emptyList()
                        }
                    } else {
                        // Standardowy tryb - otwórz kartę szczegółów
                        selectedEmotion = emotion
                        primaryEmotionsToHighlight =
                            if (emotion?.primaryEmotion1 != null && emotion.primaryEmotion2 != null) {
                                listOfNotNull(
                                    EmotionData.getEmotion(
                                        emotion.primaryEmotion1,
                                        EmotionIntensity.MEDIUM
                                    )?.id,
                                    EmotionData.getEmotion(
                                        emotion.primaryEmotion2,
                                        EmotionIntensity.MEDIUM
                                    )?.id
                                )
                            } else {
                                emptyList()
                            }
                    }
                },
                selectedEmotionId = selectedEmotion?.id,
                primaryEmotionsToHighlight = primaryEmotionsToHighlight,
                connectionModeActive = connectionModeActive,
                selectedBasicEmotionsForConnectionIds = selectedEmotionsForConnectionCategory.mapNotNull {
                    // Mapujemy BasicEmotion na ID emocji ze średnią intensywnością
                    EmotionData.getEmotion(it, EmotionIntensity.MEDIUM)?.id
                }
            )
        }

        // Karta szczegółów emocji
        AnimatedVisibility(
            visible = selectedEmotion != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            selectedEmotion?.let { emotion ->
                EmotionDetailsCard(
                    emotion = emotion,
                    onClose = {
                        selectedEmotion = null
                        primaryEmotionsToHighlight = emptyList()
                        selectedEmotionsForConnectionCategory.clear() // Wyczyść wybór w trybie łączenia przy zamknięciu karty
                    }
                )
            }
        }
    }
}

@Composable
fun EmotionDetailsCard(
    emotion: Emotion,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClose() }, // Kliknięcie na kartę zamyka ją
        elevation = CardDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Nagłówek z nazwą emocji i przyciskiem zamknięcia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mały kwadrat z kolorem emocji
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .background(emotion.color, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = emotion.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Przycisk zamknięcia
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zamknij",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Typ emocji (podstawowa/złożona) i intensywność
            Text(
                text = when {
                    emotion.emotionCategory != null && emotion.intensity != null -> {
                        val intensityText = when (emotion.intensity) {
                            EmotionIntensity.LOW -> "Niska intensywność"
                            EmotionIntensity.MEDIUM -> "Średnia intensywność"
                            EmotionIntensity.HIGH -> "Wysoka intensywność"
                        }
                        "Typ: Emocja podstawowa (${emotion.emotionCategory.name.lowercase().replace("_", " ")}, $intensityText)"
                    }
                    emotion.primaryEmotion1 != null && emotion.primaryEmotion2 != null -> {
                        "Typ: Diada pierwotna (złożona z ${emotion.primaryEmotion1.name.lowercase()} i ${emotion.primaryEmotion2.name.lowercase()})"
                    }
                    else -> "Typ: Nieznany"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(8.dp))

            // Opis emocji
            Text(
                text = "Opis: ${emotion.description}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            // Przykłady
            if (emotion.examples.isNotEmpty()) {
                Text(
                    text = "Przykłady:",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyColumn( // Użyj LazyColumn, jeśli lista przykładów może być długa
                    modifier = Modifier.height(emotion.examples.size.dp * 24).fillMaxWidth() // Ogranicz wysokość, aby nie rozciągała się poza ekran
                ) {
                    items(emotion.examples) { example ->
                        Text(
                            text = "• $example",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}