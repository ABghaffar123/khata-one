package com.khatabook.app.ui.language

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE DATA — Complete language definitions with sample text
 * ═══════════════════════════════════════════════════════════════════
 *
 * Each language defines:
 *   - Display name (native script)
 *   - Native name
 *   - Flag emoji
 *   - Direction (LTR/RTL)
 *   - Sample UI text for preview
 *   - Font family hint
 */

data class KhataLanguage(
    val code: String,                // "en", "ur", "ur-roman"
    val displayName: String,         // "English", "اردو", "Roman Urdu"
    val nativeName: String,          // "English", "اردو", "Roman Urdu"
    val flagEmoji: String,           // "🇬🇧", "🇵🇰", "🗣️"
    val isRtl: Boolean,
    val layoutDirection: LayoutDirection,
    val textAlign: TextAlign,
    val sampleTexts: SampleTexts,
    val fontFamilyHint: String       // For font selection
)

data class SampleTexts(
    val welcome: String,
    val totalDues: String,
    val amount: String,
    val customerOwes: String,
    val homeLabel: String,
    val customersLabel: String,
    val cameraLabel: String,
    val settingsLabel: String,
    val recentActivity: String,
    val viewAll: String
)


object KhataLanguages {

    // ═══════════════════════════════════════════════════════════════
    // ENGLISH
    // ═══════════════════════════════════════════════════════════════

    val English = KhataLanguage(
        code = "en",
        displayName = "English",
        nativeName = "English",
        flagEmoji = "🇬🇧",
        isRtl = false,
        layoutDirection = LayoutDirection.Ltr,
        textAlign = TextAlign.Left,
        fontFamilyHint = "default",
        sampleTexts = SampleTexts(
            welcome = "Welcome to Khata Book",
            totalDues = "Total Dues",
            amount = "Rs 45,000",
            customerOwes = "Ahmed Khan owes Rs 5,000",
            homeLabel = "Home",
            customersLabel = "Khata",
            cameraLabel = "Camera",
            settingsLabel = "Settings",
            recentActivity = "Recent Activity",
            viewAll = "View All"
        )
    )

    // ═══════════════════════════════════════════════════════════════
    // URDU (اردو)
    // ═══════════════════════════════════════════════════════════════

    val Urdu = KhataLanguage(
        code = "ur",
        displayName = "اردو (Urdu)",
        nativeName = "اردو",
        flagEmoji = "🇵🇰",
        isRtl = true,
        layoutDirection = LayoutDirection.Rtl,
        textAlign = TextAlign.Right,
        fontFamilyHint = "noto_nastaliq_urdu",
        sampleTexts = SampleTexts(
            welcome = "خ账簿 میں خوش آمدید",
            totalDues = "کل واجبات",
            amount = "45,000 روپے",
            customerOwes = "احمد خان پر 5,000 روپے باقی ہے",
            homeLabel = "ہوم",
            customersLabel = "خ账簿",
            cameraLabel = "کیمرا",
            settingsLabel = "ترتیبات",
            recentActivity = "حالیہ سرگرمی",
            viewAll = "سب دیکھیں"
        )
    )

    // ═══════════════════════════════════════════════════════════════
    // ROMAN URDU
    // ═══════════════════════════════════════════════════════════════

    val RomanUrdu = KhataLanguage(
        code = "ur-roman",
        displayName = "Roman Urdu",
        nativeName = "Roman Urdu",
        flagEmoji = "🗣️",
        isRtl = false,
        layoutDirection = LayoutDirection.Ltr,
        textAlign = TextAlign.Left,
        fontFamilyHint = "default",
        sampleTexts = SampleTexts(
            welcome = "Khata Book mein Khush Aamdeed",
            totalDues = "Kul Wajaibaat",
            amount = "45,000 Rupay",
            customerOwes = "Ahmed Khan Par 5,000 Rupay Baqi",
            homeLabel = "Home",
            customersLabel = "Khata",
            cameraLabel = "Camera",
            settingsLabel = "Settings",
            recentActivity = "Haaliya Sargarmi",
            viewAll = "Sab Dekhein"
        )
    )


    // ═══════════════════════════════════════════════════════════════
    // ALL LANGUAGES
    // ═══════════════════════════════════════════════════════════════

    val allLanguages: List<KhataLanguage> = listOf(
        English,
        Urdu,
        RomanUrdu
    )

    /**
     * Get language by code.
     */
    fun getByCode(code: String): KhataLanguage =
        allLanguages.firstOrNull { it.code == code } ?: English
}
