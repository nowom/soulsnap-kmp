package pl.soulsnaps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
import pl.soulsnaps.access.manager.AppStartupManager
import org.koin.compose.koinInject

@Composable
internal fun SoulSnapNavHost(
    appState: SoulSnapAppState,
    modifier: Modifier = Modifier,
    startDestination: Any = OnboardingGraph
) {
    val navController = appState.navController
    val startupManager: AppStartupManager = koinInject()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Onboarding Graph
        onboardingGraph(navController)
        
        // Authentication Graph
        authenticationGraph(navController, startupManager)
        
        // Home Graph (Main App)
        homeGraph(navController)
    }
}

fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation<OnboardingGraph>(startDestination = OnboardingRoute) {
        onboardingScreen(
            onComplete = {
                // Navigate to home graph after onboarding completion
                navController.navigate(HomeGraph) {
                    popUpTo(OnboardingGraph) { inclusive = true }
                }
            },
            onLogin = {
                navController.navigate(AuthenticationGraph) {
                    popUpTo(OnboardingGraph) { inclusive = true }
                }
            },
            onRegister = {
                navController.navigate(AuthenticationGraph) {
                    popUpTo(OnboardingGraph) { inclusive = true }
                }
            }
        )
    }
}

fun NavGraphBuilder.authenticationGraph(navController: NavController, startupManager: AppStartupManager) {
    navigation<AuthenticationGraph>(startDestination = LoginRoute) {
        loginScreen(
            onLoginSuccess = {
                println("DEBUG: SoulSnapNavHost - login success, updating startup manager")
                // Mark onboarding as completed and go to dashboard
                startupManager.completeOnboarding()
                println("DEBUG: SoulSnapNavHost - after completeOnboarding, navigating to HomeGraph")
                navController.navigate(HomeGraph) {
                    popUpTo(AuthenticationGraph) { inclusive = true }
                }
                println("DEBUG: SoulSnapNavHost - navigation to HomeGraph completed")
            },
            onBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigateToRegistration() },
            onContinueAsGuest = {
                println("DEBUG: SoulSnapNavHost - continue as guest, updating startup manager")
                // Mark onboarding as completed and go to dashboard for guest
                startupManager.completeOnboarding()
                navController.navigate(HomeGraph) {
                    popUpTo(AuthenticationGraph) { inclusive = true }
                }
            }
        )
        registrationScreen(
            onRegistrationSuccess = {
                println("DEBUG: SoulSnapNavHost - registration success, updating startup manager")
                // Mark onboarding as completed and go to dashboard
                startupManager.completeOnboarding()
                navController.navigate(HomeGraph) {
                    popUpTo(AuthenticationGraph) { inclusive = true }
                }
            }
        )
    }
}

fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation<HomeGraph>(startDestination = DashboardRoute) {
        dashboardScreen(
            onAddNewSnap = { navController.navigateToCaptureMoment() },
            onNavigateToSoulSnaps = { navController.navigateToMemoryHub() },
            onNavigateToAffirmations = { navController.navigateToAffirmations() },
            onNavigateToExercises = { navController.navigateToExercises() },
            onNavigateToVirtualMirror = { navController.navigateToVirtualMirror() },
            onNavigateToAnalytics = {
                println("DEBUG: Navigate to analytics")
            },
            onUpgradePlan = {
                navController.navigateToUpgrade()
            }
        )
        // Add other destinations for the home graph here
        virtualMirrorScreen(onBack = { navController.popBackStack() })
        captureMomentScreen()
        affirmationsScreen()
        memoryHubTab(
            onMemoryClick = { memoryId -> navController.navigateToMemoryDetails(memoryId) }
        )
        memoryDetailsScreen(
            onBack = { navController.popBackStack() },
            onEdit = { navController.popBackStack() },
            onDelete = { navController.popBackStack() },
            onShare = { /* TODO */ }
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
            onNavigateToOnboarding = {
                navController.navigate(OnboardingGraph) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToUpgrade = {
                navController.navigateToUpgrade()
            }
        )
        upgradeScreen(
            onBack = { navController.popBackStack() },
            onUpgradeToPlan = { planName ->
                println("DEBUG: Upgrade to plan: $planName")
                navController.popBackStack()
            },
            onDismiss = { navController.popBackStack() },
            currentPlan = "FREE_USER",
            recommendations = emptyList()
        )
    }
}