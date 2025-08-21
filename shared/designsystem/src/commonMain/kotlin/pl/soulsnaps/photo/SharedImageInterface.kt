package pl.soulsnaps.photo

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Interface for SharedImage - allows mocking in tests
 */
interface SharedImageInterface {
    fun toByteArray(): ByteArray?
    fun toImageBitmap(): ImageBitmap?
}
