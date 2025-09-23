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

/**
 * Format timestamp to date string with pattern
 */
expect fun formatTimestamp(timestamp: Long, pattern: String): String

/**
 * Get default locale for date formatting
 */
expect fun getDefaultLocale(): String

/**
 * Gets time of day period from timestamp
 */
fun getTimeOfDay(timestamp: Long): String {
    // Convert timestamp to hour of day using Kotlin Multiplatform compatible approach
    val hour = ((timestamp / (1000 * 60 * 60)) % 24).toInt()
    
    return when (hour) {
        in 5..11 -> "Poranek"
        in 12..17 -> "Południe" 
        in 18..22 -> "Wieczór"
        else -> "Noc"
    }
}

/**
 * Time of day enum for common use
 */
enum class TimeOfDay {
    EARLY_MORNING, MORNING, AFTERNOON, EVENING, NIGHT, LATE_NIGHT
}

/**
 * Gets time of day enum from timestamp for analysis
 */
fun getTimeOfDayEnum(timestamp: Long): TimeOfDay {
    // Convert timestamp to hour of day using Kotlin Multiplatform compatible approach
    val hour = ((timestamp / (1000 * 60 * 60)) % 24).toInt()
    
    return when (hour) {
        in 5..8 -> TimeOfDay.EARLY_MORNING
        in 9..11 -> TimeOfDay.MORNING
        in 12..16 -> TimeOfDay.AFTERNOON
        in 17..19 -> TimeOfDay.EVENING
        in 20..22 -> TimeOfDay.NIGHT
        else -> TimeOfDay.LATE_NIGHT
    }
}
