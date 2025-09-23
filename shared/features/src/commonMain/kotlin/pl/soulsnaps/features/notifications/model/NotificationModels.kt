package pl.soulsnaps.features.notifications.model

import kotlinx.serialization.Serializable
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Notification types for different app features
 */
@Serializable
enum class NotificationType {
    DAILY_QUIZ_REMINDER,
    QUIZ_STREAK_REMINDER,
    WEEKLY_INSIGHTS,
    AFFIRMATION_REMINDER,
    BREATHING_REMINDER,
    GRATITUDE_REMINDER,
    MEMORY_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    PLAN_UPGRADE_SUGGESTION
}

/**
 * Notification priority levels
 */
@Serializable
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Notification scheduling types
 */
@Serializable
enum class NotificationScheduleType {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

/**
 * Notification data model
 */
@Serializable
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val scheduleType: NotificationScheduleType = NotificationScheduleType.ONCE,
    val scheduledTime: Long? = null, // Unix timestamp
    val isEnabled: Boolean = true,
    val userId: String,
    val createdAt: Long = getCurrentTimeMillis(),
    val lastTriggered: Long? = null,
    val triggerCount: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Reminder configuration for specific features
 */
@Serializable
data class ReminderConfig(
    val id: String,
    val type: NotificationType,
    val isEnabled: Boolean = true,
    val timeOfDay: String, // Format: "HH:mm" (24-hour)
    val daysOfWeek: List<Int> = emptyList(), // 0=Sunday, 1=Monday, etc.
    val customMessage: String? = null,
    val userId: String,
    val createdAt: Long = getCurrentTimeMillis(),
    val updatedAt: Long = getCurrentTimeMillis()
)

/**
 * Notification settings for user preferences
 */
@Serializable
data class NotificationSettings(
    val userId: String,
    val isEnabled: Boolean = true,
    val dailyQuizReminder: ReminderConfig? = null,
    val quizStreakReminder: ReminderConfig? = null,
    val weeklyInsights: ReminderConfig? = null,
    val affirmationReminder: ReminderConfig? = null,
    val breathingReminder: ReminderConfig? = null,
    val gratitudeReminder: ReminderConfig? = null,
    val memoryReminder: ReminderConfig? = null,
    val achievementNotifications: Boolean = true,
    val planUpgradeSuggestions: Boolean = true,
    val quietHoursStart: String? = null, // Format: "HH:mm"
    val quietHoursEnd: String? = null, // Format: "HH:mm"
    val createdAt: Long = getCurrentTimeMillis(),
    val updatedAt: Long = getCurrentTimeMillis()
)

/**
 * Notification delivery status
 */
@Serializable
enum class NotificationStatus {
    PENDING,
    DELIVERED,
    FAILED,
    CANCELLED,
    SCHEDULED
}

/**
 * Notification delivery result
 */
@Serializable
data class NotificationResult(
    val notificationId: String,
    val status: NotificationStatus,
    val deliveredAt: Long? = null,
    val errorMessage: String? = null
)

/**
 * Smart reminder suggestions based on user behavior
 */
@Serializable
data class SmartReminderSuggestion(
    val type: NotificationType,
    val suggestedTime: String, // Format: "HH:mm"
    val suggestedDays: List<Int>, // 0=Sunday, 1=Monday, etc.
    val reason: String, // Why this suggestion was made
    val confidence: Float, // 0.0 to 1.0
    val userId: String
)

/**
 * Notification analytics data
 */
@Serializable
data class NotificationAnalytics(
    val userId: String,
    val totalSent: Int = 0,
    val totalDelivered: Int = 0,
    val totalOpened: Int = 0,
    val totalDismissed: Int = 0,
    val averageOpenRate: Float = 0.0f,
    val bestTimeToSend: String? = null, // Format: "HH:mm"
    val mostEngagingType: NotificationType? = null,
    val lastUpdated: Long = getCurrentTimeMillis()
)
