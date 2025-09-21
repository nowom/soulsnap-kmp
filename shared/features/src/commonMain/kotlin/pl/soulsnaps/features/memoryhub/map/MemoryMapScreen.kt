package pl.soulsnaps.features.memoryhub.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.FullScreenCircularProgress

@Composable
internal fun MemoryMapRoute(
    onMemoryClick: (Int) -> Unit,
    viewModel: MemoryMapViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MemoryMapScreen(uiState, onMemoryClick)
}

@Composable
fun MemoryMapScreen(
    uiState: MapUiState,
    onMemoryClick: (Int) -> Unit
) {
    println("DEBUG: MemoryMapScreen - isLoading: ${uiState.isLoading}, memoriesCount: ${uiState.memoriesWithLocation.size}")
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                println("DEBUG: MemoryMapScreen - showing loading")
                FullScreenCircularProgress()
            }
            uiState.memoriesWithLocation.isEmpty() -> {
                println("DEBUG: MemoryMapScreen - showing empty view")
                EmptyMapView()
            }
            else -> {
                println("DEBUG: MemoryMapScreen - showing map with ${uiState.memoriesWithLocation.size} memories")
                MapboxMapContainer(
                    memories = uiState.memoriesWithLocation,
                    onMemoryClick = onMemoryClick
                )
            }
        }
    }
}

@Composable
fun EmptyMapView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No memories with location",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Add location to your memories to see them on the map",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}

