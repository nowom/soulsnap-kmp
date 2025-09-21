package pl.soulsnaps.features.location

import androidx.activity.ComponentActivity
import org.koin.java.KoinJavaComponent.inject

/**
 * Android factory for LocationPermissionManager
 * Injects ComponentActivity from Koin
 */
actual object LocationPermissionManagerFactory {
    actual fun create(): LocationPermissionManager {
        val activity: ComponentActivity by inject(ComponentActivity::class.java)
        return LocationPermissionManager(activity)
    }
}

