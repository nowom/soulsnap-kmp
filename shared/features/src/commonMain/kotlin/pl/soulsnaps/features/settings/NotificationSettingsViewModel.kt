package pl.soulsnaps.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.notifications.ReminderManager
import pl.soulsnaps.features.notifications.model.NotificationType
import pl.soulsnaps.features.notifications.model.NotificationSettings
import pl.soulsnaps.features.notifications.PermissionResult

data class NotificationSettingsState(
    val isLoading: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyQuizReminderEnabled: Boolean = true,
    val dailyQuizReminderTime: String = "20:00",
    val quizStreakReminderEnabled: Boolean = true,
    val quizStreakReminderTime: String = "21:00",
    val weeklyInsightsEnabled: Boolean = true,
    val weeklyInsightsTime: String = "19:00",
    val achievementNotificationsEnabled: Boolean = true,
    val planUpgradeSuggestionsEnabled: Boolean = true,
    val errorMessage: String? = null
)

class NotificationSettingsViewModel(
    private val userSessionManager: UserSessionManager,
    private val reminderManager: ReminderManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(NotificationSettingsState())
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()
    
    private val userId: String
        get() = userSessionManager.getCurrentUser()?.userId ?: "anonymous_user"
    
    fun loadSettings() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                
                val settings = reminderManager.settings.value
                if (settings != null) {
                    _state.value = _state.value.copy(
                        notificationsEnabled = settings.isEnabled,
                        dailyQuizReminderEnabled = settings.dailyQuizReminder?.isEnabled ?: false,
                        dailyQuizReminderTime = settings.dailyQuizReminder?.timeOfDay ?: "20:00",
                        quizStreakReminderEnabled = settings.quizStreakReminder?.isEnabled ?: false,
                        quizStreakReminderTime = settings.quizStreakReminder?.timeOfDay ?: "21:00",
                        weeklyInsightsEnabled = settings.weeklyInsights?.isEnabled ?: false,
                        weeklyInsightsTime = settings.weeklyInsights?.timeOfDay ?: "19:00",
                        achievementNotificationsEnabled = settings.achievementNotifications,
                        planUpgradeSuggestionsEnabled = settings.planUpgradeSuggestions,
                        isLoading = false
                    )
                } else {
                    // Initialize with default settings
                    reminderManager.initializeForUser(userId)
                    _state.value = _state.value.copy(isLoading = false)
                }
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Nie udało się załadować ustawień: ${e.message}"
                )
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = reminderManager.settings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(isEnabled = enabled)
                    reminderManager.updateNotificationSettings(updatedSettings)
                    _state.value = _state.value.copy(notificationsEnabled = enabled)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się zaktualizować ustawień: ${e.message}"
                )
            }
        }
    }
    
    fun toggleReminder(type: NotificationType, enabled: Boolean) {
        viewModelScope.launch {
            try {
                reminderManager.toggleReminder(userId, type, enabled)
                
                _state.value = when (type) {
                    NotificationType.DAILY_QUIZ_REMINDER -> {
                        _state.value.copy(dailyQuizReminderEnabled = enabled)
                    }
                    NotificationType.QUIZ_STREAK_REMINDER -> {
                        _state.value.copy(quizStreakReminderEnabled = enabled)
                    }
                    NotificationType.WEEKLY_INSIGHTS -> {
                        _state.value.copy(weeklyInsightsEnabled = enabled)
                    }
                    else -> _state.value
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się zaktualizować przypomnienia: ${e.message}"
                )
            }
        }
    }
    
    fun updateReminderTime(type: NotificationType, timeOfDay: String) {
        viewModelScope.launch {
            try {
                reminderManager.updateReminderTime(userId, type, timeOfDay)
                
                _state.value = when (type) {
                    NotificationType.DAILY_QUIZ_REMINDER -> {
                        _state.value.copy(dailyQuizReminderTime = timeOfDay)
                    }
                    NotificationType.QUIZ_STREAK_REMINDER -> {
                        _state.value.copy(quizStreakReminderTime = timeOfDay)
                    }
                    NotificationType.WEEKLY_INSIGHTS -> {
                        _state.value.copy(weeklyInsightsTime = timeOfDay)
                    }
                    else -> _state.value
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się zaktualizować godziny: ${e.message}"
                )
            }
        }
    }
    
    fun toggleAchievementNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = reminderManager.settings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(achievementNotifications = enabled)
                    reminderManager.updateNotificationSettings(updatedSettings)
                    _state.value = _state.value.copy(achievementNotificationsEnabled = enabled)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się zaktualizować ustawień: ${e.message}"
                )
            }
        }
    }
    
    fun togglePlanUpgradeSuggestions(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = reminderManager.settings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(planUpgradeSuggestions = enabled)
                    reminderManager.updateNotificationSettings(updatedSettings)
                    _state.value = _state.value.copy(planUpgradeSuggestionsEnabled = enabled)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się zaktualizować ustawień: ${e.message}"
                )
            }
        }
    }
    
    fun sendTestNotification(type: NotificationType) {
        viewModelScope.launch {
            try {
                reminderManager.sendTestNotification(userId, type)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Nie udało się wysłać testowego powiadomienia: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    /**
     * Check if notification permission is granted
     */
    suspend fun checkNotificationPermission(): Boolean {
        return reminderManager.notificationService.areNotificationsEnabled()
    }
    
    /**
     * Request notification permission
     */
    fun requestNotificationPermission(onResult: (PermissionResult) -> Unit) {
        viewModelScope.launch {
            try {
                val result = reminderManager.notificationService.requestNotificationPermissions()
                onResult(result)
            } catch (e: Exception) {
                onResult(PermissionResult.ERROR)
            }
        }
    }
    
    /**
     * Open app settings
     */
    fun openAppSettings() {
        viewModelScope.launch {
            try {
                // Access permission manager through notification service
                // This is a simplified approach - in a real app you'd inject permission manager directly
                reminderManager.notificationService.openAppSettings()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}
