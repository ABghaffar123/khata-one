package com.khatabook.app.domain.validation

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ═══════════════════════════════════════════════════════════════════
 * FIELD VALIDATORS TEST SUITE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Covers all field validators with happy paths, edge cases,
 * error conditions, and OCR artifact cleaning.
 *
 * Test count: 42 tests
 */
class FieldValidatorsTest {

    // ═══════════════════════════════════════════════════════════════
    // AMOUNT VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `amount - valid whole number`() {
        val result = FieldValidators.validateAmount("5000")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `amount - valid with commas`() {
        val result = FieldValidators.validateAmount("5,000")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - valid decimal`() {
        val result = FieldValidators.validateAmount("123.45")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - OCR artifact cleaned (letter O as zero)`() {
        val result = FieldValidators.validateAmount("1,O3O")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - OCR artifact cleaned (lowercase l as one)`() {
        val result = FieldValidators.validateAmount("1l50")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - OCR artifact cleaned (S as five)`() {
        val result = FieldValidators.validateAmount("5S00")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - with Rs prefix`() {
        val result = FieldValidators.validateAmount("Rs 5000")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - with PKR prefix`() {
        val result = FieldValidators.validateAmount("PKR 5000")
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - empty returns error when required`() {
        val result = FieldValidators.validateAmount("", isRequired = true)
        assertFalse(result.isValid)
        assertEquals(ValidationRule.AMOUNT_EMPTY, result.criticals.first().rule)
    }

    @Test
    fun `amount - empty returns success when not required`() {
        val result = FieldValidators.validateAmount("", isRequired = false)
        assertTrue(result.isValid)
    }

    @Test
    fun `amount - zero returns error`() {
        val result = FieldValidators.validateAmount("0")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.AMOUNT_ZERO, result.criticals.first().rule)
    }

    @Test
    fun `amount - negative returns error`() {
        val result = FieldValidators.validateAmount("-500")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.AMOUNT_NEGATIVE, result.criticals.first().rule)
    }

    @Test
    fun `amount - too large returns error`() {
        val result = FieldValidators.validateAmount("100000000")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.AMOUNT_TOO_LARGE, result.criticals.first().rule)
    }

    @Test
    fun `amount - invalid format returns error`() {
        val result = FieldValidators.validateAmount("abc")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.AMOUNT_INVALID_FORMAT, result.criticals.first().rule)
    }

    @Test
    fun `amount - mixed invalid format`() {
        val result = FieldValidators.validateAmount("12abc")
        assertFalse(result.isValid)
    }

    @Test
    fun `amount - unusual decimal precision returns warning`() {
        val result = FieldValidators.validateAmount("123.4567")
        assertTrue(result.isValid) // Warning, not error
        assertTrue(result.hasWarnings)
        assertEquals(ValidationRule.AMOUNT_DECIMAL_PRECISION, result.warnings.first().rule)
    }


    // ═══════════════════════════════════════════════════════════════
    // DATE VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `date - valid DD/MM/YYYY`() {
        val result = FieldValidators.validateDate("15/08/2026")
        assertTrue(result.isValid)
    }

    @Test
    fun `date - valid DD-MM-YYYY`() {
        val result = FieldValidators.validateDate("15-08-2026")
        assertTrue(result.isValid)
    }

    @Test
    fun `date - valid DD.MM.YYYY`() {
        val result = FieldValidators.validateDate("15.08.2026")
        assertTrue(result.isValid)
    }

    @Test
    fun `date - valid DD/MM/YY`() {
        val result = FieldValidators.validateDate("15/08/26")
        assertTrue(result.isValid)
    }

    @Test
    fun `date - empty returns error when required`() {
        val result = FieldValidators.validateDate("", isRequired = true)
        assertFalse(result.isValid)
        assertEquals(ValidationRule.DATE_EMPTY, result.criticals.first().rule)
    }

    @Test
    fun `date - empty returns success when not required`() {
        val result = FieldValidators.validateDate("", isRequired = false)
        assertTrue(result.isValid)
    }

    @Test
    fun `date - invalid format returns error`() {
        val result = FieldValidators.validateDate("32/13/2026")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.DATE_INVALID_FORMAT, result.criticals.first().rule)
    }

    @Test
    fun `date - garbage text returns error`() {
        val result = FieldValidators.validateDate("hello world")
        assertFalse(result.isValid)
    }

    @Test
    fun `date - future date returns warning`() {
        val result = FieldValidators.validateDate("01/01/2099")
        assertTrue(result.isValid) // Warning
        assertTrue(result.hasWarnings)
        assertEquals(ValidationRule.DATE_IN_FUTURE, result.warnings.first().rule)
    }

    @Test
    fun `date - very old date returns warning`() {
        val result = FieldValidators.validateDate("01/01/2020")
        assertTrue(result.isValid) // Warning
        assertTrue(result.hasWarnings)
        assertEquals(ValidationRule.DATE_TOO_OLD, result.warnings.first().rule)
    }

    @Test
    fun `date - today is valid`() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
        val result = FieldValidators.validateDate(today)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }


    // ═══════════════════════════════════════════════════════════════
    // NAME VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `name - valid name`() {
        val result = FieldValidators.validateName("Ahmed Khan")
        assertTrue(result.isValid)
    }

    @Test
    fun `name - valid Urdu name`() {
        val result = FieldValidators.validateName("محمد علی")
        assertTrue(result.isValid)
    }

    @Test
    fun `name - valid with hyphen`() {
        val result = FieldValidators.validateName("Ali-Khan")
        assertTrue(result.isValid)
    }

    @Test
    fun `name - empty returns error when required`() {
        val result = FieldValidators.validateName("", isRequired = true)
        assertFalse(result.isValid)
        assertEquals(ValidationRule.NAME_EMPTY, result.criticals.first().rule)
    }

    @Test
    fun `name - empty returns success when not required`() {
        val result = FieldValidators.validateName("", isRequired = false)
        assertTrue(result.isValid)
    }

    @Test
    fun `name - too short returns error`() {
        val result = FieldValidators.validateName("A")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.NAME_TOO_SHORT, result.criticals.first().rule)
    }

    @Test
    fun `name - only numbers returns error`() {
        val result = FieldValidators.validateName("12345")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.NAME_INVALID_CHARS, result.criticals.first().rule)
    }

    @Test
    fun `name - only symbols returns error`() {
        val result = FieldValidators.validateName("@#$%")
        assertFalse(result.isValid)
    }

    @Test
    fun `name - looks like amount returns warning`() {
        val result = FieldValidators.validateName("5000")
        assertTrue(result.isValid) // Warning
        assertTrue(result.hasWarnings)
    }

    @Test
    fun `name - whitespace trimmed`() {
        val result = FieldValidators.validateName("  Ahmed  ")
        assertTrue(result.isValid)
    }


    // ═══════════════════════════════════════════════════════════════
    // PHONE VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `phone - valid Pakistani format`() {
        val result = FieldValidators.validatePhone("0300-1234567")
        assertTrue(result.isValid)
    }

    @Test
    fun `phone - valid with +92`() {
        val result = FieldValidators.validatePhone("+923001234567")
        assertTrue(result.isValid)
    }

    @Test
    fun `phone - empty is optional`() {
        val result = FieldValidators.validatePhone("")
        assertTrue(result.isValid)
    }

    @Test
    fun `phone - too short returns error`() {
        val result = FieldValidators.validatePhone("123")
        assertFalse(result.isValid)
        assertEquals(ValidationRule.PHONE_TOO_SHORT, result.criticals.first().rule)
    }

    @Test
    fun `phone - invalid format returns warning`() {
        val result = FieldValidators.validatePhone("1234567890")
        assertTrue(result.isValid) // Warning
        assertTrue(result.hasWarnings)
    }


    // ═══════════════════════════════════════════════════════════════
    // NOTES VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `notes - valid text`() {
        val result = FieldValidators.validateNotes("Will pay next week")
        assertTrue(result.isValid)
    }

    @Test
    fun `notes - empty is OK`() {
        val result = FieldValidators.validateNotes("")
        assertTrue(result.isValid)
    }

    @Test
    fun `notes - looks like amount returns warning`() {
        val result = FieldValidators.validateNotes("5000")
        assertTrue(result.isValid) // Warning
        assertTrue(result.hasWarnings)
    }


    // ═══════════════════════════════════════════════════════════════
    // COMPOSITE VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `customer validation - all valid`() {
        val result = FieldValidators.validateCustomer(
            name = "Ahmed Khan",
            phone = "0300-1234567",
            notes = "Regular customer"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `customer validation - invalid name blocks`() {
        val result = FieldValidators.validateCustomer(
            name = "",
            phone = "0300-1234567"
        )
        assertFalse(result.isValid)
    }

    @Test
    fun `transaction validation - all valid`() {
        val result = FieldValidators.validateTransaction(
            amount = "5000",
            date = "15/08/2026",
            customerName = "Ahmed Khan"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `transaction validation - empty amount blocks`() {
        val result = FieldValidators.validateTransaction(
            amount = "",
            date = "15/08/2026",
            customerName = "Ahmed Khan"
        )
        assertFalse(result.isValid)
    }

    @Test
    fun `validation result - plus operator merges`() {
        val r1 = ValidationResult.success()
        val r2 = ValidationResult.warning("field", ValidationRule.AMOUNT_DECIMAL_PRECISION, "msg")
        val combined = r1 + r2
        assertTrue(combined.isValid)
        assertTrue(combined.hasWarnings)
    }

    @Test
    fun `validation result - errorForField returns first error`() {
        val result = ValidationResult.of(
            FieldError("amount", ValidationRule.AMOUNT_EMPTY, "Amount required"),
            FieldError("amount", ValidationRule.AMOUNT_ZERO, "Cannot be zero"),
            FieldError("name", ValidationRule.NAME_EMPTY, "Name required")
        )
        val amountError = result.errorForField("amount")
        assertNotNull(amountError)
        assertEquals(ValidationRule.AMOUNT_EMPTY, amountError!!.rule)
    }

    @Test
    fun `validation result - allErrorsForField returns multiple`() {
        val result = ValidationResult.of(
            FieldError("amount", ValidationRule.AMOUNT_EMPTY, "Amount required"),
            FieldError("amount", ValidationRule.AMOUNT_ZERO, "Cannot be zero")
        )
        val errors = result.allErrorsForField("amount")
        assertEquals(2, errors.size)
    }
}
