package pl.soulsnaps.features.notifications

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object NotificationServiceFactory : KoinComponent {
    
    private val context: Context by inject()
    private val permissionManager: NotificationPermissionManager by inject()
    
    actual fun create(): NotificationService {
        return NotificationService(context, permissionManager)
    }
}
