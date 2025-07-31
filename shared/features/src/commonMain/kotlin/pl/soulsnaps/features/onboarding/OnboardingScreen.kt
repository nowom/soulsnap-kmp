package pl.soulsnaps.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.designsystem.AppColorScheme

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        FullScreenCircularProgress()
    } else {
        OnboardingContent(
            state = state,
            onNext = { viewModel.handleIntent(OnboardingIntent.NextStep) },
            onPrevious = { viewModel.handleIntent(OnboardingIntent.PreviousStep) },
            onSkipVoice = { viewModel.handleIntent(OnboardingIntent.SkipVoiceSetup) },
            onRecordVoice = { audioPath -> viewModel.handleIntent(OnboardingIntent.RecordVoice(audioPath)) },
            onSelectGoal = { goal -> viewModel.handleIntent(OnboardingIntent.SelectGoal(goal)) },
            onGrantPermission = { permission -> viewModel.handleIntent(OnboardingIntent.GrantPermission(permission)) },
            onGetStarted = {
                viewModel.handleIntent(OnboardingIntent.GetStarted)
                onComplete()
            },
            canGoNext = viewModel.canGoNext(),
            canGoPrevious = viewModel.canGoPrevious(),
            progress = viewModel.getProgress()
        )
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkipVoice: () -> Unit,
    onRecordVoice: (String) -> Unit,
    onSelectGoal: (UserGoal) -> Unit,
    onGrantPermission: (Permission) -> Unit,
    onGetStarted: () -> Unit,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            color = AppColorScheme.primary,
            trackColor = AppColorScheme.surfaceVariant
        )

        // Content based on current step
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (state.currentStep) {
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.VOICE_SETUP -> VoiceSetupStep(
                    onRecord = onRecordVoice,
                    onSkip = onSkipVoice
                )
                OnboardingStep.GOALS -> GoalsStep(
                    selectedGoal = state.selectedGoal,
                    onSelectGoal = onSelectGoal
                )
                OnboardingStep.PERMISSIONS -> PermissionsStep(
                    grantedPermissions = state.permissionsGranted,
                    onGrantPermission = onGrantPermission
                )
                OnboardingStep.GET_STARTED -> GetStartedStep(
                    selectedGoal = state.selectedGoal,
                    onGetStarted = onGetStarted
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (canGoPrevious) {
                SecondaryButton(
                    text = "Wstecz",
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.width(16.dp))

            when (state.currentStep) {
                OnboardingStep.GET_STARTED -> {
                    PrimaryButton(
                        text = "Rozpocznij",
                        onClick = onGetStarted
                    )
                }
                else -> {
                    PrimaryButton(
                        text = "Dalej",
                        enabled = canGoNext,
                        onClick = onNext,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Witaj w SoulSnaps",
            color = AppColorScheme.onBackground
        )
        Text(
            text = "🧘",
            style = MaterialTheme.typography.displayMedium
        )
        BodyText(
            text = "Twoje osobiste narzędzie do dbania o emocjonalny dobrostan",
            color = AppColorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Co oferuje SoulSnaps?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onPrimaryContainer
                )
                FeatureItem("📸", "Dziennik emocjonalny z fotografiami")
                FeatureItem("🎧", "Spersonalizowane afirmacje głosowe")
                FeatureItem("🧠", "Ćwiczenia mindfulness i relaksacji")
                FeatureItem("🪞", "Wirtualne lustro z analizą emocji")
            }
        }
    }
}

@Composable
private fun FeatureItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VoiceSetupStep(
    onRecord: (String) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Nagranie głosu",
            color = AppColorScheme.onBackground
        )
        Text(
            text = "🎤",
            style = MaterialTheme.typography.displayMedium
        )
        BodyText(
            text = "Nagraj swój głos, aby otrzymywać spersonalizowane afirmacje w Twoim głosie",
            color = AppColorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ActionButton(
            icon = "🎤",
            text = "Nagraj głos",
            onClick = { onRecord("fake_audio_path.mp3") },
            modifier = Modifier.fillMaxWidth()
        )
        SecondaryButton(
            text = "Pomiń",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GoalsStep(
    selectedGoal: UserGoal?,
    onSelectGoal: (UserGoal) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Wybierz swój cel",
            color = AppColorScheme.onBackground
        )
        BodyText(
            text = "Co jest dla Ciebie najważniejsze?",
            color = AppColorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserGoal.values().forEach { goal ->
                GoalCard(
                    goal = goal,
                    isSelected = selectedGoal == goal,
                    onClick = { onSelectGoal(goal) }
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: UserGoal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColorScheme.primaryContainer else AppColorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.emoji,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) AppColorScheme.onPrimaryContainer else AppColorScheme.onSurface
                )
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) AppColorScheme.onPrimaryContainer.copy(alpha = 0.8f) else AppColorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionsStep(
    grantedPermissions: Set<Permission>,
    onGrantPermission: (Permission) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Uprawnienia",
            color = AppColorScheme.onBackground
        )
        Text(
            text = "🔐",
            style = MaterialTheme.typography.displayMedium
        )
        BodyText(
            text = "SoulSnaps potrzebuje kilku uprawnień, aby działać w pełni",
            color = AppColorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Permission.values().forEach { permission ->
                PermissionCard(
                    permission = permission,
                    isGranted = grantedPermissions.contains(permission),
                    onGrant = { onGrantPermission(permission) }
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permission: Permission,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) AppColorScheme.primaryContainer else AppColorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) AppColorScheme.onPrimaryContainer else AppColorScheme.onSurface
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) AppColorScheme.onPrimaryContainer.copy(alpha = 0.8f) else AppColorScheme.onSurfaceVariant
                )
            }
            if (!isGranted) {
                ActionButton(
                    icon = "✅",
                    text = "Zezwól",
                    onClick = onGrant
                )
            } else {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GetStartedStep(
    selectedGoal: UserGoal?,
    onGetStarted: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(
            text = "Wszystko gotowe!",
            color = AppColorScheme.onBackground
        )
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayMedium
        )
        BodyText(
            text = "Twój cel: ${selectedGoal?.title ?: "Nie wybrano"}",
            color = AppColorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rozpocznij swoją podróż",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onPrimaryContainer
                )
                Text(
                    text = "SoulSnaps jest gotowy, aby pomóc Ci w dbaniu o emocjonalny dobrostan",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = AppColorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
} 