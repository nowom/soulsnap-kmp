package pl.soulsnaps.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect fun formatDate(date: LocalDateTime, pattern: String = "yyyy-MM-dd"): String

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
}

expect fun getCurrentTimeMillis(): Long
