package pl.soulsnaps.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager {
    return remember { CameraManager { onResult(null) } }
}

actual class CameraManager(
    actual val onLaunch: () -> Unit
) {
    actual fun launch() {
        // Camera functionality is not available in WebAssembly
        onLaunch()
    }
}
