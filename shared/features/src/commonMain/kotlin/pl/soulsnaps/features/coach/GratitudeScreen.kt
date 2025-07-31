package pl.soulsnaps.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GratitudeScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(listOf<String>()) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Wdzięczność", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Zapisz 1-3 rzeczy, za które jesteś dziś wdzięczny.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Za co jesteś wdzięczny?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (input.isNotBlank()) {
                    entries = listOf(input) + entries
                    input = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = input.isNotBlank()
        ) { Text("Zapisz") }
        Spacer(Modifier.height(16.dp))
        Text("Twoje wpisy:", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        entries.forEach {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Powrót") }
    }
} 