package pl.soulsnaps.photo

import androidx.compose.ui.graphics.ImageBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SharedImage : SharedImageInterface {
    override fun toByteArray(): ByteArray?
    override fun toImageBitmap(): ImageBitmap?
}