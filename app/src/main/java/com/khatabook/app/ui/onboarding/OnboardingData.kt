package com.khatabook.app.ui.onboarding

import com.khatabook.app.ui.language.KhataLanguage
import com.khatabook.app.ui.language.KhataLanguages

/**
 * ═══════════════════════════════════════════════════════════════════
 * ONBOARDING DATA — 4 premium onboarding screens
 * ═══════════════════════════════════════════════════════════════════
 *
 * Flow:
 *   Screen 1: Welcome + Language Selection
 *   Screen 2: Digital Khata (Core value)
 *   Screen 3: OCR Scan Magic (Unique feature)
 *   Screen 4: Secure & Offline (Trust)
 */

data class OnboardingScreen(
    val id: Int,
    val illustrationType: IllustrationType,
    val title: String,
    val titleUrdu: String,
    val subtitle: String,
    val subtitleUrdu: String,
    val features: List<String>,
    val featuresUrdu: List<String>,
    val showLanguageSelector: Boolean = false,
    val isLastScreen: Boolean = false,
    val buttonText: String = "Continue",
    val buttonTextUrdu: String = "جاری رہیں",
    val buttonUrduRoman: String = "Jaari Rahein"
)

enum class IllustrationType {
    GLOBE,           // Screen 1 — Language/Welcome
    LEDGER_BOOK,     // Screen 2 — Digital Khata
    CAMERA_SCAN,     // Screen 3 — OCR
    SHIELD_LOCK      // Screen 4 — Security
}


object OnboardingScreens {

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 1: WELCOME + LANGUAGE SELECTION
    // ═══════════════════════════════════════════════════════════════

    val Welcome = OnboardingScreen(
        id = 0,
        illustrationType = IllustrationType.GLOBE,
        title = "Khata Book",
        titleUrdu = "خ账簿",
        subtitle = "Your digital khata for\nPakistani shopkeepers",
        subtitleUrdu = "پاکستانی دکانداروں کے لیے\nڈیجیٹل خ账簿",
        features = emptyList(),
        featuresUrdu = emptyList(),
        showLanguageSelector = true,
        isLastScreen = false,
        buttonText = "Continue",
        buttonTextUrdu = "جاری رہیں",
        buttonUrduRoman = "Jaari Rahein"
    )

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 2: DIGITAL KHATA
    // ═══════════════════════════════════════════════════════════════

    val DigitalKhata = OnboardingScreen(
        id = 1,
        illustrationType = IllustrationType.LEDGER_BOOK,
        title = "Digital Khata Book",
        titleUrdu = "ڈیجیٹل خ账簿",
        subtitle = "Replace your manual khata with\na smart digital ledger",
        subtitleUrdu = "اپنا دستی خ账簿 ڈیجیٹل\nلیجر سے بدلیں",
        features = listOf(
            "Track all customers & balances",
            "Credit, payment & cash sale tracking",
            "Daily ledger view — like your khata"
        ),
        featuresUrdu = listOf(
            "تمام صارفین اور بیلنس ٹریک کریں",
            "قرض، ادائیگی اور کیش سیل ٹریکنگ",
            "روزانہ لیجر ویو — جیسے آپ کا خ账簿"
        ),
        showLanguageSelector = false,
        isLastScreen = false,
        buttonText = "Continue",
        buttonTextUrdu = "جاری رہیں",
        buttonUrduRoman = "Jaari Rahein"
    )

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 3: OCR SCAN
    // ═══════════════════════════════════════════════════════════════

    val OcrScan = OnboardingScreen(
        id = 2,
        illustrationType = IllustrationType.CAMERA_SCAN,
        title = "Scan & Extract",
        titleUrdu = "اسکین اور نکالیں",
        subtitle = "Photograph your khata page\nand AI extracts all entries",
        subtitleUrdu = "اپنے خ账簿 کا صفحہ تصویر لیں\nاور AI تمام اندراجات نکالے گا",
        features = listOf(
            "Capture handwritten khata pages",
            "AI-powered text extraction",
            "Edit & save in one tap"
        ),
        featuresUrdu = listOf(
            "ہاتھ سے لکھے خ账簿 کے صفحات کی تصویر لیں",
            "AI سے متن نکالیں",
            "ایک ٹیپ میں ترمیم اور محفوظ"
        ),
        showLanguageSelector = false,
        isLastScreen = false,
        buttonText = "Continue",
        buttonTextUrdu = "جاری رہیں",
        buttonUrduRoman = "Jaari Rahein"
    )

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 4: SECURE & OFFLINE
    // ═══════════════════════════════════════════════════════════════

    val SecureOffline = OnboardingScreen(
        id = 3,
        illustrationType = IllustrationType.SHIELD_LOCK,
        title = "Secure & Offline",
        titleUrdu = "محفوظ اور آفلائن",
        subtitle = "Your data stays on your phone\n— 100% private",
        subtitleUrdu = "آپ کا ڈیٹا آپ کے فون پر رہتا ہے\n— 100% نجی",
        features = listOf(
            "PIN + Biometric lock",
            "Automatic backup & restore",
            "Works without internet"
        ),
        featuresUrdu = listOf(
            "PIN + بائیو میٹرک لاک",
            "خودکار بیک اپ اور بحالی",
            "انٹرنیٹ کےبغیر کام کرتا ہے"
        ),
        showLanguageSelector = false,
        isLastScreen = true,
        buttonText = "Get Started",
        buttonTextUrdu = "شروع کریں",
        buttonUrduRoman = "Shuru Karein"
    )


    // ═══════════════════════════════════════════════════════════════
    // ALL SCREENS
    // ═══════════════════════════════════════════════════════════════

    val allScreens: List<OnboardingScreen> = listOf(
        Welcome,
        DigitalKhata,
        OcrScan,
        SecureOffline
    )
}
