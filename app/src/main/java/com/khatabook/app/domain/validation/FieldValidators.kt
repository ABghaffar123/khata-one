package com.khatabook.app.domain.validation

import com.khatabook.app.util.PkDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * ═══════════════════════════════════════════════════════════════════
 * FIELD VALIDATORS — Pure functions, no Android dependencies
 * ═══════════════════════════════════════════════════════════════════
 *
 * Each validator is a single-responsibility function that returns
 * ValidationResult. Composable via `plus` operator.
 *
 * All validators are stateless and thread-safe.
 */

object FieldValidators {

    // ═══════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════

    private const val MAX_AMOUNT = 10_000_000.0       // 1 Crore PKR
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_NOTES_LENGTH = 500
    private const val MAX_PHONE_LENGTH = 15
    private const val MIN_PHONE_LENGTH = 10
    private const val MAX_DAILY_TRANSACTIONS = 20

    // Pakistani phone pattern: 03XX-XXXXXXX or +92-3XX-XXXXXXX
    private val PAK_PHONE_PATTERN = Pattern.compile(
        "^(\\+92|0092|92|0)?3[0-9]{2}[-\\s]?[0-9]{7}$"
    )

    // OCR amount noise: characters that look like digits but aren't
    private val OCR_AMOUNT_NOISE = setOf('l', 'I', 'O', 'o', 'S', 's', 'Z', 'z', 'B', 'G')

    // Characters that are clearly not names
    private val NAME_INVALID_PATTERN = Pattern.compile("^[0-9\\p{Punct}]+$")

    // Common OCR misreads
    private val OCR_O_LOOKS_LIKE_ZERO = Regex("[oO](?=[0-9])")
    private val OCR_L_LOOKS_LIKE_ONE = Regex("(?<=[0-9])[lI](?=[0-9])")
    private val OCR_S_LOOKS_LIKE_FIVE = Regex("(?<=[0-9])[sS](?=[0-9])")


