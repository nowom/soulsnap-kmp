package pl.soulsnaps.features.notifications

actual object NotificationPermissionManagerFactory {
    
    actual fun create(): NotificationPermissionManager {
        return NotificationPermissionManager()
    }
}

