package com.khatabook.app.util

/**
 * Shared form validation utility for all forms.
 * Returns error message string if invalid, null if valid.
 */
object FormValidator {

    // ═══════════════════ REQUIRED FIELDS ═══════════════════

    fun required(value: String?, fieldName: String = "This field"): String? {
        return if (value.isNullOrBlank()) "$fieldName is required" else null
    }

    fun requiredAmount(value: String?): String? {
        if (value.isNullOrBlank()) return "Amount is required"
        val amount = value.toDoubleOrNull()
        if (amount == null) return "Enter a valid amount"
        if (amount <= 0) return "Amount must be greater than 0"
        if (amount > 10_000_000) return "Amount is too large"
        return null
    }

    // ═══════════════════ PHONE VALIDATION ═══════════════════

    fun phone(value: String?): String? {
        if (value.isNullOrBlank()) return "Phone number is required"
        val normalized = value.replace(Regex("[^\\d+]"), "")
        return when {
            normalized.startsWith("+92") && normalized.length == 13 -> null
            normalized.startsWith("03") && normalized.length == 11 -> null
            else -> "Enter a valid phone number (e.g. 03001234567)"
        }
    }

    fun phoneOptional(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return phone(value)
    }

    // ═══════════════════ NAME VALIDATION ═══════════════════

    fun name(value: String?): String? {
        if (value.isNullOrBlank()) return "Name is required"
        if (value.trim().length < 2) return "Name must be at least 2 characters"
        return null
    }

    // ═══════════════════ QUANTITY / STOCK ═══════════════════

    fun quantity(value: String?, allowDecimal: Boolean = false): String? {
        if (value.isNullOrBlank()) return "Quantity is required"
        val qty = value.toDoubleOrNull()
        if (qty == null) return "Enter a valid number"
        if (qty < 0) return "Quantity cannot be negative"
        if (!allowDecimal && qty != qty.toLong().toDouble()) return "Quantity must be a whole number"
        return null
    }

    // ═══════════════════ PRICE VALIDATION ═══════════════════

    fun price(value: String?, required: Boolean = true): String? {
        if (value.isNullOrBlank()) return if (required) "Price is required" else null
        val price = value.toDoubleOrNull()
        if (price == null) return "Enter a valid price"
        if (price < 0) return "Price cannot be negative"
        return null
    }

    // ═══════════════════ PRODUCT NAME ═══════════════════

    fun productName(value: String?): String? {
        if (value.isNullOrBlank()) return "Product name is required"
        if (value.trim().length < 2) return "Product name must be at least 2 characters"
        return null
    }

    // ═══════════════════ COMBINED VALIDATION ═══════════════════

    /**
     * Validates multiple fields and returns the first error, or null if all valid.
     */
    fun validateAll(vararg results: String?): String? {
        return results.firstOrNull { it != null }
    }
}
