package com.khatabook.app.domain.validation

/**
 * ═══════════════════════════════════════════════════════════════════
 * OCR MISTAKE HANDLER — Safe handling of ML Kit OCR errors
 * ═══════════════════════════════════════════════════════════════════
 *
 * ML Kit often makes these mistakes on handwritten khata pages:
 *
 *   1. "O" vs "0" — letter O confused with zero
 *   2. "l" vs "1" — lowercase L confused with one
 *   3. "S" vs "5" — letter S confused with five
 *   4. "B" vs "8" — letter B confused with eight
 *   5. "|" vs "1" — pipe confused with one
 *   6. Dots/marks interpreted as decimal points
 *   7. Urdu text mixed with English amounts
 *   8. Names read as amounts or vice versa
 *
 * This handler:
 *   - Scores confidence of extracted data
 *   - Applies contextual corrections
 *   - Flags ambiguous readings for user review
 *   - Never auto-corrects silently — always shows user what was found
 */

class OcrMistakeHandler {

    // ═══════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════

    /**
     * A single parsed entry from OCR text.
     * Before save, user reviews these on the OCR Review Screen.
     */
    data class OcrEntry(
        val rawText: String,              // Original text from ML Kit
        val correctedName: String,        // Cleaned customer name
        val correctedAmount: String,      // Cleaned amount string
        val correctedDate: String,        // Cleaned date string
        val correctedDescription: String, // Cleaned description
        val confidence: Float,            // 0.0 - 1.0
        val flags: List<OcrFlag>,         // What looks suspicious
        val suggestion: String? = null    // Auto-correct suggestion (user decides)
    )

    /**
     * Flags raised by the OCR handler.
     */
    sealed class OcrFlag {
        object LowConfidence : OcrFlag()
        object AmountAmbiguous : OcrFlag()
        object NameLooksLikeNumber : OcrFlag()
        object DateUnparseable : OcrFlag()
        object MultipleAmounts : OcrFlag()
        object NoCustomerName : OcrFlag()
        object TextGarbled : OcrFlag()
        object UrduMixedWithEnglish : OcrFlag()
        data class CharacterCorrection(val from: Char, val to: Char) : OcrFlag()
    }

    /**
     * Complete OCR processing result.
     */
    data class OcrProcessResult(
        val entries: List<OcrEntry>,
        val overallConfidence: Float,
        val totalFlags: Int,
        val rawText: String,
        val recommendation: ProcessingRecommendation
    )

    enum class ProcessingRecommendation {
        AUTO_SAVE,       // High confidence, few flags — can auto-save
        REVIEW_REQUIRED, // Medium confidence — user should review
        MANUAL_ENTRY,    // Low confidence — better to enter manually
        RECAPTURE        // Image too poor — retake the photo
    }


    // ═══════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Process raw OCR text and extract structured entries.
     *
     * @param rawText     Raw text from ML Kit
     * @param mlKitConfidence  ML Kit's reported confidence (0.0 - 1.0)
     * @return OcrProcessResult with extracted entries and flags
     */
    fun processOcrText(
        rawText: String,
        mlKitConfidence: Float = 1.0f
    ): OcrProcessResult {
        // Step 1: Initial quality check
        if (rawText.isBlank()) {
            return OcrProcessResult(
                entries = emptyList(),
                overallConfidence = 0f,
                totalFlags = 0,
                rawText = rawText,
                recommendation = ProcessingRecommendation.RECAPTURE
            )
        }

        val normalizedText = normalizeText(rawText)
        val flags = mutableListOf<OcrFlag>()

        // Step 2: Check for garbled text
        val garbleRatio = calculateGarbleRatio(normalizedText)
        if (garbleRatio > 0.5f) {
            flags.add(OcrFlag.TextGarbled)
        }

        // Step 3: Split into lines and extract entries
        val lines = rawText.lines().filter { it.isNotBlank() }
        val entries = mutableListOf<OcrEntry>()

        for (line in lines) {
            val entry = extractEntry(line, mlKitConfidence)
            entries.add(entry)
            flags.addAll(entry.flags)
        }

        // Step 4: Check for Urdu/English mixing
        if (containsUrdu(rawText) && containsEnglishNumbers(rawText)) {
            flags.add(OcrFlag.UrduMixedWithEnglish)
        }

        // Step 5: Calculate overall confidence
        val entryConfidences = entries.map { it.confidence }
        val avgConfidence = if (entryConfidences.isNotEmpty()) {
            entryConfidences.average().toFloat()
        } else {
            mlKitConfidence * 0.3f // No entries found = low confidence
        }

        val overallConfidence = (avgConfidence * 0.7f) + (mlKitConfidence * 0.3f)

        // Step 6: Determine recommendation
        val recommendation = when {
            overallConfidence >= 0.8f && flags.size <= 2 ->
                ProcessingRecommendation.AUTO_SAVE
            overallConfidence >= 0.5f ->
                ProcessingRecommendation.REVIEW_REQUIRED
            overallConfidence >= 0.2f ->
                ProcessingRecommendation.MANUAL_ENTRY
            else ->
                ProcessingRecommendation.RECAPTURE
        }

        return OcrProcessResult(
            entries = entries,
            overallConfidence = overallConfidence,
            totalFlags = flags.size,
            rawText = rawText,
            recommendation = recommendation
        )
    }


