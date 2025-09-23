package pl.soulsnaps.features.notifications

import android.content.Context
import androidx.activity.ComponentActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object NotificationPermissionManagerFactory : KoinComponent {
    
    private val context: Context by inject()
    private val activity: ComponentActivity by inject()
    
    actual fun create(): NotificationPermissionManager {
        return NotificationPermissionManager(context, activity)
    }
}

