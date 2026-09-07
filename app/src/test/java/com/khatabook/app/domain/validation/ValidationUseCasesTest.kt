package com.khatabook.app.domain.validation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════════════
 * VALIDATION USE CASES TEST SUITE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Tests the orchestration layer that combines field validation,
 * duplicate detection, and OCR handling.
 *
 * Test count: 20 tests
 */
class ValidationUseCasesTest {

    private val now = System.currentTimeMillis()
    private lateinit var validateCustomer: ValidateCustomerUseCase
    private lateinit var validateTransaction: ValidateTransactionUseCase
    private lateinit var validateOcrScan: ValidateOcrScanUseCase
    private lateinit var duplicateEngine: DuplicateDetectionEngine
    private lateinit var ocrHandler: OcrMistakeHandler

    @Before
    fun setup() {
        validateCustomer = ValidateCustomerUseCase()
        duplicateEngine = DuplicateDetectionEngine()
        validateTransaction = ValidateTransactionUseCase(duplicateEngine)
        ocrHandler = OcrMistakeHandler()
        validateOcrScan = ValidateOcrScanUseCase(ocrHandler, duplicateEngine)
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATE CUSTOMER USE CASE
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `validateCustomer - valid data returns save`() {
        val result = validateCustomer(
            name = "Ahmed Khan",
            phone = "0300-1234567"
        )
        assertTrue(result.canSave)
        assertEquals(UiRecommendation.SAVE, result.uiRecommendation)
    }

    @Test
    fun `validateCustomer - empty name blocks save`() {
        val result = validateCustomer(name = "")
        assertFalse(result.canSave)
        assertEquals(UiRecommendation.BLOCK_SAVE, result.uiRecommendation)
    }

    @Test
    fun `validateCustomer - duplicate name warns`() {
        val result = validateCustomer(
            name = "Ahmed Khan",
            existingNames = listOf("Ahmed Khan", "Ali Khan")
        )
        assertTrue(result.canSave) // Warning, not error
        assertEquals(UiRecommendation.SHOW_WARNING, result.uiRecommendation)
    }

    @Test
    fun `validateCustomer - duplicate phone blocks`() {
        val result = validateCustomer(
            name = "New Customer",
            phone = "0300-1234567",
            existingPhones = listOf("0300-1234567")
        )
        assertFalse(result.canSave)
    }

    @Test
    fun `validateCustomer - no existing customers returns save`() {
        val result = validateCustomer(
            name = "First Customer",
            phone = "0300-1234567"
        )
        assertTrue(result.canSave)
    }


    // ═══════════════════════════════════════════════════════════════
    // VALIDATE TRANSACTION USE CASE
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `validateTransaction - valid data returns save`() {
        val result = validateTransaction(
            amount = "5000",
            date = "15/08/2026",
            customerName = "Ahmed Khan",
            customerId = 1
        )
        assertTrue(result.canSave)
        assertEquals(UiRecommendation.SAVE, result.uiRecommendation)
    }

    @Test
    fun `validateTransaction - empty amount blocks`() {
        val result = validateTransaction(
            amount = "",
            date = "15/08/2026",
            customerName = "Ahmed Khan",
            customerId = 1
        )
        assertFalse(result.canSave)
    }

    @Test
    fun `validateTransaction - duplicate detected`() {
        // Create engine with existing transaction
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val validator = ValidateTransactionUseCase(engine)

        val result = validator(
            amount = "5000",
            date = "15/08/2026",
            customerName = "Ahmed Khan",
            customerId = 1
        )
        assertTrue(result.hasDuplicates)
    }

    @Test
    fun `validateTransaction - future date warns`() {
        val result = validateTransaction(
            amount = "5000",
            date = "01/01/2099",
            customerName = "Ahmed Khan",
            customerId = 1
        )
        assertTrue(result.canSave) // Warning
        assertTrue(result.hasWarnings)
    }

    @Test
    fun `validateTransaction - payment much larger than balance warns`() {
        val result = validateTransaction(
            amount = "50000",
            date = "15/08/2026",
            customerName = "Ahmed Khan",
            customerId = 1,
            transactionType = "PAYMENT",
            currentBalance = 5000.0
        )
        assertTrue(result.canSave) // Warning
        assertTrue(result.hasWarnings)
    }


    // ═══════════════════════════════════════════════════════════════
    // VALIDATE OCR SCAN USE CASE
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `validateOcrScan - empty text returns recapture`() {
        val result = validateOcrScan(
            rawText = "",
            mlKitConfidence = 0.8f,
            imageHash = "abc123"
        )
        assertEquals(UiRecommendation.ASK_RECAPTURE, result.uiRecommendation)
    }

    @Test
    fun `validateOcrScan - duplicate image blocks`() {
        val engine = DuplicateDetectionEngine(
            existingImageHashes = listOf(
                ImageHashRecord(1, "abc123", null, now)
            )
        )
        val validator = ValidateOcrScanUseCase(ocrHandler, engine)

        val result = validator(
            rawText = "Ahmed 5000",
            mlKitConfidence = 0.9f,
            imageHash = "abc123"
        )
        assertFalse(result.canSave)
    }

    @Test
    fun `validateOcrScan - valid text with high confidence saves`() {
        val result = validateOcrScan(
            rawText = "Ahmed Khan 15/08/2026 Rice 5000",
            mlKitConfidence = 0.95f,
            imageHash = "unique_hash_123"
        )
        // Should be save or review depending on OCR flags
        assertTrue(result.canSave)
    }

    @Test
    fun `validateOcrScan - low confidence text warns`() {
        val result = validateOcrScan(
            rawText = "12345 67890 11111 22222", // Garbled/ambiguous
            mlKitConfidence = 0.3f,
            imageHash = "unique_hash_456"
        )
        // Low confidence should trigger review or block
        assertFalse(result.canSave) // MANUAL_ENTRY recommendation
    }

    @Test
    fun `validateOcrScan - garbled text recommends recapture`() {
        val result = validateOcrScan(
            rawText = "!@#$%^&*()_+{}|:<>?abc",
            mlKitConfidence = 0.2f,
            imageHash = "unique_hash_789"
        )
        assertEquals(UiRecommendation.ASK_RECAPTURE, result.uiRecommendation)
    }


    // ═══════════════════════════════════════════════════════════════
    // PRE-SAVE RESULT COMPOSITION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `preSaveResult - hasCriticalErrors matches canSave inverse`() {
        val result = PreSaveResult(
            canSave = false,
            fieldValidation = ValidationResult.error(
                "amount", ValidationRule.AMOUNT_EMPTY, "Amount required"
            )
        )
        assertTrue(result.hasCriticalErrors)
    }

    @Test
    fun `preSaveResult - hasWarnings when warnings exist`() {
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Future date"
            )
        )
        assertTrue(result.hasWarnings)
    }

    @Test
    fun `preSaveResult - shouldShowReviewScreen when errors or warnings`() {
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Future date"
            )
        )
        assertTrue(result.shouldShowReviewScreen)
    }

    @Test
    fun `preSaveResult - no issues returns save`() {
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.success(),
            duplicateCheck = DuplicateCheckResult.NoDuplicate,
            uiRecommendation = UiRecommendation.SAVE
        )
        assertFalse(result.shouldShowReviewScreen)
        assertEquals(UiRecommendation.SAVE, result.uiRecommendation)
    }
}
