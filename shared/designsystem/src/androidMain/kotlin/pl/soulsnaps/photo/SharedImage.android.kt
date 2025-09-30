package pl.soulsnaps.photo

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

actual class SharedImage(private val bitmap: android.graphics.Bitmap?) : SharedImageInterface {
    override actual fun toByteArray(): ByteArray? {
        return if (bitmap != null && !bitmap.isRecycled) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            // Use higher JPEG compression to reduce file size and prevent SQLite CursorWindow errors
            @Suppress("MagicNumber") bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream
            )
            byteArrayOutputStream.toByteArray()
        } else {
            println("toByteArray null or bitmap recycled")
            null
        }
    }

    override actual fun toImageBitmap(): ImageBitmap? {
        return if (bitmap != null && !bitmap.isRecycled) {
            bitmap.asImageBitmap()
        } else {
            println("toImageBitmap null or bitmap recycled")
            null
        }
    }
}