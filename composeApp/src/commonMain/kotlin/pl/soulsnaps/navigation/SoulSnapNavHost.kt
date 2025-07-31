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
import pl.soulsnaps.features.capturemoment.captureMomentScreen
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.dashboard.dashboardScreen
import pl.soulsnaps.features.exersises.exercisesScreen
import pl.soulsnaps.features.memoryhub.memoryHubTab
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.onboarding.onboardingScreen
import pl.soulsnaps.features.dashboard.navigateToDashboard
import pl.soulsnaps.features.memoryhub.MemoryHubRoute
import pl.soulsnaps.features.memoryhub.navigateToMemoryHub
import pl.soulsnaps.features.affirmation.navigateToAffirmations
import pl.soulsnaps.features.capturemoment.navigateToCaptureMoment
import pl.soulsnaps.features.exersises.navigateToExercises
import pl.soulsnaps.features.virtualmirror.virtualMirrorScreen
import pl.soulsnaps.features.virtualmirror.navigateToVirtualMirror
import pl.soulsnaps.features.coach.breathingSessionScreen
import pl.soulsnaps.features.coach.gratitudeScreen
import pl.soulsnaps.features.exersises.ExercisesScreen

@Composable
internal fun SoulSnapNavHost(
    appState: SoulSnapAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    var quizCompletedToday by remember { mutableStateOf(false) }
    var streak by remember { mutableStateOf(0) }
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute,
        modifier = modifier,
    ) {
        onboardingScreen(
            onComplete = { navController.navigateToDashboard() }
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
        captureMomentScreen()
        affirmationsScreen()
        memoryHubTab({

        })
        exercisesScreen(
            onOpenBreathing = { navController.navigate("breathingSession") },
            onOpenGratitude = { navController.navigate("gratitude") })
        breathingSessionScreen(onDone = { navController.popBackStack() })
        gratitudeScreen(onDone = { navController.popBackStack() })
    }
}
