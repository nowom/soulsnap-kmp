package pl.soulsnaps.features.startup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.components.BodyText
import pl.soulsnaps.components.HeadingText
import pl.soulsnaps.designsystem.AppColorScheme

@Composable
fun AppStartupScreen(
    onStartupComplete: (StartupState) -> Unit,
    startupManager: AppStartupManager = remember { AppStartupManager() }
) {
    val startupState by startupManager.startupState.collectAsState(initial = StartupState.Loading)
    
    // Inicjalizuj aplikację przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        println("DEBUG: AppStartupScreen - starting app initialization")
        startupManager.initializeApp()
    }
    
    // Obserwuj zmiany stanu i przekaż do parent
    LaunchedEffect(startupState) {
        if (startupState !is StartupState.Loading) {
            println("DEBUG: AppStartupScreen - startup state changed to: $startupState")
            onStartupComplete(startupState)
        }
    }
    
    // UI dla loading state
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo lub ikona aplikacji
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder dla logo - możesz dodać Image z logo
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nazwa aplikacji
            HeadingText(
                text = "SoulSnaps",
                color = AppColorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Podtytuł
            BodyText(
                text = "Zadbaj o swój emocjonalny dobrostan",
                color = AppColorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = AppColorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading text
            Text(
                text = when (startupState) {
                    is StartupState.Loading -> "Ładowanie aplikacji..."
                    is StartupState.ShowOnboarding -> "Przygotowywanie onboardingu..."
                    is StartupState.ShowDashboard -> "Przygotowywanie dashboardu..."
                    is StartupState.Error -> "Wystąpił błąd..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            // Error message
            if (startupState is StartupState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (startupState as StartupState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColorScheme.error
                )
            }
        }
    }
}

