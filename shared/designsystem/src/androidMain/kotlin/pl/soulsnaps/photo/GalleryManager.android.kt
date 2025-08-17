package pl.soulsnaps.photo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import pl.soulsnaps.utils.BitmapUtils

@Composable
actual fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { selectedUri ->
                val sharedImage = SharedImage(BitmapUtils.getBitmapFromUri(selectedUri, contentResolver))
                onResult.invoke(sharedImage)
            }
        }
    )
    
    return remember {
        GalleryManager(
            onLaunch = {
                galleryLauncher.launch("image/*")
            }
        )
    }
}

actual class GalleryManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}