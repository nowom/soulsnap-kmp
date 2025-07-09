package pl.soulsnaps.features.memoryhub.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.domain.model.Memory

@Composable
internal fun MomentsGalleryRoute(
    onMemoryClick: (Int) -> Unit,
    onAddMemoryClick: () -> Unit,
    viewModel: MomentsGalleryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GalleryScreen(uiState = state, onMemoryClick = onMemoryClick)
}

@Composable
fun GalleryScreen(
    uiState: MomentsGalleryViewModel.GalleryUiState,
    onMemoryClick: (Int) -> Unit
) {
    when {
        uiState.isLoading -> {
            FullScreenCircularProgress()
        }

        uiState.memories.isEmpty() -> {
            EmptyGalleryView()
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.memories) { memory ->
                    GallerySnapItem(memory = memory, onClick = { onMemoryClick(memory.id) })
                }
            }
        }
    }
}

@Composable
fun GallerySnapItem(
    memory: Memory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
    ) {
        memory.photoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun EmptyGalleryView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Collections, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Text("Brak zapisanych chwil", color = Color.Gray)
        }
    }
}