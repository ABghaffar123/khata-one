package com.khatabook.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.khatabook.app.ui.responsive.ProvideResponsiveTokens
import com.khatabook.app.ui.responsive.ProvideWindowSize

// ═══════════════════════════════════════════════════════════
// LOCAL PROVIDERS
// ═══════════════════════════════════════════════════════════
val LocalKhataLanguage = staticCompositionLocalOf { "en" }

// ═══════════════════════════════════════════════════════════
// COLOR SCHEMES
// ═══════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface
)

// ═══════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════
/**
 * Khata One theme — Fully adaptive and responsive.
 *
 * Features:
 * - Light/Dark mode support
 * - RTL layout for Urdu language
 * - Status bar color matching theme
 * - Dynamic color on Android 12+ (optional)
 * - Responsive spacing, typography, and sizing via WindowSize
 *
 * @param language Current language code ("en", "ur", "ur-roman")
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Use Material You dynamic color (Android 12+)
 * @param content Composable content
 */
@Composable
fun KhataTheme(
    language: String = "en",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // RTL for Urdu language
    val layoutDirection = if (language == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

    // Status bar color
    val view = (LocalContext.current as? Activity)?.window?.decorView
    SideEffect {
        view?.let {
            WindowCompat.getInsetsController(
                (LocalContext.current as Activity).window,
                it
            ).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide responsive system + theme + localized strings
    val appStrings = com.khatabook.app.ui.language.getAppStrings(language)

    ProvideWindowSize {
        ProvideResponsiveTokens {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalKhataLanguage provides language,
                LocalLayoutDirection provides layoutDirection,
                com.khatabook.app.ui.language.LocalStrings provides appStrings
            ) {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = KhataTypography,
                    shapes = KhataShapes,
                    content = content
                )
            }
        }
    }
}
