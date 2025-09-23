package pl.soulsnaps.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.features.notifications.model.NotificationType
import pl.soulsnaps.features.notifications.PermissionResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia powiadomień") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (state.notificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            tint = if (state.notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Powiadomienia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Zarządzaj powiadomieniami i przypomnieniami",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Global notification toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Włącz powiadomienia",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Główny przełącznik dla wszystkich powiadomień",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }
                }
            }
            
            // Individual notification settings
            if (state.notificationsEnabled) {
                // Daily Quiz Reminder
                NotificationSettingCard(
                    title = "Dzienny quiz emocji",
                    description = "Przypomnienie o codziennym quizie emocji",
                    isEnabled = state.dailyQuizReminderEnabled,
                    onToggle = { viewModel.toggleReminder(NotificationType.DAILY_QUIZ_REMINDER, it) },
                    timeOfDay = state.dailyQuizReminderTime,
                    onTimeChange = { viewModel.updateReminderTime(NotificationType.DAILY_QUIZ_REMINDER, it) }
                )
                
                // Quiz Streak Reminder
                NotificationSettingCard(
                    title = "Przypomnienie o serii",
                    description = "Przypomnienie o utrzymaniu serii quizów",
                    isEnabled = state.quizStreakReminderEnabled,
                    onToggle = { viewModel.toggleReminder(NotificationType.QUIZ_STREAK_REMINDER, it) },
                    timeOfDay = state.quizStreakReminderTime,
                    onTimeChange = { viewModel.updateReminderTime(NotificationType.QUIZ_STREAK_REMINDER, it) }
                )
                
                // Weekly Insights
                NotificationSettingCard(
                    title = "Tygodniowe podsumowania",
                    description = "Cotygodniowe podsumowania i analizy",
                    isEnabled = state.weeklyInsightsEnabled,
                    onToggle = { viewModel.toggleReminder(NotificationType.WEEKLY_INSIGHTS, it) },
                    timeOfDay = state.weeklyInsightsTime,
                    onTimeChange = { viewModel.updateReminderTime(NotificationType.WEEKLY_INSIGHTS, it) }
                )
                
                // Other notifications
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Inne powiadomienia",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Achievement notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Powiadomienia o osiągnięciach",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Powiadomienia o odblokowanych osiągnięciach",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.achievementNotificationsEnabled,
                                onCheckedChange = { viewModel.toggleAchievementNotifications(it) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Plan upgrade suggestions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sugestie ulepszeń planu",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Powiadomienia o dostępnych ulepszeniach planu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.planUpgradeSuggestionsEnabled,
                                onCheckedChange = { viewModel.togglePlanUpgradeSuggestions(it) }
                            )
                        }
                    }
                }
            }
            
            // Test notifications button
            if (state.notificationsEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Testowanie",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Wyślij testowe powiadomienie, aby sprawdzić czy wszystko działa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.sendTestNotification(NotificationType.DAILY_QUIZ_REMINDER) }
                            ) {
                                Text("Test Quiz")
                            }
                            Button(
                                onClick = { viewModel.sendTestNotification(NotificationType.QUIZ_STREAK_REMINDER) }
                            ) {
                                Text("Test Seria")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    timeOfDay: String,
    onTimeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
            
            if (isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Godzina:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = onTimeChange,
                        label = { Text("HH:mm") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}
