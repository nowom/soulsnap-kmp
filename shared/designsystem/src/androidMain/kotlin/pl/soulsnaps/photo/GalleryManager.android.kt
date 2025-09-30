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
                try {
                    // Grant persistent URI permissions to avoid WM lock issues
                    context.contentResolver.takePersistableUriPermission(
                        selectedUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    
                    // Process bitmap in a coroutine to avoid blocking UI thread
                    val bitmap = BitmapUtils.getBitmapFromUri(selectedUri, contentResolver)
                    if (bitmap != null) {
                        val sharedImage = SharedImage(bitmap)
                        onResult.invoke(sharedImage)
                    } else {
                        println("GalleryManager: Failed to load bitmap from URI: $selectedUri")
                        onResult.invoke(null)
                    }
                } catch (e: SecurityException) {
                    println("GalleryManager: Security exception when accessing URI: ${e.message}")
                    onResult.invoke(null)
                } catch (e: Exception) {
                    println("GalleryManager: Exception when processing image: ${e.message}")
                    onResult.invoke(null)
                }
            } ?: run {
                onResult.invoke(null)
            }
        }
    )
    
    return remember {
        GalleryManager(
            onLaunch = {
                try {
                    galleryLauncher.launch("image/*")
                } catch (e: Exception) {
                    println("GalleryManager: Exception when launching gallery: ${e.message}")
                    onResult.invoke(null)
                }
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