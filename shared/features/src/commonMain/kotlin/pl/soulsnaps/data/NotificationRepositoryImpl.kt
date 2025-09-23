package pl.soulsnaps.data

import pl.soulsnaps.domain.repository.NotificationRepository
import pl.soulsnaps.features.notifications.model.*

/**
 * In-memory implementation of NotificationRepository
 * In production, this would use a database or remote storage
 */
class NotificationRepositoryImpl : NotificationRepository {
    
    private val notificationSettings = mutableMapOf<String, NotificationSettings>()
    private val notifications = mutableMapOf<String, MutableList<AppNotification>>()
    private val notificationAnalytics = mutableMapOf<String, NotificationAnalytics>()
    private val smartReminderSuggestions = mutableMapOf<String, MutableList<SmartReminderSuggestion>>()
    private val reminderConfigs = mutableMapOf<String, MutableList<ReminderConfig>>()
    
    override suspend fun saveNotificationSettings(settings: NotificationSettings): Boolean {
        println("DEBUG: NotificationRepository.saveNotificationSettings - userId: ${settings.userId}")
        notificationSettings[settings.userId] = settings
        return true
    }
    
    override suspend fun getNotificationSettings(userId: String): NotificationSettings? {
        println("DEBUG: NotificationRepository.getNotificationSettings - userId: $userId")
        return notificationSettings[userId]
    }
    
    override suspend fun saveNotification(notification: AppNotification): Boolean {
        println("DEBUG: NotificationRepository.saveNotification - id: ${notification.id}, userId: ${notification.userId}")
        val userNotifications = notifications.getOrPut(notification.userId) { mutableListOf() }
        userNotifications.add(notification)
        return true
    }
    
    override suspend fun getNotifications(userId: String): List<AppNotification> {
        println("DEBUG: NotificationRepository.getNotifications - userId: $userId")
        return notifications[userId] ?: emptyList()
    }
    
    override suspend fun getNotificationsByType(userId: String, type: NotificationType): List<AppNotification> {
        println("DEBUG: NotificationRepository.getNotificationsByType - userId: $userId, type: $type")
        return notifications[userId]?.filter { it.type == type } ?: emptyList()
    }
    
    override suspend fun deleteNotification(notificationId: String): Boolean {
        println("DEBUG: NotificationRepository.deleteNotification - id: $notificationId")
        notifications.values.forEach { userNotifications ->
            userNotifications.removeAll { it.id == notificationId }
        }
        return true
    }
    
    override suspend fun deleteNotificationsByType(userId: String, type: NotificationType): Int {
        println("DEBUG: NotificationRepository.deleteNotificationsByType - userId: $userId, type: $type")
        val userNotifications = notifications[userId] ?: return 0
        val initialSize = userNotifications.size
        userNotifications.removeAll { it.type == type }
        return initialSize - userNotifications.size
    }
    
    override suspend fun saveNotificationAnalytics(analytics: NotificationAnalytics): Boolean {
        println("DEBUG: NotificationRepository.saveNotificationAnalytics - userId: ${analytics.userId}")
        notificationAnalytics[analytics.userId] = analytics
        return true
    }
    
    override suspend fun getNotificationAnalytics(userId: String): NotificationAnalytics? {
        println("DEBUG: NotificationRepository.getNotificationAnalytics - userId: $userId")
        return notificationAnalytics[userId]
    }
    
    override suspend fun updateNotificationAnalytics(analytics: NotificationAnalytics): Boolean {
        println("DEBUG: NotificationRepository.updateNotificationAnalytics - userId: ${analytics.userId}")
        notificationAnalytics[analytics.userId] = analytics
        return true
    }
    
    override suspend fun saveSmartReminderSuggestions(userId: String, suggestions: List<SmartReminderSuggestion>): Boolean {
        println("DEBUG: NotificationRepository.saveSmartReminderSuggestions - userId: $userId, count: ${suggestions.size}")
        smartReminderSuggestions[userId] = suggestions.toMutableList()
        return true
    }
    
    override suspend fun getSmartReminderSuggestions(userId: String): List<SmartReminderSuggestion> {
        println("DEBUG: NotificationRepository.getSmartReminderSuggestions - userId: $userId")
        return smartReminderSuggestions[userId] ?: emptyList()
    }
    
    override suspend fun saveReminderConfig(config: ReminderConfig): Boolean {
        println("DEBUG: NotificationRepository.saveReminderConfig - id: ${config.id}, userId: ${config.userId}")
        val userConfigs = reminderConfigs.getOrPut(config.userId) { mutableListOf() }
        userConfigs.removeAll { it.id == config.id }
        userConfigs.add(config)
        return true
    }
    
    override suspend fun getReminderConfig(userId: String, type: NotificationType): ReminderConfig? {
        println("DEBUG: NotificationRepository.getReminderConfig - userId: $userId, type: $type")
        return reminderConfigs[userId]?.find { it.type == type }
    }
    
    override suspend fun getReminderConfigs(userId: String): List<ReminderConfig> {
        println("DEBUG: NotificationRepository.getReminderConfigs - userId: $userId")
        return reminderConfigs[userId] ?: emptyList()
    }
    
    override suspend fun deleteReminderConfig(configId: String): Boolean {
        println("DEBUG: NotificationRepository.deleteReminderConfig - id: $configId")
        reminderConfigs.values.forEach { userConfigs ->
            userConfigs.removeAll { it.id == configId }
        }
        return true
    }
}

