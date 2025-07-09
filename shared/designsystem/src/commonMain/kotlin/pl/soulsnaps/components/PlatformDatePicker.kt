package pl.soulsnaps.components

import androidx.compose.runtime.Composable

@Composable
expect fun showPlatformDatePicker(
    initialDateMillis: Long?,
    onDateSelected: (Long) -> Unit
)