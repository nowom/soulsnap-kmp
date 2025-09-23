package pl.soulsnaps.features.notifications

/**
 * Factory for creating platform-specific NotificationService instances
 */
expect object NotificationServiceFactory {
    fun create(): NotificationService
}

