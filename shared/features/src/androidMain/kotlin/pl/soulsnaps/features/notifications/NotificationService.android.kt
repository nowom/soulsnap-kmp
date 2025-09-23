package pl.soulsnaps.features.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.soulsnaps.features.notifications.model.*
import kotlin.coroutines.resume
import pl.soulsnaps.utils.getCurrentTimeMillis

actual class NotificationService(
    private val context: Context,
    private val permissionManager: NotificationPermissionManager
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Daily Quiz Reminder Channel
            val dailyQuizChannel = NotificationChannel(
                "daily_quiz_reminder",
                "Daily Quiz Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to take your daily emotion quiz"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Quiz Streak Channel
            val streakChannel = NotificationChannel(
                "quiz_streak_reminder",
                "Quiz Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to maintain your quiz streak"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Weekly Insights Channel
            val insightsChannel = NotificationChannel(
                "weekly_insights",
                "Weekly Insights",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Weekly emotional insights and analytics"
                enableVibration(false)
                setShowBadge(false)
            }
            
            // General App Channel
            val generalChannel = NotificationChannel(
                "general_notifications",
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            
            systemNotificationManager.createNotificationChannels(
                listOf(dailyQuizChannel, streakChannel, insightsChannel, generalChannel)
            )
        }
    }
    
    actual suspend fun areNotificationsEnabled(): Boolean {
        return permissionManager.hasNotificationPermission()
    }
    
    actual suspend fun requestNotificationPermissions(): PermissionResult {
        return permissionManager.requestNotificationPermission()
    }
    
    actual suspend fun scheduleNotification(notification: AppNotification): NotificationResult = suspendCancellableCoroutine { continuation ->
        try {
            val channelId = getChannelIdForType(notification.type)
            val priority = getPriorityForType(notification.priority)
            
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_id", notification.id)
                putExtra("notification_type", notification.type.name)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notification.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setPriority(priority)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(notification.scheduledTime ?: getCurrentTimeMillis())
            
            // Add action buttons based on notification type
            when (notification.type) {
                NotificationType.DAILY_QUIZ_REMINDER -> {
                    val takeQuizIntent = Intent().apply {
                        putExtra("action", "take_quiz")
                        putExtra("notification_id", notification.id)
                    }
                    val takeQuizPendingIntent = PendingIntent.getActivity(
                        context,
                        notification.id.hashCode() + 1,
                        takeQuizIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        android.R.drawable.ic_media_play,
                        "Take Quiz",
                        takeQuizPendingIntent
                    )
                }
                NotificationType.QUIZ_STREAK_REMINDER -> {
                    val maintainStreakIntent = Intent().apply {
                        putExtra("action", "maintain_streak")
                        putExtra("notification_id", notification.id)
                    }
                    val maintainStreakPendingIntent = PendingIntent.getActivity(
                        context,
                        notification.id.hashCode() + 2,
                        maintainStreakIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        android.R.drawable.ic_media_play,
                        "Maintain Streak",
                        maintainStreakPendingIntent
                    )
                }
                else -> {
                    // No specific actions for other notification types
                }
            }
            
            notificationManager.notify(notification.id.hashCode(), builder.build())
            
            val result = NotificationResult(
                notificationId = notification.id,
                status = NotificationStatus.DELIVERED,
                deliveredAt = getCurrentTimeMillis()
            )
            continuation.resume(result)
        } catch (e: Exception) {
            val result = NotificationResult(
                notificationId = notification.id,
                status = NotificationStatus.FAILED,
                errorMessage = e.message
            )
            continuation.resume(result)
        }
    }
    
    actual suspend fun scheduleRecurringNotification(notification: AppNotification): NotificationResult {
        // For now, we'll treat recurring notifications the same as one-time notifications
        // In a real implementation, you'd use AlarmManager or WorkManager for recurring notifications
        return scheduleNotification(notification)
    }
    
    actual suspend fun cancelNotification(notificationId: String): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            notificationManager.cancel(notificationId.hashCode())
            continuation.resume(true)
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }
    
    actual suspend fun cancelNotificationsByType(type: NotificationType): Int = suspendCancellableCoroutine { continuation ->
        // For simplicity, we'll return 0. In a real implementation, you'd track notification IDs by type
        continuation.resume(0)
    }
    
    actual suspend fun cancelAllNotifications(): Int = suspendCancellableCoroutine { continuation ->
        try {
            notificationManager.cancelAll()
            continuation.resume(0) // Return count of cancelled notifications
        } catch (e: Exception) {
            continuation.resume(0)
        }
    }
    
    actual suspend fun getScheduledNotifications(): List<AppNotification> = suspendCancellableCoroutine { continuation ->
        // For simplicity, return empty list. In a real implementation, you'd query scheduled notifications
        continuation.resume(emptyList())
    }
    
    actual suspend fun isNotificationScheduled(notificationId: String): Boolean = suspendCancellableCoroutine { continuation ->
        // For simplicity, return false. In a real implementation, you'd check if notification is scheduled
        continuation.resume(false)
    }
    
    actual suspend fun updateNotificationSettings(settings: NotificationSettings): Boolean = suspendCancellableCoroutine { continuation ->
        // Store settings in SharedPreferences or database
        // For now, return true
        continuation.resume(true)
    }
    
    actual suspend fun getNotificationSettings(userId: String): NotificationSettings? = suspendCancellableCoroutine { continuation ->
        // Load settings from SharedPreferences or database
        // For now, return null
        continuation.resume(null)
    }
    
    actual suspend fun sendImmediateNotification(notification: AppNotification): NotificationResult {
        return scheduleNotification(notification)
    }
    
    actual suspend fun handleNotificationTap(notificationId: String, action: String?) {
        // Handle notification tap - this would typically navigate to the appropriate screen
        println("DEBUG: Notification tapped - ID: $notificationId, Action: $action")
    }
    
    actual suspend fun getNotificationAnalytics(userId: String): NotificationAnalytics? = suspendCancellableCoroutine { continuation ->
        // Load analytics from storage
        // For now, return null
        continuation.resume(null)
    }
    
    actual suspend fun updateNotificationAnalytics(analytics: NotificationAnalytics): Boolean = suspendCancellableCoroutine { continuation ->
        // Save analytics to storage
        // For now, return true
        continuation.resume(true)
    }
    
    actual suspend fun openAppSettings(): Boolean {
        return permissionManager.openAppSettings()
    }
    
    private fun getChannelIdForType(type: NotificationType): String {
        return when (type) {
            NotificationType.DAILY_QUIZ_REMINDER -> "daily_quiz_reminder"
            NotificationType.QUIZ_STREAK_REMINDER -> "quiz_streak_reminder"
            NotificationType.WEEKLY_INSIGHTS -> "weekly_insights"
            else -> "general_notifications"
        }
    }
    
    private fun getPriorityForType(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_MAX
        }
    }
}
