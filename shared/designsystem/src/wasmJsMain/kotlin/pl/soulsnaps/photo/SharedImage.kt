package pl.soulsnaps.photo

import androidx.compose.ui.graphics.ImageBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedImage : SharedImageInterface {
    actual override fun toByteArray(): ByteArray? {
        // Image conversion is not available in WebAssembly
        return null
    }
    
    actual override fun toImageBitmap(): ImageBitmap? {
        // Image conversion is not available in WebAssembly
        return null
    }
}
