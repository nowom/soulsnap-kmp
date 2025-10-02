package pl.soulsnaps.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import pl.soulsnaps.features.auth.SessionRefreshService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToUpgrade: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessionRefreshService: SessionRefreshService = koinInject()
    var refreshMessage by remember { mutableStateOf("") }
    
    // Debug logs
    LaunchedEffect(state) {
        println("DEBUG: SettingsScreen - state changed: userEmail=${state.userEmail}, currentPlan=${state.currentPlan}")
    }
    
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
        
        // Debug status for testing
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.userEmail != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = if (state.userEmail != null) 
                    "✅ Jesteś zalogowany (${state.userEmail})" 
                else "❌ Jesteś niezalogowany",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = if (state.userEmail != null) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onErrorContainer
            )
        }
        
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
        
        // Guest User Registration Card - only show for actual guests
        if (state.currentPlan == "GUEST" && state.userEmail == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💾 Zabezpiecz swoje dane",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jesteś gościem. Zarejestruj się aby:\n" +
                                "✓ Zabezpieczyć swoje wspomnienia\n" +
                                "✓ Synchronizować dane między urządzeniami\n" +
                                "✓ Uzyskać dostęp do więcej funkcji\n" +
                                "✓ Wszystkie obecne dane zostaną zachowane!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToRegistration,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Zarejestruj się i zachowaj dane 🔒")
                    }
                }
            }
        }
        
        // Upgrade Plan Button for Registered Users
        if (state.currentPlan != "PREMIUM_USER" && state.currentPlan != "ENTERPRISE_USER" && state.currentPlan != "GUEST" && state.userEmail != null) {
            Button(
                onClick = onNavigateToUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Rozszerz plan")
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
        
        // Session Management
        if (state.userEmail != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Zarządzanie sesją",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Sesja jest automatycznie odświeżana co 5 minut",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Manual refresh button
                    Button(
                        onClick = {
                            refreshMessage = "Odświeżanie sesji..."
                            kotlinx.coroutines.GlobalScope.launch {
                                try {
                                    sessionRefreshService.refreshNow()
                                    refreshMessage = "✅ Sesja odświeżona pomyślnie"
                                } catch (e: Exception) {
                                    refreshMessage = "❌ Błąd odświeżania: ${e.message}"
                                }
                                kotlinx.coroutines.delay(2000)
                                refreshMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odśwież sesję teraz")
                    }
                    
                    if (refreshMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = refreshMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (refreshMessage.startsWith("✅")) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        )
                    }
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
