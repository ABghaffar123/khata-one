package com.khatabook.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ═══════════════════════════════════════════════════════════════════
 * GRADIENT THEME PRESETS — 8 Premium Gradient Themes
 * ═══════════════════════════════════════════════════════════════════
 *
 * All gradients use 135° angle (top-left to bottom-right).
 * Each preset defines a complete gradient + derived color scheme.
 * Contrast logic auto-selects white or black text based on luminance.
 */
data class KhataGradientPreset(
    val id: String,
    val nameResKey: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val isDarkTheme: Boolean = false
) {
    /** The main 135° gradient used across the app */
    val gradient: Brush = Brush.linearGradient(
        colors = listOf(gradientStart, gradientEnd),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Reversed gradient for special elements */
    val reversedGradient: Brush = Brush.linearGradient(
        colors = listOf(gradientStart, gradientEnd),
        start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        end = androidx.compose.ui.geometry.Offset(0f, 0f)
    )

    /** Subtle gradient for cards/surfaces */
    val subtleGradient: Brush = Brush.linearGradient(
        colors = listOf(
            gradientStart.copy(alpha = 0.05f),
            gradientEnd.copy(alpha = 0.10f)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Surface tint gradient */
    val surfaceGradient: Brush = Brush.linearGradient(
        colors = listOf(
            gradientStart.copy(alpha = 0.08f),
            gradientEnd.copy(alpha = 0.12f)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Middle color derived from the two gradient endpoints */
    val middleColor: Color = Color(
        red = (gradientStart.red + gradientEnd.red) / 2,
        green = (gradientStart.green + gradientEnd.green) / 2,
        blue = (gradientStart.blue + gradientEnd.blue) / 2
    )

    /** Light variant of start color */
    val lightStart: Color = Color(
        red = (gradientStart.red + 1f) / 2,
        green = (gradientStart.green + 1f) / 2,
        blue = (gradientStart.blue + 1f) / 2
    )

    /** Light variant of end color */
    val lightEnd: Color = Color(
        red = (gradientEnd.red + 1f) / 2,
        green = (gradientEnd.green + 1f) / 2,
        blue = (gradientEnd.blue + 1f) / 2
    )

    /** Dark variant of start color */
    val darkStart: Color = Color(
        red = gradientStart.red * 0.7f,
        green = gradientStart.green * 0.7f,
        blue = gradientStart.blue * 0.7f
    )

    /** Dark variant of end color */
    val darkEnd: Color = Color(
        red = gradientEnd.red * 0.7f,
        green = gradientEnd.green * 0.7f,
        blue = gradientEnd.blue * 0.7f
    )

    /** Preview top bar color */
    val previewTopBarColor: Color = gradientStart

    /** Preview card color */
    val previewCardColor: Color = Color.White

    /** Preview nav color */
    val previewNavColor: Color = gradientStart.copy(alpha = 0.08f)


    // ═══════════════════════════════════════════════════════════
    // CONTRAST / READABILITY
    // ═══════════════════════════════════════════════════════════

    /** W3C relative luminance of the gradient midpoint */
    private val midLuminance: Float
        get() {
            val mid = middleColor
            return (0.299f * mid.red + 0.587f * mid.green + 0.114f * mid.blue)
        }

    /**
     * Whether this gradient needs dark text on top.
     * Bright gradients like Fresh Lime, Sunset Gold need dark text.
     */
    val needsDarkText: Boolean get() = midLuminance > 0.5f

    /** The correct text color for labels placed directly on the gradient */
    val onGradient: Color get() = if (needsDarkText) Color.Black else Color.White

    /** Slightly transparent version for secondary text on gradient */
    val onGradientSecondary: Color
        get() = if (needsDarkText) Color.Black.copy(alpha = 0.6f)
        else Color.White.copy(alpha = 0.8f)

    /** Icon color for active/selected states on gradient background */
    val onGradientIcon: Color
        get() = if (needsDarkText) Color.Black.copy(alpha = 0.8f)
        else Color.White.copy(alpha = 0.9f)
}

object KhataGradientPresets {

    // ═══════════════════════════════════════════════════════════════
    // 8 PREMIUM GRADIENT THEMES
    // ═══════════════════════════════════════════════════════════════

    val OceanBlue = KhataGradientPreset(
        id = "ocean_blue",
        nameResKey = "theme_ocean_blue",
        gradientStart = Color(0xFF00C9FF),
        gradientEnd = Color(0xFF0066FF)
    )

    val VioletSky = KhataGradientPreset(
        id = "violet_sky",
        nameResKey = "theme_violet_sky",
        gradientStart = Color(0xFF3E8EFF),
        gradientEnd = Color(0xFF7B2FF7)
    )

    val EmeraldTeal = KhataGradientPreset(
        id = "emerald_teal",
        nameResKey = "theme_emerald_teal",
        gradientStart = Color(0xFF00E0A8),
        gradientEnd = Color(0xFF00B4DB)
    )

    val FreshLime = KhataGradientPreset(
        id = "fresh_lime",
        nameResKey = "theme_fresh_lime",
        gradientStart = Color(0xFF8BFFAB),
        gradientEnd = Color(0xFF22D46B)
    )

    val SunsetGold = KhataGradientPreset(
        id = "sunset_gold",
        nameResKey = "theme_sunset_gold",
        gradientStart = Color(0xFFFFE259),
        gradientEnd = Color(0xFFFFA751)
    )

    val FireOrange = KhataGradientPreset(
        id = "fire_orange",
        nameResKey = "theme_fire_orange",
        gradientStart = Color(0xFFFF8008),
        gradientEnd = Color(0xFFFF3D00)
    )

    val CrimsonRed = KhataGradientPreset(
        id = "crimson_red",
        nameResKey = "theme_crimson_red",
        gradientStart = Color(0xFFFF416C),
        gradientEnd = Color(0xFFFF4B2B)
    )

    val RoyalPurple = KhataGradientPreset(
        id = "royal_purple",
        nameResKey = "theme_royal_purple",
        gradientStart = Color(0xFF8E2DE2),
        gradientEnd = Color(0xFFFF00CC)
    )


    // ═══════════════════════════════════════════════════════════════
    // ALL PRESETS — Ordered list for grid display
    // ═══════════════════════════════════════════════════════════════

    val allPresets: List<KhataGradientPreset> = listOf(
        OceanBlue,
        VioletSky,
        EmeraldTeal,
        FreshLime,
        SunsetGold,
        FireOrange,
        CrimsonRed,
        RoyalPurple
    )

    fun getById(id: String): KhataGradientPreset =
        allPresets.firstOrNull { it.id == id } ?: FireOrange


    // ═══════════════════════════════════════════════════════════════
    // DISPLAY MODE
    // ═══════════════════════════════════════════════════════════════

    enum class DisplayMode(val key: String) {
        LIGHT("light"),
        DARK("dark"),
        SYSTEM("system")
    }
}
