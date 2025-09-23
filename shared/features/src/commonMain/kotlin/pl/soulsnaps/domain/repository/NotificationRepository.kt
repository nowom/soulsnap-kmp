package pl.soulsnaps.domain.repository

import pl.soulsnaps.features.notifications.model.*

/**
 * Repository interface for notification and reminder data operations
 */
interface NotificationRepository {
    
    /**
     * Save notification settings for a user
     */
    suspend fun saveNotificationSettings(settings: NotificationSettings): Boolean
    
    /**
     * Get notification settings for a user
     */
    suspend fun getNotificationSettings(userId: String): NotificationSettings?
    
    /**
     * Save a notification
     */
    suspend fun saveNotification(notification: AppNotification): Boolean
    
    /**
     * Get notifications for a user
     */
    suspend fun getNotifications(userId: String): List<AppNotification>
    
    /**
     * Get notifications by type
     */
    suspend fun getNotificationsByType(userId: String, type: NotificationType): List<AppNotification>
    
    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Boolean
    
    /**
     * Delete notifications by type
     */
    suspend fun deleteNotificationsByType(userId: String, type: NotificationType): Int
    
    /**
     * Save notification analytics
     */
    suspend fun saveNotificationAnalytics(analytics: NotificationAnalytics): Boolean
    
    /**
     * Get notification analytics for a user
     */
    suspend fun getNotificationAnalytics(userId: String): NotificationAnalytics?
    
    /**
     * Update notification analytics
     */
    suspend fun updateNotificationAnalytics(analytics: NotificationAnalytics): Boolean
    
    /**
     * Save smart reminder suggestions
     */
    suspend fun saveSmartReminderSuggestions(userId: String, suggestions: List<SmartReminderSuggestion>): Boolean
    
    /**
     * Get smart reminder suggestions for a user
     */
    suspend fun getSmartReminderSuggestions(userId: String): List<SmartReminderSuggestion>
    
    /**
     * Save reminder configuration
     */
    suspend fun saveReminderConfig(config: ReminderConfig): Boolean
    
    /**
     * Get reminder configuration by type
     */
    suspend fun getReminderConfig(userId: String, type: NotificationType): ReminderConfig?
    
    /**
     * Get all reminder configurations for a user
     */
    suspend fun getReminderConfigs(userId: String): List<ReminderConfig>
    
    /**
     * Delete reminder configuration
     */
    suspend fun deleteReminderConfig(configId: String): Boolean
}

