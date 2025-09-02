package pl.soulsnaps.features.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.soulsnaps.utils.getCurrentTimeMillis

@Composable
fun VoiceRecorder(
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingTime++
            }
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recording status
        if (isRecording) {
            Text(
                text = "Nagrywanie... ${recordingTime}s",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Kliknij, aby rozpocząć nagrywanie",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Recording button
        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    // Stop recording
                    isRecording = false
                    onRecordingComplete("voice_recording_${getCurrentTimeMillis()}.mp3")
                } else {
                    // Start recording
                    isRecording = true
                    recordingTime = 0
                }
            },
            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                tint = Color.White
            )
        }
        
        // Recording instructions
        if (!isRecording) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Wskazówki:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Mów wyraźnie i naturalnie",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Nagraj kilka zdań o sobie",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• To pomoże w personalizacji afirmacji",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 