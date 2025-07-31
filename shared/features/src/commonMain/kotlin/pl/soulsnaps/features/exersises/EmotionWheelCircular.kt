//
//package com.soulunity.ui.components
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import kotlin.math.PI
//import kotlin.math.cos
//import kotlin.math.sin
//
//@Composable
//fun EmotionWheelCircular(
//    emotions: List<Emotion> = baseEmotions,
//    radius: Dp = 140.dp,
//    onSelectionChanged: (List<Emotion>) -> Unit = {}
//) {
//    val selectedEmotions = remember { mutableStateListOf<Emotion>() }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(radius * 2 + 40.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val center = Offset(size.width / 2, size.height / 2)
//            val rPx = radius.toPx()
//            val angleStep = 2 * PI / emotions.size
//
//            emotions.forEachIndexed { i, emotion ->
//                val angle = i * angleStep - PI / 2
//                val x = center.x + cos(angle) * rPx
//                val y = center.y + sin(angle) * rPx
//
//                drawCircle(
//                    color = if (selectedEmotions.contains(emotion)) emotion.color else emotion.color.copy(alpha = 0.3f),
//                    radius = 40f,
//                    center = Offset(x.toFloat(), y.toFloat())
//                )
//            }
//        }
//
//        emotions.forEachIndexed { i, emotion ->
//            val angle = i * (2 * PI / emotions.size) - PI / 2
//            val xOffset = cos(angle) * radius.value
//            val yOffset = sin(angle) * radius.value
//
//            Box(
//                modifier = Modifier
//                    .offset(x = xOffset.dp, y = yOffset.dp)
//                    .size(60.dp)
//                    .background(
//                        color = if (selectedEmotions.contains(emotion)) emotion.color else emotion.color.copy(alpha = 0.2f),
//                        shape = CircleShape
//                    )
//                    .clickable {
//                        if (selectedEmotions.contains(emotion)) {
//                            selectedEmotions.remove(emotion)
//                        } else {
//                            selectedEmotions.add(emotion)
//                        }
//                        onSelectionChanged(selectedEmotions.toList())
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(emotion.emoji, style = MaterialTheme.typography.headlineSmall)
//            }
//        }
//    }
//
//    if (selectedEmotions.isNotEmpty()) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("Wybrane emocje:", fontWeight = FontWeight.Bold)
//            selectedEmotions.forEach {
//                Text("• ${it.name}", style = MaterialTheme.typography.bodyMedium)
//            }
//        }
//    }
//}
//
