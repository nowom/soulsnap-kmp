package pl.soulsnaps.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens the app settings page on Android
 */
actual fun openAppSettings(context: Any?) {
    val androidContext = context as? Context ?: return
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", androidContext.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    androidContext.startActivity(intent)
}
