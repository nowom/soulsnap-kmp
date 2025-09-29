package pl.soulsnaps.components

import platform.UIKit.UIApplication
import platform.Foundation.NSURL

/**
 * Opens the app settings page on iOS
 */

class IOSSettingsNavigator : SettingsNavigator {
    override fun openAppSettings(): Boolean {

        val settingsUrl = NSURL.URLWithString("app-settings:")
        val app = UIApplication.sharedApplication
        return if (settingsUrl != null && app.canOpenURL(settingsUrl)) {
            settingsUrl.let { url ->
                UIApplication.sharedApplication.openURL(url)
            }
            true
        } else false
    }
}
