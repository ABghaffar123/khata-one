package com.khatabook.app.domain.validation

/**
 * ═══════════════════════════════════════════════════════════════════
 * VALIDATION USE CASES — Pre-save validation pipeline
 * ═══════════════════════════════════════════════════════════════════
 *
 * Each use case orchestrates:
 *   1. Field validation (FieldValidators)
 *   2. Duplicate detection (DuplicateDetectionEngine)
 *   3. Business rule checks
 *
 * Returns a composite PreSaveResult that the UI maps to:
 *   - RED banner:   Critical errors, cannot save
 *   - YELLOW banner: Warnings, can override
 *   - GREEN:        All clear, safe to save
 */

// ═══════════════════════════════════════════════════════════════════
// COMPOSITE RESULT
// ═══════════════════════════════════════════════════════════════════

/**
 * Complete pre-save validation result.
 *
 * @param canSave          True if no critical errors
 * @param fieldValidation  Field-level validation result
 * @param duplicateCheck   Duplicate detection result
 * @param ocrFlags         OCR-specific flags (if from OCR scan)
 * @param uiRecommendation What the UI should show
 */
data class PreSaveResult(
    val canSave: Boolean,
    val fieldValidation: ValidationResult = ValidationResult.success(),
    val duplicateCheck: DuplicateCheckResult = DuplicateCheckResult.NoDuplicate,
    val ocrFlags: List<OcrMistakeHandler.OcrFlag> = emptyList(),
    val uiRecommendation: UiRecommendation = UiRecommendation.SAVE
) {
    val hasCriticalErrors: Boolean
        get() = !canSave

    val hasWarnings: Boolean
        get() = fieldValidation.hasWarnings || duplicateCheck is DuplicateCheckResult.PotentialDuplicate

    val hasDuplicates: Boolean
        get() = duplicateCheck.isDuplicate

    val shouldShowReviewScreen: Boolean
        get() = hasCriticalErrors || hasWarnings || hasDuplicates || ocrFlags.isNotEmpty()
}

enum class UiRecommendation {
    SAVE,               // Green — all clear
    SHOW_WARNING,       // Yellow — warnings exist, user can override
    SHOW_DUPLICATE,     // Orange — duplicate found, ask user
    BLOCK_SAVE,         // Red — critical errors, must fix
    ASK_RECAPTURE       // Purple — image too poor, retake photo
}


// ═══════════════════════════════════════════════════════════════════
// USE CASE 1: Validate Customer Before Save
// ═══════════════════════════════════════════════════════════════════

class ValidateCustomerUseCase {

    /**
     * Validate a customer before saving.
     *
     * Checks:
     *   - Name is valid
     *   - Phone format is correct
     *   - Notes are within limits
     *   - No duplicate customer with same name+phone
     *
     * @param name       Customer name (required)
     * @param phone      Phone number (optional)
     * @param notes      Notes (optional)
     * @param existingNames  Names of existing customers (for duplicate check)
     * @param existingPhones Phones of existing customers (for duplicate check)
     * @param editingCustomerId  If editing, exclude this customer from duplicate check
     */
    operator fun invoke(
        name: String,
        phone: String = "",
        notes: String = "",
        existingNames: List<String> = emptyList(),
        existingPhones: List<String> = emptyList(),
        editingCustomerId: Long? = null
    ): PreSaveResult {
        // Step 1: Field validation
        val fieldResult = FieldValidators.validateCustomer(name, phone, notes)

        // Step 2: Duplicate name check
        val normalizedInput = name.trim().lowercase()
        val isDuplicateName = existingNames.any { existing ->
            existing.lowercase() == normalizedInput
        }

        // Step 3: Duplicate phone check (if phone provided)
        val normalizedPhone = phone.replace(Regex("[\\s\\-]"), "")
        val isDuplicatePhone = phone.isNotBlank() && existingPhones.any { existing ->
            existing.replace(Regex("[\\s\\-]"), "") == normalizedPhone
        }

        // Step 4: Combine results
        var combinedValidation = fieldResult

        if (isDuplicateName) {
            combinedValidation = combinedValidation + ValidationResult.warning(
                "name",
                ValidationRule.TRANSACTION_DUPLICATE,
                "A customer with this name already exists. Use a different name or verify it's the same person."
            )
        }

        if (isDuplicatePhone) {
            combinedValidation = combinedValidation + ValidationResult.error(
                "phone",
                ValidationRule.TRANSACTION_DUPLICATE,
                "A customer with this phone number already exists."
            )
        }

        val recommendation = when {
            combinedValidation.hasCriticals -> UiRecommendation.BLOCK_SAVE
            combinedValidation.hasWarnings -> UiRecommendation.SHOW_WARNING
            else -> UiRecommendation.SAVE
        }

        return PreSaveResult(
            canSave = combinedValidation.isValid,
            fieldValidation = combinedValidation,
            uiRecommendation = recommendation
        )
    }
}


