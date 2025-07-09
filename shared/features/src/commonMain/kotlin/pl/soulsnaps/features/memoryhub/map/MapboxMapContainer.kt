package pl.soulsnaps.features.memoryhub.map

import androidx.compose.runtime.Composable
import pl.soulsnaps.domain.model.Memory

@Composable
expect fun MapboxMapContainer(
    memories: List<Memory>,
    onMemoryClick: (Int) -> Unit
)