package com.khatabook.app.domain.validation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════════════
 * PRE-SAVE REVIEW MANAGER TEST SUITE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Tests the state machine that coordinates validation → review → save.
 *
 * Test count: 18 tests
 */
class PreSaveReviewManagerTest {

    private lateinit var manager: PreSaveReviewManager

    @Before
    fun setup() {
        manager = PreSaveReviewManager()
    }

    // ═══════════════════════════════════════════════════════════════
    // STATE TRANSITIONS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `initial state is Idle`() {
        assertTrue(manager.getState() is PreSaveReviewManager.ReviewState.Idle)
    }

    @Test
    fun `startValidation transitions to Validating`() {
        val state = manager.startValidation()
        assertTrue(state is PreSaveReviewManager.ReviewState.Validating)
    }

    @Test
    fun `reset returns to Idle`() {
        manager.startValidation()
        val state = manager.reset()
        assertTrue(state is PreSaveReviewManager.ReviewState.Idle)
    }


    // ═══════════════════════════════════════════════════════════════
    // VALIDATION COMPLETE → STATE MAPPING
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `all clear result transitions to Saving`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.success(),
            duplicateCheck = DuplicateCheckResult.NoDuplicate,
            uiRecommendation = UiRecommendation.SAVE
        )
        val state = manager.onValidationComplete(result)
        assertTrue(state is PreSaveReviewManager.ReviewState.Saving)
    }

    @Test
    fun `critical errors transition to Blocked`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = false,
            fieldValidation = ValidationResult.error(
                "amount", ValidationRule.AMOUNT_EMPTY, "Amount required"
            ),
            uiRecommendation = UiRecommendation.BLOCK_SAVE
        )
        val state = manager.onValidationComplete(result)
        assertTrue(state is PreSaveReviewManager.ReviewState.Blocked)
    }

    @Test
    fun `duplicate detected transitions to DuplicateWarning`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            duplicateCheck = DuplicateCheckResult.ExactDuplicate(
                existingId = 1,
                existingDate = System.currentTimeMillis(),
                matchType = DuplicateMatchType.TRANSACTION_FIELDS,
                message = "Duplicate found"
            ),
            uiRecommendation = UiRecommendation.SHOW_DUPLICATE
        )
        val state = manager.onValidationComplete(result)
        assertTrue(state is PreSaveReviewManager.ReviewState.DuplicateWarning)
    }

    @Test
    fun `warnings transition to ReviewScreen`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Date is in future"
            ),
            uiRecommendation = UiRecommendation.SHOW_WARNING
        )
        val state = manager.onValidationComplete(result)
        assertTrue(state is PreSaveReviewManager.ReviewState.ReviewScreen)
    }

    @Test
    fun `OCR flags transition to OcrReview`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            ocrFlags = listOf(OcrMistakeHandler.OcrFlag.LowConfidence),
            uiRecommendation = UiRecommendation.SHOW_WARNING
        )
        val state = manager.onValidationComplete(result)
        assertTrue(state is PreSaveReviewManager.ReviewState.OcrReview)
    }


    // ═══════════════════════════════════════════════════════════════
    // USER ACTIONS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `user confirm from ReviewScreen transitions to Saving`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Date is in future"
            ),
            uiRecommendation = UiRecommendation.SHOW_WARNING
        )
        manager.onValidationComplete(result)
        val state = manager.onUserConfirm()
        assertTrue(state is PreSaveReviewManager.ReviewState.Saving)
    }

    @Test
    fun `user confirm from DuplicateWarning transitions to Saving`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            duplicateCheck = DuplicateCheckResult.ExactDuplicate(
                existingId = 1,
                existingDate = System.currentTimeMillis(),
                matchType = DuplicateMatchType.TRANSACTION_FIELDS,
                message = "Duplicate found"
            ),
            uiRecommendation = UiRecommendation.SHOW_DUPLICATE
        )
        manager.onValidationComplete(result)
        val state = manager.onUserConfirm()
        assertTrue(state is PreSaveReviewManager.ReviewState.Saving)
    }

    @Test
    fun `user cancel transitions to Cancelled`() {
        manager.startValidation()
        val state = manager.onCancel("Changed my mind")
        assertTrue(state is PreSaveReviewManager.ReviewState.Cancelled)
        assertEquals("Changed my mind", (state as PreSaveReviewManager.ReviewState.Cancelled).reason)
    }

    @Test
    fun `save complete transitions to Saved`() {
        manager.startValidation()
        val state = manager.onSaveComplete(42L)
        assertTrue(state is PreSaveReviewManager.ReviewState.Saved)
        assertEquals(42L, (state as PreSaveReviewManager.ReviewState.Saved).recordId)
    }

    @Test
    fun `save failed transitions to Cancelled`() {
        manager.startValidation()
        val state = manager.onSaveFailed("Database full")
        assertTrue(state is PreSaveReviewManager.ReviewState.Cancelled)
    }


    // ═══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `shouldShowReview true for ReviewScreen state`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Date is in future"
            ),
            uiRecommendation = UiRecommendation.SHOW_WARNING
        )
        manager.onValidationComplete(result)
        assertTrue(manager.shouldShowReview())
    }

    @Test
    fun `shouldShowReview false for Idle state`() {
        assertFalse(manager.shouldShowReview())
    }

    @Test
    fun `isBlocked true for Blocked state`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = false,
            fieldValidation = ValidationResult.error(
                "amount", ValidationRule.AMOUNT_EMPTY, "Amount required"
            ),
            uiRecommendation = UiRecommendation.BLOCK_SAVE
        )
        manager.onValidationComplete(result)
        assertTrue(manager.isBlocked())
    }

    @Test
    fun `isBlocked false for non-Blocked states`() {
        assertFalse(manager.isBlocked())
    }

    @Test
    fun `getActionButtonLabel returns appropriate text`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = true,
            fieldValidation = ValidationResult.warning(
                "date", ValidationRule.DATE_IN_FUTURE, "Date is in future"
            ),
            uiRecommendation = UiRecommendation.SHOW_WARNING
        )
        manager.onValidationComplete(result)
        assertEquals("Save Anyway", manager.getActionButtonLabel())
    }

    @Test
    fun `isActionButtonEnabled false for Blocked state`() {
        manager.startValidation()
        val result = PreSaveResult(
            canSave = false,
            fieldValidation = ValidationResult.error(
                "amount", ValidationRule.AMOUNT_EMPTY, "Amount required"
            ),
            uiRecommendation = UiRecommendation.BLOCK_SAVE
        )
        manager.onValidationComplete(result)
        assertFalse(manager.isActionButtonEnabled())
    }
}
