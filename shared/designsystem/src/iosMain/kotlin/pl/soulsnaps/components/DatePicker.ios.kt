package pl.soulsnaps.components

import androidx.compose.runtime.Composable

@Composable
actual fun showPlatformDatePicker(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    // For now, just call the callback with the initial date
    // In a real implementation, this would show iOS's DatePicker
    onDateSelected(initialDateMillis)
}

