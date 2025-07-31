package pl.soulsnaps.features.virtualmirror

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.CameraPreview
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.permissions.WithCameraPermission

@Composable
fun VirtualMirrorScreen(
    onBack: () -> Unit,
    viewModel: VirtualMirrorViewModel = koinViewModel()
) {
    WithCameraPermission(
        content = {
            CameraPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            )
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Analizuj emocje",
                    onClick = { viewModel.handleIntent(VirtualMirrorIntent.AnalyzeEmotion) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Wróć",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        deniedContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aby korzystać z Wirtualnego Lustra, musisz udzielić dostępu do kamery.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { /* Open app settings */ }) {
                    Text("Otwórz ustawienia")
                }
            }
        }
    )
}

@Composable
private fun EmotionResult(
    emotion: DetectedEmotion?,
    prompt: String?,
    onTryAgain: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emotion?.emoji ?: "",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = emotion?.label ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.onPrimaryContainer
                )
                Text(
                    text = "Pewność: ${(emotion?.confidence?.times(100)?.toInt() ?: 0)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (!prompt.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColorScheme.onSurface,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(
            text = "Spróbuj ponownie",
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(
            text = "Wróć",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
} 