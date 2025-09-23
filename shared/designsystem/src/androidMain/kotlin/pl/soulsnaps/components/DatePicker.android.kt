package pl.soulsnaps.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
actual fun showPlatformDatePicker(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    // For now, just call the callback with the initial date
    // In a real implementation, this would show Android's DatePickerDialog
    onDateSelected(initialDateMillis)
}

