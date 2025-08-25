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
import pl.soulsnaps.features.dashboard.DashboardRoute
import pl.soulsnaps.features.exersises.exercisesScreen
import pl.soulsnaps.features.exersises.navigateToExercises
import pl.soulsnaps.features.exersises.plutchikwheel.modernEmotionWheelScreen
import pl.soulsnaps.features.exersises.plutchikwheel.navigateToModernEmotionWheel
import pl.soulsnaps.features.exersises.plutchikwheel.ModernEmotionWheelRoute
import pl.soulsnaps.features.memoryhub.memoryHubTab
import pl.soulsnaps.features.memoryhub.navigateToMemoryHub
import pl.soulsnaps.features.memoryhub.MemoryHubRoute
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
import pl.soulsnaps.features.memoryhub.details.memoryDetailsScreen
import pl.soulsnaps.features.memoryhub.details.navigateToMemoryDetails
import pl.soulsnaps.features.upgrade.upgradeScreen
import pl.soulsnaps.features.upgrade.navigateToUpgrade
import pl.soulsnaps.features.settings.SettingsRoute
import pl.soulsnaps.features.settings.SettingsScreen
import pl.soulsnaps.features.settings.navigateToSettings
import pl.soulsnaps.features.settings.settingsScreen

@Composable
internal fun SoulSnapNavHost(
    appState: SoulSnapAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    
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
        startDestination = DashboardRoute, // Start with dashboard, onboarding will be handled by AppStartupManager
        modifier = modifier,
    ) {
        onboardingScreen(
            onComplete = { 
                navController.navigate(DashboardRoute) {
                    popUpTo(OnboardingRoute) { inclusive = true }
                }
            },
            onLogin = { 
                navController.navigateToLogin()
            },
            onRegister = { 
                navController.navigateToRegistration()
            }
        )
        dashboardScreen(
            onAddNewSnap = { navController.navigateToCaptureMoment() },
            onNavigateToSoulSnaps = { navController.navigateToMemoryHub() },
            onNavigateToAffirmations = { navController.navigateToAffirmations() },
            onNavigateToExercises = { navController.navigateToExercises() },
            onNavigateToVirtualMirror = { navController.navigateToVirtualMirror() },
            onNavigateToAnalytics = { 
                // TODO: Navigate to analytics
                println("DEBUG: Navigate to analytics")
            },
            onUpgradePlan = { 
                navController.navigateToUpgrade()
            }
        )
        virtualMirrorScreen(
            onBack = { navController.popBackStack() }
        )
        loginScreen(
            onLoginSuccess = { 
                println("DEBUG: onLoginSuccess called")
                navController.navigate(DashboardRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigateToRegistration() },
            onContinueAsGuest = { 
                println("DEBUG: onContinueAsGuest called")
                navController.navigate(DashboardRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            }
        )
        registrationScreen(
            onRegistrationSuccess = {}
        )
        captureMomentScreen()
        affirmationsScreen()
        memoryHubTab(
            onMemoryClick = { memoryId ->
                navController.navigateToMemoryDetails(memoryId)
            }
        )
        memoryDetailsScreen(
            onBack = { navController.popBackStack() },
            onEdit = { 
                // TODO: Navigate to edit screen
                navController.popBackStack()
            },
            onDelete = { 
                // TODO: Show delete confirmation
                navController.popBackStack()
            },
            onShare = { 
                // TODO: Implement share functionality
            }
        )
        exercisesScreen(
            onOpenBreathing = { navController.navigateToBreathingSession() },
            onOpenGratitude = { navController.navigateToGratitude() },
            onOpenEmotionWheel = { navController.navigateToModernEmotionWheel() }
        )
        breathingSessionScreen(onDone = { navController.popBackStack() })
        gratitudeScreen(onDone = { navController.popBackStack() })
        modernEmotionWheelScreen(onDone = { navController.popBackStack() })

        settingsScreen(
            {
                navController.navigate(OnboardingRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
        
        // Upgrade screen
        upgradeScreen(
            onBack = { navController.popBackStack() },
            onUpgradeToPlan = { planName ->
                // TODO: Handle plan upgrade
                println("DEBUG: Upgrade to plan: $planName")
                navController.popBackStack()
            },
            onDismiss = { navController.popBackStack() },
            currentPlan = "FREE_USER", // TODO: Get from UserPlanManager
            recommendations = emptyList() // TODO: Get from UpgradeRecommendationEngine
        )
    }
}
