package pl.soulsnaps.features.exersises

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import pl.soulsnaps.domain.model.BreathingPhase
import pl.soulsnaps.utils.getCurrentTimeMillis

@Composable
fun BreathingSessionScreen(
    totalDurationMillis: Long = 2 * 60 * 1000L // 2 minuty sesji
) {
    var phase by remember { mutableStateOf("Wdech") }
    var scale by remember { mutableStateOf(1f) }
    var remainingTime by remember { mutableStateOf(totalDurationMillis) }
    var isFinished by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val infiniteAnim = rememberInfiniteTransition()
    val animatedScale = infiniteAnim.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Timer
    LaunchedEffect(Unit) {
        val start = getCurrentTimeMillis()
        while (isActive && remainingTime > 0) {
            delay(1000L)
            val elapsed =  getCurrentTimeMillis() - start
            remainingTime = totalDurationMillis - elapsed
        }
        if (remainingTime <= 0) {
            isFinished = true
        }
    }

    // Oddechowy cykl
    LaunchedEffect(Unit) {
        while (isActive && !isFinished) {
            phase = "Wdech"
            scale = 1f
            delay(4000)

            phase = "Wstrzymaj"
            delay(7000)

            phase = "Wydech"
            scale = 0.5f
            delay(8000)

            phase = "Wstrzymaj"
            delay(7000)
        }
    }

    // Gradientowe tło
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFinished) {
            Text(
                text = "Sesja zakończona 🌟",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(phase, style = MaterialTheme.typography.headlineSmall)

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(animatedScale.value)
                        .background(color = Color.White.copy(alpha = 0.3f), shape = CircleShape)
                )

                Text(
                    text = formatMillis(remainingTime),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

/**
 * Ekran pełnej sesji oddechowej z animacją i timerem.
 * Full breathing session screen with animation and timer.
 */
@Composable
fun BreathingSessionScreen(
    phase: BreathingPhase,
    timeRemaining: Int,
    phaseProgress: Float,
    onSessionEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val inhaleColor = Color(0xFF81C784) // Light Green
    val holdColor = Color(0xFF64B5F6) // Light Blue
    val exhaleColor = Color(0xFFE57373) // Light Red

    val targetColor = when (phase) {
        BreathingPhase.INHALE -> inhaleColor
        BreathingPhase.HOLD -> holdColor
        BreathingPhase.EXHALE -> exhaleColor
        BreathingPhase.PAUSE -> Color.Gray // Should not be visible during actual phases
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    val animatedRadius by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (phase) {
                    BreathingPhase.INHALE -> 4000
                    BreathingPhase.HOLD -> 7000
                    BreathingPhase.EXHALE -> 8000
                    BreathingPhase.PAUSE -> 0
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val currentRadius = when (phase) {
        BreathingPhase.INHALE -> 0.3f + (0.5f - 0.3f) * phaseProgress
        BreathingPhase.HOLD -> 0.5f
        BreathingPhase.EXHALE -> 0.5f - (0.5f - 0.3f) * phaseProgress
        BreathingPhase.PAUSE -> 0.3f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedBackgroundColor.copy(alpha = 0.4f),
                        animatedBackgroundColor.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animowany okrąg oddechu
        // Animated breathing circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * currentRadius
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = radius,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = when (phase) {
                    BreathingPhase.INHALE -> "Wdech"
                    BreathingPhase.HOLD -> "Wstrzymaj"
                    BreathingPhase.EXHALE -> "Wydech"
                    BreathingPhase.PAUSE -> "Przygotuj się"
                },
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "test", //"${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}"
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 64.dp)
            )

            if (timeRemaining <= 0) {
                Text(
                    text = "Sesja zakończona 🌟",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 32.dp)
                )
                Button(
                    onClick = onSessionEnd,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Zakończ")
                }
            } else {
                Button(
                    onClick = onSessionEnd,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Zakończ Sesję")
                }
            }
        }
    }
}