    // ═══════════════════════════════════════════════════════════════
    // EXTRACTION — Parse individual line into structured entry
    // ═══════════════════════════════════════════════════════════════

    private fun extractEntry(line: String, baseConfidence: Float): OcrEntry {
        val flags = mutableListOf<OcrFlag>()
        var confidence = baseConfidence

        // Try to extract amount (largest number in the line)
        val amountResult = extractAmount(line)
        if (amountResult == null) {
            flags.add(OcrFlag.NoCustomerName)
            confidence *= 0.5f
        }

        // Try to extract date
        val dateResult = extractDate(line)
        if (dateResult == null && line.contains(Regex("\\d{1,2}[/\\-\\.]\\d{1,2}"))) {
            flags.add(OcrFlag.DateUnparseable)
            confidence *= 0.8f
        }

        // Try to extract customer name (text that isn't amount/date)
        val nameResult = extractName(line, amountResult, dateResult)
        if (nameResult == null) {
            flags.add(OcrFlag.NoCustomerName)
            confidence *= 0.7f
        } else if (nameResult.all { it.isDigit() }) {
            flags.add(OcrFlag.NameLooksLikeNumber)
            confidence *= 0.6f
        }

        // Check for multiple amounts (ambiguous line)
        val allAmounts = Regex("\\d[\\d,]*\\.?\\d*").findAll(line).toList()
        if (allAmounts.size > 3) {
            flags.add(OcrFlag.MultipleAmounts)
            confidence *= 0.7f
        }

        // Apply character corrections
        val correctedLine = applyCharCorrections(line)
        if (correctedLine != line) {
            val corrections = line.zip(correctedLine).filter { (a, b) -> a != b }
            for ((from, to) in corrections) {
                flags.add(OcrFlag.CharacterCorrection(from, to))
            }
        }

        // Build the entry
        return OcrEntry(
            rawText = line,
            correctedName = nameResult?.let { cleanName(it) } ?: "Unknown",
            correctedAmount = amountResult?.let { cleanAmount(it) } ?: "",
            correctedDate = dateResult?.let { cleanDate(it) } ?: "",
            correctedDescription = extractDescription(line, nameResult, amountResult, dateResult),
            confidence = confidence.coerceIn(0f, 1f),
            flags = flags,
            suggestion = generateSuggestion(nameResult, amountResult, flags)
        )
    }


    // ═══════════════════════════════════════════════════════════════
    // FIELD EXTRACTION
    // ═══════════════════════════════════════════════════════════════

