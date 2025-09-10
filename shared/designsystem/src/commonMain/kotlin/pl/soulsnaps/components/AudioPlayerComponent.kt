package pl.soulsnaps.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.soulsnaps.audio.*
import pl.soulsnaps.designsystem.AppColorScheme

/**
 * Audio player component for playing affirmations and other audio content
 */
@Composable
fun AudioPlayerComponent(
    audioManager: AudioManager,
    modifier: Modifier = Modifier,
    showVolumeControl: Boolean = true,
    showSpeedControl: Boolean = true,
    compactMode: Boolean = false
) {
    val isPlaying by audioManager.isPlaying.collectAsState()
    val currentAudio by audioManager.currentAudio.collectAsState()
    val playbackState by audioManager.playbackState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showControls by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1.0f) }
    var speed by remember { mutableStateOf(1.0f) }
    
    if (currentAudio != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppColorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Audio info
                AudioInfoSection(
                    audioItem = currentAudio!!,
                    isPlaying = isPlaying,
                    playbackState = playbackState
                )
                
                // Main controls
                AudioControlsSection(
                    isPlaying = isPlaying,
                    playbackState = playbackState,
                    onPlayPause = {
                        coroutineScope.launch {
                            audioManager.togglePlayPause()
                        }
                    },
                    onStop = {
                        coroutineScope.launch {
                            audioManager.stop()
                        }
                    }
                )
                
                // Extended controls
                if (showControls && !compactMode) {
                    ExtendedControlsSection(
                        volume = volume,
                        speed = speed,
                        showVolumeControl = showVolumeControl,
                        showSpeedControl = showSpeedControl,
                        onVolumeChange = { newVolume ->
                            volume = newVolume
                            coroutineScope.launch {
                                audioManager.setVolume(newVolume)
                            }
                        },
                        onSpeedChange = { newSpeed ->
                            speed = newSpeed
                            coroutineScope.launch {
                                audioManager.setPlaybackSpeed(newSpeed)
                            }
                        }
                    )
                }
                
                // Toggle controls button
                if (!compactMode) {
                    TextButton(
                        onClick = { showControls = !showControls }
                    ) {
                        Text(
                            text = if (showControls) "Ukryj opcje" else "Pokaż opcje",
                            color = AppColorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioInfoSection(
    audioItem: AudioItem,
    isPlaying: Boolean,
    playbackState: PlaybackState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Audio type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when (audioItem.type) {
                        AudioType.AFFIRMATION -> AppColorScheme.primary.copy(alpha = 0.1f)
                        AudioType.MEMORY -> AppColorScheme.secondary.copy(alpha = 0.1f)
                        AudioType.EXERCISE -> AppColorScheme.tertiary.copy(alpha = 0.1f)
                        AudioType.MEDITATION -> AppColorScheme.primary.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (audioItem.type) {
                    AudioType.AFFIRMATION -> "🎧"
                    AudioType.MEMORY -> "🎵"
                    AudioType.EXERCISE -> "🧘"
                    AudioType.MEDITATION -> "🕯️"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Audio text/info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (audioItem.type) {
                    AudioType.AFFIRMATION -> "Afirmacja"
                    AudioType.MEMORY -> "Nagranie"
                    AudioType.EXERCISE -> "Ćwiczenie"
                    AudioType.MEDITATION -> "Medytacja"
                },
                style = MaterialTheme.typography.labelMedium,
                color = AppColorScheme.onSurfaceVariant
            )
            
            if (audioItem.text != null) {
                Text(
                    text = audioItem.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColorScheme.onSurface,
                    maxLines = 2
                )
            }
            
            // Status
            Text(
                text = when (playbackState) {
                    PlaybackState.LOADING -> "Ładowanie..."
                    PlaybackState.PLAYING -> "Odtwarzanie"
                    PlaybackState.PAUSED -> "Wstrzymane"
                    PlaybackState.STOPPED -> "Zatrzymane"
                    PlaybackState.ERROR -> "Błąd"
                    else -> ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = when (playbackState) {
                    PlaybackState.ERROR -> MaterialTheme.colorScheme.error
                    else -> AppColorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun AudioControlsSection(
    isPlaying: Boolean,
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.size(56.dp),
            containerColor = AppColorScheme.primary
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pauza" else "Odtwórz",
                tint = AppColorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Stop button
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppColorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zatrzymaj",
                tint = AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExtendedControlsSection(
    volume: Float,
    speed: Float,
    showVolumeControl: Boolean,
    showSpeedControl: Boolean,
    onVolumeChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showVolumeControl) {
            VolumeControl(
                volume = volume,
                onVolumeChange = onVolumeChange
            )
        }
        
        if (showSpeedControl) {
            SpeedControl(
                speed = speed,
                onSpeedChange = onSpeedChange
            )
        }
    }
}

@Composable
private fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Głośność",
                tint = AppColorScheme.onSurfaceVariant
            )
            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = AppColorScheme.onSurfaceVariant
            )
        }
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SpeedControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prędkość",
                style = MaterialTheme.typography.labelMedium,
                color = AppColorScheme.onSurfaceVariant
            )
            Text(
                text = "${speed}x",
                style = MaterialTheme.typography.labelMedium,
                color = AppColorScheme.onSurfaceVariant
            )
        }
        
        Slider(
            value = speed,
            onValueChange = onSpeedChange,
            valueRange = 0.5f..2.0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
