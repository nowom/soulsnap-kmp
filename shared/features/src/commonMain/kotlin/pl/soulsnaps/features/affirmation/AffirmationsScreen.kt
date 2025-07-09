package pl.soulsnaps.features.affirmation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.domain.model.Affirmation

@Composable
fun AffirmationsScreen() {
    val viewModel: AffirmationsViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("🎧 Affirmations", style = MaterialTheme.typography.titleLarge)

        FilterSection(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = { viewModel.onEvent(AffirmationsEvent.SelectFilter(it)) }
        )

        FavoriteToggleSection(
            showOnlyFavorites = uiState.showOnlyFavorites,
            onToggle = { viewModel.onEvent(AffirmationsEvent.ToggleFavoritesOnly) }
        )

        Spacer(Modifier.height(12.dp))

        if (uiState.affirmations.isEmpty()) {
            Text("No affirmations available.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            LazyColumn {
                items(uiState.affirmations) { affirmation ->
                    AffirmationItem(
                        affirmation = affirmation,
                        onPlayClick = { viewModel.onEvent(AffirmationsEvent.Play(affirmation)) },
                        onFavoriteClick = { viewModel.onEvent(AffirmationsEvent.ToggleFavorite(affirmation)) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FilterSection(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("Emotion", "Time of Day", "Theme")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { filter ->
            AssistChip(
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun FavoriteToggleSection(showOnlyFavorites: Boolean, onToggle: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterChip(
            selected = !showOnlyFavorites,
            onClick = { if (showOnlyFavorites) onToggle() },
            label = { Text("All") }
        )
        FilterChip(
            selected = showOnlyFavorites,
            onClick = { if (!showOnlyFavorites) onToggle() },
            label = { Text("★ Favorites") }
        )
    }
}

@Composable
fun AffirmationItem(
    affirmation: Affirmation,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = affirmation.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text("▶", modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { onPlayClick() })
            Text(
                text = if (affirmation.isFavorite) "★" else "☆",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onFavoriteClick() }
            )
        }
    }
}