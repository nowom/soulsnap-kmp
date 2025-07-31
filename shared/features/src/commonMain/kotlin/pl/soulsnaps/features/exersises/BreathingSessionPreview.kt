package pl.soulsnaps.features.exersises

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Podgląd sesji oddechowej z przyciskiem "Rozpocznij".
 * Breathing session preview with "Start" button.
 */
@Composable
fun BreathingSessionPreview(
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(
            text = "Sesja Oddechu 4-7-8",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Companion.Bold,
            modifier = Modifier.Companion.padding(bottom = 8.dp)
        )
        Text(
            text = "Zrelaksuj się i uspokój swój umysł.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.Companion.padding(bottom = 16.dp)
        )
        Button(
            onClick = onStartSession,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("Rozpocznij Sesję")
        }
    }
}