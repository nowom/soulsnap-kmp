package pl.soulsnaps.features.exersises

import androidx.compose.animation.core.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock

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
        val start = Clock.System.now().toEpochMilliseconds()
        while (isActive && remainingTime > 0) {
            delay(1000L)
            val elapsed =  Clock.System.now().toEpochMilliseconds() - start
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
