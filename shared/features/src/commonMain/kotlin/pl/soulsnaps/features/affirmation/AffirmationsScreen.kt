package pl.soulsnaps.features.affirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.components.AudioPlayerComponent
import pl.soulsnaps.audio.AudioManager
import org.koin.compose.koinInject

@Composable
fun AffirmationsScreen(
    audioManager: AudioManager = koinInject()
) {
    val viewModel: AffirmationsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        AffirmationsHeader(
            title = "🎧 Afirmacje",
            subtitle = "Pozytywne myśli na każdy dzień"
        )

        // Filters and controls
        AffirmationsControls(
            selectedFilter = uiState.selectedFilter,
            showOnlyFavorites = uiState.showOnlyFavorites,
            onFilterSelected = { viewModel.onEvent(AffirmationsEvent.SelectFilter(it)) },
            onToggleFavorites = { viewModel.onEvent(AffirmationsEvent.ToggleFavoritesOnly) }
        )

        // Audio Player - removed, using ViewModel's audioManager instead

        // Affirmations list
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.affirmations.isEmpty()) {
            EmptyAffirmationsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.affirmations) { affirmation ->
                    AffirmationCard(
                        affirmation = affirmation,
                        isPlaying = uiState.isPlaying && uiState.currentPlayingId == affirmation.id,
                        onPlayClick = { 
                            if (uiState.isPlaying && uiState.currentPlayingId == affirmation.id) {
                                viewModel.onEvent(AffirmationsEvent.Stop)
                            } else {
                                viewModel.onEvent(AffirmationsEvent.Play(affirmation))
                            }
                        },
                        onFavoriteClick = { viewModel.onEvent(AffirmationsEvent.ToggleFavorite(affirmation)) }
                    )
                }
            }
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun AffirmationsHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AffirmationsControls(
    selectedFilter: String,
    showOnlyFavorites: Boolean,
    onFilterSelected: (String) -> Unit,
    onToggleFavorites: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter chips
        FilterSection(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )
        
        // Favorites toggle
        FavoriteToggleSection(
            showOnlyFavorites = showOnlyFavorites,
            onToggle = onToggleFavorites
        )
    }
}

@Composable
fun FilterSection(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf(
        "Wszystkie" to "Wszystkie afirmacje",
        "Emocja" to "Spokój i relaks", 
        "Pora dnia" to "Poranne afirmacje",
        "Temat" to "Miłość do siebie"
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (filter, description) ->
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = {
                    Column(Modifier.height(36.dp),
                        verticalArrangement = Arrangement.Center) {
                        Text(filter)
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedFilter == filter)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                selected = selectedFilter == filter,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (selectedFilter == filter)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun FavoriteToggleSection(showOnlyFavorites: Boolean, onToggle: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !showOnlyFavorites,
            onClick = { if (showOnlyFavorites) onToggle() },
            label = { Text("Wszystkie") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Wszystkie",
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        FilterChip(
            selected = showOnlyFavorites,
            onClick = { if (!showOnlyFavorites) onToggle() },
            label = { Text("Ulubione") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Ulubione",
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
fun AffirmationCard(
    affirmation: Affirmation,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Affirmation text
            Text(
                text = affirmation.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emotion and time tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmotionChip(emotion = affirmation.emotion)
                    TimeChip(timeOfDay = affirmation.timeOfDay)
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play/Pause button
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isPlaying) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Zatrzymaj" else "Odtwórz",
                            tint = if (isPlaying) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Favorite button
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (affirmation.isFavorite) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = if (affirmation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (affirmation.isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
                            tint = if (affirmation.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionChip(emotion: String) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = emotion,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TimeChip(timeOfDay: String) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            text = timeOfDay,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun EmptyAffirmationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎧",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Brak afirmacji",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nie ma jeszcze żadnych afirmacji do wyświetlenia.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}