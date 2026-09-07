package com.khatabook.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ═══════════════════════════════════════════════════════════════════
 * PKT TIMEZONE UTILITY — Asia/Karachi (UTC+5, no DST)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Pakistan Standard Time is fixed at UTC+5 year-round.
 * Pakistan does NOT observe daylight saving time.
 *
 * RULES:
 *   1. All internal storage = UTC (epoch millis)
 *   2. All user-facing display = PKT
 *   3. All "today" / "this week" / "this month" ranges = PKT
 *   4. All notifications / alarms = scheduled in PKT
 */
object PkDateTime {

    /** IANA timezone ID for Pakistan Standard Time */
    const val TIMEZONE_ID = "Asia/Karachi"

    /** UTC offset in milliseconds: +5 hours = 18,000,000 ms */
    const val UTC_OFFSET_MS = 5 * 60 * 60 * 1000L

    /** PKT timezone object — reused across the app */
    val PKT_TIMEZONE: TimeZone = TimeZone.getTimeZone(TIMEZONE_ID)

    /** PKT Locale for consistent formatting */
    val PKT_LOCALE: Locale = Locale("en", "PK")


    // ═══════════════════════════════════════════════════════════════
    // CALENDAR FACTORIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get a Calendar set to PKT timezone.
     * Use this everywhere instead of Calendar.getInstance().
     */
    fun pktCalendar(): Calendar {
        return Calendar.getInstance(PKT_TIMEZONE, PKT_LOCALE)
    }

    /**
     * Get a Calendar set to PKT, initialized to the given UTC epoch millis.
     */
    fun pktCalendarFor(utcMillis: Long): Calendar {
        return pktCalendar().apply { timeInMillis = utcMillis }
    }


    // ═══════════════════════════════════════════════════════════════
    // CURRENT TIME
    // ═══════════════════════════════════════════════════════════════

    /**
     * Current UTC epoch millis. Use for database storage.
     */
    fun nowUtc(): Long = System.currentTimeMillis()

    /**
     * Current PKT hour (0-23). Useful for greeting, notifications, etc.
     */
    fun currentPktHour(): Int {
        return pktCalendar().get(Calendar.HOUR_OF_DAY)
    }


