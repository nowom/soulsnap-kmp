package pl.soulsnaps.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.access.manager.PlanDefinition
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.DashboardCard
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.TitleText
import pl.soulsnaps.components.AudioPlayerComponent
import pl.soulsnaps.audio.AudioManager
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.model.PlanType

@Composable
fun ScopeAwareDashboard(
    state: DashboardState,
    onAddNewSnap: () -> Unit = {},
    onNavigateToSoulSnaps: () -> Unit = {},
    onNavigateToAffirmations: () -> Unit = {},
    onNavigateToExercises: () -> Unit = {},
    onNavigateToVirtualMirror: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToDailyQuiz: () -> Unit = {},
    onUpgradePlan: () -> Unit = {},
    onPlayAffirmation: () -> Unit = {},
    onPauseAffirmation: () -> Unit = {},
    onChangeAffirmation: () -> Unit = {},
    onFavoriteAffirmation: () -> Unit = {},
    onTakeMoodQuiz: () -> Unit = {},
    onShowNotifications: () -> Unit = {},
    onRefreshDashboard: () -> Unit = {},
    userPlanManager: UserPlanManager,
    planRegistry: PlanRegistryReader,
    audioManager: AudioManager = org.koin.compose.koinInject()
) {
    val currentPlan by userPlanManager.currentPlan.collectAsState(initial = null)
    var planDefinition by remember { mutableStateOf<PlanDefinition?>(null) }

    LaunchedEffect(currentPlan) {
        if (currentPlan != null) {
            planDefinition = planRegistry.getPlan(currentPlan!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1) Pasek powitania + data
        GreetingCard(
            userName = state.userName,
            onNotificationClick = onShowNotifications
        )

        // 2) Karta "Afirmacja dnia"
        AffirmationOfTheDayCard(
            affirmation = state.affirmationOfTheDay,
            isPlaying = state.isAffirmationPlaying,
            isOffline = state.isOffline,
            onPlayClick = onPlayAffirmation,
            onPauseClick = onPauseAffirmation,
            onChangeClick = onChangeAffirmation,
            onFavoriteClick = onFavoriteAffirmation,
            onNavigateToAffirmations = onNavigateToAffirmations
        )
        
        // Audio Player Component
        AudioPlayerComponent(
            audioManager = audioManager,
            compactMode = true
        )

        // 3) "Twoje emocje dziś"
        MoodTodayCard(
            emotion = state.emotionOfTheDay.name,
            emoji = state.emotionOfTheDay.emoji,
            description = state.emotionOfTheDay.description,
            onQuizClick = onTakeMoodQuiz
        )

        // 4) "Ostatni SoulSnap"
        LastSnapCard(
            lastSnap = state.lastSoulSnap,
            monthlyUsage = state.monthlyUsage,
            monthlyLimit = state.monthlyLimit,
            onSnapClick = { /* TODO: Navigate to snap details */ },
            onAddFirstSnap = onAddNewSnap
        )

        // 5) Biofeedback (light) - opcjonalny
        if (state.biofeedbackData.heartRate != null ||
            state.biofeedbackData.sleep != null ||
            state.biofeedbackData.steps != null) {
            BiofeedbackStrip(
                heartRate = state.biofeedbackData.heartRate,
                sleep = state.biofeedbackData.sleep,
                steps = state.biofeedbackData.steps
            )
        }

        // 6) Szybkie skróty
        QuickShortcutsRow(
            onSnapsClick = onNavigateToSoulSnaps,
            onAffirmationsClick = onNavigateToAffirmations,
            onExercisesClick = onNavigateToExercises,
            onDailyQuizClick = onNavigateToDailyQuiz
        )

        // Plan-specific information (if needed)
        currentPlan?.let { plan ->
            planDefinition?.let { definition ->
                PlanInfoCard(
                    currentPlan = plan,
                    planDefinition = definition,
                    onUpgrade = onUpgradePlan
                )
            }
        }
    }
}

@Composable
private fun PlanInfoCard(
    currentPlan: String,
    planDefinition: PlanDefinition,
    onUpgrade: () -> Unit
) {
    DashboardCard(
        title = "Twój plan: ${getPlanDisplayName(currentPlan)}",
        subtitle = getPlanDescription(currentPlan)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    TitleText(
                        text = getPlanDisplayName(currentPlan) ?: currentPlan,
                        color = AppColorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BodyText(
                        text = getPlanDescription(currentPlan) ?: "",
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                // Plan badge
                Text(
                    text = currentPlan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (currentPlan) {
                        PlanType.GUEST.name -> AppColorScheme.error
                        PlanType.FREE_USER.name -> AppColorScheme.primary
                        PlanType.PREMIUM_USER.name -> AppColorScheme.secondary
                        else -> AppColorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Show upgrade option for non-premium users
            if (currentPlan != PlanType.PREMIUM_USER.name) {
                Spacer(modifier = Modifier.height(12.dp))
                ActionButton(
                    icon = "⬆️",
                    text = "Upgrade Plan",
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// Helper functions
private fun getPlanDisplayName(planName: String?): String? {
    return when (planName) {
        PlanType.GUEST.name -> "Gość"
        PlanType.FREE_USER.name -> "Użytkownik Darmowy"
        PlanType.PREMIUM_USER.name -> "Użytkownik Premium"
        PlanType.ENTERPRISE_USER.name -> "Użytkownik Enterprise"
        else -> planName
    }
}

private fun getPlanDescription(planName: String?): String? {
    return when (planName) {
        PlanType.GUEST.name -> "Podstawowy dostęp z ograniczonymi funkcjami"
        PlanType.FREE_USER.name -> "Darmowy plan z podstawowymi funkcjami"
        PlanType.PREMIUM_USER.name -> "Premium plan ze wszystkimi funkcjami"
        PlanType.ENTERPRISE_USER.name -> "Enterprise plan z nieograniczonym dostępem"
        else -> "Plan użytkownika"
    }
}
