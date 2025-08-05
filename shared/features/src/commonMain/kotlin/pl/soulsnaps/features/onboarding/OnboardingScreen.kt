package pl.soulsnaps.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.components.ButtonGroupVertical
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.components.SubtitleText
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.CaptionText
import pl.soulsnaps.components.TitleText
import pl.soulsnaps.designsystem.AppColorScheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

// Soft violet accent color as specified
private val softViolet = Color(0xFF8B5CF6)

// Subtle gradient background
private val gradientColors = listOf(
    Color(0xFFF8F7FF), // Very light lavender
    Color(0xFFF0F0FF), // Light purple tint
    Color(0xFFF5F5FF)  // Soft blue tint
)

// Tour screens data
private data class TourScreen(
    val icon: String,
    val title: String,
    val description: String
)

private val tourScreens = listOf(
    TourScreen(
        icon = "📸",
        title = "Capture Emotional Moments",
        description = "Take photos to document your emotional journey and build a visual diary of your feelings"
    ),
    TourScreen(
        icon = "🧠",
        title = "Emotion Wheel",
        description = "Explore and understand your emotions with our interactive Plutchik's Wheel of Emotions"
    ),
    TourScreen(
        icon = "💭",
        title = "AI Coach",
        description = "Get personalized guidance, affirmations, and emotional support from your AI companion"
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        FullScreenCircularProgress()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = gradientColors)
                )
        ) {
            OnboardingContent(
                state = state,
                onNext = { viewModel.handleIntent(OnboardingIntent.NextStep) },
                onPrevious = { viewModel.handleIntent(OnboardingIntent.PreviousStep) },
                onSkipTour = { viewModel.handleIntent(OnboardingIntent.SkipTour) },
                onSelectFocus = { focus -> viewModel.handleIntent(OnboardingIntent.SelectFocus(focus)) },
                onAuthenticate = { authType -> viewModel.handleIntent(OnboardingIntent.Authenticate(authType)) },
                onUpdateEmail = { email -> viewModel.handleIntent(OnboardingIntent.UpdateEmail(email)) },
                onUpdatePassword = { password -> viewModel.handleIntent(OnboardingIntent.UpdatePassword(password)) },
                onGetStarted = {
                    viewModel.handleIntent(OnboardingIntent.GetStarted)
                    onComplete()
                },
                onLogin = onLogin,
                onRegister = onRegister,
                canGoNext = viewModel.canGoNext(),
                canGoPrevious = viewModel.canGoPrevious(),
                progress = viewModel.getProgress()
            )
        }
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkipTour: () -> Unit,
    onSelectFocus: (UserFocus) -> Unit,
    onAuthenticate: (AuthType) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onGetStarted: () -> Unit,
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Main content area (takes available space)
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when (state.currentStep) {
                OnboardingStep.WELCOME -> WelcomeStep(
                    onNext = onNext,
                    onAuthenticate = onAuthenticate,
                    onLogin = onLogin,
                    onRegister = onRegister
                )
                OnboardingStep.APP_TOUR -> AppTourStep(
                    onSkip = onSkipTour
                )
                OnboardingStep.PERSONALIZATION -> PersonalizationStep(
                    selectedFocus = state.selectedFocus,
                    onSelectFocus = onSelectFocus
                )
                OnboardingStep.AUTH -> AuthStep(
                    onAuthenticate = onAuthenticate,
                    onLogin = onLogin,
                    onRegister = onRegister
                )
                OnboardingStep.GET_STARTED -> GetStartedStep(
                    selectedFocus = state.selectedFocus,
                )
            }
        }

        // Navigation buttons (fixed at bottom)
        NavigationButtons(
            currentStep = state.currentStep,
            canGoNext = canGoNext,
            canGoPrevious = canGoPrevious,
            onNext = onNext,
            onPrevious = onPrevious,
            onGetStarted = onGetStarted
        )
    }
}

