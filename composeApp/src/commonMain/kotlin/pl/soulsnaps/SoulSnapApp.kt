package pl.soulsnaps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.designsystem.SoulSnapsTheme
import pl.soulsnaps.access.manager.AppStartupManager
import pl.soulsnaps.access.manager.StartupState
import pl.soulsnaps.navigation.LocalNavController
import pl.soulsnaps.navigation.MainBottomMenu
import pl.soulsnaps.navigation.SoulSnapNavHost
import pl.soulsnaps.navigation.rememberAppState
import pl.soulsnaps.features.auth.navigateToLogin
import pl.soulsnaps.features.auth.navigateToRegistration
import pl.soulsnaps.features.onboarding.OnboardingRoute

@Composable
fun SoulSnapsApp() {
    SoulSnapsTheme {
        println("DEBUG: SoulSnapsApp - initializing app")
        
        val startupManager: AppStartupManager = koinInject()
        val startupState by startupManager.startupState.collectAsStateWithLifecycle(initialValue = StartupState.CHECKING)
        val appState = rememberAppState()
        val navController = appState.navController
        val coroutineScope = rememberCoroutineScope()
        
        // Provide LocalNavController for all child composables
        CompositionLocalProvider(LocalNavController provides navController) {
        
        // Inicjalizuj AppStartupManager przy starcie
        LaunchedEffect(Unit) {
            println("DEBUG: SoulSnapsApp - initializing AppStartupManager")
            startupManager.initializeApp()
        }
        
        // Main app content based on startup state
        when (startupState) {
            StartupState.CHECKING -> {
                // Loading state - pokaż loading
                println("DEBUG: SoulSnapsApp - showing loading state")
                LoadingScreen()
            }
            
            StartupState.READY_FOR_ONBOARDING, StartupState.ONBOARDING_ACTIVE -> {
                // Pokaż onboarding bezpośrednio
                println("DEBUG: SoulSnapsApp - showing onboarding directly")
                pl.soulsnaps.features.onboarding.OnboardingScreen(
                    onComplete = {
                        println("DEBUG: SoulSnapsApp - onboarding completed, switching to dashboard")
                        coroutineScope.launch {
                            startupManager.resetStartupState()
                        }
                    },
                    onLogin = {
                        println("DEBUG: SoulSnapsApp - login clicked")
                        // W stanie onboarding nie używamy nawigacji - pokazujemy ekran bezpośrednio
                        // TODO: Implement direct screen display or separate NavController for onboarding
                    },
                    onRegister = {
                        println("DEBUG: SoulSnapsApp - register clicked")
                        // W stanie onboarding nie używamy nawigacji - pokazujemy ekran bezpośrednio
                        // TODO: Implement direct screen display or separate NavController for onboarding
                    }
                )
            }
            
            StartupState.READY_FOR_DASHBOARD -> {
                // Pokaż główną aplikację
                println("DEBUG: SoulSnapsApp - showing main app with dashboard")
                MainAppContent(appState = appState)
            }
        }
        }
    }
}

@Composable
private fun LoadingScreen() {
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
                text = "Ładowanie aplikacji...",
                color = AppColorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BodyText(
                text = "Sprawdzanie stanu użytkownika...",
                color = AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MainAppContent(
    appState: pl.soulsnaps.navigation.SoulSnapAppState,
    onOnboardingComplete: (() -> Unit)? = null
) {
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
            modifier = Modifier.padding(paddingValues),
            onOnboardingComplete = onOnboardingComplete
        )
    }
    }
}
