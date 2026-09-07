package com.khatabook.app.domain.validation

/**
 * ═══════════════════════════════════════════════════════════════════
 * VALIDATION RESULT — Sealed class hierarchy for all validation outcomes
 * ═══════════════════════════════════════════════════════════════════
 *
 * Every validator returns one of these. UI layer maps them to user-friendly messages.
 *
 * Flow:
 *   Input → Validator → ValidationResult → UI (show errors/warnings)
 *
 * Two severity levels:
 *   - ERROR:   Blocks saving. User MUST fix before proceeding.
 *   - WARNING: Allows saving but warns user. Can be overridden.
 */

// ─── Single field error ───────────────────────────────────────────

/**
 * Represents a validation failure on a specific field.
 *
 * @param field    Which field failed (matches UI field ID)
 * @param rule     Which validation rule was violated
 * @param message  User-facing error message (already localized)
 * @param severity ERROR = must fix, WARNING = can override
 */
data class FieldError(
    val field: String,
    val rule: ValidationRule,
    val message: String,
    val severity: Severity = Severity.ERROR
)

enum class Severity {
    ERROR,    // Blocks save — user must fix
    WARNING   // Allows save — user can override
}

// ─── All validation rules ─────────────────────────────────────────

enum class ValidationRule {
    // Amount rules
    AMOUNT_EMPTY,
    AMOUNT_ZERO,
    AMOUNT_NEGATIVE,
    AMOUNT_TOO_LARGE,           // > 10,000,000 PKR
    AMOUNT_INVALID_FORMAT,      // "1,23abc" from OCR
    AMOUNT_DECIMAL_PRECISION,   // > 2 decimal places

    // Date rules
    DATE_EMPTY,
    DATE_IN_FUTURE,
    DATE_TOO_OLD,               // > 1 year ago
    DATE_INVALID_FORMAT,        // Unparseable from OCR
    DATE_SEQUENCE_ERROR,        // Payment before credit

    // Name rules
    NAME_EMPTY,
    NAME_TOO_SHORT,             // < 2 chars
    NAME_TOO_LONG,              // > 100 chars
    NAME_INVALID_CHARS,         // Only numbers or symbols
    NAME_LOOKS_LIKE_AMOUNT,     // OCR misread name as number

    // Phone rules
    PHONE_INVALID_FORMAT,       // Not matching PK pattern
    PHONE_TOO_SHORT,
    PHONE_TOO_LONG,

    // Notes/Description rules
    NOTES_TOO_LONG,             // > 500 chars
    DESCRIPTION_LOOKS_LIKE_AMOUNT,  // OCR injected number into description

    // Transaction rules
    TRANSACTION_DUPLICATE,      // Same customer + amount + date within 5 min
    TRANSACTION_IMPOSSIBLE,     // Payment > current balance (by huge margin)
    TRANSACTION_SAME_DAY_EXCESSIVE, // > 20 transactions same day for same customer

    // OCR rules
    OCR_LOW_CONFIDENCE,         // ML Kit confidence < 0.5
    OCR_NO_AMOUNT_FOUND,        // Couldn't extract any amount
    OCR_AMBIGUOUS_CUSTOMER,     // Multiple similar names detected
    OCR_TEXT_TOO_SHORT,         // < 10 chars extracted
    OCR_GARBLED_TEXT,           // Mostly non-alphanumeric characters

    // Duplicate image rules
    IMAGE_DUPLICATE,            // Same image scanned before
    IMAGE_SIMILAR,              // > 90% perceptual hash match
    IMAGE_RECENTLY_SCANNED,     // Same image within 24 hours
    IMAGE_BLUR_OR_LOW_QUALITY   // Image too blurry for OCR
}

// ─── Composite validation result ──────────────────────────────────

/**
 * Complete validation result for a single entity or operation.
 *
 * @param isValid    True if no ERRORs (warnings are OK)
 * @param errors     All field-level errors and warnings
 * @param warnings   Shortcut — only WARNING severity items
 * @param criticals  Shortcut — only ERROR severity items
 */
data class ValidationResult(
    val isValid: Boolean = true,
    val errors: List<FieldError> = emptyList()
) {
    val warnings: List<FieldError>
        get() = errors.filter { it.severity == Severity.WARNING }

    val criticals: List<FieldError>
        get() = errors.filter { it.severity == Severity.ERROR }

    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()

    val hasCriticals: Boolean
        get() = criticals.isNotEmpty()

    /**
     * Get the first error for a specific field.
     * Useful for showing inline field-level messages.
     */
    fun errorForField(field: String): FieldError? =
        errors.firstOrNull { it.field == field && it.severity == Severity.ERROR }

    fun warningForField(field: String): FieldError? =
        errors.firstOrNull { it.field == field && it.severity == Severity.WARNING }

    fun allErrorsForField(field: String): List<FieldError> =
        errors.filter { it.field == field }

    /**
     * Merge two validation results.
     * Invalid if either is invalid.
     */
    operator fun plus(other: ValidationResult): ValidationResult {
        return ValidationResult(
            isValid = this.isValid && other.isValid,
            errors = this.errors + other.errors
        )
    }

    companion object {
        fun success() = ValidationResult(isValid = true, errors = emptyList())

        fun error(field: String, rule: ValidationRule, message: String) =
            ValidationResult(
                isValid = false,
                errors = listOf(FieldError(field, rule, message, Severity.ERROR))
            )

        fun warning(field: String, rule: ValidationRule, message: String) =
            ValidationResult(
                isValid = true,
                errors = listOf(FieldError(field, rule, message, Severity.WARNING))
            )

        fun of(vararg errors: FieldError): ValidationResult {
            val hasCritical = errors.any { it.severity == Severity.ERROR }
            return ValidationResult(
                isValid = !hasCritical,
                errors = errors.toList()
            )
        }
    }
}

// ─── Duplicate detection result ───────────────────────────────────

/**
 * Result of a duplicate check. Used by DuplicateDetectionEngine.
 */
sealed class DuplicateCheckResult {
    /** No duplicates found — safe to proceed */
    object NoDuplicate : DuplicateCheckResult()

    /** Exact duplicate found — same image/transaction */
    data class ExactDuplicate(
        val existingId: Long,
        val existingDate: Long,
        val matchType: DuplicateMatchType,
        val message: String
    ) : DuplicateCheckResult()

    /** Potential duplicate — similar but not identical */
    data class PotentialDuplicate(
        val existingId: Long,
        val existingDate: Long,
        val similarityScore: Float, // 0.0 - 1.0
        val matchType: DuplicateMatchType,
        val message: String
    ) : DuplicateCheckResult()

    val isDuplicate: Boolean
        get() = this !is NoDuplicate

    val isExact: Boolean
        get() = this is ExactDuplicate
}

enum class DuplicateMatchType {
    IMAGE_HASH,           // Same image file hash
    IMAGE_PERCEPTUAL,     // Visually similar image
    TRANSACTION_FIELDS,   // Same customer + amount + date
    OCR_TEXT,             // Same OCR text output
    IMAGE_TIME_WINDOW     // Same image within time window
}
