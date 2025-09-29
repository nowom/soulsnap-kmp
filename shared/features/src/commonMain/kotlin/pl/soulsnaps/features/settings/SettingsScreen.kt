package pl.soulsnaps.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToUpgrade: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // User Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Informacje o użytkowniku",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Email
                state.userEmail?.let { email ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Email:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Display Name
                state.userDisplayName?.let { displayName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nazwa:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Plan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Plan:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.currentPlan ?: "Nie ustawiono",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                    )
                }
            }
        }
        
        // Upgrade Plan Button
        if (state.currentPlan != "PREMIUM_USER" && state.currentPlan != "ENTERPRISE_USER") {
            Button(
                onClick = {
                    // Jeśli użytkownik jest gościem (nie ma emaila), przekieruj do logowania/rejestracji
                    if (state.currentPlan == "GUEST" || state.userEmail == null) {
                        onNavigateToAuth()
                    } else {
                        onNavigateToUpgrade()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (state.currentPlan == "GUEST" || state.userEmail == null) {
                        "Zaloguj się aby rozszerzyć plan"
                    } else {
                        "Rozszerz plan"
                    }
                )
            }
        }
        
        // Login Button for Guest Users
        if (state.currentPlan == "GUEST" || state.userEmail == null) {
            Button(
                onClick = onNavigateToAuth,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Zaloguj się")
            }
        }
        
        // Notification Settings Button
        OutlinedButton(
            onClick = onNavigateToNotificationSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ustawienia powiadomień")
        }
        
        // Data Management Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zarządzanie danymi",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Wybierz poziom czyszczenia danych zgodnie z RODO",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Clear sensitive data only
                OutlinedButton(
                    onClick = { viewModel.clearSensitiveDataOnly() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Wyczyść tylko dane wrażliwe")
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Clear user data only
                OutlinedButton(
                    onClick = { viewModel.clearUserDataOnly() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Wyczyść dane użytkownika")
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Clear all data
                OutlinedButton(
                    onClick = { viewModel.clearAllUserData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !state.isLoading
                ) {
                    Text("Wyczyść wszystkie dane")
                }
            }
        }
        
        // Loading indicator
        if (state.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Czyszczenie danych...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Logout Button
        Button(
            onClick = {
                viewModel.logout()
                onNavigateToOnboarding()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Wyloguj się")
        }
        
        // Spacer to push content to top
        Spacer(modifier = Modifier.weight(1f))
        
        // App Version at bottom
        Text(
            text = "Wersja aplikacji: ${state.appVersion}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
