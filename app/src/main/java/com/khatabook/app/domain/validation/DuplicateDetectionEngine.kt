package com.khatabook.app.domain.validation

import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.min

/**
 * ═══════════════════════════════════════════════════════════════════
 * DUPLICATE DETECTION ENGINE — Multi-strategy duplicate prevention
 * ═══════════════════════════════════════════════════════════════════
 *
 * Three detection layers:
 *
 *   Layer 1: IMAGE HASH         — Exact image file match (SHA-256)
 *   Layer 2: PERCEPTUAL HASH    — Visually similar images (pHash)
 *   Layer 3: TRANSACTION FIELDS — Same customer + amount + date
 *
 * Usage:
 *   val engine = DuplicateDetectionEngine(existingTransactions, existingImages)
 *   val result = engine.checkTransaction(newTransaction)
 *   when (result) {
 *       is NoDuplicate -> save()
 *       is ExactDuplicate -> showWarning(result.message)
 *       is PotentialDuplicate -> showConfirmation(result.message)
 *   }
 */

class DuplicateDetectionEngine(
    private val existingTransactions: List<TransactionRecord> = emptyList(),
    private val existingImageHashes: List<ImageHashRecord> = emptyList()
) {

    private val pktNow: Long get() = com.khatabook.app.util.PkDateTime.nowUtc()

    // ═══════════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════════

    companion object {
        /** Time window for transaction duplicate detection (5 minutes) */
        const val TRANSACTION_TIME_WINDOW_MS = 5 * 60 * 1000L

        /** Time window for image duplicate detection (24 hours) */
        const val IMAGE_TIME_WINDOW_MS = 24 * 60 * 60 * 1000L

        /** Perceptual hash similarity threshold (0.0 - 1.0) */
        const val PERCEPTUAL_HASH_THRESHOLD = 0.90f

        /** Maximum daily transactions per customer before warning */
        const val MAX_DAILY_TXN_PER_CUSTOMER = 20

        /** Amount tolerance for fuzzy matching (1% or Rs 10, whichever larger) */
        const val AMOUNT_FUZZY_TOLERANCE_PERCENT = 0.01
        const val AMOUNT_FUZZY_TOLERANCE_MIN = 10.0
    }


    // ═══════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if a new transaction is a duplicate.
     *
     * @param customerId     Customer ID
     * @param amount         Transaction amount (already parsed)
     * @param date           Transaction date (timestamp)
     * @param description    Transaction description
     * @param transactionType CREDIT, PAYMENT, etc.
     * @return DuplicateCheckResult
     */
    fun checkTransaction(
        customerId: Long,
        amount: Double,
        date: Long,
        description: String = "",
        transactionType: String = "CREDIT"
    ): DuplicateCheckResult {
        val now = pktNow

        // Layer 1: Exact field match within time window
        val exactMatch = existingTransactions.find { txn ->
            txn.customerId == customerId &&
                    abs(txn.amount - amount) < 0.01 &&
                    abs(txn.date - date) < TRANSACTION_TIME_WINDOW_MS &&
                    txn.transactionType == transactionType &&
                    now - txn.createdAt < TRANSACTION_TIME_WINDOW_MS
        }

        if (exactMatch != null) {
            return DuplicateCheckResult.ExactDuplicate(
                existingId = exactMatch.id,
                existingDate = exactMatch.date,
                matchType = DuplicateMatchType.TRANSACTION_FIELDS,
                message = "Same transaction (Rs ${String.format("%,.0f", amount)}) " +
                        "for this customer was recorded just now. " +
                        "This is likely a duplicate."
            )
        }

        // Layer 2: Fuzzy match — same customer, similar amount, same day
        val fuzzyMatch = existingTransactions.find { txn ->
            txn.customerId == customerId &&
                    txn.transactionType == transactionType &&
                    isSameDay(txn.date, date) &&
                    isAmountSimilar(txn.amount, amount)
        }

        if (fuzzyMatch != null) {
            return DuplicateCheckResult.PotentialDuplicate(
                existingId = fuzzyMatch.id,
                existingDate = fuzzyMatch.date,
                similarityScore = 0.85f,
                matchType = DuplicateMatchType.TRANSACTION_FIELDS,
                message = "A similar transaction (Rs ${String.format("%,.0f", fuzzyMatch.amount)}) " +
                        "for this customer already exists on this day. " +
                        "Verify this is a new transaction."
            )
        }

        // Layer 3: Excessive same-day transactions
        val todayTxnCount = existingTransactions.count { txn ->
            txn.customerId == customerId && isSameDay(txn.date, date)
        }

        if (todayTxnCount >= MAX_DAILY_TXN_PER_CUSTOMER) {
            return DuplicateCheckResult.PotentialDuplicate(
                existingId = 0,
                existingDate = date,
                similarityScore = 0.5f,
                matchType = DuplicateMatchType.TRANSACTION_FIELDS,
                message = "This customer already has $todayTxnCount transactions today. " +
                        "Please verify this entry is correct."
            )
        }

        return DuplicateCheckResult.NoDuplicate
    }

    /**
     * Check if an image has been scanned before.
     *
     * @param imageHash   SHA-256 hash of the image file
     * @param pHash       Perceptual hash of the image (optional)
     * @return DuplicateCheckResult
     */
    fun checkImage(
        imageHash: String,
        pHash: Long? = null
    ): DuplicateCheckResult {
        val now = pktNow

        // Layer 1: Exact image hash match
        val exactMatch = existingImageHashes.find { img ->
            img.fileHash == imageHash
        }

        if (exactMatch != null) {
            val hoursSince = (now - exactMatch.scannedAt) / (60 * 60 * 1000)
            val timeDesc = when {
                hoursSince < 1 -> "just now"
                hoursSince < 24 -> "${hoursSince}h ago"
                else -> "${hoursSince / 24}d ago"
            }

            return if (now - exactMatch.scannedAt < IMAGE_TIME_WINDOW_MS) {
                DuplicateCheckResult.ExactDuplicate(
                    existingId = exactMatch.ocrCaptureId,
                    existingDate = exactMatch.scannedAt,
                    matchType = DuplicateMatchType.IMAGE_HASH,
                    message = "This exact image was scanned $timeDesc. " +
                            "Scanning it again will create duplicate entries."
                )
            } else {
                DuplicateCheckResult.PotentialDuplicate(
                    existingId = exactMatch.ocrCaptureId,
                    existingDate = exactMatch.scannedAt,
                    similarityScore = 1.0f,
                    matchType = DuplicateMatchType.IMAGE_HASH,
                    message = "This image was scanned $timeDesc. " +
                            "Proceed if you want to re-process it."
                )
            }
        }

        // Layer 2: Perceptual hash — visually similar image
        if (pHash != null) {
            val similarMatch = existingImageHashes.find { img ->
                img.pHash != null && perceptualSimilarity(pHash, img.pHash) >= PERCEPTUAL_HASH_THRESHOLD
            }

            if (similarMatch != null) {
                val similarity = perceptualSimilarity(pHash, similarMatch.pHash)
                val similarityPercent = (similarity * 100).toInt()

                return DuplicateCheckResult.PotentialDuplicate(
                    existingId = similarMatch.ocrCaptureId,
                    existingDate = similarMatch.scannedAt,
                    similarityScore = similarity,
                    matchType = DuplicateMatchType.IMAGE_PERCEPTUAL,
                    message = "A visually similar image (${similarityPercent}% match) " +
                            "was scanned before. This might be the same khata page " +
                            "taken from a different angle."
                )
            }
        }

        return DuplicateCheckResult.NoDuplicate
    }

    /**
     * Check OCR text for duplicate entries within existing scans.
     */
    fun checkOcrText(
        ocrText: String,
        customerId: Long? = null
    ): DuplicateCheckResult {
        if (ocrText.length < 20) return DuplicateCheckResult.NoDuplicate

        val normalizedText = normalizeOcrText(ocrText)

        val match = existingTransactions.find { txn ->
            val txnDesc = normalizeOcrText(txn.description)
            txnDesc.isNotEmpty() &&
                    textSimilarity(normalizedText, txnDesc) >= 0.85f &&
                    (customerId == null || txn.customerId == customerId)
        }

        if (match != null) {
            return DuplicateCheckResult.PotentialDuplicate(
                existingId = match.id,
                existingDate = match.date,
                similarityScore = textSimilarity(normalizedText, normalizeOcrText(match.description)),
                matchType = DuplicateMatchType.OCR_TEXT,
                message = "Similar text content was found in a previous transaction. " +
                        "This might be the same khata page."
            )
        }

        return DuplicateCheckResult.NoDuplicate
    }


    // ═══════════════════════════════════════════════════════════════
    // IMAGE HASHING UTILITIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Compute SHA-256 hash of image bytes.
     */
    fun computeFileHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Compute a simple perceptual hash for image similarity.
     * Uses average hash (aHash) approach:
     *   1. Resize to 8x8
     *   2. Convert to grayscale
     *   3. Compute average pixel value
     *   4. Each bit = pixel > average
     *
     * @param pixels  Raw pixel data (width * height * 4 bytes, ARGB)
     * @param width   Image width
     * @param height  Image height
     * @return 64-bit perceptual hash
     */
    fun computePerceptualHash(pixels: IntArray, width: Int, height: Int): Long {
        // Downsample to 8x8
        val blockSize = width / 8
        if (blockSize <= 0) return 0L

        val gray = LongArray(64)
        var hash = 0L

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                var sum = 0L
                var count = 0
                for (y in row * blockSize until (row + 1) * blockSize) {
                    for (x in col * blockSize until (col + 1) * blockSize) {
                        if (y < height && x < width) {
                            val pixel = pixels[y * width + x]
                            // Extract grayscale from ARGB
                            val r = (pixel shr 16) and 0xFF
                            val g = (pixel shr 8) and 0xFF
                            val b = pixel and 0xFF
                            sum += (r * 0.299 + g * 0.587 + b * 0.114).toLong()
                            count++
                        }
                    }
                }
                gray[row * 8 + col] = if (count > 0) sum / count else 0
            }
        }

        // Compute average
        val avg = gray.average()

        // Build hash
        for (i in 0 until 64) {
            if (gray[i] > avg) {
                hash = hash or (1L shl i)
            }
        }

        return hash
    }

    /**
     * Compute similarity between two perceptual hashes.
     * Returns 0.0 (completely different) to 1.0 (identical).
     */
    private fun perceptualSimilarity(hash1: Long, hash2: Long): Float {
        val xor = hash1 xor hash2
        val bits = java.lang.Long.bitCount(xor)
        return 1.0f - bits.toFloat() / 64f
    }


    // ═══════════════════════════════════════════════════════════════
    // COMPARISON HELPERS
    // ═══════════════════════════════════════════════════════════════

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val pkt = com.khatabook.app.util.PkDateTime.PKT_TIMEZONE
        val cal1 = java.util.Calendar.getInstance(pkt).apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance(pkt).apply { timeInMillis = timestamp2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isAmountSimilar(amount1: Double, amount2: Double): Boolean {
        val tolerance = maxOf(amount1 * AMOUNT_FUZZY_TOLERANCE_PERCENT, AMOUNT_FUZZY_TOLERANCE_MIN)
        return abs(amount1 - amount2) <= tolerance
    }

    private fun normalizeOcrText(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Simple Jaccard similarity for short text strings.
     */
    private fun textSimilarity(text1: String, text2: String): Float {
        if (text1.isEmpty() || text2.isEmpty()) return 0f

        val words1 = text1.split(" ").toSet()
        val words2 = text2.split(" ").toSet()

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return if (union > 0) intersection.toFloat() / union else 0f
    }
}


// ═══════════════════════════════════════════════════════════════════
// DATA RECORDS — Input types for the engine
// ═══════════════════════════════════════════════════════════════════

/**
 * Simplified transaction record for duplicate checking.
 * Maps to the full Transaction entity but only includes fields needed for dedup.
 */
data class TransactionRecord(
    val id: Long,
    val customerId: Long,
    val amount: Double,
    val date: Long,
    val description: String,
    val transactionType: String,
    val createdAt: Long
)

/**
 * Image hash record for duplicate image detection.
 */
data class ImageHashRecord(
    val ocrCaptureId: Long,
    val fileHash: String,
    val pHash: Long?,
    val scannedAt: Long
)
