package pl.soulsnaps.components

import platform.UIKit.UIApplication
import platform.Foundation.NSURL

/**
 * Opens the app settings page on iOS
 */
actual fun openAppSettings(context: Any?) {
    val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
    settingsUrl?.let { url ->
        UIApplication.sharedApplication.openURL(url)
    }
}
