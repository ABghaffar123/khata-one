package com.khatabook.app.util

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

// ═══════════════════════════════════════════════════════════
// CURRENCY FORMATTING
// ═══════════════════════════════════════════════════════════

/**
 * Format amount as PKR currency string.
 * Example: 5000.0 → "Rs 5,000"
 */
fun Double.toCurrency(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "PK"))
    format.maximumFractionDigits = 0
    return "${Constants.CURRENCY_SYMBOL} ${format.format(this)}"
}

/**
 * Format amount with decimals.
 * Example: 5000.50 → "Rs 5,000.50"
 */
fun Double.toCurrencyWithDecimals(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "PK"))
    format.maximumFractionDigits = 2
    return "${Constants.CURRENCY_SYMBOL} ${format.format(this)}"
}

// ═══════════════════════════════════════════════════════════
// DATE FORMATTING
// ═══════════════════════════════════════════════════════════

/**
 * Format timestamp to display date (PKT).
 * Example: timestamp → "28 Aug 2025"
 */
fun Long.toDisplayDate(): String = PkDateTime.formatDisplayDate(this)

/**
 * Format timestamp to full date (PKT).
 * Example: timestamp → "28 August 2025"
 */
fun Long.toFullDate(): String = PkDateTime.formatFullDate(this)

/**
 * Format timestamp to short date (PKT).
 * Example: timestamp → "28/08/2025"
 */
fun Long.toShortDate(): String = PkDateTime.formatShortDate(this)

/**
 * Get relative time string (PKT).
 * Example: timestamp → "2 hours ago", "Yesterday", "3 days ago"
 */
fun Long.toRelativeTime(): String = PkDateTime.formatRelativeTime(this)

/**
 * Check if timestamp is today (PKT).
 * Uses Asia/Karachi timezone regardless of device timezone.
 */
fun Long.isToday(): Boolean = PkDateTime.isPktToday(this)

/**
 * Get start of day for a timestamp (PKT).
 */
fun Long.startOfDay(): Long = PkDateTime.startOfDayPkt(this)

/**
 * Get greeting based on time of day (PKT).
 */
fun getGreeting(): String {
    val hour = PkDateTime.currentPktHour()
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

// ═══════════════════════════════════════════════════════════
// STRING UTILS
// ═══════════════════════════════════════════════════════════

/**
 * Get initials from name.
 * Example: "Muhammad Ali" → "MA"
 */
fun String.getInitials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

/**
 * Normalize phone number (remove dashes, spaces, etc.).
 * Example: "0300-123 4567" → "03001234567"
 */
fun String.normalizePhone(): String {
    return replace(Regex("[^\\d+]"), "")
}

/**
 * Check if string is a valid Pakistani phone number.
 */
fun String.isValidPakPhone(): Boolean {
    val normalized = normalizePhone()
    return when {
        normalized.startsWith("+92") && normalized.length == 13 -> true
        normalized.startsWith("03") && normalized.length == 11 -> true
        else -> false
    }
}

// ═══════════════════════════════════════════════════════════
// COLLECTION UTILS
// ═══════════════════════════════════════════════════════════

/**
 * Sum of a specific field in a list.
 */
inline fun <T> List<T>.sumByDouble(selector: (T) -> Double): Double {
    return fold(0.0) { acc, element -> acc + selector(element) }
}
