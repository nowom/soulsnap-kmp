package pl.soulsnaps.features.exersises.plutchikwheel

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.EmotionData
import pl.soulsnaps.domain.model.EmotionIntensity
import pl.soulsnaps.util.toDegrees
import pl.soulsnaps.util.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Klasa danych do przechowywania "gorącego punktu" (hotspotu) emocji złożonej.
 * Pomaga w precyzyjnym wykrywaniu kliknięć na tekstach diad.
 */
data class EmotionHotspot(
    val emotion: Emotion,
    val bounds: Rect
)

/**
 * Komponent Composowy rysujący interaktywne Koło Emocji Plutchika.
 * Umożliwia wybór emocji podstawowych (z intensywnością) oraz emocji złożonych (diad pierwotnych).
 *
 * @param modifier Modyfikator do zastosowania do Canvasu.
 * @param onEmotionSelected Callback wywoływany po wybraniu emocji. Zwraca null, jeśli kliknięto poza emocją.
 */
@Composable
fun PlutchikWheelCanvas(
    modifier: Modifier = Modifier,
    onEmotionSelected: (Emotion?) -> Unit, // Zmieniono na EmotionData.Emotion
    selectedEmotionId: String? = null,
    primaryEmotionsToHighlight: List<String> = emptyList(), // Dodany parametr
    connectionModeActive: Boolean = false, // Dodany parametr
    selectedBasicEmotionsForConnectionIds: List<String> = emptyList() // Dodany parametr
) {
    val textMeasurer = rememberTextMeasurer()
    val combinedEmotionHotspots = remember { mutableStateListOf<EmotionHotspot>() }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedStrokeWidth by infiniteTransition.animateFloat(
        initialValue = 2.dp.value,
        targetValue = 4.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        val size = min(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val radius = size / 2
        val centerX = size / 2
        val centerY = size / 2

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        var clickedEmotion: Emotion? = null

                        // 1. Sprawdź, czy kliknięto w obszarze koła
                        if (distance <= radius) {
                            val angleRad = atan2(dy, dx)
                            val angleDeg = (angleRad.toDegrees() + 360) % 360

                            val basicEmotion = getBasicEmotionFromAngle(angleDeg)
                            val intensity = getEmotionIntensityFromDistance(distance, radius)

                            // W trybie łączenia interesują nas tylko emocje podstawowe na poziomie MEDIUM
                            if (connectionModeActive) {
                                if (basicEmotion != null && intensity == EmotionIntensity.MEDIUM) {
                                    clickedEmotion = EmotionData.getEmotion(basicEmotion, EmotionIntensity.MEDIUM)
                                }
                            } else {
                                // W normalnym trybie interesują nas wszystkie intensywności
                                if (basicEmotion != null) {
                                    clickedEmotion = EmotionData.getEmotion(basicEmotion, intensity)
                                }
                            }
                        }

                        // 2. Jeśli nie kliknięto w obszarze podstawowych emocji LUB nie jesteśmy w trybie łączenia,
                        // sprawdź hotspoty emocji złożonych (diad).
                        // WAŻNE: W trybie łączenia, kliknięcia na diady nie powinny wyzwalać ich wyboru,
                        // bo mają być "odkrywane" przez łączenie emocji podstawowych.
                        if (!connectionModeActive && clickedEmotion == null) {
                            for (hotspot in combinedEmotionHotspots) {
                                if (hotspot.bounds.contains(offset)) {
                                    clickedEmotion = hotspot.emotion
                                    break // Znaleziono hotspot, wychodzimy z pętli
                                }
                            }
                        }

                        onEmotionSelected(clickedEmotion)
                    }
                }
        ) {
            combinedEmotionHotspots.clear()
            drawWheel(this, radius, centerX, centerY, textMeasurer, combinedEmotionHotspots,
                selectedEmotionId, animatedStrokeWidth.sp.toPx(), animatedAlpha, primaryEmotionsToHighlight,
                connectionModeActive, selectedBasicEmotionsForConnectionIds)
        }
    }
}

/**
 * Rysuje całe Koło Emocji Plutchika na DrawScope.
 */
