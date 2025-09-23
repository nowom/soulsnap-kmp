package pl.soulsnaps.features.notifications

import pl.soulsnaps.features.notifications.model.*

/**
 * Platform-specific notification service interface
 */
expect class NotificationService {
    
    /**
     * Check if notifications are enabled for the app
     */
    suspend fun areNotificationsEnabled(): Boolean
    
    /**
     * Request notification permissions from the user
     */
    suspend fun requestNotificationPermissions(): PermissionResult
    
    /**
     * Schedule a one-time notification
     */
    suspend fun scheduleNotification(notification: AppNotification): NotificationResult
    
    /**
     * Schedule a recurring notification
     */
    suspend fun scheduleRecurringNotification(notification: AppNotification): NotificationResult
    
    /**
     * Cancel a scheduled notification
     */
    suspend fun cancelNotification(notificationId: String): Boolean
    
    /**
     * Cancel all notifications of a specific type
     */
    suspend fun cancelNotificationsByType(type: NotificationType): Int
    
    /**
     * Cancel all notifications for the user
     */
    suspend fun cancelAllNotifications(): Int
    
    /**
     * Get all scheduled notifications
     */
    suspend fun getScheduledNotifications(): List<AppNotification>
    
    /**
     * Check if a specific notification is scheduled
     */
    suspend fun isNotificationScheduled(notificationId: String): Boolean
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(settings: NotificationSettings): Boolean
    
    /**
     * Get current notification settings
     */
    suspend fun getNotificationSettings(userId: String): NotificationSettings?
    
    /**
     * Send immediate notification (for testing or urgent messages)
     */
    suspend fun sendImmediateNotification(notification: AppNotification): NotificationResult
    
    /**
     * Handle notification tap/click
     */
    suspend fun handleNotificationTap(notificationId: String, action: String? = null)
    
    /**
     * Get notification analytics
     */
    suspend fun getNotificationAnalytics(userId: String): NotificationAnalytics?
    
    /**
     * Update notification analytics
     */
    suspend fun updateNotificationAnalytics(analytics: NotificationAnalytics): Boolean
    
    /**
     * Open app settings for manual permission grant
     */
    suspend fun openAppSettings(): Boolean
}
