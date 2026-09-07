package com.khatabook.app.domain.validation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════════════
 * DUPLICATE DETECTION ENGINE TEST SUITE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Tests all three detection layers:
 *   1. Transaction field matching (amount, customer, date)
 *   2. Image hash matching (SHA-256)
 *   3. Perceptual hash matching (pHash)
 *
 * Test count: 20 tests
 */
class DuplicateDetectionEngineTest {

    private val now = System.currentTimeMillis()
    private val fiveMinutesAgo = now - 5 * 60 * 1000L
    private val tenMinutesAgo = now - 10 * 60 * 1000L
    private val oneDayAgo = now - 24 * 60 * 60 * 1000L

    // ═══════════════════════════════════════════════════════════════
    // TRANSACTION DUPLICATE TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `transaction - exact duplicate detected within time window`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val result = engine.checkTransaction(1, 5000.0, now, "Rice", "CREDIT")
        assertTrue(result is DuplicateCheckResult.ExactDuplicate)
        assertEquals(1L, (result as DuplicateCheckResult.ExactDuplicate).existingId)
    }

    @Test
    fun `transaction - no duplicate for different customer`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val result = engine.checkTransaction(2, 5000.0, now, "Rice", "CREDIT")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }

    @Test
    fun `transaction - no duplicate for different amount`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val result = engine.checkTransaction(1, 3000.0, now, "Rice", "CREDIT")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }

    @Test
    fun `transaction - no duplicate for different type`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val result = engine.checkTransaction(1, 5000.0, now, "Rice", "PAYMENT")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }

    @Test
    fun `transaction - no duplicate outside time window`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, tenMinutesAgo, "Rice", "CREDIT", tenMinutesAgo)
            )
        )
        // Same data but outside the 5-minute window for exact match
        val result = engine.checkTransaction(1, 5000.0, now, "Rice", "CREDIT")
        // Should be NoDuplicate or PotentialDuplicate (fuzzy), not ExactDuplicate
        assertFalse(result is DuplicateCheckResult.ExactDuplicate)
    }

    @Test
    fun `transaction - fuzzy match for similar amount same day`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        // 4990 is within 1% tolerance of 5000
        val result = engine.checkTransaction(1, 4990.0, now, "Rice", "CREDIT")
        assertTrue(result is DuplicateCheckResult.PotentialDuplicate)
    }

    @Test
    fun `transaction - excessive same-day transactions warning`() {
        val transactions = (1..20).map { i ->
            TransactionRecord(i.toLong(), 1, i * 100.0, now, "Item $i", "CREDIT", now)
        }
        val engine = DuplicateDetectionEngine(existingTransactions = transactions)
        val result = engine.checkTransaction(1, 9999.0, now, "Big purchase", "CREDIT")
        assertTrue(result is DuplicateCheckResult.PotentialDuplicate)
    }

    @Test
    fun `transaction - empty existing list returns no duplicate`() {
        val engine = DuplicateDetectionEngine()
        val result = engine.checkTransaction(1, 5000.0, now, "Rice", "CREDIT")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }


    // ═══════════════════════════════════════════════════════════════
    // IMAGE HASH TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `image - exact hash match detected`() {
        val hash = "abc123def456"
        val engine = DuplicateDetectionEngine(
            existingImageHashes = listOf(
                ImageHashRecord(1, hash, null, now)
            )
        )
        val result = engine.checkImage(hash)
        assertTrue(result is DuplicateCheckResult.ExactDuplicate)
    }

    @Test
    fun `image - different hash returns no duplicate`() {
        val engine = DuplicateDetectionEngine(
            existingImageHashes = listOf(
                ImageHashRecord(1, "abc123", null, now)
            )
        )
        val result = engine.checkImage("xyz789")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }

    @Test
    fun `image - old hash returns potential duplicate (not exact)`() {
        val hash = "abc123"
        val engine = DuplicateDetectionEngine(
            existingImageHashes = listOf(
                ImageHashRecord(1, hash, null, oneDayAgo)
            )
        )
        val result = engine.checkImage(hash)
        // Old hash is PotentialDuplicate (not exact because outside 24h window)
        assertTrue(result is DuplicateCheckResult.PotentialDuplicate)
    }

    @Test
    fun `image - empty hash list returns no duplicate`() {
        val engine = DuplicateDetectionEngine()
        val result = engine.checkImage("abc123")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }

    @Test
    fun `image - file hash computation is deterministic`() {
        val engine = DuplicateDetectionEngine()
        val data = "test image data".toByteArray()
        val hash1 = engine.computeFileHash(data)
        val hash2 = engine.computeFileHash(data)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `image - different data produces different hash`() {
        val engine = DuplicateDetectionEngine()
        val hash1 = engine.computeFileHash("image1".toByteArray())
        val hash2 = engine.computeFileHash("image2".toByteArray())
        assertNotEquals(hash1, hash2)
    }


    // ═══════════════════════════════════════════════════════════════
    // PERCEPTUAL HASH TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `perceptual hash - identical images produce same hash`() {
        val engine = DuplicateDetectionEngine()
        val pixels = IntArray(64 * 64) { 0xFF000000.toInt() } // All black
        val hash1 = engine.computePerceptualHash(pixels, 64, 64)
        val hash2 = engine.computePerceptualHash(pixels, 64, 64)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `perceptual hash - different images produce different hashes`() {
        val engine = DuplicateDetectionEngine()
        val black = IntArray(64 * 64) { 0xFF000000.toInt() }
        val white = IntArray(64 * 64) { 0xFFFFFFFF.toInt() }
        val hash1 = engine.computePerceptualHash(black, 64, 64)
        val hash2 = engine.computePerceptualHash(white, 64, 64)
        assertNotEquals(hash1, hash2)
    }


    // ═══════════════════════════════════════════════════════════════
    // OCR TEXT DUPLICATE TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ocr text - similar text detected as potential duplicate`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Ahmed bought rice 20kg sugar 5kg", "CREDIT", now)
            )
        )
        val result = engine.checkOcrText("Ahmed bought rice 20kg sugar 5kg")
        assertTrue(result is DuplicateCheckResult.PotentialDuplicate)
    }

    @Test
    fun `ocr text - short text returns no duplicate`() {
        val engine = DuplicateDetectionEngine(
            existingTransactions = listOf(
                TransactionRecord(1, 1, 5000.0, now, "Rice", "CREDIT", now)
            )
        )
        val result = engine.checkOcrText("hi")
        assertTrue(result is DuplicateCheckResult.NoDuplicate)
    }
}
