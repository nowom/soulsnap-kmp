package pl.soulsnaps.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.utils.formatTimestamp
import pl.soulsnaps.utils.getDefaultLocale

/**
 * Show platform-specific date picker
 */
@Composable
expect fun showPlatformDatePicker(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit
)

@Composable
fun DatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "Select a date",
    enabled: Boolean = true,
    showClearButton: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val displayText = selectedDateMillis?.let { dateMillis ->
        val dateStr = formatTimestamp(dateMillis, "MMM dd, yyyy")
        val timeStr = formatTimestamp(dateMillis, "HH:mm")
        "$dateStr at $timeStr"
    } ?: placeholder
    
    Column(modifier = modifier) {
        // Date display field
        OutlinedTextField(
            value = displayText,
            onValueChange = { }, // Read-only
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row {
                    if (showClearButton && selectedDateMillis != null) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                onDateSelected(0) // Clear date
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        )
        
        // Date picker button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedDateMillis != null) "Change Date" else "Select Date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Quick date options
        if (selectedDateMillis == null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDateButton(
                    text = "Today",
                    onClick = { onDateSelected(getCurrentTimeMillis()) }
                )
                QuickDateButton(
                    text = "Yesterday",
                    onClick = { 
                        val yesterday = getCurrentTimeMillis() - (24 * 60 * 60 * 1000)
                        onDateSelected(yesterday)
                    }
                )
            }
        }
    }
    
    // Show platform date picker
    if (showDatePicker) {
        showPlatformDatePicker(
            initialDateMillis = selectedDateMillis ?: getCurrentTimeMillis(),
            onDateSelected = { timestamp ->
                onDateSelected(timestamp)
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun QuickDateButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
