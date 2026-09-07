package com.khatabook.app.domain.validation

/**
 * ═══════════════════════════════════════════════════════════════════
 * PRE-SAVE REVIEW MANAGER — Coordinates the validation → save flow
 * ═══════════════════════════════════════════════════════════════════
 *
 * State machine:
 *
 *   IDLE → VALIDATING → REVIEW_SCREEN → CONFIRMED → SAVING → SAVED
 *                     ↘ BLOCKED (errors)
 *                     ↘ DUPLICATE_WARN → CONFIRMED or CANCELLED
 *                     ↘ OCR_REVIEW → EDITED → CONFIRMED
 *
 * This manager is used by ViewModels to drive the review screen.
 * It does NOT touch Android or Room — pure Kotlin state machine.
 */

class PreSaveReviewManager {

    // ═══════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════

    private var currentState: ReviewState = ReviewState.Idle
    private var currentResult: PreSaveResult? = null

    /**
     * Current review state.
     */
    sealed class ReviewState {
        /** No active review */
        object Idle : ReviewState()

        /** Validation in progress */
        object Validating : ReviewState()

        /** Validation complete, showing review screen */
        data class ReviewScreen(
            val result: PreSaveResult,
            val editableFields: MutableMap<String, String> = mutableMapOf()
        ) : ReviewState()

        /** Blocking errors found, cannot save */
        data class Blocked(
            val result: PreSaveResult
        ) : ReviewState()

        /** Duplicate warning shown, waiting for user decision */
        data class DuplicateWarning(
            val result: PreSaveResult,
            val duplicateResult: DuplicateCheckResult
        ) : ReviewState()

        /** OCR review screen shown */
        data class OcrReview(
            val result: PreSaveResult,
            val ocrEntries: List<OcrMistakeHandler.OcrEntry>
        ) : ReviewState()

        /** User confirmed, saving in progress */
        object Saving : ReviewState()

        /** Save complete */
        data class Saved(val recordId: Long) : ReviewState()

        /** User cancelled or save failed */
        data class Cancelled(val reason: String = "") : ReviewState()
    }

    /**
     * Get the current state.
     */
    fun getState(): ReviewState = currentState

    /**
     * Check if the review screen should be shown.
     */
    fun shouldShowReview(): Boolean = currentState is ReviewState.ReviewScreen ||
            currentState is ReviewState.DuplicateWarning ||
            currentState is ReviewState.OcrReview

    /**
     * Check if save is currently blocked.
     */
    fun isBlocked(): Boolean = currentState is ReviewState.Blocked

