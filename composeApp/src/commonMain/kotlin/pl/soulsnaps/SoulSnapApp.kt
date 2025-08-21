package pl.soulsnaps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import pl.soulsnaps.designsystem.DsIcons
import pl.soulsnaps.designsystem.SoulSnapsTheme
import pl.soulsnaps.features.affirmation.AffirmationsScreen
import pl.soulsnaps.navigation.MainBottomMenu
import pl.soulsnaps.navigation.SoulSnapAppState
import pl.soulsnaps.navigation.SoulSnapNavHost
import pl.soulsnaps.navigation.rememberAppState
import pl.soulsnaps.navigation.bottomNavItems
import pl.soulsnaps.features.onboarding.OnboardingCompletionTracker
import pl.soulsnaps.features.onboarding.createOnboardingDataStore
import pl.soulsnaps.features.onboarding.OnboardingRoute
import pl.soulsnaps.features.dashboard.DashboardRoute

@Composable
fun SoulSnapsApp() {
    SoulSnapsTheme {
        println("DEBUG: SoulSnapsApp - initializing UserPreferencesStorageFactory")

        val appState = rememberAppState()

        // Use the OnboardingCompletionTracker composable to handle completion status
        OnboardingCompletionTracker(
            onComplete = {
                // Navigate to dashboard if onboarding is completed
                appState.navController.navigate(DashboardRoute) {
                    popUpTo(OnboardingRoute) { inclusive = true }
                }
            }
        )

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
