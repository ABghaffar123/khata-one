package com.khatabook.app.ui.responsive

import androidx.compose.ui.unit.dp

/**
 * Screen size breakpoints for Material 3 adaptive design.
 *
 * Based on Material Design 3 canonical device sizes:
 * - Compact: Small phones (< 600dp width)
 * - Medium: Regular phones, small foldables (600-840dp)
 * - Expanded: Large phones, tablets in portrait (840-1200dp)
 * - Large: Tablets in landscape, desktop (>= 1200dp)
 *
 * Reference: https://m3.material.io/foundations/layout/applying-layout
 */
object Breakpoints {
    val Compact = 0.dp
    val Medium = 600.dp
    val Expanded = 840.dp
    val Large = 1200.dp

    // Convenience max widths
    val CompactMax = Medium - 0.01.dp
    val MediumMax = Expanded - 0.01.dp
    val ExpandedMax = Large - 0.01.dp
}

/**
 * Device width categories for responsive decisions.
 */
enum class WindowWidthSizeClass {
    /** Small phones (< 600dp) — single column, bottom nav */
    Compact,
    /** Regular phones, small foldables (600-840dp) — wider cards, possible rail */
    Medium,
    /** Large phones, tablets portrait (840-1200dp) — list-detail, navigation rail */
    Expanded,
    /** Tablets landscape, desktop (>= 1200dp) — multi-pane, full navigation */
    Large
}

/**
 * Device height categories for responsive decisions.
 */
enum class WindowHeightSizeClass {
    /** Short devices (< 480dp) — compact layouts */
    Compact,
    /** Regular devices (480-900dp) — standard layouts */
    Medium,
    /** Tall devices (>= 900dp) — expanded content possible */
    Large
}

/**
 * Fold posture for foldable devices.
 */
enum class FoldPosture {
    /** Flat (unfolded) — full screen */
    Flat,
    /** Half-open (book mode) — dual pane possible */
    HalfOpened,
    /** Closed (normal phone mode) */
    Closed
}