    // ═══════════════════════════════════════════════════════════════
    // AMOUNT VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate a monetary amount field.
     *
     * @param value     Raw string input (may contain OCR artifacts)
     * @param isRequired  If true, empty = error. If false, empty = skip.
     * @param fieldName Display name for error messages
     * @return ValidationResult
     */
    fun validateAmount(
        value: String,
        isRequired: Boolean = true,
        fieldName: String = "amount"
    ): ValidationResult {
        val trimmed = value.trim()

        // Empty check
        if (trimmed.isEmpty()) {
            return if (isRequired) {
                ValidationResult.error(fieldName, ValidationRule.AMOUNT_EMPTY, "Amount is required")
            } else {
                ValidationResult.success()
            }
        }

        // Clean OCR artifacts before parsing
        val cleaned = cleanAmountFromOcr(trimmed)
        val amount = cleaned.toDoubleOrNull()

        if (amount == null) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.AMOUNT_INVALID_FORMAT,
                "\"$trimmed\" is not a valid amount"
            )
        }

        // Zero check
        if (amount == 0.0) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.AMOUNT_ZERO,
                "Amount cannot be zero"
            )
        }

        // Negative check
        if (amount < 0) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.AMOUNT_NEGATIVE,
                "Amount cannot be negative. Use transaction type to indicate direction."
            )
        }

        // Too large check
        if (amount > MAX_AMOUNT) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.AMOUNT_TOO_LARGE,
                "Amount cannot exceed Rs ${String.format("%,.0f", MAX_AMOUNT)}"
            )
        }

        // Decimal precision check (PKR has no paisa in common use)
        val decimalPart = amount - amount.toLong()
        if (decimalPart > 0.001 && decimalPart.toString().substringAfter(".").length > 2) {
            return ValidationResult.warning(
                fieldName,
                ValidationRule.AMOUNT_DECIMAL_PRECISION,
                "Amount has unusual decimal places. Verify the amount."
            )
        }

        return ValidationResult.success()
    }

    /**
     * Validate amount for a transaction where direction matters.
     * CREDIT/PURCHASE: amount must be positive (what was given)
     * PAYMENT/SUPPLIER_PAYMENT: amount must be positive (what was received/paid)
     */
    fun validateTransactionAmount(
        value: String,
        transactionType: String,
        currentBalance: Double = 0.0
    ): ValidationResult {
        val base = validateAmount(value, isRequired = true)

        if (!base.isValid) return base

        val amount = cleanAmountFromOcr(value.trim()).toDoubleOrNull() ?: return base

        // Payment exceeding balance by huge margin is suspicious
        if (transactionType == "PAYMENT" && currentBalance > 0 && amount > currentBalance * 2) {
            return base + ValidationResult.warning(
                "amount",
                ValidationRule.TRANSACTION_IMPOSSIBLE,
                "Payment (Rs ${String.format("%,.0f", amount)}) is much larger than " +
                        "outstanding balance (Rs ${String.format("%,.0f", currentBalance)}). " +
                        "Please verify."
            )
        }

        return base
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate a date field.
     *
     * @param value     Raw date string (may be OCR output)
     * @param isRequired
     * @param fieldName
     * @return ValidationResult
     */
    fun validateDate(
        value: String,
        isRequired: Boolean = true,
        fieldName: String = "date"
    ): ValidationResult {
        val trimmed = value.trim()

        if (trimmed.isEmpty()) {
            return if (isRequired) {
                ValidationResult.error(fieldName, ValidationRule.DATE_EMPTY, "Date is required")
            } else {
                ValidationResult.success()
            }
        }

        val parsed = parseFlexibleDate(trimmed)

        if (parsed == null) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.DATE_INVALID_FORMAT,
                "\"$trimmed\" could not be parsed as a date. Use DD/MM/YYYY format."
            )
        }

        val now = PkDateTime.nowUtc()
        val oneYearAgo = PkDateTime.pktCalendar().apply {
            add(Calendar.YEAR, -1)
        }.timeInMillis
        val today = PkDateTime.startOfPktToday()

        // Future date check
        if (parsed > today + 86_400_000) { // +1 day tolerance for timezone issues
            return ValidationResult.warning(
                fieldName,
                ValidationRule.DATE_IN_FUTURE,
                "Date is in the future. Verify the date is correct."
            )
        }

        // Too old check
        if (parsed < oneYearAgo) {
            return ValidationResult.warning(
                fieldName,
                ValidationRule.DATE_TOO_OLD,
                "Date is more than 1 year ago. Verify the date is correct."
            )
        }

        return ValidationResult.success()
    }

    /**
     * Validate date sequence: payment date should be >= credit date.
     */
    fun validateDateSequence(
        transactionDate: Long,
        referenceDate: Long?,
        transactionType: String
    ): ValidationResult {
        if (referenceDate == null) return ValidationResult.success()

        if (transactionType == "PAYMENT" && transactionDate < referenceDate) {
            return ValidationResult.warning(
                "date",
                ValidationRule.DATE_SEQUENCE_ERROR,
                "Payment date is before the credit date. Verify the dates."
            )
        }

        return ValidationResult.success()
    }


    // ═══════════════════════════════════════════════════════════════
    // NAME VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate a customer/supplier name field.
     */
    fun validateName(
        value: String,
        isRequired: Boolean = true,
        fieldName: String = "name"
    ): ValidationResult {
        val trimmed = value.trim()

        // Empty check
        if (trimmed.isEmpty()) {
            return if (isRequired) {
                ValidationResult.error(fieldName, ValidationRule.NAME_EMPTY, "Name is required")
            } else {
                ValidationResult.success()
            }
        }

        // Too short
        if (trimmed.length < MIN_NAME_LENGTH) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.NAME_TOO_SHORT,
                "Name must be at least $MIN_NAME_LENGTH characters"
            )
        }

        // Too long
        if (trimmed.length > MAX_NAME_LENGTH) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.NAME_TOO_LONG,
                "Name cannot exceed $MAX_NAME_LENGTH characters"
            )
        }

        // Invalid characters — only numbers/symbols
        if (NAME_INVALID_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.NAME_INVALID_CHARS,
                "Name cannot be only numbers or symbols"
            )
        }

        // Looks like an amount — OCR misread
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length > trimmed.length * 0.6 && trimmed.length > 3) {
            return ValidationResult.warning(
                fieldName,
                ValidationRule.NAME_LOOKS_LIKE_AMOUNT,
                "\"$trimmed\" looks like a number, not a name. Please verify."
            )
        }

        return ValidationResult.success()
    }


    // ═══════════════════════════════════════════════════════════════
    // PHONE VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate a Pakistani phone number.
     * Optional field — empty is OK.
     */
    fun validatePhone(
        value: String,
        fieldName: String = "phone"
    ): ValidationResult {
        val trimmed = value.trim()

        // Phone is optional
        if (trimmed.isEmpty()) return ValidationResult.success()

        // Remove common separators
        val normalized = trimmed.replace(Regex("[\\s\\-()]"), "")

        // Length check
        if (normalized.length < MIN_PHONE_LENGTH) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.PHONE_TOO_SHORT,
                "Phone number is too short"
            )
        }

        if (normalized.length > MAX_PHONE_LENGTH) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.PHONE_TOO_LONG,
                "Phone number is too long"
            )
        }

        // Format check
        if (!PAK_PHONE_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.warning(
                fieldName,
                ValidationRule.PHONE_INVALID_FORMAT,
                "Phone format doesn't look Pakistani. Expected: 03XX-XXXXXXX"
            )
        }

        return ValidationResult.success()
    }


    // ═══════════════════════════════════════════════════════════════
    // NOTES / DESCRIPTION VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate notes or description field.
     */
    fun validateNotes(
        value: String,
        fieldName: String = "notes"
    ): ValidationResult {
        if (value.length > MAX_NOTES_LENGTH) {
            return ValidationResult.error(
                fieldName,
                ValidationRule.NOTES_TOO_LONG,
                "Notes cannot exceed $MAX_NOTES_LENGTH characters"
            )
        }

        // Check if description looks like an amount (OCR injection)
        val trimmed = value.trim()
        if (trimmed.length in 1..10) {
            val cleaned = cleanAmountFromOcr(trimmed)
            if (cleaned.toDoubleOrNull() != null) {
                return ValidationResult.warning(
                    fieldName,
                    ValidationRule.DESCRIPTION_LOOKS_LIKE_AMOUNT,
                    "\"$trimmed\" looks like a number, not a description. Verify this is correct."
                )
            }
        }

        return ValidationResult.success()
    }


    // ═══════════════════════════════════════════════════════════════
    // COMPOSITE VALIDATORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate all customer fields at once.
     */
    fun validateCustomer(
        name: String,
        phone: String = "",
        notes: String = ""
    ): ValidationResult {
        return validateName(name) +
                validatePhone(phone) +
                validateNotes(notes, "customer_notes")
    }

    /**
     * Validate all transaction fields at once.
     */
    fun validateTransaction(
        amount: String,
        date: String,
        customerName: String,
        description: String = "",
        transactionType: String = "CREDIT",
        currentBalance: Double = 0.0
    ): ValidationResult {
        return validateTransactionAmount(amount, transactionType, currentBalance) +
                validateDate(date) +
                validateName(customerName, fieldName = "customer_name") +
                validateNotes(description, "description")
    }


    // ═══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Clean common OCR artifacts from amount strings.
     * "1,O3O" → "1030", "l500" → "1500", "S00" → "500"
     */
    private fun cleanAmountFromOcr(raw: String): String {
        var cleaned = raw
            .replace(Regex("[,\\s]"), "")        // Remove commas and spaces
            .replace(Regex("^Rs\\.?\\s*", RegexOption.IGNORE_CASE), "")  // Remove "Rs" prefix
            .replace(Regex("^PKR\\s*", RegexOption.IGNORE_CASE), "")     // Remove "PKR" prefix
            .replace(Regex("^[ روپے]+"), "")     // Remove Urdu prefix

        // OCR character corrections (context-aware)
        cleaned = cleaned.replace(OCR_O_LOOKS_LIKE_ZERO, "0")
        cleaned = cleaned.replace(OCR_L_LOOKS_LIKE_ONE, "1")
        cleaned = cleaned.replace(OCR_S_LOOKS_LIKE_FIVE, "5")

        // Remove trailing dots/commas
        cleaned = cleaned.trimEnd('.', ',')

        return cleaned
    }

    /**
     * Parse dates in multiple formats (OCR-friendly).
     * Supports: DD/MM/YYYY, DD-MM-YYYY, DD.MM.YYYY, DD/MM/YY, DD-MM-YY
     */
    private fun parseFlexibleDate(input: String): Long? {
        val cleaned = input.trim()
            .replace(Regex("[/\\-\\.]"), "/")
            .replace(Regex("\\s+"), "")

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
                val sdf = SimpleDateFormat(format, PkDateTime.PKT_LOCALE)
                sdf.timeZone = PkDateTime.PKT_TIMEZONE
                sdf.isLenient = false
                val date = sdf.parse(cleaned)
                if (date != null) {
                    // Sanity check: year should be 2000-2100
                    val cal = PkDateTime.pktCalendar()
                    cal.time = date
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
}