@Composable
private fun WelcomeStep(
    onNext: () -> Unit,
    onAuthenticate: (AuthType) -> Unit,
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Logo and tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SoulUnity Logo (icon + text)
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(containerColor = softViolet),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧘",
                        fontSize = 32.sp
                    )
                }
            }

            HeadingText(
                text = "SoulUnity",
                color = AppColorScheme.onBackground
            )

            SubtitleText(
                text = "Your Emotional Companion",
                color = AppColorScheme.onSurfaceVariant
            )
        }

        // Welcome message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BodyText(
                text = "Welcome to SoulUnity! Let's take a quick tour to see how we can help you on your emotional wellness journey.",
                color = AppColorScheme.onSurfaceVariant
            )
        }

    }
}



@Composable
private fun PersonalizationStep(
    selectedFocus: UserFocus?,
    onSelectFocus: (UserFocus) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Question: "What's your current focus?" (large header text, centered)
        HeadingText(
            text = "What's your current focus?",
            color = AppColorScheme.onBackground
        )

        // Choice Cards (3 large selectable buttons)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            UserFocus.values().forEach { focus ->
                FocusCard(
                    focus = focus,
                    isSelected = selectedFocus == focus,
                    onClick = { onSelectFocus(focus) }
                )
            }
        }

        // Top-Right Link: "Skip" (small, grey text link)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { /* Skip personalization */ },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                CaptionText(
                    text = "Skip",
                    color = AppColorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FocusCard(
    focus: UserFocus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) softViolet.copy(alpha = 0.1f) else AppColorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = focus.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                TitleText(
                    text = focus.title,
                    color = if (isSelected) softViolet else AppColorScheme.onSurface
                )
                BodyText(
                    text = focus.description,
                    color = if (isSelected) softViolet.copy(alpha = 0.8f) else AppColorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTourStep(
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tourScreens.size })

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Skip button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                CaptionText(
                    text = "Skip",
                    color = AppColorScheme.onSurfaceVariant
                )
            }
        }

        // Progress indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(tourScreens.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == pagerState.currentPage) softViolet else AppColorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        // Swipeable tour content (more compact)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            TourScreenContent(tourScreens[page])
        }
    }
}

@Composable
private fun TourScreenContent(tourScreen: TourScreen) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero illustration (smaller)
        Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(containerColor = softViolet.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tourScreen.icon,
                    fontSize = 32.sp
                )
            }
        }

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleText(
                text = tourScreen.title,
                color = AppColorScheme.onBackground
            )

            BodyText(
                text = tourScreen.description,
                color = AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuthStep(
    onAuthenticate: (AuthType) -> Unit,
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeadingText(
                text = "Ready to Get Started?",
                color = AppColorScheme.onBackground
            )

            SubtitleText(
                text = "Choose how you'd like to access SoulUnity",
                color = AppColorScheme.onSurfaceVariant
            )
        }

        // Action buttons using ButtonGroupVertical
        ButtonGroupVertical(
            primaryButtonText = "Sign Up / Register",
            secondaryButtonText = "Log In",
            onPrimaryClick = { onRegister() },
            onSecondaryClick = { onLogin() },
            spacing = 16
        )

        // Continue as Guest option
        TextButton(
            onClick = { onAuthenticate(AuthType.ANONYMOUS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            CaptionText(
                text = "Continue as Guest",
                color = AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GetStartedStep(
    selectedFocus: UserFocus?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HeadingText(
            text = "You're ready to begin!",
            color = AppColorScheme.onBackground
        )

        SubtitleText(
            text = "Let's build emotional awareness together",
            color = AppColorScheme.onSurfaceVariant
        )

        // Celebration emoji
        Text(
            text = "🎉",
            fontSize = 64.sp
        )
    }
}

@Composable
private fun NavigationButtons(
    currentStep: OnboardingStep,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (currentStep) {
            OnboardingStep.WELCOME -> {
                PrimaryButton(
                    text = "Start Tour",
                    enabled = canGoNext,
                    onClick = onNext,
                )
            }
            OnboardingStep.GET_STARTED -> {
                PrimaryButton(
                    text = "Get Started",
                    onClick = onGetStarted
                )
            }
            else -> {
                PrimaryButton(
                    text = "Next",
                    enabled = canGoNext,
                    onClick = onNext,
                )
            }
        }

        if (canGoPrevious) {
            SecondaryButton(
                text = "Back",
                onClick = onPrevious,
            )
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }
    }
}