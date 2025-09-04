package pl.soulsnaps.utils

import kotlinx.datetime.*
import platform.Foundation.*

actual fun formatDate(date: LocalDateTime, pattern: String): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = pattern
    formatter.locale = NSLocale.currentLocale()
    val nsDate = localDateTimeToNSDate(date)
    return formatter.stringFromDate(nsDate)
}

// Helper function
fun localDateTimeToNSDate(dateTime: LocalDateTime): NSDate {
    val components = NSDateComponents().apply {
        year = dateTime.year.toLong()
        month = dateTime.monthNumber.toLong()
        day = dateTime.dayOfMonth.toLong()
        hour = dateTime.hour.toLong()
        minute = dateTime.minute.toLong()
        second = dateTime.second.toLong()
    }

    val calendar = NSCalendar.currentCalendar()
    return calendar.dateFromComponents(components)!!
}

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
