package pl.soulsnaps.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

actual fun formatDate(date: LocalDateTime, pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.toJavaLocalDateTime().format(formatter)
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
