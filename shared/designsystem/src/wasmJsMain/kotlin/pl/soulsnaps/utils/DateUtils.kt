package pl.soulsnaps.utils

import kotlinx.datetime.LocalDateTime

actual fun formatDate(date: LocalDateTime, pattern: String): String {
    // Simple date formatting for wasm
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}


