package pl.soulsnaps.features.memoryhub.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            FullScreenCircularProgress()
        } else if (uiState.memoriesWithLocation.isEmpty()) {
            //EmptyMapView()
        } else {
            MapboxMapContainer(
                memories = uiState.memoriesWithLocation,
                onMemoryClick = onMemoryClick
            )
        }
    }
}

