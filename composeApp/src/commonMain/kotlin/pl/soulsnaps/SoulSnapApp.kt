package pl.soulsnaps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.soulsnaps.designsystem.DsIcons
import pl.soulsnaps.designsystem.SoulSnapsTheme
import pl.soulsnaps.features.affirmation.AffirmationsScreen
import pl.soulsnaps.components.ActionButton
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.navigation.MainBottomMenu
import pl.soulsnaps.navigation.SoulSnapAppState
import pl.soulsnaps.navigation.SoulSnapNavHost
import pl.soulsnaps.navigation.rememberAppState
import pl.soulsnaps.navigation.bottomNavItems
import pl.soulsnaps.features.onboarding.OnboardingScreen
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.navigation.LocalNavController

@Composable
fun SoulSnapsApp() {
    SoulSnapsTheme {
        println("DEBUG: SoulSnapsApp - initializing app")
        
        var startupState by remember { mutableStateOf<pl.soulsnaps.features.startup.StartupState?>(null) }
        val appState = rememberAppState()
        
        // App startup screen - zapobiega mignięciu onboardingu
        if (startupState == null) {
            pl.soulsnaps.features.startup.AppStartupScreen(
                onStartupComplete = { state ->
                    println("DEBUG: SoulSnapsApp - startup complete: $state")
                    startupState = state
                }
            )
        } else {
            // Main app content
            when (startupState) {
                is pl.soulsnaps.features.startup.StartupState.ShowOnboarding -> {
                    // Pokaż onboarding bez mignięcia
                    println("DEBUG: SoulSnapsApp - showing onboarding directly")
                    pl.soulsnaps.features.onboarding.OnboardingScreen(
                        onComplete = {
                            println("DEBUG: SoulSnapsApp - onboarding completed, switching to dashboard")
                            startupState = pl.soulsnaps.features.startup.StartupState.ShowDashboard
                        }
                    )
                }
                
                is pl.soulsnaps.features.startup.StartupState.ShowDashboard -> {
                    // Pokaż główną aplikację
                    println("DEBUG: SoulSnapsApp - showing main app with dashboard")
                    MainAppContent(appState = appState)
                }
                
                is pl.soulsnaps.features.startup.StartupState.Error -> {
                    // Pokaż ekran błędu
                    println("DEBUG: SoulSnapsApp - showing error: ${(startupState as pl.soulsnaps.features.startup.StartupState.Error).message}")
                    ErrorScreen(
                        message = (startupState as pl.soulsnaps.features.startup.StartupState.Error).message,
                        onRetry = {
                            startupState = null // Reset startup state
                        }
                    )
                }
                
                else -> {
                    // Loading state - nie powinno się zdarzyć
                    println("DEBUG: SoulSnapsApp - unexpected state: $startupState")
                }
            }
        }
    }
}

@Composable
private fun MainAppContent(appState: pl.soulsnaps.navigation.SoulSnapAppState) {
    println("DEBUG: MainAppContent - rendering main app with dashboard")
    // Provide LocalNavController for all child composables
    CompositionLocalProvider(LocalNavController provides appState.navController) {
        // Main app layout with bottom navigation
        Scaffold(
        floatingActionButton = {
            if (appState.shouldShowFab) {
                FloatingActionButton(
                    onClick = appState::navigateToAddSnap,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Dodaj Snap")
                }
            }
        },
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                MainBottomMenu(
                    destinations = appState.topLevelDestinations,
                    currentDestination = appState.currentDestination,
                    onNavigateToDestination = { destination ->
                        appState.navigateToTopLevelDestination(destination)
                    }
                )
            }
        }
    ) { paddingValues ->
        SoulSnapNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues)
        )
    }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingText(
                text = "Wystąpił błąd",
                color = AppColorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BodyText(
                text = message,
                color = AppColorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            pl.soulsnaps.components.ActionButton(
                icon = "🔄",
                text = "Spróbuj ponownie",
                onClick = onRetry
            )
        }
    }
}
