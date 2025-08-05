package pl.soulsnaps.features.exersises

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.EmotionColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SimplePlutchikWheel(
    onWheelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(280.dp)
            .clickable { onWheelClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val textMeasurer = rememberTextMeasurer()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2 * 0.8f

                // Define the 8 basic emotions in order
                val emotions = listOf(
                    EmotionCategory.JOY to EmotionColors.JOY,
                    EmotionCategory.TRUST to EmotionColors.TRUST,
                    EmotionCategory.FEAR to EmotionColors.FEAR,
                    EmotionCategory.SURPRISE to EmotionColors.SURPRISE,
                    EmotionCategory.SADNESS to EmotionColors.SADNESS,
                    EmotionCategory.DISGUST to EmotionColors.DISGUST,
                    EmotionCategory.ANGER to EmotionColors.ANGER,
                    EmotionCategory.ANTICIPATION to EmotionColors.ANTICIPATION
                )
                
                val segmentAngle = 360f / emotions.size
                
                emotions.forEachIndexed { index, (emotion, color) ->
                    val startAngle = (index * segmentAngle) - 90f // Start from top
                    val sweepAngle = segmentAngle
                    
                    // Draw segment
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // Draw border
                    drawArc(
                        color = Color.Black.copy(alpha = 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Draw emotion name
                    val midAngle = startAngle + sweepAngle / 2
                    val midAngleRad = (midAngle * PI / 180f).toFloat()
                    val textRadius = radius * 0.6f
                    val textX = center.x + textRadius * cos(midAngleRad)
                    val textY = center.y + textRadius * sin(midAngleRad)
                    
                    val emotionName = when (emotion) {
                        EmotionCategory.JOY -> "Radość"
                        EmotionCategory.TRUST -> "Zaufanie"
                        EmotionCategory.FEAR -> "Strach"
                        EmotionCategory.SURPRISE -> "Zaskoczenie"
                        EmotionCategory.SADNESS -> "Smutek"
                        EmotionCategory.DISGUST -> "Wstręt"
                        EmotionCategory.ANGER -> "Złość"
                        EmotionCategory.ANTICIPATION -> "Oczekiwanie"
                    }
                    
                    val textStyle = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    val textResult = textMeasurer.measure(emotionName, textStyle)
                    
                    drawText(
                        textMeasurer = textMeasurer,
                        text = emotionName,
                        topLeft = Offset(
                            textX - textResult.size.width / 2,
                            textY - textResult.size.height / 2
                        ),
                        style = textStyle
                    )
                }
            }
            
            // Center text
            Text(
                text = "Kliknij, aby\nzobaczyć szczegóły",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
} 