    /**
     * Get the current editable fields (user can modify before save).
     */
    fun getEditableFields(): Map<String, String> {
        return when (val state = currentState) {
            is ReviewState.ReviewScreen -> state.editableFields
            is ReviewState.OcrReview -> buildEditableMap(state.ocrEntries)
            else -> emptyMap()
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // STATE TRANSITIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Start validation. Called when user taps "Save".
     */
    fun startValidation(): ReviewState {
        currentState = ReviewState.Validating
        return currentState
    }

    /**
     * Receive validation result and transition to appropriate state.
     */
    fun onValidationComplete(result: PreSaveResult): ReviewState {
        currentResult = result

        currentState = when {
            // Blocking errors → show errors, block save
            result.hasCriticalErrors && !result.hasDuplicates -> {
                ReviewState.Blocked(result)
            }

            // Duplicate detected → show warning
            result.hasDuplicates -> {
                ReviewState.DuplicateWarning(
                    result = result,
                    duplicateResult = result.duplicateCheck
                )
            }

            // OCR flags exist → show OCR review
            result.ocrFlags.isNotEmpty() -> {
                ReviewState.OcrReview(
                    result = result,
                    ocrEntries = emptyList() // Populated by caller
                )
            }

            // Warnings exist → show review screen
            result.hasWarnings -> {
                ReviewState.ReviewScreen(
                    result = result,
                    editableFields = mutableMapOf()
                )
            }

            // All clear → skip review, save directly
            else -> {
                ReviewState.Saving
            }
        }

        return currentState
    }

    /**
     * User edits a field on the review screen.
     */
    fun onFieldEdited(field: String, value: String): ReviewState {
        when (val state = currentState) {
            is ReviewState.ReviewScreen -> {
                state.editableFields[field] = value
            }
            is ReviewState.OcrReview -> {
                // Update the editable map
            }
            else -> { /* Ignore edits in other states */ }
        }
        return currentState
    }

    /**
     * User confirms despite warnings.
     */
    fun onUserConfirm(): ReviewState {
        currentState = when (currentState) {
            is ReviewState.ReviewScreen -> ReviewState.Saving
            is ReviewState.DuplicateWarning -> ReviewState.Saving
            is ReviewState.OcrReview -> ReviewState.Saving
            else -> currentState
        }
        return currentState
    }

    /**
     * User overrides a specific warning.
     */
    fun onUserOverride(field: String, rule: ValidationRule): ReviewState {
        // Remove the specific warning from the result
        val updatedResult = currentResult?.let { result ->
            val updatedErrors = result.fieldValidation.errors.filter {
                !(it.field == field && it.rule == rule)
            }
            result.copy(
                fieldValidation = ValidationResult(
                    isValid = true, // User overrode
                    errors = updatedErrors
                )
            )
        }

        if (updatedResult != null) {
            currentResult = updatedResult
            if (updatedResult.fieldValidation.errors.isEmpty() && !updatedResult.hasDuplicates) {
                currentState = ReviewState.Saving
            }
        }

        return currentState
    }

    /**
     * User cancels the save.
     */
    fun onCancel(reason: String = "User cancelled"): ReviewState {
        currentState = ReviewState.Cancelled(reason)
        return currentState
    }

    /**
     * Save completed successfully.
     */
    fun onSaveComplete(recordId: Long): ReviewState {
        currentState = ReviewState.Saved(recordId)
        return currentState
    }

    /**
     * Save failed.
     */
    fun onSaveFailed(error: String): ReviewState {
        currentState = ReviewState.Cancelled("Save failed: $error")
        return currentState
    }

    /**
     * Reset to idle state.
     */
    fun reset(): ReviewState {
        currentState = ReviewState.Idle
        currentResult = null
        return currentState
    }


    // ═══════════════════════════════════════════════════════════════
    // UI HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get user-friendly message for current state.
     * Used by UI to show appropriate banner/text.
     */
    fun getStateMessage(): String {
        return when (val state = currentState) {
            is ReviewState.Idle -> ""
            is ReviewState.Validating -> "Checking data..."
            is ReviewState.ReviewScreen -> buildReviewMessage(state.result)
            is ReviewState.Blocked -> buildBlockedMessage(state.result)
            is ReviewState.DuplicateWarning -> buildDuplicateMessage(state.duplicateResult)
            is ReviewState.OcrReview -> "Review OCR results before saving"
            is ReviewState.Saving -> "Saving..."
            is ReviewState.Saved -> "Saved successfully!"
            is ReviewState.Cancelled -> state.reason
        }
    }

    /**
     * Get the appropriate button label for the review screen.
     */
    fun getActionButtonLabel(): String {
        return when (currentState) {
            is ReviewState.ReviewScreen -> "Save Anyway"
            is ReviewState.DuplicateWarning -> "Save Despite Duplicate"
            is ReviewState.OcrReview -> "Save OCR Entries"
            is ReviewState.Blocked -> "Fix Errors"
            else -> "Save"
        }
    }

    /**
     * Check if the action button should be enabled.
     */
    fun isActionButtonEnabled(): Boolean {
        return when (currentState) {
            is ReviewState.ReviewScreen -> true
            is ReviewState.DuplicateWarning -> true
            is ReviewState.OcrReview -> true
            is ReviewState.Blocked -> false
            is ReviewState.Saving -> false
            else -> true
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private fun buildReviewMessage(result: PreSaveResult): String {
        val parts = mutableListOf<String>()

        if (result.hasWarnings) {
            val warningCount = result.fieldValidation.warnings.size
            parts.add("$warningCount warning(s) found")
        }

        if (result.hasDuplicates) {
            parts.add("Potential duplicate detected")
        }

        return parts.joinToString(". ").ifEmpty { "Review your entry" }
    }

    private fun buildBlockedMessage(result: PreSaveResult): String {
        val criticalCount = result.fieldValidation.criticals.size
        val errors = result.fieldValidation.criticals.joinToString("\n") {
            "• ${it.message}"
        }
        return "Cannot save: $criticalCount error(s)\n$errors"
    }

    private fun buildDuplicateMessage(duplicate: DuplicateCheckResult): String {
        return when (duplicate) {
            is DuplicateCheckResult.ExactDuplicate ->
                "⚠️ EXACT DUPLICATE FOUND\n\n${duplicate.message}\n\n" +
                        "Saving again will create a duplicate record."
            is DuplicateCheckResult.PotentialDuplicate ->
                "⚠️ POSSIBLE DUPLICATE\n\n${duplicate.message}\n\n" +
                        "If this is a new transaction, you can proceed."
            is DuplicateCheckResult.NoDuplicate -> ""
        }
    }

    private fun buildEditableMap(entries: List<OcrMistakeHandler.OcrEntry>): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        for ((index, entry) in entries.withIndex()) {
            map["ocr_name_$index"] = entry.correctedName
            map["ocr_amount_$index"] = entry.correctedAmount
            map["ocr_date_$index"] = entry.correctedDate
            map["ocr_desc_$index"] = entry.correctedDescription
        }
        return map
    }
}
