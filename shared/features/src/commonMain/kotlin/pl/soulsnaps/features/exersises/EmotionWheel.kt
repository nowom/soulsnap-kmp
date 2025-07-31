package pl.soulsnaps.features.exersises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionData
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Tooltip(
    text: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Popup(
            properties = PopupProperties(
                focusable = false,
                clippingEnabled = false
            )
        ) {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Komponent Koła Emocji z możliwością wyboru wielu emocji i tooltipami.
 * Emotion Wheel component with multi-selection and tooltips.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun EmotionWheelCircular(
    emotions: List<Emotion> = EmotionData.getAllBasicEmotions(),
    selectedEmotionIds: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var circleRadius by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    var tooltipVisible by remember { mutableStateOf(false) }
    var tooltipText by remember { mutableStateOf("") }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onSizeChanged { size ->
                circleRadius = min(size.width.toFloat(), size.height.toFloat()) / 2f * 0.8f // 80% of min dimension
                center = Offset(size.width / 2f, size.height / 2f)
            }
            .pointerInput(emotions) {
                detectTapGestures(
                    onPress = { offset ->
                        tooltipVisible = false // Hide tooltip on press
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance <= circleRadius) {
                            val angle = (atan2(dy, dx) + 2 * PI) % (2 * PI) // Angle from 0 to 2PI

                            val segmentAngle = 2 * PI / emotions.size
                            val segmentIndex = (angle / segmentAngle).toInt()

                            if (segmentIndex < emotions.size) {
                                val clickedEmotion = emotions[segmentIndex]
                                val newSelection = if (selectedEmotionIds.contains(clickedEmotion.id)) {
                                    selectedEmotionIds - clickedEmotion.id
                                } else {
                                    selectedEmotionIds + clickedEmotion.id
                                }
                                onSelectionChanged(newSelection)

                                // Show tooltip
                                tooltipText = clickedEmotion.description
                                tooltipOffset = offset
                                tooltipVisible = true
                            }
                        }
                    },
                    onTap = {
                        // Keep tooltip visible for a short duration after tap
                        // This might require a delayed hiding mechanism if not handled by Popup's auto-dismiss
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (circleRadius == 0f) return@Canvas

            val segmentAngle = 360f / emotions.size
            val innerRadius = circleRadius * 0.3f // Inner circle for text/center

            emotions.forEachIndexed { index, emotion ->
                val startAngle = (index * segmentAngle) - 90f // Start from top
                val sweepAngle = segmentAngle

                val isSelected = selectedEmotionIds.contains(emotion.id)
                val color = if (isSelected) emotion.color.copy(alpha = 0.8f) else emotion.color.copy(alpha = 0.5f)

                // Rysowanie segmentu koła
                // Draw wheel segment
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - circleRadius, center.y - circleRadius),
                    size = Size(circleRadius * 2, circleRadius * 2)
                )

                // Rysowanie obramowania dla wybranych emocji
                // Draw border for selected emotions
                if (isSelected) {
                    drawArc(
                        color = Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - circleRadius, center.y - circleRadius),
                        size = Size(circleRadius * 2, circleRadius * 2),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Obliczanie pozycji dla emoji i nazwy emocji
                // Calculate position for emoji and emotion name
                val midAngleRad =  ((startAngle + sweepAngle / 2f) * (PI / 180f)).toFloat()
                val textRadius = innerRadius + (circleRadius - innerRadius) / 2
                val textX = center.x + textRadius * cos(midAngleRad)
                val textY = center.y + textRadius * sin(midAngleRad)

                // Rysowanie nazwy emocji (bez emoji)
                // Draw emotion name (without emoji)
                val emotionText = emotion.name
                val emotionStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
                val emotionResult = textMeasurer.measure(emotionText, emotionStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = emotionText,
                    topLeft = Offset(textX - emotionResult.size.width / 2, textY - emotionResult.size.height / 2),
                    style = emotionStyle
                )

                // Rysowanie nazwy emocji
                // Draw emotion name
                val nameText = emotion.name
                val nameStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
                val nameResult = textMeasurer.measure(nameText, nameStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = nameText,
                    topLeft = Offset(textX - nameResult.size.width / 2, textY - nameResult.size.height / 2 + 10.dp.toPx()),
                    style = nameStyle
                )
            }

            // Rysowanie centralnego okręgu
            // Draw central circle
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = Color.Cyan,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Tooltip
        if (tooltipVisible) {
            Tooltip(
                text = tooltipText,
                isVisible = tooltipVisible,
                modifier = Modifier
                    .padding(
                        start = (tooltipOffset.x / 5).dp,
                        top = (tooltipOffset.y / 5).dp
                    )
            )
        }
    }
}