// ═══════════════════════════════════════════════════════════════════
// USE CASE 2: Validate Transaction Before Save
// ═══════════════════════════════════════════════════════════════════

class ValidateTransactionUseCase(
    private val duplicateEngine: DuplicateDetectionEngine
) {

    /**
     * Validate a transaction before saving.
     *
     * Checks:
     *   - Amount is valid and reasonable
     *   - Date is valid
     *   - Customer name is valid
     *   - No duplicate transaction
     *   - Business rules (payment vs balance)
     */
    operator fun invoke(
        amount: String,
        date: String,
        customerName: String,
        customerId: Long,
        description: String = "",
        transactionType: String = "CREDIT",
        currentBalance: Double = 0.0
    ): PreSaveResult {
        // Step 1: Field validation
        val fieldResult = FieldValidators.validateTransaction(
            amount = amount,
            date = date,
            customerName = customerName,
            description = description,
            transactionType = transactionType,
            currentBalance = currentBalance
        )

        // Step 2: Duplicate detection
        val parsedAmount = amount.replace(Regex("[,\\s]"), "").toDoubleOrNull()
        val parsedDate = parseDate(date)

        val duplicateResult = if (parsedAmount != null && parsedDate != null) {
            duplicateEngine.checkTransaction(
                customerId = customerId,
                amount = parsedAmount,
                date = parsedDate,
                description = description,
                transactionType = transactionType
            )
        } else {
            DuplicateCheckResult.NoDuplicate
        }

        // Step 3: Combine
        val hasCritical = !fieldResult.isValid || duplicateResult is DuplicateCheckResult.ExactDuplicate
        val hasWarning = fieldResult.hasWarnings || duplicateResult is DuplicateCheckResult.PotentialDuplicate

        val recommendation = when {
            hasCritical -> UiRecommendation.BLOCK_SAVE
            duplicateResult is DuplicateCheckResult.PotentialDuplicate ->
                UiRecommendation.SHOW_DUPLICATE
            hasWarning -> UiRecommendation.SHOW_WARNING
            else -> UiRecommendation.SAVE
        }

        return PreSaveResult(
            canSave = !hasCritical,
            fieldValidation = fieldResult,
            duplicateCheck = duplicateResult,
            uiRecommendation = recommendation
        )
    }

    private fun parseDate(dateStr: String): Long? {
        return try {
            val cleaned = dateStr.trim().replace(Regex("[/\\-\\.]"), "/")
            val formats = listOf("dd/MM/yyyy", "dd/MM/yy", "d/M/yyyy")
            for (format in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(format, com.khatabook.app.util.PkDateTime.PKT_LOCALE)
                    sdf.timeZone = com.khatabook.app.util.PkDateTime.PKT_TIMEZONE
                    sdf.isLenient = false
                    sdf.parse(cleaned)?.time
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) { null }
    }
}


// ═══════════════════════════════════════════════════════════════════
// USE CASE 3: Validate OCR Scan Before Save
// ═══════════════════════════════════════════════════════════════════

class ValidateOcrScanUseCase(
    private val ocrHandler: OcrMistakeHandler,
    private val duplicateEngine: DuplicateDetectionEngine
) {

    /**
     * Complete OCR validation pipeline.
     *
     * Steps:
     *   1. Check if image was scanned before
     *   2. Process OCR text for errors
     *   3. Validate extracted entries
     *   4. Check for duplicates in extracted data
     *   5. Recommend action
     *
     * @param rawText         ML Kit extracted text
     * @param mlKitConfidence ML Kit reported confidence
     * @param imageHash       SHA-256 of the captured image
     * @param pHash           Perceptual hash (optional)
     * @return PreSaveResult with OCR-specific flags
     */
    operator fun invoke(
        rawText: String,
        mlKitConfidence: Float,
        imageHash: String,
        pHash: Long? = null
    ): PreSaveResult {
        val allFlags = mutableListOf<OcrMistakeHandler.OcrFlag>()

        // Step 1: Image duplicate check
        val imageDuplicate = duplicateEngine.checkImage(imageHash, pHash)
        if (imageDuplicate.isDuplicate) {
            return PreSaveResult(
                canSave = imageDuplicate.isExact,
                duplicateCheck = imageDuplicate,
                uiRecommendation = if (imageDuplicate.isExact) {
                    UiRecommendation.BLOCK_SAVE
                } else {
                    UiRecommendation.SHOW_DUPLICATE
                }
            )
        }

        // Step 2: Process OCR text
        val ocrResult = ocrHandler.processOcrText(rawText, mlKitConfidence)
        allFlags.addAll(ocrResult.entries.flatMap { it.flags })

        // Step 3: Check if recapture is needed
        if (ocrResult.recommendation == OcrMistakeHandler.ProcessingRecommendation.RECAPTURE) {
            return PreSaveResult(
                canSave = false,
                ocrFlags = allFlags,
                uiRecommendation = UiRecommendation.ASK_RECAPTURE
            )
        }

        // Step 4: Check extracted entries for duplicates
        val duplicateFlags = mutableListOf<DuplicateCheckResult>()
        for (entry in ocrResult.entries) {
            val amount = entry.correctedAmount.toDoubleOrNull()
            if (amount != null) {
                // Check OCR text duplicate
                val textDup = duplicateEngine.checkOcrText(entry.rawText)
                if (textDup.isDuplicate) {
                    duplicateFlags.add(textDup)
                }
            }
        }

        // Step 5: Determine recommendation
        val hasCritical = ocrResult.recommendation ==
                OcrMistakeHandler.ProcessingRecommendation.MANUAL_ENTRY
        val hasWarnings = ocrResult.recommendation ==
                OcrMistakeHandler.ProcessingRecommendation.REVIEW_REQUIRED

        val recommendation = when {
            hasCritical -> UiRecommendation.BLOCK_SAVE
            hasWarnings -> UiRecommendation.SHOW_WARNING
            ocrFlags.isNotEmpty() -> UiRecommendation.SHOW_WARNING
            duplicateFlags.isNotEmpty() -> UiRecommendation.SHOW_DUPLICATE
            else -> UiRecommendation.SAVE
        }

        return PreSaveResult(
            canSave = !hasCritical,
            fieldValidation = ValidationResult(
                isValid = !hasCritical,
                errors = ocrResult.entries.flatMap { entry ->
                    entry.flags.map { flag ->
                        FieldError(
                            field = "ocr",
                            rule = when (flag) {
                                is OcrMistakeHandler.OcrFlag.LowConfidence ->
                                    ValidationRule.OCR_LOW_CONFIDENCE
                                is OcrMistakeHandler.OcrFlag.AmountAmbiguous ->
                                    ValidationRule.OCR_NO_AMOUNT_FOUND
                                is OcrMistakeHandler.OcrFlag.NameLooksLikeNumber ->
                                    ValidationRule.NAME_LOOKS_LIKE_AMOUNT
                                is OcrMistakeHandler.OcrFlag.DateUnparseable ->
                                    ValidationRule.DATE_INVALID_FORMAT
                                is OcrMistakeHandler.OcrFlag.MultipleAmounts ->
                                    ValidationRule.OCR_AMBIGUOUS_CUSTOMER
                                is OcrMistakeHandler.OcrFlag.NoCustomerName ->
                                    ValidationRule.OCR_AMBIGUOUS_CUSTOMER
                                is OcrMistakeHandler.OcrFlag.TextGarbled ->
                                    ValidationRule.OCR_GARBLED_TEXT
                                is OcrMistakeHandler.OcrFlag.UrduMixedWithEnglish ->
                                    ValidationRule.OCR_LOW_CONFIDENCE
                                is OcrMistakeHandler.OcrFlag.CharacterCorrection ->
                                    ValidationRule.OCR_LOW_CONFIDENCE
                            },
                            message = "OCR: ${flag::class.simpleName}",
                            severity = Severity.WARNING
                        )
                    }
                }
            ),
            ocrFlags = allFlags,
            uiRecommendation = recommendation
        )
    }
}


// ═══════════════════════════════════════════════════════════════════
// USE CASE 4: Validate Image Before OCR Processing
// ═══════════════════════════════════════════════════════════════════

class ValidateImageUseCase(
    private val duplicateEngine: DuplicateDetectionEngine
) {

    /**
     * Validate an image before sending to ML Kit.
     *
     * Checks:
     *   - Image file hash doesn't match recent scans
     *   - Image is not corrupted (basic size check)
     *   - Image dimensions are reasonable for OCR
     *
     * @param imageBytes   Raw image bytes
     * @param width        Image width in pixels
     * @param height       Image height in pixels
     * @param fileSize     File size in bytes
     * @return PreSaveResult
     */
    operator fun invoke(
        imageBytes: ByteArray,
        width: Int = 0,
        height: Int = 0,
        fileSize: Long = 0
    ): PreSaveResult {
        val flags = mutableListOf<OcrMistakeHandler.OcrFlag>()

        // Check 1: File size sanity
        if (fileSize < 10_000) { // < 10KB is likely corrupted
            return PreSaveResult(
                canSave = false,
                uiRecommendation = UiRecommendation.ASK_RECAPTURE
            )
        }

        if (fileSize > 20_000_000) { // > 20MB is too large
            return PreSaveResult(
                canSave = false,
                uiRecommendation = UiRecommendation.ASK_RECAPTURE
            )
        }

        // Check 2: Image hash duplicate
        val fileHash = duplicateEngine.computeFileHash(imageBytes)
        val pHash = if (width >= 64 && height >= 64) {
            // Only compute pHash if image is large enough
            val pixels = IntArray(width * height)
            // Extract pixels from imageBytes (simplified — real impl uses Bitmap)
            duplicateEngine.computePerceptualHash(pixels, width, height)
        } else {
            null
        }

        val imageResult = duplicateEngine.checkImage(fileHash, pHash)
        if (imageResult.isExact) {
            return PreSaveResult(
                canSave = false,
                duplicateCheck = imageResult,
                uiRecommendation = UiRecommendation.BLOCK_SAVE
            )
        }

        if (imageResult is DuplicateCheckResult.PotentialDuplicate) {
            return PreSaveResult(
                canSave = true,
                duplicateCheck = imageResult,
                uiRecommendation = UiRecommendation.SHOW_DUPLICATE
            )
        }

        // Check 3: Dimensions too small for OCR
        if (width > 0 && height > 0 && (width < 200 || height < 200)) {
            return PreSaveResult(
                canSave = false,
                uiRecommendation = UiRecommendation.ASK_RECAPTURE
            )
        }

        return PreSaveResult(canSave = true, uiRecommendation = UiRecommendation.SAVE)
    }
}
