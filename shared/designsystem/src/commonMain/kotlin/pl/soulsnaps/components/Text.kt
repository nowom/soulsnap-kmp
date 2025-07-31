package pl.soulsnaps.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import pl.soulsnaps.designsystem.AppColorScheme

@Composable
fun HeadingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

@Composable
fun SubtitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = modifier
    )
}

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier
    )
}

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = modifier
    )
} 