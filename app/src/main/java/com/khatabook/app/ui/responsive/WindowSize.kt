package com.khatabook.app.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides current window size information to the entire composition tree.
 *
 * Usage:
 * ```
 * val windowSize = LocalWindowSize.current
 * if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) {
 *     // Show two-pane layout
 * }
 * ```
 */
@Immutable
data class WindowSize(
    val widthDp: Dp,
    val heightDp: Dp,
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
) {
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == WindowWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded
    val isLarge: Boolean get() = widthSizeClass == WindowWidthSizeClass.Large

    val isPortrait: Boolean get() = heightDp > widthDp
    val isLandscape: Boolean get() = widthDp > heightDp

    /** Whether to show navigation rail instead of bottom bar */
    val showNavigationRail: Boolean get() = widthSizeClass >= WindowWidthSizeClass.Expanded

    /** Whether to use list-detail canonical layout */
    val useListDetailLayout: Boolean get() = widthSizeClass >= WindowWidthSizeClass.Expanded

    /** Maximum content width to prevent over-stretching on large screens */
    val maxContentWidth: Dp get() = when {
        widthSizeClass == WindowWidthSizeClass.Large -> 840.dp
        widthSizeClass == WindowWidthSizeClass.Expanded -> 720.dp
        else -> widthDp  // On compact/medium, use full width
    }

    /** Horizontal margin to center content on large screens */
    val horizontalMargin: Dp get() = if (widthDp > maxContentWidth) {
        (widthDp - maxContentWidth) / 2
    } else {
        0.dp
    }

    /** Number of columns for grid layouts */
    val gridColumns: Int get() = when {
        widthSizeClass >= WindowWidthSizeClass.Large -> 4
        widthSizeClass >= WindowWidthSizeClass.Expanded -> 3
        widthSizeClass >= WindowWidthSizeClass.Medium -> 2
        else -> 1
    }
}

/**
 * CompositionLocal for providing WindowSize to the tree.
 */
val LocalWindowSize = staticCompositionLocalOf {
    WindowSize(
        widthDp = 360.dp,
        heightDp = 640.dp,
        widthSizeClass = WindowWidthSizeClass.Compact,
        heightSizeClass = WindowHeightSizeClass.Medium
    )
}

/**
 * Composable that provides WindowSize to the composition tree.
 * Wrap the app content with this to enable responsive layouts.
 */
@Composable
fun ProvideWindowSize(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val widthSizeClass = when {
        widthDp >= Breakpoints.Large -> WindowWidthSizeClass.Large
        widthDp >= Breakpoints.Expanded -> WindowWidthSizeClass.Expanded
        widthDp >= Breakpoints.Medium -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Compact
    }

    val heightSizeClass = when {
        heightDp >= 900.dp -> WindowHeightSizeClass.Large
        heightDp >= 480.dp -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Compact
    }

    val windowSize = WindowSize(
        widthDp = widthDp,
        heightDp = heightDp,
        widthSizeClass = widthSizeClass,
        heightSizeClass = heightSizeClass
    )

    CompositionLocalProvider(LocalWindowSize provides windowSize) {
        content()
    }
}