private fun drawWheel(
    drawScope: DrawScope,
    fullSizeRadius: Float,
    centerX: Float,
    centerY: Float,
    textMeasurer: TextMeasurer,
    combinedEmotionHotspots: SnapshotStateList<EmotionHotspot>,
    selectedEmotionId: String?,
    animatedStrokeWidthPx: Float,
    animatedAlpha: Float,
    primaryEmotionsToHighlight: List<String>, // Dodany parametr
    connectionModeActive: Boolean, // Dodany parametr
    selectedBasicEmotionsForConnectionIds: List<String> // Dodany parametr
) {
    with(drawScope) {
        val scaleFactor = 0.85f // Możesz dostosować ten współczynnik (np. 0.9f dla 90% rozmiaru, 0.8f dla 80%)
        val radius = fullSizeRadius * scaleFactor

        val emotionsOrderCategories = listOf(
            EmotionCategory.JOY, EmotionCategory.TRUST, EmotionCategory.FEAR, EmotionCategory.SURPRISE,
            EmotionCategory.SADNESS, EmotionCategory.DISGUST, EmotionCategory.ANGER, EmotionCategory.ANTICIPATION
        )
        val segmentAngle = 360f / 8 // Kąt dla każdego segmentu podstawowego (45 stopni)

        val emotionCategoryStartingAngles = mapOf(
            EmotionCategory.JOY to -22.5f,
            EmotionCategory.TRUST to 22.5f,
            EmotionCategory.FEAR to 67.5f,
            EmotionCategory.SURPRISE to 112.5f,
            EmotionCategory.SADNESS to 157.5f,
            EmotionCategory.DISGUST to 202.5f,
            EmotionCategory.ANGER to 247.5f,
            EmotionCategory.ANTICIPATION to 292.5f
        )

        // MAPA DLA SZYBKIEGO DOSTĘPU DO POZYCJI TEKSTU EMOCJI PODSTAWOWYCH
        val basicEmotionTextPositions = mutableMapOf<String, Offset>()


        // --- Rysowanie segmentów podstawowych emocji i ich tekstu ---
        emotionsOrderCategories.forEachIndexed { index, basicEmotion ->
            val startAngleForDrawing = (index * segmentAngle) - (segmentAngle / 2)

            // Rysuj 3 warstwy intensywności dla każdej emocji podstawowej
            EmotionIntensity.entries.forEach { intensity ->
                val (innerRadius, outerRadius) = when (intensity) {
                    EmotionIntensity.LOW -> (radius * 0.66f) to (radius * 1.0f)
                    EmotionIntensity.MEDIUM -> (radius * 0.33f) to (radius * 0.66f)
                    EmotionIntensity.HIGH -> (radius * 0f) to (radius * 0.33f)
                }

                val emotion = EmotionData.getEmotion(basicEmotion, intensity)
                val color = emotion?.color ?: Color.Red

                // Warunek podświetlenia dla segmentu:
                // 1. Jest główną wybraną emocją (z karty szczegółów)
                // 2. Jest składową emocji diady (gdy diada jest wybrana)
                // 3. Jest emocją podstawową wybraną w trybie łączenia
                val shouldHighlightSegment = emotion?.id == selectedEmotionId ||
                        primaryEmotionsToHighlight.contains(emotion?.id) ||
                        (connectionModeActive && selectedBasicEmotionsForConnectionIds.contains(emotion?.id))

//                val gradientColors = when (intensity) {
//                    EmotionIntensity.HIGH -> listOf(baseColor, baseColor.lighten(0.5f).copy(alpha = 0.0f)) // Fades to transparent/lighter at center
//                    EmotionIntensity.MEDIUM -> listOf(baseColor.darken(0.05f), baseColor.lighten(0.05f)) // Subtelny gradient dla medium
//                    EmotionIntensity.LOW -> listOf(baseColor, baseColor.lighten(0.1f)) // Subtelny gradient dla low
//                }
//
//                val gradientBrush = Brush.radialGradient(
//                    colors = gradientColors,
//                    center = Offset(centerX, centerY),
//                    radius = outerRadius
//                )

                // Rysuj właściwy segment emocji
                drawArc(
                    color = color,
                    startAngle = startAngleForDrawing,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2)
                )

                // UŻYJ ANIMOWANEJ SZEROKOŚCI OBRYSU DLA PODŚWIETLENIA PODSTAWOWYCH EMOCJI
                if (shouldHighlightSegment) {
                    drawArc(
                        color = Color.White.copy(alpha = animatedAlpha),
                        startAngle = startAngleForDrawing,
                        sweepAngle = segmentAngle,
                        useCenter = true,
                        topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = animatedStrokeWidthPx)
                    )
                }
            }

            // Rysuj tekst dla wszystkich poziomów intensywności emocji podstawowych
            EmotionIntensity.entries.forEach { intensity ->
                val emotion = EmotionData.getEmotion(basicEmotion, intensity)
                if (emotion != null) {
                    // Oblicz promień tekstu na podstawie intensywności
                    val textRadius = when (intensity) {
                        EmotionIntensity.HIGH -> radius * 0.23f // Najbliżej centrum
                        EmotionIntensity.MEDIUM -> radius * 0.5f // Środkowy pierścień
                        EmotionIntensity.LOW -> radius * 0.83f // Najdalej od centrum
                    }

                    val textAngleDeg = startAngleForDrawing + segmentAngle / 2f
                    val textAngleRad = textAngleDeg.toRadians()

                    // Dostosuj rozmiar czcionki na podstawie intensywności
                    val fontSize = when (intensity) {
                        EmotionIntensity.HIGH -> 6.sp // Mniejsza czcionka dla wewnętrznego pierścienia
                        EmotionIntensity.MEDIUM -> 10.sp // Standardowa czcionka
                        EmotionIntensity.LOW -> 10.sp // Nieco mniejsza dla zewnętrznego pierścienia
                    }

                    val textLayoutResult = textMeasurer.measure(
                        text = emotion.name,
                        style = TextStyle(fontSize = fontSize, color = Color.Black, fontWeight = FontWeight.Bold)
                    )
                    val textX = centerX + textRadius * cos(textAngleRad) - textLayoutResult.size.width / 2
                    val textY = centerY + textRadius * sin(textAngleRad) - textLayoutResult.size.height / 2

                    // Zapisz pozycję tylko dla MEDIUM intensywności (dla hotspotów)
                    if (intensity == EmotionIntensity.MEDIUM) {
                        basicEmotionTextPositions[emotion.id] = Offset(textX + textLayoutResult.size.width / 2, textY + textLayoutResult.size.height / 2)
                    }

                    // Warunek podświetlenia dla tekstu:
                    // 1. Jest tekstem głównej wybranej emocji
                    // 2. Jest tekstem składowej emocji diady
                    // 3. Jest tekstem emocji podstawowej wybranej w trybie łączenia
                    val shouldHighlightText = (emotion.id == selectedEmotionId ||
                            primaryEmotionsToHighlight.contains(emotion.id) ||
                            (connectionModeActive && selectedBasicEmotionsForConnectionIds.contains(emotion.id)))

                    // Rysuj tekst emocji podstawowej
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(textX, textY)
                    )

                    // Podświetlanie tekstu z animacją - tło zamiast ramki
                    if (shouldHighlightText) {
                        // Tło z zaokrąglonymi rogami
                        drawRoundRect(
                            color = Color.White.copy(alpha = animatedAlpha * 0.3f),
                            topLeft = Offset(textX - 4.dp.toPx(), textY - 2.dp.toPx()),
                            size = Size(
                                textLayoutResult.size.width + 8.dp.toPx(),
                                textLayoutResult.size.height + 4.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                         //Subtelna ramka
                        drawRoundRect(
                            color = Color.White.copy(alpha = animatedAlpha),
                            topLeft = Offset(textX - 4.dp.toPx(), textY - 2.dp.toPx()),
                            size = Size(
                                textLayoutResult.size.width + 8.dp.toPx(),
                                textLayoutResult.size.height + 4.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(4.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }

        // --- Rysowanie linii oddzielających segmenty i intensywność ---
        // Linie promieniowe (między emocjami) - przesunięte o połowę segmentu
        for (i in 0 until 8) {
            val angleRad = (i * segmentAngle + segmentAngle / 2).toRadians()
            drawLine(
                color = Color.Black,
                start = Offset(centerX, centerY),
                end = Offset(centerX + radius * cos(angleRad), centerY + radius * sin(angleRad)),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Koła rozgraniczające intensywność (zgodnie z promieniami)
        drawCircle(Color(0x80666666), radius * 0.33f, center = Offset(centerX, centerY), style = Stroke(width = 0.6.dp.toPx()))
        drawCircle(Color(0x80666666), radius * 0.66f, center = Offset(centerX, centerY), style = Stroke(width = 0.5.dp.toPx()))


        // --- Rysowanie nazw emocji złożonych (Diady Pierwotne) ---
        val combinedEmotionsRadius = radius * 1.05f // Promień, na którym będą rysowane nazwy diad
        val combinedEmotionPairs = listOf(
            Pair(EmotionCategory.JOY, EmotionCategory.TRUST),
            Pair(EmotionCategory.TRUST, EmotionCategory.FEAR),
            Pair(EmotionCategory.FEAR, EmotionCategory.SURPRISE),
            Pair(EmotionCategory.SURPRISE, EmotionCategory.SADNESS),
            Pair(EmotionCategory.SADNESS, EmotionCategory.DISGUST),
            Pair(EmotionCategory.DISGUST, EmotionCategory.ANGER),
            Pair(EmotionCategory.ANGER, EmotionCategory.ANTICIPATION),
            Pair(EmotionCategory.ANTICIPATION, EmotionCategory.JOY)
        )

        combinedEmotionPairs.forEach { (em1, em2) ->
            val emotion = EmotionData.getCombinedEmotion(em1, em2) ?: return@forEach

            val angle1 = emotionCategoryStartingAngles[em1] ?: 0f
            val angle2 = emotionCategoryStartingAngles[em2] ?: 0f

            // POPRAWIONE OBLICZANIE ŚRODKOWEGO KĄTA DLA DIAD
            // Konwertujemy kąty na wektory, sumujemy je i obliczamy kąt wynikowy.
            val x1 = cos(angle1.toRadians())
            val y1 = sin(angle1.toRadians())
            val x2 = cos(angle2.toRadians())
            val y2 = sin(angle2.toRadians())

            val sumX = x1 + x2
            val sumY = y1 + y2

            // Obliczamy kąt sumy wektorów
            var avgAngleRad = atan2(sumY, sumX)
            // Konwertujemy na stopnie i normalizujemy do zakresu 0-360
            val angle1Raw = getBasicEmotionStartingAngle(em1)
            val angle2Raw = getBasicEmotionStartingAngle(em2)
            val avgAngleDeg = calculateAverageAngle(angle1Raw, angle2Raw)

            val avgAngleDeg1 = if ((em1 == EmotionCategory.ANTICIPATION && em2 == EmotionCategory.JOY) ||
                (em2 == EmotionCategory.ANTICIPATION && em1 == EmotionCategory.JOY)) {
                val normalizedAngle1 = if (angle1 < 0) angle1 + 360 else angle1
                val normalizedAngle2 = if (angle2 < 0) angle2 + 360 else angle2
                ((normalizedAngle1 + normalizedAngle2 + segmentAngle) / 2f) % 360f
            } else {
                (angle1 + angle2 + segmentAngle) / 2f
            }
            val textAngleRad = avgAngleDeg1.toRadians()

            val textLayoutResult = textMeasurer.measure(
                text = emotion.name,
                style = TextStyle(fontSize = 10.sp, color = Color.Black)
            )
            val textX = centerX + combinedEmotionsRadius * cos(textAngleRad) - textLayoutResult.size.width / 2
            val textY = centerY + combinedEmotionsRadius * sin(textAngleRad) - textLayoutResult.size.height / 2

            val combinedEmotionTextPosition = Offset(textX + textLayoutResult.size.width / 2, textY + textLayoutResult.size.height / 2)

            // Podświetlanie dla diad (tekstu) z animacją - tło zamiast ramki
            if (emotion.id == selectedEmotionId) {
                // Tło z zaokrąglonymi rogami
//                drawRoundRect(
//                    color = Color.White.copy(alpha = animatedAlpha * 0.3f),
//                    topLeft = Offset(textX - 4.dp.toPx(), textY - 2.dp.toPx()),
//                    size = Size(
//                        textLayoutResult.size.width + 8.dp.toPx(),
//                        textLayoutResult.size.height + 4.dp.toPx()
//                    ),
//                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
//                )
//
//                // Subtelna ramka
//                drawRoundRect(
//                    color = Color.White.copy(alpha = animatedAlpha),
//                    topLeft = Offset(textX - 4.dp.toPx(), textY - 2.dp.toPx()),
//                    size = Size(
//                        textLayoutResult.size.width + 8.dp.toPx(),
//                        textLayoutResult.size.height + 4.dp.toPx()
//                    ),
//                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
//                    style = Stroke(width = 1.dp.toPx())
//                )
            }

            // Dodaj Hotspot dla emocji złożonej (diady)
            val hotspotRect = Rect(
                left = textX,
                top = textY,
                right = textX + textLayoutResult.size.width,
                bottom = textY + textLayoutResult.size.height
            )
            combinedEmotionHotspots.add(EmotionHotspot(emotion, hotspotRect))
            val stroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            val combinedEmotionsRadius = radius * 1.05f // Promień, na którym będą rysowane nazwy diad
            val diadCircleRadius = radius * 1.10f // Promień dla dodatkowego okręgu (teraz otacza nazwy diad)

            // DODANIE OKRĘGU DLA WARSTWY DIAD, OTACZAJĄCEGO NAZWY EMOCJI
            drawCircle(Color.DarkGray, diadCircleRadius, center = Offset(centerX, centerY), style = stroke)
            // --- RYSOWANIE TEKSTU DIAD Z ROTACJĄ STYCZNĄ DO KOŁA ---
            // Punkt obrotu to środek bloku tekstowego
            val pivotX = textX + textLayoutResult.size.width / 2f
            val pivotY = textY + textLayoutResult.size.height / 2f

            // Kąt obrotu: kąt emocji + 90 stopni, aby tekst był styczny i skierowany na zewnątrz
            rotate(degrees = avgAngleDeg + 90f, pivot = Offset(pivotX, pivotY)) {
                // ULEPSZONE WIZUALNIE PODŚWIETLENIE DLA DIAD (wypełnione tło)
//                if (isDiadSelected) {
//                    drawRect(
//                        color = emotion.color.lighten(0.3f).copy(alpha = 0.7f),
//                        topLeft = Offset(textX - 4.dp.toPx(), textY - 4.dp.toPx()),
//                        size = Size(
//                            textLayoutResult.size.width + 8.dp.toPx(),
//                            textLayoutResult.size.height + 8.dp.toPx()
//                        )
//                    )
//                }

                // Rysuj właściwy tekst diady
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textX, textY)
                )
            }

            // RYSOWANIE LINII ŁĄCZĄCYCH (tylko dla wybranej diady)
            if (emotion.id == selectedEmotionId && emotion.primaryEmotion1 != null && emotion.primaryEmotion2 != null) {
                val primary1Id = EmotionData.getEmotion(emotion.primaryEmotion1, EmotionIntensity.MEDIUM)?.id
                val primary2Id = EmotionData.getEmotion(emotion.primaryEmotion2, EmotionIntensity.MEDIUM)?.id

                val pos1 = primary1Id?.let { basicEmotionTextPositions[it] }
                val pos2 = primary2Id?.let { basicEmotionTextPositions[it] }

                if (pos1 != null && pos2 != null) {
                    drawLine(
                        color = Color.Gray,
                        start = pos1,
                        end = combinedEmotionTextPosition, // combinedEmotionTextPosition to środek nierotowanego bloku
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawLine(
                        color = Color.Gray,
                        start = pos2,
                        end = combinedEmotionTextPosition,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
        }
    }
}

/**
 * Określa podstawową emocję na podstawie kąta w stopniach.
 * Kąty są od 0 do 360, gdzie 0 jest na prawo (JOY).
 */
private fun getBasicEmotionFromAngle(angleDeg: Float): EmotionCategory? {
    // Normalizuj kąt, aby był w zakresie [0, 360)
    val normalizedAngle = (angleDeg + 360) % 360

    return when (normalizedAngle) {
        in 337.5f..360f, in 0f..22.5f -> EmotionCategory.JOY
        in 22.5f..67.5f -> EmotionCategory.TRUST
        in 67.5f..112.5f -> EmotionCategory.FEAR
        in 112.5f..157.5f -> EmotionCategory.SURPRISE
        in 157.5f..202.5f -> EmotionCategory.SADNESS
        in 202.5f..247.5f -> EmotionCategory.DISGUST
        in 247.5f..292.5f -> EmotionCategory.ANGER
        in 292.5f..337.5f -> EmotionCategory.ANTICIPATION
        else -> null
    }
}

/**
 * Określa intensywność emocji na podstawie odległości od centrum koła.
 * Im bliżej centrum, tym wyższa intensywność.
 */
private fun getEmotionIntensityFromDistance(distance: Float, radius: Float): EmotionIntensity {
    return when {
        distance <= radius * 0.33f -> EmotionIntensity.HIGH // Najbliżej centrum
        distance <= radius * 0.66f -> EmotionIntensity.MEDIUM // Środkowy pierścień
        else -> EmotionIntensity.LOW // Najdalej od centrum
    }
}

private fun getBasicEmotionStartingAngle(emotionCategory: EmotionCategory): Float {
    val emotionsOrderCategories = listOf(
        EmotionCategory.JOY, EmotionCategory.TRUST, EmotionCategory.FEAR, EmotionCategory.SURPRISE,
        EmotionCategory.SADNESS, EmotionCategory.DISGUST, EmotionCategory.ANGER, EmotionCategory.ANTICIPATION
    )
    val segmentAngle = 360f / 8f
    val index = emotionsOrderCategories.indexOf(emotionCategory)
    return (index * segmentAngle)
}

// Helper do obliczania średniego kąta, obsługujący zawijanie dla kątów bliskich 0/360
private fun calculateAverageAngle(angle1: Float, angle2: Float): Float {
    val diff = abs(angle1 - angle2)
    return if (diff > 180) {
        val avg = (angle1 + angle2 + 360) / 2
        avg % 360
    } else {
        (angle1 + angle2) / 2
    }
}