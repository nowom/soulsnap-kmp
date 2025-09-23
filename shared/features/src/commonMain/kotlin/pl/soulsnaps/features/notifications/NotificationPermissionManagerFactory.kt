package pl.soulsnaps.features.notifications

/**
 * Factory for creating platform-specific NotificationPermissionManager instances
 */
expect object NotificationPermissionManagerFactory {
    fun create(): NotificationPermissionManager
}

