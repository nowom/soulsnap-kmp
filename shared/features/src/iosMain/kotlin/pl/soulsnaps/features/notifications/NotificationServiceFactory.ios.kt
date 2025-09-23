package pl.soulsnaps.features.notifications

actual object NotificationServiceFactory {
    
    actual fun create(): NotificationService {
        return NotificationService(NotificationPermissionManagerFactory.create())
    }
}
