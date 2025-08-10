package pl.soulsnaps.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.onboarding.onboardingScreen
import pl.soulsnaps.features.auth.LoginRoute
import pl.soulsnaps.features.auth.RegistrationRoute
import pl.soulsnaps.features.auth.loginScreen
import pl.soulsnaps.features.auth.registrationScreen
import pl.soulsnaps.features.dashboard.dashboardScreen
import pl.soulsnaps.features.dashboard.navigateToDashboard
import pl.soulsnaps.features.affirmation.affirmationsScreen
import pl.soulsnaps.features.affirmation.navigateToAffirmations
import pl.soulsnaps.features.capturemoment.captureMomentScreen
import pl.soulsnaps.features.capturemoment.navigateToCaptureMoment
import pl.soulsnaps.features.virtualmirror.virtualMirrorScreen
import pl.soulsnaps.features.virtualmirror.navigateToVirtualMirror
import pl.soulsnaps.features.memoryhub.memoryHubTab
import pl.soulsnaps.features.memoryhub.navigateToMemoryHub
import pl.soulsnaps.features.exersises.exercisesScreen
import pl.soulsnaps.features.exersises.navigateToExercises
import pl.soulsnaps.features.coach.breathingSessionScreen
import pl.soulsnaps.features.coach.gratitudeScreen
import pl.soulsnaps.features.exersises.plutchikwheel.ModernEmotionWheelScreen

// Onboarding Graph
fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    onComplete: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    onboardingScreen(
        onComplete = onComplete,
        onLogin = onNavigateToAuth,
        onRegister = onNavigateToAuth
    )
}

// Auth Graph
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    onBackToOnboarding: () -> Unit
) {
    loginScreen(
        onLoginSuccess = onAuthSuccess
    )
    registrationScreen(
        onRegistrationSuccess = onAuthSuccess
    )
}

// Main App Graph
fun NavGraphBuilder.mainAppGraph(
    navController: NavHostController
) {
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
    
    captureMomentScreen()
    
    affirmationsScreen()
    
    memoryHubTab({})
    
    exercisesScreen(
        onOpenBreathing = { navController.navigate("breathingSession") },
        onOpenGratitude = { navController.navigate("gratitude") },
        onOpenEmotionWheel = { navController.navigate("modernEmotionWheel") }
    )
    
    breathingSessionScreen(
        onDone = { navController.popBackStack() }
    )
    
    gratitudeScreen(
        onDone = { navController.popBackStack() }
    )
    
    composable("modernEmotionWheel") {
        ModernEmotionWheelScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
} 