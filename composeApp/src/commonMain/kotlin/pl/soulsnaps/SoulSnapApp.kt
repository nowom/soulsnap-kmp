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
import pl.soulsnaps.features.startup.SplashViewModel
import pl.soulsnaps.domain.model.StartupState
import pl.soulsnaps.navigation.LocalNavController
import pl.soulsnaps.navigation.MainBottomMenu
import pl.soulsnaps.navigation.SoulSnapNavHost
import pl.soulsnaps.navigation.rememberAppState
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.navigation.OnboardingGraph
import pl.soulsnaps.navigation.AuthenticationGraph
import pl.soulsnaps.navigation.HomeGraph

@Composable
fun SoulSnapsApp() {
    SoulSnapsTheme {
        println("DEBUG: SoulSnapsApp - initializing app")

        val splashViewModel: SplashViewModel = koinInject()
        val startupState by splashViewModel.state.collectAsStateWithLifecycle(initialValue = pl.soulsnaps.domain.model.StartupUiState())
        val appState = rememberAppState()
        val navController = appState.navController
        // Provide LocalNavController for all child composables
        CompositionLocalProvider(LocalNavController provides navController) {

            // SplashViewModel initializes automatically in init block

            // Main app content based on startup state
            println("DEBUG: SoulSnapsApp - current startupState: ${startupState.state}")
            when (startupState.state) {
                StartupState.CHECKING -> {
                    // Loading state - pokaż loading
                    println("DEBUG: SoulSnapsApp - showing loading state")
                    LoadingScreen()
                }

                StartupState.READY_FOR_ONBOARDING, StartupState.ONBOARDING_ACTIVE -> {
                    // Pokaż onboarding przez nawigację
                    println("DEBUG: SoulSnapsApp - showing onboarding via navigation, state: ${startupState.state}")
                    SoulSnapNavHost(
                        appState = appState,
                        startDestination = OnboardingGraph,
                    )
                }

                StartupState.READY_FOR_AUTH -> {
                    // Pokaż ekran logowania
                    println("DEBUG: SoulSnapsApp - showing authentication via navigation, state: ${startupState.state}")
                    SoulSnapNavHost(
                        appState = appState,
                        startDestination = AuthenticationGraph,
                    )
                }

                StartupState.READY_FOR_DASHBOARD -> {
                    // Pokaż główną aplikację
                    println("DEBUG: SoulSnapsApp - showing main app with dashboard, state: ${startupState.state}")
                    MainAppContent(
                        appState = appState,
                        startDestination = HomeGraph
                    )
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
    startDestination: Any = HomeGraph
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
                startDestination = startDestination,
            )
        }
    }
}