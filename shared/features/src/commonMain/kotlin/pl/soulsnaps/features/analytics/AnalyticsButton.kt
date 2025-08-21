package pl.soulsnaps.features.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * AnalyticsButton - uniwersalny komponent do wyświetlania przycisku analityki
 * 
 * Funkcjonalności:
 * - Wyświetla przycisk z ikoną analityki
 * - Pokazuje badge z liczbą alertów
 * - Zmienia kolor na podstawie alertów
 * - Obsługuje kliknięcie do otwarcia analityki
 */
@Composable
fun AnalyticsButton(
    onShowAnalytics: () -> Unit,
    alerts: List<CapacityAlert> = emptyList(),
    modifier: Modifier = Modifier
) {
    val hasCriticalAlerts = alerts.any { it.type == AlertType.CRITICAL }
    val hasWarnings = alerts.any { it.type == AlertType.WARNING }
    
    val buttonColor = when {
        hasCriticalAlerts -> MaterialTheme.colorScheme.error
        hasWarnings -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    val iconColor = when {
        hasCriticalAlerts -> Color.White
        hasWarnings -> Color.White
        else -> MaterialTheme.colorScheme.onPrimary
    }
    
    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onShowAnalytics,
            containerColor = buttonColor,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (alerts.isNotEmpty()) Icons.Default.Warning else Icons.Default.Analytics,
                contentDescription = "Analytics",
                tint = iconColor
            )
        }
        
        // Badge z liczbą alertów
        if (alerts.isNotEmpty()) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp),
                containerColor = if (hasCriticalAlerts) Color.Red else Color(0xFFFF8C00)
            ) {
                Text(
                    text = alerts.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * AnalyticsButton z tekstem - większa wersja z opisem
 */
@Composable
fun AnalyticsButtonWithText(
    onShowAnalytics: () -> Unit,
    alerts: List<CapacityAlert> = emptyList(),
    modifier: Modifier = Modifier
) {
    val hasCriticalAlerts = alerts.any { it.type == AlertType.CRITICAL }
    val hasWarnings = alerts.any { it.type == AlertType.WARNING }
    
    val buttonColor = when {
        hasCriticalAlerts -> MaterialTheme.colorScheme.error
        hasWarnings -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    val textColor = when {
        hasCriticalAlerts -> Color.White
        hasWarnings -> Color.White
        else -> MaterialTheme.colorScheme.onPrimary
    }
    
    Button(
        onClick = onShowAnalytics,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (alerts.isNotEmpty()) Icons.Default.Warning else Icons.Default.Analytics,
                contentDescription = "Analytics",
                tint = textColor
            )
            
            Text(
                text = if (alerts.isNotEmpty()) {
                    "Alerty (${alerts.size})"
                } else {
                    "Analityka"
                },
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * CompactAnalyticsButton - kompaktowa wersja
 */
@Composable
fun CompactAnalyticsButton(
    onShowAnalytics: () -> Unit,
    alerts: List<CapacityAlert> = emptyList(),
    modifier: Modifier = Modifier
) {
    val hasAlerts = alerts.isNotEmpty()
    
    IconButton(
        onClick = onShowAnalytics,
        modifier = modifier
    ) {
        Box {
            Icon(
                imageVector = if (hasAlerts) Icons.Default.Warning else Icons.Default.Analytics,
                contentDescription = "Analytics",
                tint = if (hasAlerts) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            
            if (hasAlerts) {
                Badge(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = alerts.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
