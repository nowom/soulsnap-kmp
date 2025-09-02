package pl.soulsnaps.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager {
    return remember { GalleryManager { onResult(null) } }
}

actual class GalleryManager(
    actual val onLaunch: () -> Unit
) {
    actual fun launch() {
        // Gallery functionality is not available in WebAssembly
        onLaunch()
    }
}