    // ═══════════════════════════════════════════════════════════════
    // "TODAY" RANGE IN PKT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Start of today in PKT (00:00:00.000 PKT).
     * Returns UTC epoch millis.
     */
    fun startOfPktToday(): Long {
        return pktCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * End of today in PKT (23:59:59.999 PKT).
     * Returns UTC epoch millis.
     */
    fun endOfPktToday(): Long {
        return pktCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Today's range as a Pair<start, end> in UTC epoch millis.
     */
    fun pktTodayRange(): Pair<Long, Long> {
        return Pair(startOfPktToday(), endOfPktToday())
    }


    // ═══════════════════════════════════════════════════════════════
    // "THIS WEEK" RANGE IN PKT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Start of this week in PKT (Monday 00:00:00.000 PKT).
     * Returns UTC epoch millis.
     */
    fun startOfPktWeek(): Long {
        return pktCalendar().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * End of this week in PKT (Sunday 23:59:59.999 PKT).
     * Returns UTC epoch millis.
     */
    fun endOfPktWeek(): Long {
        return pktCalendar().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            add(Calendar.DAY_OF_WEEK, 7)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * This week's range as a Pair<start, end> in UTC epoch millis.
     */
    fun pktWeekRange(): Pair<Long, Long> {
        return Pair(startOfPktWeek(), endOfPktWeek())
    }


    // ═══════════════════════════════════════════════════════════════
    // "THIS MONTH" RANGE IN PKT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Start of this month in PKT (1st day 00:00:00.000 PKT).
     * Returns UTC epoch millis.
     */
    fun startOfPktMonth(): Long {
        return pktCalendar().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * End of this month in PKT (last day 23:59:59.999 PKT).
     * Returns UTC epoch millis.
     */
    fun endOfPktMonth(): Long {
        return pktCalendar().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 0) // Last day of current month
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * This month's range as a Pair<start, end> in UTC epoch millis.
     */
    fun pktMonthRange(): Pair<Long, Long> {
        return Pair(startOfPktMonth(), endOfPktMonth())
    }


    // ═══════════════════════════════════════════════════════════════
    // "TODAY KEY" FOR SHARED PREFS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Today's date key in PKT for SharedPreferences (opening_cash_YYYY_M_D).
     */
    fun pktTodayKey(): String {
        val cal = pktCalendar()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}_${cal.get(Calendar.DAY_OF_MONTH)}"
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE FORMATTING (UTC millis → PKT string)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Format UTC millis to PKT display date.
     * Example: "28 Aug 2025"
     */
    fun formatDisplayDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, PKT_LOCALE)
        sdf.timeZone = PKT_TIMEZONE
        return sdf.format(Date(utcMillis))
    }

    /**
     * Format UTC millis to PKT full date.
     * Example: "28 August 2025"
     */
    fun formatFullDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat(Constants.DATE_FORMAT_FULL, PKT_LOCALE)
        sdf.timeZone = PKT_TIMEZONE
        return sdf.format(Date(utcMillis))
    }

    /**
     * Format UTC millis to PKT short date.
     * Example: "28/08/2025"
     */
    fun formatShortDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat(Constants.DATE_FORMAT_SHORT, PKT_LOCALE)
        sdf.timeZone = PKT_TIMEZONE
        return sdf.format(Date(utcMillis))
    }

    /**
     * Format UTC millis to PKT time.
     * Example: "02:30 PM"
     */
    fun formatTime(utcMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", PKT_LOCALE)
        sdf.timeZone = PKT_TIMEZONE
        return sdf.format(Date(utcMillis))
    }

    /**
     * Format UTC millis to PKT date+time.
     * Example: "28 Aug 2025, 02:30 PM"
     */
    fun formatDateTime(utcMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", PKT_LOCALE)
        sdf.timeZone = PKT_TIMEZONE
        return sdf.format(Date(utcMillis))
    }

    /**
     * Format UTC millis to relative time in PKT.
     * Example: "Just now", "2 hours ago", "Yesterday", "3 days ago"
     */
    fun formatRelativeTime(utcMillis: Long): String {
        val now = nowUtc()
        val diff = now - utcMillis

        return when {
            diff < 60_000L -> "Just now"                          // < 1 min
            diff < 3_600_000L -> "${diff / 60_000}m ago"          // < 1 hour
            diff < 86_400_000L -> "${diff / 3_600_000}h ago"      // < 1 day
            diff < 172_800_000L -> "Yesterday"                     // < 2 days
            diff < 604_800_000L -> "${diff / 86_400_000} days ago" // < 7 days
            diff < 2_592_000_000L -> "${diff / 604_800_000} weeks ago" // < 30 days
            else -> formatDisplayDate(utcMillis)
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE CHECKS (is today in PKT?)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if a UTC timestamp falls within today in PKT.
     * Handles midnight rollover correctly.
     */
    fun isPktToday(utcMillis: Long): Boolean {
        val today = startOfPktToday()
        val tomorrow = today + 86_400_000L
        return utcMillis in today until tomorrow
    }

    /**
     * Check if a UTC timestamp is in the past (before today in PKT).
     */
    fun isBeforePktToday(utcMillis: Long): Boolean {
        return utcMillis < startOfPktToday()
    }

    /**
     * Check if a UTC timestamp is in the future (after today in PKT).
     */
    fun isAfterPktToday(utcMillis: Long): Boolean {
        return utcMillis > endOfPktToday()
    }


    // ═══════════════════════════════════════════════════════════════
    // START/END OF DAY IN PKT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get start of day in PKT for any given UTC timestamp.
     * Useful for grouping transactions by day.
     */
    fun startOfDayPkt(utcMillis: Long): Long {
        return pktCalendarFor(utcMillis).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get end of day in PKT for any given UTC timestamp.
     */
    fun endOfDayPkt(utcMillis: Long): Long {
        return pktCalendarFor(utcMillis).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE PARSING (PKT string → UTC millis)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Parse a date string in PKT timezone.
     * Supports multiple formats for OCR flexibility.
     */
    fun parseDatePkt(dateStr: String): Long? {
        val cleaned = dateStr.trim().replace(Regex("[/\\-.]"), "/")
        val formats = listOf(
            "dd/MM/yyyy",
            "dd/MM/yy",
            "d/M/yyyy",
            "d/M/yy",
            "yyyyMMdd",
            "ddMMyyyy"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, PKT_LOCALE)
                sdf.timeZone = PKT_TIMEZONE
                sdf.isLenient = false
                val date = sdf.parse(cleaned)
                if (date != null) {
                    val cal = pktCalendarFor(date.time)
                    val year = cal.get(Calendar.YEAR)
                    if (year in 2000..2100) {
                        return date.time
                    }
                }
            } catch (_: Exception) {
                // Try next format
            }
        }
        return null
    }


    // ═══════════════════════════════════════════════════════════════
    // NOTIFICATION SCHEDULING HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get the next 9:00 PM PKT trigger time in UTC epoch millis.
     * If it's already past 9 PM PKT today, returns tomorrow 9 PM PKT.
     */
    fun nextDailySummaryTriggerUtc(): Long {
        val cal = pktCalendar()
        cal.set(Calendar.HOUR_OF_DAY, 21)  // 9 PM
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // If 9 PM PKT has already passed today, move to tomorrow
        if (cal.timeInMillis <= nowUtc()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return cal.timeInMillis
    }

    /**
     * Get milliseconds until next 9:00 PM PKT from now.
     * Used for scheduling the daily summary notification.
     */
    fun millisUntilNextDailySummary(): Long {
        return nextDailySummaryTriggerUtc() - nowUtc()
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE PICKER HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get today's date in PKT as epoch millis for date picker default.
     */
    fun todayPktMillis(): Long = startOfPktToday()

    /**
     * Convert a date picker's selected millis (which may be in device tz)
     * to a proper PKT-aligned millis.
     */
    fun alignToPktDay(utcMillis: Long): Long {
        return startOfDayPkt(utcMillis)
    }
}
