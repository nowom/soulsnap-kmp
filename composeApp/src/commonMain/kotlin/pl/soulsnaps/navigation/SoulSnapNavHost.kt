package pl.soulsnaps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.soulsnaps.features.affirmation.affirmationsScreen
import pl.soulsnaps.features.affirmation.navigateToAffirmations
import pl.soulsnaps.features.capturemoment.captureMomentScreen
import pl.soulsnaps.features.capturemoment.navigateToCaptureMoment
import pl.soulsnaps.features.coach.breathingSessionScreen
import pl.soulsnaps.features.coach.gratitudeScreen
import pl.soulsnaps.features.coach.navigateToBreathingSession
import pl.soulsnaps.features.coach.navigateToGratitude
import pl.soulsnaps.features.dashboard.dashboardScreen
import pl.soulsnaps.features.dashboard.navigateToDashboard
import pl.soulsnaps.features.exersises.exercisesScreen
import pl.soulsnaps.features.exersises.navigateToExercises
import pl.soulsnaps.features.exersises.plutchikwheel.modernEmotionWheelScreen
import pl.soulsnaps.features.exersises.plutchikwheel.navigateToModernEmotionWheel
import pl.soulsnaps.features.exersises.plutchikwheel.ModernEmotionWheelRoute
import pl.soulsnaps.features.memoryhub.memoryHubTab
import pl.soulsnaps.features.memoryhub.navigateToMemoryHub
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.onboarding.createOnboardingDataStore
import pl.soulsnaps.features.onboarding.onboardingScreen
import pl.soulsnaps.features.virtualmirror.navigateToVirtualMirror
import pl.soulsnaps.features.virtualmirror.virtualMirrorScreen
import pl.soulsnaps.features.auth.LoginRoute
import pl.soulsnaps.features.auth.loginScreen
import pl.soulsnaps.features.auth.RegistrationRoute
import pl.soulsnaps.features.auth.navigateToLogin
import pl.soulsnaps.features.auth.navigateToRegistration
import pl.soulsnaps.features.auth.registrationScreen

@Composable
internal fun SoulSnapNavHost(
    appState: SoulSnapAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    var quizCompletedToday by remember { mutableStateOf(false) }
    var streak by remember { mutableStateOf(0) }
    
    // Use the OnboardingCompletionTracker composable to handle completion status
//    OnboardingCompletionTracker(
//        dataStore = createOnboardingDataStore(),
//        onComplete = {
//            // Navigate to dashboard if onboarding is completed
//            navController.navigate("dashboard") {
//                popUpTo(OnboardingRoute) { inclusive = true }
//            }
//        }
//    )
    
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute, // Always start with onboarding, let tracker handle navigation
        modifier = modifier,
    ) {
        onboardingScreen(
            onComplete = { navController.navigateToDashboard() },
            onLogin = { navController.navigateToLogin() },
            onRegister = { navController.navigateToRegistration() }
        )
        dashboardScreen(
            onAddNewSnap = { navController.navigateToCaptureMoment() },
            onNavigateToSoulSnaps = { navController.navigateToMemoryHub() },
            onNavigateToAffirmations = { navController.navigateToAffirmations() },
            onNavigateToExercises = { navController.navigateToExercises() },
            onNavigateToVirtualMirror = { navController.navigateToVirtualMirror() }
        )
        virtualMirrorScreen(
            onBack = { navController.popBackStack() }
        )
        loginScreen(
            onLoginSuccess = { navController.navigateToDashboard() }
        )
        registrationScreen(
            onRegistrationSuccess = {}
        )
        captureMomentScreen()
        affirmationsScreen()
        memoryHubTab({

        })
        exercisesScreen(
            onOpenBreathing = { navController.navigateToBreathingSession() },
            onOpenGratitude = { navController.navigateToGratitude() },
            onOpenEmotionWheel = { navController.navigateToModernEmotionWheel() }
        )
        breathingSessionScreen(onDone = { navController.popBackStack() })
        gratitudeScreen(onDone = { navController.popBackStack() })
        modernEmotionWheelScreen(onDone = { navController.popBackStack() })
    }
}
