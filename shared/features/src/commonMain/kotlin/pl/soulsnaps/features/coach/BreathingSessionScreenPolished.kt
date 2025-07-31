package pl.soulsnaps.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.soulsnaps.features.exersises.BreathingSessionScreen

@Composable
fun BreathingSessionScreenPolished(onBack: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ćwiczenie oddechowe 4-7-8", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Oddychaj zgodnie z instrukcją, aby się zrelaksować.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (started) {
            BreathingSessionScreen()
            Spacer(Modifier.height(16.dp))
            Button(onClick = { started = false }, modifier = Modifier.fillMaxWidth()) { Text("Zatrzymaj") }
        } else {
            Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth()) { Text("Rozpocznij ćwiczenie") }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Powrót") }
    }
} 