package com.khatabook.app.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal providers for responsive design tokens.
 *
 * These locals are provided at the root of the composition tree
 * and provide screen-size-aware values throughout the app.
 *
 * Usage in any composable:
 * ```
 * val spacing = LocalResponsiveSpacing.current
 * val typography = LocalResponsiveTypography.current
 * val sizing = LocalResponsiveSizing.current
 * ```
 */

val LocalResponsiveSpacing = staticCompositionLocalOf { CompactSpacing }
val LocalResponsiveTypography = staticCompositionLocalOf { CompactTypography }
val LocalResponsiveSizing = staticCompositionLocalOf { CompactSizing }

/**
 * Provides all responsive locals to the composition tree.
 * Should be called inside ProvideWindowSize.
 */
@Composable
fun ProvideResponsiveTokens(content: @Composable () -> Unit) {
    val spacing = responsiveSpacing()
    val typography = responsiveTypography()
    val sizing = responsiveSizing()

    CompositionLocalProvider(
        LocalResponsiveSpacing provides spacing,
        LocalResponsiveTypography provides typography,
        LocalResponsiveSizing provides sizing
    ) {
        content()
    }
}
