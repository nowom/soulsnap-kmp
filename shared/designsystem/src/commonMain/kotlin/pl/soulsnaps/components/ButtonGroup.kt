package pl.soulsnaps.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.designsystem.AppColorScheme

enum class ButtonType {
    PRIMARY,    // Filled button
    SECONDARY,  // Outlined button
    TEXT        // Text button
}

data class ButtonConfig(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val type: ButtonType = ButtonType.PRIMARY,
    val color: Color? = null
)

@Composable
fun ButtonGroup(
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    primaryButtonColor: Color = AppColorScheme.primary,
    secondaryButtonColor: Color = AppColorScheme.onSurface,
    spacing: Int = 12
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Secondary Button (left)
        OutlinedButton(
            onClick = onSecondaryClick,
            enabled = secondaryEnabled,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = secondaryButtonColor
            )
        ) {
            Text(
                text = secondaryButtonText,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Primary Button (right)
        Button(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = primaryButtonText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ButtonGroupVertical(
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    primaryButtonColor: Color = AppColorScheme.primary,
    secondaryButtonColor: Color = AppColorScheme.onSurface,
    spacing: Int = 12
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Primary Button (top)
        Button(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = primaryButtonText,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Secondary Button (bottom)
        OutlinedButton(
            onClick = onSecondaryClick,
            enabled = secondaryEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = secondaryButtonColor
            )
        ) {
            Text(
                text = secondaryButtonText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FlexibleButtonGroup(
    buttons: List<ButtonConfig>,
    modifier: Modifier = Modifier,
    horizontalSpacing: Int = 12,
    verticalSpacing: Int = 12,
    horizontalArrangement: Boolean = true
) {
    if (horizontalArrangement) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttons.forEach { config ->
                when (config.type) {
                    ButtonType.PRIMARY -> {
                        Button(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = config.color ?: AppColorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    ButtonType.SECONDARY -> {
                        OutlinedButton(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = config.color ?: AppColorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    ButtonType.TEXT -> {
                        TextButton(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium,
                                color = config.color ?: AppColorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    } else {
        androidx.compose.foundation.layout.Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            buttons.forEach { config ->
                when (config.type) {
                    ButtonType.PRIMARY -> {
                        Button(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = config.color ?: AppColorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    ButtonType.SECONDARY -> {
                        OutlinedButton(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = config.color ?: AppColorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    ButtonType.TEXT -> {
                        TextButton(
                            onClick = config.onClick,
                            enabled = config.enabled,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = config.text,
                                fontWeight = FontWeight.Medium,
                                color = config.color ?: AppColorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
} 