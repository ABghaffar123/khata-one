package com.khatabook.app.util

/**
 * App-wide constants for Khata One.
 */
object Constants {

    // ═══ App Info ═══
    const val APP_NAME = "Khata One"
    const val APP_VERSION = "1.0.0"
    const val PACKAGE_NAME = "com.khatabook.app"

    // ═══ Splash ═══
    const val SPLASH_DURATION_MS = 1800L  // 1.8 seconds total splash
    const val SPLASH_MIN_DISPLAY_MS = 1200L  // Minimum 1.2s display

    // ═══ Languages ═══
    const val LANG_ENGLISH = "en"
    const val LANG_URDU = "ur"
    const val LANG_ROMAN_URDU = "ur-roman"

    // ═══ Shared Preferences ═══
    const val PREFS_NAME = "khata_prefs"
    const val KEY_LANGUAGE_SELECTED = "language_selected"
    const val KEY_LANGUAGE = "language"
    const val KEY_THEME = "theme"
    const val KEY_SHOP_NAME = "shop_name"
    const val KEY_FIRST_LAUNCH = "first_launch"

    // ═══ Transaction Types ═══
    const val TRANSACTION_CREDIT = "CREDIT"
    const val TRANSACTION_PAYMENT = "PAYMENT"
    const val TRANSACTION_CASH_SALE = "CASH_SALE"
    const val TRANSACTION_PURCHASE = "PURCHASE"
    const val TRANSACTION_EXPENSE = "EXPENSE"

    // ═══ Currency ═══
    const val CURRENCY_SYMBOL = "Rs"
    const val CURRENCY_CODE = "PKR"

    // ═══ Timezone ═══
    const val TIMEZONE_ID = "Asia/Karachi"
    const val TIMEZONE_LABEL = "PKT (UTC+5)"

    // ═══ Limits ═══
    const val MAX_NAME_LENGTH = 100
    const val MAX_PHONE_LENGTH = 15
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MAX_NOTE_LENGTH = 1000
    const val MAX_AMOUNT = 99_999_999.99
    const val MIN_AMOUNT = 0.01

    // ═══ Date Formats ═══
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy"
    const val DATE_FORMAT_SHORT = "dd/MM/yyyy"
    const val DATE_FORMAT_MONTH_YEAR = "MMM yyyy"

    // ═══ Database ═══
    const val DATABASE_NAME = "khata_database"
    const val DATABASE_VERSION = 1
}
