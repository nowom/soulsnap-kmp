package pl.soulsnaps.features.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.features.notifications.model.*
import pl.soulsnaps.domain.repository.NotificationRepository
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.utils.formatTimestamp
import pl.soulsnaps.utils.getDefaultLocale

/**
 * ReminderManager - Central manager for handling all app reminders and notifications
 */
class ReminderManager(
    val notificationService: NotificationService,
    private val notificationRepository: NotificationRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    
    private val _settings = MutableStateFlow<NotificationSettings?>(null)
    val settings: StateFlow<NotificationSettings?> = _settings.asStateFlow()
    
    private val _analytics = MutableStateFlow<NotificationAnalytics?>(null)
    val analytics: StateFlow<NotificationAnalytics?> = _analytics.asStateFlow()
    
    init {
        println("DEBUG: ReminderManager initialized")
    }
    
    /**
     * Initialize reminder manager for a user
     */
    suspend fun initializeForUser(userId: String) {
        println("DEBUG: ReminderManager.initializeForUser - userId: $userId")
        
        // Load user settings
        val userSettings = notificationRepository.getNotificationSettings(userId)
        _settings.value = userSettings
        
        // Load analytics
        val userAnalytics = notificationRepository.getNotificationAnalytics(userId)
        _analytics.value = userAnalytics
        
        // Set up default settings if none exist
        if (userSettings == null) {
            setupDefaultSettings(userId)
        }
        
        // Schedule any enabled reminders
        scheduleEnabledReminders(userId)
    }
    
    /**
     * Set up default notification settings for a new user
     */
    private suspend fun setupDefaultSettings(userId: String) {
        println("DEBUG: ReminderManager.setupDefaultSettings - userId: $userId")
        
        val defaultSettings = NotificationSettings(
            userId = userId,
            isEnabled = true,
            dailyQuizReminder = ReminderConfig(
                id = "daily_quiz_${userId}",
                type = NotificationType.DAILY_QUIZ_REMINDER,
                isEnabled = true,
                timeOfDay = "20:00", // 8 PM default
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7), // Every day
                userId = userId
            ),
            quizStreakReminder = ReminderConfig(
                id = "quiz_streak_${userId}",
                type = NotificationType.QUIZ_STREAK_REMINDER,
                isEnabled = true,
                timeOfDay = "21:00", // 9 PM default
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7), // Every day
                userId = userId
            ),
            weeklyInsights = ReminderConfig(
                id = "weekly_insights_${userId}",
                type = NotificationType.WEEKLY_INSIGHTS,
                isEnabled = true,
                timeOfDay = "19:00", // 7 PM default
                daysOfWeek = listOf(1), // Sunday
                userId = userId
            ),
            achievementNotifications = true,
            planUpgradeSuggestions = true
        )
        
        notificationRepository.saveNotificationSettings(defaultSettings)
        _settings.value = defaultSettings
    }
    
    /**
     * Schedule all enabled reminders for a user
     */
    private suspend fun scheduleEnabledReminders(userId: String) {
        println("DEBUG: ReminderManager.scheduleEnabledReminders - userId: $userId")
        
        val settings = _settings.value ?: return
        
        // Schedule daily quiz reminder
        settings.dailyQuizReminder?.let { config ->
            if (config.isEnabled) {
                scheduleDailyQuizReminder(userId, config)
            }
        }
        
        // Schedule quiz streak reminder
        settings.quizStreakReminder?.let { config ->
            if (config.isEnabled) {
                scheduleQuizStreakReminder(userId, config)
            }
        }
        
        // Schedule weekly insights
        settings.weeklyInsights?.let { config ->
            if (config.isEnabled) {
                scheduleWeeklyInsights(userId, config)
            }
        }
    }
    
    /**
     * Schedule daily quiz reminder
     */
    suspend fun scheduleDailyQuizReminder(userId: String, config: ReminderConfig) {
        println("DEBUG: ReminderManager.scheduleDailyQuizReminder - userId: $userId, time: ${config.timeOfDay}")
        
        val notification = createDailyQuizNotification(userId, config)
        val result = notificationService.scheduleRecurringNotification(notification)
        
        if (result.status == NotificationStatus.SCHEDULED) {
            println("DEBUG: Daily quiz reminder scheduled successfully")
        } else {
            println("ERROR: Failed to schedule daily quiz reminder: ${result.errorMessage}")
        }
    }
    
    /**
     * Schedule quiz streak reminder
     */
    suspend fun scheduleQuizStreakReminder(userId: String, config: ReminderConfig) {
        println("DEBUG: ReminderManager.scheduleQuizStreakReminder - userId: $userId, time: ${config.timeOfDay}")
        
        val notification = createQuizStreakNotification(userId, config)
        val result = notificationService.scheduleRecurringNotification(notification)
        
        if (result.status == NotificationStatus.SCHEDULED) {
            println("DEBUG: Quiz streak reminder scheduled successfully")
        } else {
            println("ERROR: Failed to schedule quiz streak reminder: ${result.errorMessage}")
        }
    }
    
    /**
     * Schedule weekly insights
     */
    suspend fun scheduleWeeklyInsights(userId: String, config: ReminderConfig) {
        println("DEBUG: ReminderManager.scheduleWeeklyInsights - userId: $userId, time: ${config.timeOfDay}")
        
        val notification = createWeeklyInsightsNotification(userId, config)
        val result = notificationService.scheduleRecurringNotification(notification)
        
        if (result.status == NotificationStatus.SCHEDULED) {
            println("DEBUG: Weekly insights scheduled successfully")
        } else {
            println("ERROR: Failed to schedule weekly insights: ${result.errorMessage}")
        }
    }
    
    /**
     * Create daily quiz notification
     */
    private fun createDailyQuizNotification(userId: String, config: ReminderConfig): AppNotification {
        val messages = listOf(
            "🧠 Time for your daily emotion check-in! How are you feeling today?",
            "✨ Ready to reflect on your day? Take your daily emotion quiz!",
            "🌟 Your daily emotional wellness check is waiting for you!",
            "💭 Take a moment to check in with your emotions today",
            "🎯 Daily quiz time! Let's explore how you're feeling"
        )
        
        val randomMessage = messages.random()
        
        return AppNotification(
            id = config.id,
            type = NotificationType.DAILY_QUIZ_REMINDER,
            title = "Daily Emotion Quiz",
            message = randomMessage,
            priority = NotificationPriority.NORMAL,
            scheduleType = NotificationScheduleType.DAILY,
            scheduledTime = getScheduledTime(config.timeOfDay),
            isEnabled = config.isEnabled,
            userId = userId,
            metadata = mapOf(
                "reminder_type" to "daily_quiz",
                "time_of_day" to config.timeOfDay
            )
        )
    }
    
    /**
     * Create quiz streak reminder notification
     */
    private fun createQuizStreakNotification(userId: String, config: ReminderConfig): AppNotification {
        val messages = listOf(
            "🔥 Don't break your quiz streak! Take your daily emotion quiz now",
            "⚡ Keep your emotional wellness streak alive! Quiz time!",
            "🎯 Your streak is waiting! Complete today's emotion quiz",
            "💪 Maintain your daily habit! Take your emotion quiz now",
            "🌟 Don't let your streak slip! Daily quiz is ready"
        )
        
        val randomMessage = messages.random()
        
        return AppNotification(
            id = config.id,
            type = NotificationType.QUIZ_STREAK_REMINDER,
            title = "Maintain Your Streak!",
            message = randomMessage,
            priority = NotificationPriority.HIGH,
            scheduleType = NotificationScheduleType.DAILY,
            scheduledTime = getScheduledTime(config.timeOfDay),
            isEnabled = config.isEnabled,
            userId = userId,
            metadata = mapOf(
                "reminder_type" to "quiz_streak",
                "time_of_day" to config.timeOfDay
            )
        )
    }
    
    /**
     * Create weekly insights notification
     */
    private fun createWeeklyInsightsNotification(userId: String, config: ReminderConfig): AppNotification {
        val messages = listOf(
            "📊 Your weekly emotional insights are ready! Check out your progress",
            "📈 Weekly emotional wellness report is available!",
            "🎯 Discover your emotional patterns this week",
            "💡 Your weekly emotional insights await!",
            "📋 Weekly emotional wellness summary is ready"
        )
        
        val randomMessage = messages.random()
        
        return AppNotification(
            id = config.id,
            type = NotificationType.WEEKLY_INSIGHTS,
            title = "Weekly Insights",
            message = randomMessage,
            priority = NotificationPriority.LOW,
            scheduleType = NotificationScheduleType.WEEKLY,
            scheduledTime = getScheduledTime(config.timeOfDay),
            isEnabled = config.isEnabled,
            userId = userId,
            metadata = mapOf(
                "reminder_type" to "weekly_insights",
                "time_of_day" to config.timeOfDay
            )
        )
    }
    
    /**
     * Get scheduled time for a given time string
     */
    private fun getScheduledTime(timeOfDay: String): Long {
        // Parse time string (HH:mm format)
        val timeParts = timeOfDay.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        // Get current time
        val currentTime = getCurrentTimeMillis()
        val oneDayInMs = 24 * 60 * 60 * 1000L
        
        // Calculate today's scheduled time
        val todayStart = (currentTime / oneDayInMs) * oneDayInMs
        val scheduledTime = todayStart + (hour * 60 * 60 * 1000L) + (minute * 60 * 1000L)
        
        // If the time has already passed today, schedule for tomorrow
        return if (scheduledTime <= currentTime) {
            scheduledTime + oneDayInMs
        } else {
            scheduledTime
        }
    }
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        println("DEBUG: ReminderManager.updateNotificationSettings - userId: ${settings.userId}")
        
        notificationRepository.saveNotificationSettings(settings)
        _settings.value = settings
        
        // Reschedule reminders with new settings
        scheduleEnabledReminders(settings.userId)
    }
    
    /**
     * Enable/disable a specific reminder type
     */
    suspend fun toggleReminder(userId: String, type: NotificationType, enabled: Boolean) {
        println("DEBUG: ReminderManager.toggleReminder - userId: $userId, type: $type, enabled: $enabled")
        
        val currentSettings = _settings.value ?: return
        
        val updatedSettings = when (type) {
            NotificationType.DAILY_QUIZ_REMINDER -> {
                currentSettings.copy(
                    dailyQuizReminder = currentSettings.dailyQuizReminder?.copy(isEnabled = enabled)
                )
            }
            NotificationType.QUIZ_STREAK_REMINDER -> {
                currentSettings.copy(
                    quizStreakReminder = currentSettings.quizStreakReminder?.copy(isEnabled = enabled)
                )
            }
            NotificationType.WEEKLY_INSIGHTS -> {
                currentSettings.copy(
                    weeklyInsights = currentSettings.weeklyInsights?.copy(isEnabled = enabled)
                )
            }
            else -> currentSettings
        }
        
        updateNotificationSettings(updatedSettings)
    }
    
    /**
     * Update reminder time
     */
    suspend fun updateReminderTime(userId: String, type: NotificationType, timeOfDay: String) {
        println("DEBUG: ReminderManager.updateReminderTime - userId: $userId, type: $type, time: $timeOfDay")
        
        val currentSettings = _settings.value ?: return
        
        val updatedSettings = when (type) {
            NotificationType.DAILY_QUIZ_REMINDER -> {
                currentSettings.copy(
                    dailyQuizReminder = currentSettings.dailyQuizReminder?.copy(timeOfDay = timeOfDay)
                )
            }
            NotificationType.QUIZ_STREAK_REMINDER -> {
                currentSettings.copy(
                    quizStreakReminder = currentSettings.quizStreakReminder?.copy(timeOfDay = timeOfDay)
                )
            }
            NotificationType.WEEKLY_INSIGHTS -> {
                currentSettings.copy(
                    weeklyInsights = currentSettings.weeklyInsights?.copy(timeOfDay = timeOfDay)
                )
            }
            else -> currentSettings
        }
        
        updateNotificationSettings(updatedSettings)
    }
    
    /**
     * Send immediate notification for testing
     */
    suspend fun sendTestNotification(userId: String, type: NotificationType) {
        println("DEBUG: ReminderManager.sendTestNotification - userId: $userId, type: $type")
        
        val notification = when (type) {
            NotificationType.DAILY_QUIZ_REMINDER -> {
                AppNotification(
                    id = "test_daily_quiz_${getCurrentTimeMillis()}",
                    type = type,
                    title = "Test: Daily Emotion Quiz",
                    message = "This is a test notification for daily quiz reminder",
                    userId = userId
                )
            }
            NotificationType.QUIZ_STREAK_REMINDER -> {
                AppNotification(
                    id = "test_quiz_streak_${getCurrentTimeMillis()}",
                    type = type,
                    title = "Test: Quiz Streak Reminder",
                    message = "This is a test notification for quiz streak reminder",
                    userId = userId
                )
            }
            else -> {
                AppNotification(
                    id = "test_${type.name.lowercase()}_${getCurrentTimeMillis()}",
                    type = type,
                    title = "Test: ${type.name}",
                    message = "This is a test notification",
                    userId = userId
                )
            }
        }
        
        val result = notificationService.sendImmediateNotification(notification)
        println("DEBUG: Test notification result: ${result.status}")
    }
    
    /**
     * Get smart reminder suggestions based on user behavior
     */
    suspend fun getSmartReminderSuggestions(userId: String): List<SmartReminderSuggestion> {
        // For now, return basic suggestions
        // In a real implementation, this would analyze user behavior patterns
        return listOf(
            SmartReminderSuggestion(
                type = NotificationType.DAILY_QUIZ_REMINDER,
                suggestedTime = "20:00",
                suggestedDays = listOf(1, 2, 3, 4, 5, 6, 7),
                reason = "Based on your typical quiz completion times",
                confidence = 0.8f,
                userId = userId
            )
        )
    }
}
