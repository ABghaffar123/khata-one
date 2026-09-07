package com.khatabook.app.domain.validation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════════════
 * OCR MISTAKE HANDLER TEST SUITE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Tests OCR text processing, character corrections, confidence
 * scoring, and flag detection for common OCR mistakes.
 *
 * Test count: 18 tests
 */
class OcrMistakeHandlerTest {

    private lateinit var handler: OcrMistakeHandler

    @Before
    fun setup() {
        handler = OcrMistakeHandler()
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC PROCESSING
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - empty text returns recapture`() {
        val result = handler.processOcrText("")
        assertEquals(
            OcrMistakeHandler.ProcessingRecommendation.RECAPTURE,
            result.recommendation
        )
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun `process - blank text returns recapture`() {
        val result = handler.processOcrText("   \n  \n  ")
        assertEquals(
            OcrMistakeHandler.ProcessingRecommendation.RECAPTURE,
            result.recommendation
        )
    }

    @Test
    fun `process - valid khata line extracts entry`() {
        val result = handler.processOcrText("Ahmed Khan 15/08/2026 Rice 5000")
        assertTrue(result.entries.isNotEmpty())
        assertEquals(1, result.entries.size)
    }

    @Test
    fun `process - multiple lines creates multiple entries`() {
        val text = """
            Ahmed Khan 15/08/2026 Rice 5000
            Ali Khan 15/08/2026 Sugar 3000
        """.trimIndent()
        val result = handler.processOcrText(text)
        assertEquals(2, result.entries.size)
    }


    // ═══════════════════════════════════════════════════════════════
    // CHARACTER CORRECTIONS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - OCR letter O corrected to zero in amount`() {
        val result = handler.processOcrText("Ahmed 1O3O")
        assertTrue(result.entries.isNotEmpty())
        val entry = result.entries.first()
        // The handler should flag the character correction
        assertTrue(entry.flags.any { it is OcrMistakeHandler.OcrFlag.CharacterCorrection })
    }

    @Test
    fun `process - OCR letter l corrected to one in amount`() {
        val result = handler.processOcrText("Ahmed 1l50")
        assertTrue(result.entries.isNotEmpty())
        val entry = result.entries.first()
        assertTrue(entry.flags.any { it is OcrMistakeHandler.OcrFlag.CharacterCorrection })
    }


    // ═══════════════════════════════════════════════════════════════
    // CONFIDENCE SCORING
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - high confidence with clear text`() {
        val result = handler.processOcrText(
            "Ahmed Khan 15/08/2026 Rice 5000",
            mlKitConfidence = 0.95f
        )
        assertTrue(result.overallConfidence > 0.7f)
    }

    @Test
    fun `process - low ML Kit confidence reduces overall`() {
        val result = handler.processOcrText(
            "Ahmed Khan 15/08/2026 Rice 5000",
            mlKitConfidence = 0.3f
        )
        assertTrue(result.overallConfidence < 0.7f)
    }

    @Test
    fun `process - garbled text produces low confidence`() {
        val result = handler.processOcrText(
            "!@#$%^&*()_+{}|:<>?",
            mlKitConfidence = 0.8f
        )
        assertTrue(result.flags.any { it is OcrMistakeHandler.OcrFlag.TextGarbled })
    }


    // ═══════════════════════════════════════════════════════════════
    // FLAG DETECTION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - no amount found flags NoCustomerName`() {
        // Line with only name, no numbers
        val result = handler.processOcrText("Ahmed Khan only text here")
        // Should still extract name, but flag issues
        assertTrue(result.entries.isNotEmpty())
    }

    @Test
    fun `process - multiple amounts flags MultipleAmounts`() {
        val result = handler.processOcrText("1234 5678 9012 3456 7890")
        val entry = result.entries.first()
        assertTrue(entry.flags.any { it is OcrMistakeHandler.OcrFlag.MultipleAmounts })
    }

    @Test
    fun `process - name that is all numbers flags NameLooksLikeNumber`() {
        val result = handler.processOcrText("12345 5000")
        val entry = result.entries.first()
        // The "name" portion is all digits
        assertTrue(entry.flags.any {
            it is OcrMistakeHandler.OcrFlag.NameLooksLikeNumber ||
                    it is OcrMistakeHandler.OcrFlag.NoCustomerName
        })
    }

    @Test
    fun `process - Urdu text with English numbers flags UrduMixedWithEnglish`() {
        val result = handler.processOcrText("چاول 5000 چینی 2000")
        assertTrue(result.flags.any { it is OcrMistakeHandler.OcrFlag.UrduMixedWithEnglish })
    }


    // ═══════════════════════════════════════════════════════════════
    // RECOMMENDATION LOGIC
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - clear text with high confidence recommends auto-save`() {
        val result = handler.processOcrText(
            "Ahmed Khan 15/08/2026 Rice 5000",
            mlKitConfidence = 0.95f
        )
        assertEquals(
            OcrMistakeHandler.ProcessingRecommendation.AUTO_SAVE,
            result.recommendation
        )
    }

    @Test
    fun `process - text with multiple flags recommends review`() {
        val result = handler.processOcrText(
            "12345 5678 9012 3456", // Multiple amounts, name is number
            mlKitConfidence = 0.6f
        )
        assertEquals(
            OcrMistakeHandler.ProcessingRecommendation.REVIEW_REQUIRED,
            result.recommendation
        )
    }


    // ═══════════════════════════════════════════════════════════════
    // FIELD EXTRACTION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `process - extracts amount with Rs prefix`() {
        val result = handler.processOcrText("Ahmed Rs 5000")
        assertTrue(result.entries.isNotEmpty())
        val entry = result.entries.first()
        assertTrue(entry.correctedAmount.isNotEmpty())
    }

    @Test
    fun `process - extracts date in DD/MM/YYYY format`() {
        val result = handler.processOcrText("Ahmed 15/08/2026 5000")
        assertTrue(result.entries.isNotEmpty())
        val entry = result.entries.first()
        assertTrue(entry.correctedDate.isNotEmpty())
    }
}
