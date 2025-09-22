package pl.soulsnaps.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.AffirmationCard
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.CaptionText
import pl.soulsnaps.components.DashboardCard
import pl.soulsnaps.components.EmotionCard
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.components.LabelText
import pl.soulsnaps.components.TitleText
import pl.soulsnaps.components.AudioPlayerComponent
import pl.soulsnaps.audio.AudioManager
import pl.soulsnaps.utils.formatDate
import pl.soulsnaps.utils.toLocalDateTime

@Composable
fun DashboardScreen(
    onAddNewSnap: () -> Unit = {},
    onNavigateToSoulSnaps: () -> Unit = {},
    onNavigateToAffirmations: () -> Unit = {},
    onNavigateToExercises: () -> Unit = {},
    onNavigateToVirtualMirror: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToDailyQuiz: () -> Unit = {},
    onUpgradePlan: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    userPlanManager: UserPlanManager = koinInject(),
    planRegistry: PlanRegistryReader = koinInject(),
    audioManager: AudioManager = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    println("DEBUG: DashboardScreen composable called")

    if (state.isLoading) {
        FullScreenCircularProgress()
    } else {
        // Use scope-aware dashboard
        ScopeAwareDashboard(
            state = state,
            onAddNewSnap = onAddNewSnap,
            onNavigateToSoulSnaps = onNavigateToSoulSnaps,
            onNavigateToAffirmations = onNavigateToAffirmations,
            onNavigateToExercises = onNavigateToExercises,
            onNavigateToVirtualMirror = onNavigateToVirtualMirror,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToDailyQuiz = onNavigateToDailyQuiz,
            onUpgradePlan = onUpgradePlan,
            onPlayAffirmation = { viewModel.handleIntent(DashboardIntent.PlayAffirmation) },
            onPauseAffirmation = { viewModel.handleIntent(DashboardIntent.PauseAffirmation) },
            onChangeAffirmation = { viewModel.handleIntent(DashboardIntent.ChangeAffirmation) },
            onFavoriteAffirmation = { viewModel.handleIntent(DashboardIntent.FavoriteAffirmation) },
            onTakeMoodQuiz = { viewModel.handleIntent(DashboardIntent.TakeMoodQuiz) },
            onShowNotifications = { viewModel.handleIntent(DashboardIntent.ShowNotifications) },
            onRefreshDashboard = { viewModel.handleIntent(DashboardIntent.RefreshDashboard) },
            userPlanManager = userPlanManager,
            planRegistry = planRegistry
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    onPlayAffirmation: () -> Unit,
    onPauseAffirmation: () -> Unit,
    onAddNewSnap: () -> Unit,
    onNavigateToSoulSnaps: () -> Unit,
    onNavigateToAffirmations: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToVirtualMirror: () -> Unit
) {
    Scaffold(
        containerColor = AppColorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            HeaderSection()
            
            // Affirmation of the day
            AffirmationSection(
                affirmation = state.affirmationOfTheDay,
                isPlaying = state.isAffirmationPlaying,
                onPlayClick = if (state.isAffirmationPlaying) onPauseAffirmation else onPlayAffirmation
            )
            
            // Emotion of the day
            EmotionSection(
                emotionOfTheDay = state.emotionOfTheDay
            )
            
            // Last SoulSnap
            LastSoulSnapSection(
                lastSnap = state.lastSoulSnap,
                onViewAllClick = onNavigateToSoulSnaps
            )
            
            // Quick Actions
            QuickActionsSection(
                onAddNewSnap = onAddNewSnap,
                onNavigateToAffirmations = onNavigateToAffirmations,
                onNavigateToExercises = onNavigateToExercises,
                onNavigateToVirtualMirror = onNavigateToVirtualMirror
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        HeadingText(
            text = "Witaj w SoulSnaps",
            color = AppColorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            text = "Zadbaj o swój emocjonalny dobrostan",
            color = AppColorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AffirmationSection(
    affirmation: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    DashboardCard(
        title = "Afirmacja dnia",
        subtitle = "Dzisiejsza pozytywna myśl dla Ciebie"
    ) {
        AffirmationCard(
            text = affirmation,
            isPlaying = isPlaying,
            onPlayClick = onPlayClick
        )
    }
}

@Composable
private fun EmotionSection(
    emotionOfTheDay: EmotionOfTheDay
) {
    DashboardCard(
        title = "Emocja dnia",
        subtitle = "Jak się dzisiaj czujesz?"
    ) {
        EmotionCard(
            emotion = emotionOfTheDay.name,
            emoji = emotionOfTheDay.emoji,
            description = emotionOfTheDay.description
        )
    }
}

@Composable
private fun LastSoulSnapSection(
    lastSnap: Memory?,
    onViewAllClick: () -> Unit
) {
    DashboardCard(
        title = "Ostatni SoulSnap",
        subtitle = if (lastSnap != null) "Twoje ostatnie wspomnienie" else "Brak wspomnień"
    ) {
        if (lastSnap != null) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!lastSnap.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = lastSnap.photoUri,
                            contentDescription = "SoulSnap thumbnail",
                            modifier = Modifier
                                .height(64.dp)
                                .weight(1f)
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .height(64.dp)
                                .weight(1f)
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Brak zdjęcia", color = Color.DarkGray)
                        }
                    }
                    Column(modifier = Modifier.weight(2f)) {
                        TitleText(
                            text = lastSnap.title,
                            color = AppColorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BodyText(
                            text = lastSnap.description,
                            color = AppColorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelText(
                        text = formatDate(lastSnap.createdAt.toLocalDateTime()),
                        color = AppColorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    ActionButton(
                        icon = "📸",
                        text = "Zobacz wszystkie",
                        onClick = onViewAllClick
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BodyText(
                    text = "Nie masz jeszcze żadnych SoulSnapów",
                    color = AppColorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    icon = "➕",
                    text = "Dodaj pierwszy",
                    onClick = onViewAllClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddNewSnap: () -> Unit,
    onNavigateToAffirmations: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToVirtualMirror: () -> Unit
) {
    DashboardCard(
        title = "Szybkie akcje",
        subtitle = "Co chcesz zrobić?"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                icon = "📸",
                text = "Dodaj nowy SoulSnap",
                onClick = onAddNewSnap,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = "🎧",
                    text = "Afirmacje",
                    onClick = onNavigateToAffirmations,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = "🧠",
                    text = "Ćwiczenia",
                    onClick = onNavigateToExercises,
                    modifier = Modifier.weight(1f)
                )
            }
            
            ActionButton(
                icon = "🪞",
                text = "Wirtualne Lustro",
                onClick = onNavigateToVirtualMirror,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}