package pl.soulsnaps.features.exersises.plutchikwheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.soulsnaps.domain.model.Emotion
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.EmotionIntensity
import pl.soulsnaps.domain.model.darker
import pl.soulsnaps.domain.model.lighter
import pl.soulsnaps.util.toDegrees
import pl.soulsnaps.util.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class EmotionSegment(
    val emotion: Emotion,
    val startAngle: Float, // Normalized to [0, 360) degrees, 0 at right, increasing counter-clockwise
    val endAngle: Float,   // Normalized to [0, 360) degrees
    val innerRadius: Float,
    val outerRadius: Float
)

data class DiadSegment(
    val emotion: Emotion,
    val startAngle: Float, // Normalized to [0, 360) degrees, 0 at right, increasing counter-clockwise
    val endAngle: Float,   // Normalized to [0, 360) degrees
    val innerRadius: Float,
    val outerRadius: Float,
    val textPosition: Offset // Store text position for click detection on text
)

@Composable
fun PlutchikLeafWheelWithLayers(
    emotionMap: Map<EmotionCategory?, List<Emotion>>,
    diadEmotions: List<Emotion>,
    onClick: (Emotion) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val emotionSegments = remember { mutableStateListOf<EmotionSegment>() }
    val diadSegments = remember { mutableStateListOf<DiadSegment>() }
    val canvasCenter = remember { mutableStateOf(Offset.Companion.Zero) }
    val canvasMaxRadius = remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier.Companion
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val center = canvasCenter.value
                    val maxRadius = canvasMaxRadius.value
                    val distance =
                        sqrt((tapOffset.x - center.x).pow(2) + (tapOffset.y - center.y).pow(2))

                    // === UJEDNOLICONY SYSTEM KĄTOWY ===
                    // 0 degrees at positive X axis (right), increasing counter-clockwise
                    val rawAngleRad = atan2(tapOffset.y - center.y, tapOffset.x - center.x)
                    var tapAngleDeg = rawAngleRad.toDegrees()
                    if (tapAngleDeg < 0) {
                        tapAngleDeg += 360f
                    }
                    println("TAP: angle=$tapAngleDeg, distance=$distance")

                    // Check if tap is within wheel bounds
                    if (distance > maxRadius * 1.5f) { // Increased outer bound for click detection
                        println("TAP: outside wheel bounds")
                        return@detectTapGestures
                    }

                    // Check emotion segments
                    for (segment in emotionSegments) {
                        if (isPointInSegment(tapAngleDeg, distance, segment)) {
                            println("TAP: selected emotion ${segment.emotion.name}")
                            onClick(segment.emotion)
                            return@detectTapGestures
                        }
                    }

                    // Check diad segments (click on text area)
                    for (segment in diadSegments) {
                        // For diads, we check if the tap is within the text bounding box
                        val textRect = Rect(
                            left = segment.textPosition.x,
                            top = segment.textPosition.y,
                            right = segment.textPosition.x + textMeasurer.measure(
                                AnnotatedString(segment.emotion.name),
                                TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Companion.Bold)
                            ).size.width,
                            bottom = segment.textPosition.y + textMeasurer.measure(
                                androidx.compose.ui.text.AnnotatedString(segment.emotion.name),
                                TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Companion.Bold)
                            ).size.height
                        )
                        if (textRect.contains(tapOffset)) {
                            println("TAP: selected diad (text) ${segment.emotion.name}")
                            onClick(segment.emotion)
                            return@detectTapGestures
                        }
                    }

                    // Fallback: find closest segment by angle if no exact match
                    val closestEmotion = emotionSegments.minByOrNull { segment ->
                        val segmentCenterAngle = (segment.startAngle + segment.endAngle) / 2f
                        val angleDiff = abs(tapAngleDeg - segmentCenterAngle)
                        min(angleDiff, 360f - angleDiff)
                    }

                    if (closestEmotion != null) {
                        println("TAP: fallback to ${closestEmotion.emotion.name}")
                        onClick(closestEmotion.emotion)
                    }
                }
            }) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = min(size.width, size.height) / 2.5f

        // Update remembered values
        canvasCenter.value = center
        canvasMaxRadius.value = maxRadius

        // Clear previous segments
        emotionSegments.clear()
        diadSegments.clear()

        val basicEmotions = emotionMap.keys.filterNotNull().sortedBy { it.ordinal }
        val sweepAngle = 360f / basicEmotions.size

        // Constants for leaf shape control
        val leafBulgeFactor = 0.9f
        val leafControlRadiusFactor = 0.6f

        basicEmotions.forEachIndexed { index, basic ->
            val emotions = emotionMap[basic] ?: return@forEachIndexed

            // Calculate the center angle for this basic emotion (0 degrees is right, increasing counter-clockwise)
            val centerAngle = index * sweepAngle

            val widenFactor = 1.1f
            val startAngle = centerAngle - (sweepAngle * widenFactor / 2f)
            val endAngle = centerAngle + (sweepAngle * widenFactor / 2f)

            val startRad = startAngle.toRadians()
            val endRad = endAngle.toRadians()
            val midRad = centerAngle.toRadians() // Mid angle for the petal tip

            val innerRadius = 0f
            val outerRadius = maxRadius

            val innerStart = Offset(
                center.x + innerRadius * cos(startRad),
                center.y + innerRadius * sin(startRad)
            )
            val innerEnd =
                Offset(center.x + innerRadius * cos(endRad), center.y + innerRadius * sin(endRad))
            val outerTip =
                Offset(center.x + outerRadius * cos(midRad), center.y + outerRadius * sin(midRad))

            // Control points for the quadratic Bezier curves
            val controlLeft = Offset(
                center.x + outerRadius * 0.65f * cos(startRad),
                center.y + outerRadius * 0.65f * sin(startRad)
            )
            val controlRight = Offset(
                center.x + outerRadius * 0.65f * cos(endRad),
                center.y + outerRadius * 0.65f * sin(endRad)
            )
            val controlBottom = Offset(
                (innerStart.x + innerEnd.x) / 2,
                (innerStart.y + innerEnd.y) / 2
            ) // This control point might need refinement for leaf shape

            val path = Path().apply {
                moveTo(innerStart.x, innerStart.y)
                quadraticBezierTo(controlLeft.x, controlLeft.y, outerTip.x, outerTip.y)
                quadraticBezierTo(controlRight.x, controlRight.y, innerEnd.x, innerEnd.y)
                quadraticBezierTo(controlBottom.x, controlBottom.y, innerStart.x, innerStart.y)
                close()
            }

            val lowColor =
                emotions.firstOrNull { it.intensity == EmotionIntensity.LOW }?.color?.lighter(0.2f)
            val mediumColor =
                emotions.firstOrNull { it.intensity == EmotionIntensity.MEDIUM }?.color
            val highColor =
                emotions.firstOrNull { it.intensity == EmotionIntensity.HIGH }?.color?.darker(0.2f)

            val gradient = Brush.Companion.radialGradient(
                colors = listOfNotNull(lowColor, mediumColor, highColor),
                center = center,
                radius = maxRadius
            )

            drawPath(path = path, brush = gradient)

            emotions.forEach { emotion ->

                // Store segment info for click detection
                val layerCount = 3
                val layerIndex = when (emotion.intensity) {
                    EmotionIntensity.HIGH -> 0
                    EmotionIntensity.MEDIUM -> 1
                    EmotionIntensity.LOW -> 2
                    else -> 1
                }
                val inner = maxRadius * (layerIndex.toFloat() / layerCount)
                val outer = maxRadius * ((layerIndex + 1f) / layerCount)

                // Convert angles to the 0-360 counter-clockwise system for storage
                val segmentStartAngleNormalized = (startAngle + 360f) % 360f
                val segmentEndAngleNormalized = (endAngle + 360f) % 360f

                emotionSegments.add(
                    EmotionSegment(
                        emotion = emotion,
                        startAngle = segmentStartAngleNormalized,
                        endAngle = segmentEndAngleNormalized,
                        innerRadius = inner,
                        outerRadius = outer
                    )
                )

                val layerCenterRadius = (inner + outer) / 2f
                val textCenter = Offset(
                    center.x + layerCenterRadius * cos(midRad),
                    center.y + layerCenterRadius * sin(midRad)
                )

                val maxTextWidth = (outer - inner) * 0.8f // 80% of layer width for text
//                val maxTextWidth = when (emotion.intensity) {
//                    EmotionIntensity.HIGH -> layerWidth * 0.6f // Smaller width for HIGH intensity (inner layer)
//                    else -> layerWidth * 0.9f // Normal width for other intensities
//                }
                val fontSize = when (emotion.intensity) {
                    EmotionIntensity.HIGH -> 6.sp
                    else -> 8.sp
                }
                val textLayoutResult = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(emotion.name),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Companion.Bold,
                        color = Color.Companion.Black
                    ),
                    maxLines = when (emotion.intensity) {
                        EmotionIntensity.HIGH -> 1
                        else -> 2
                    },
                    softWrap = true
                )
                val textSize = textLayoutResult.size
                val finalTextWidth = min(textSize.width.toFloat(), maxTextWidth)
                val finalTextHeight = textSize.height

                // === POPRAWIONE WYSRODKOWANIE TEKSTU EMOCJI PODSTAWOWYCH ===
                val textPosition = Offset(
                    textCenter.x - finalTextWidth / 2f,
                    textCenter.y - finalTextHeight / 2f
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textPosition
                )
            }
        }

        // Draw dashed circles for intensity levels
        val levels = 3
        val stroke = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.Companion.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        repeat(levels) { i ->
            val radius = maxRadius * (i + 1) / levels
            drawCircle(
                color = Color.Companion.DarkGray,
                center = center,
                radius = radius,
                style = stroke
            )
        }

        // Draw diads as outer ring labels
        val outerDiadRingRadius = maxRadius * 1.25f
        val diadTextRadius = outerDiadRingRadius + 15.dp.toPx()
        val diadAngleStep = 360f / diadEmotions.size

        diadEmotions.forEachIndexed { index, emotion ->
            // Calculate angle for diads (midpoint between basic emotions)
            // JOY (0) - TRUST (1) -> Diad between them is at 0.5 * sweepAngle
            // TRUST (1) - FEAR (2) -> Diad between them is at 1.5 * sweepAngle
            val diadCenterAngle = (index * diadAngleStep) + (diadAngleStep / 2f)
            val angleRad = diadCenterAngle.toRadians()

            val x = center.x + diadTextRadius * cos(angleRad)
            val y = center.y + diadTextRadius * sin(angleRad)

            val diadInnerRadius = maxRadius * 1.1f
            val diadOuterRadius = maxRadius * 1.4f

            // Calculate angles for diad segment for click detection
            val diadSegmentStartAngle = diadCenterAngle - (diadAngleStep / 2f)
            val diadSegmentEndAngle = diadCenterAngle + (diadAngleStep / 2f)

            // Normalize angles for storage
            val normalizedDiadStartAngle = (diadSegmentStartAngle + 360f) % 360f
            val normalizedDiadEndAngle = (diadSegmentEndAngle + 360f) % 360f

            val textLayoutResult = textMeasurer.measure(
                text = androidx.compose.ui.text.AnnotatedString(emotion.name),
                style = TextStyle(
                    fontSize = 9.sp,
                    color = Color.Companion.Black,
                    fontWeight = FontWeight.Companion.Bold
                ),
                maxLines = 2,
                softWrap = true
            )
            val textSize = textLayoutResult.size
            val maxDiadWidth = (maxRadius * 0.3f)
            val finalTextWidth = min(textSize.width.toFloat(), maxDiadWidth)
            val finalTextHeight = textSize.height

            // === POPRAWIONE WYSRODKOWANIE TEKSTU DIAD ===
            val textPosition = Offset(
                x - finalTextWidth / 2f,
                y - finalTextHeight / 2f
            )

            // Store diad segment for click detection
            diadSegments.add(
                DiadSegment(
                    emotion = emotion,
                    startAngle = normalizedDiadStartAngle,
                    endAngle = normalizedDiadEndAngle,
                    innerRadius = diadInnerRadius,
                    outerRadius = diadOuterRadius,
                    textPosition = textPosition // Store text position for accurate click detection on text
                )
            )

            // Draw background for diad text
            drawRect(
                color = Color.Companion.White.copy(alpha = 0.8f),
                topLeft = Offset(
                    textPosition.x - 3f,
                    textPosition.y - 2f
                ),
                size = Size(finalTextWidth + 6f, finalTextHeight + 4f)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textPosition
            )
        }
    }
}

/**
 * Check if a point is within a specific segment based on angle and distance
 * Uses a normalized angle system: [0, 360) degrees, 0 at right, increasing counter-clockwise.
 */
private fun isPointInSegment(angle: Float, distance: Float, segment: EmotionSegment): Boolean {
    // Check if distance is within segment bounds
    if (distance < segment.innerRadius || distance > segment.outerRadius) {
        return false
    }

    // Check if angle is within segment bounds
    // Handle case where segment crosses 0/360 degree boundary (e.g., JOY: 337.5 to 22.5)
    return if (segment.startAngle <= segment.endAngle) {
        angle >= segment.startAngle && angle <= segment.endAngle
    } else {
        angle >= segment.startAngle || angle <= segment.endAngle
    }
}