    private fun extractAmount(line: String): String? {
        // Patterns: "5000", "5,000", "Rs 5000", "PKR 5000", "روپے 5000"
        val patterns = listOf(
            Regex("(?:Rs|PKR|rp|روپے)\\s*(\\d[\\d,]*\\.?\\d*)", RegexOption.IGNORE_CASE),
            Regex("(\\d[\\d,]*\\.\\d{2})"),   // Decimal amounts: 123.45
            Regex("(\\d{1,3}(?:,\\d{3})+)"),  // Comma-separated: 1,000, 5,000
            Regex("\\b(\\d{4,8})\\b")          // Plain 4-8 digit numbers
        )

        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                return match.groupValues.getOrElse(1) { match.value }
            }
        }
        return null
    }

    private fun extractDate(line: String): String? {
        val patterns = listOf(
            Regex("(\\d{1,2})[/\\-\\.](\\d{1,2})[/\\-\\.](\\d{2,4})"),
            Regex("(\\d{4})[/\\-\\.](\\d{1,2})[/\\-\\.](\\d{1,2})")
        )

        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                return match.value
            }
        }
        return null
    }

    private fun extractName(line: String, amount: String?, date: String?): String? {
        var cleaned = line

        // Remove amount
        amount?.let { cleaned = cleaned.replace(it, "") }

        // Remove date
        date?.let { cleaned = cleaned.replace(it, "") }

        // Remove common prefixes
        cleaned = cleaned
            .replace(Regex("(?:Rs|PKR|rp|روپے)\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\d{1,2}[/\\-\\.]\\d{1,2}[/\\-\\.]\\d{2,4}"), "")
            .replace(Regex("[,\\-\\.]"), " ")
            .trim()

        // Extract the text portion (non-numeric)
        val nameCandidate = cleaned.split(Regex("\\s+"))
            .filter { it.isNotBlank() && !it.all { c -> c.isDigit() } }
            .joinToString(" ")
            .trim()

        return nameCandidate.ifBlank { null }
    }

    private fun extractDescription(
        line: String,
        name: String?,
        amount: String?,
        date: String?
    ): String {
        var cleaned = line
        name?.let { cleaned = cleaned.replace(it, "") }
        amount?.let { cleaned = cleaned.replace(it, "") }
        date?.let { cleaned = cleaned.replace(it, "") }
        return cleaned.trim().replace(Regex("\\s+"), " ")
    }


    // ═══════════════════════════════════════════════════════════════
    // CLEANING & CORRECTION
    // ═══════════════════════════════════════════════════════════════

    private fun applyCharCorrections(text: String): String {
        var result = text

        // OCR character corrections (context-dependent)
        // O → 0 when surrounded by digits
        result = result.replace(Regex("(\\d)[oO](\\d)"), "$10$2")
        // l/I → 1 when surrounded by digits
        result = result.replace(Regex("(\\d)[lI](\\d)"), "$11$2")
        // S → 5 when surrounded by digits
        result = result.replace(Regex("(\\d)[sS](\\d)"), "$15$2")
        // B → 8 when surrounded by digits
        result = result.replace(Regex("(\\d)[bB](\\d)"), "$18$2")

        return result
    }

    private fun cleanName(raw: String): String {
        return raw
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^\\p{L}\\s\\-]"), "") // Keep letters, spaces, hyphens
            .trim()
    }

    private fun cleanAmount(raw: String): String {
        return raw
            .replace(Regex("[,\\s]"), "")
            .replace(Regex("^Rs\\.?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^PKR\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanDate(raw: String): String {
        return raw.trim()
    }

    private fun normalizeText(text: String): String {
        return text.lowercase().replace(Regex("\\s+"), " ").trim()
    }

    private fun calculateGarbleRatio(text: String): Float {
        if (text.isEmpty()) return 1f
        val total = text.length
        val garbled = text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        return garbled.toFloat() / total
    }

    private fun containsUrdu(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' || it in '\uFB50'..'\uFDFF' }
    }

    private fun containsEnglishNumbers(text: String): Boolean {
        return text.contains(Regex("\\d{2,}"))
    }

    /**
     * Generate a human-readable suggestion based on flags.
     */
    private fun generateSuggestion(
        name: String?,
        amount: String?,
        flags: List<OcrFlag>
    ): String? {
        val parts = mutableListOf<String>()

        if (name == null) {
            parts.add("Could not detect a customer name")
        }
        if (amount == null) {
            parts.add("Could not detect an amount")
        }
        if (flags.any { it is OcrFlag.NameLooksLikeNumber }) {
            parts.add("The name looks like a number — OCR may have misread it")
        }
        if (flags.any { it is OcrFlag.MultipleAmounts }) {
            parts.add("Multiple numbers found — clarify which is the amount")
        }

        return if (parts.isNotEmpty()) {
            parts.joinToString(". ") + ". Please review carefully."
        } else {
            null
        }
    }
}
