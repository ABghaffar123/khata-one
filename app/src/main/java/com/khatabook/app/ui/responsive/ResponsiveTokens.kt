package com.khatabook.app.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatabook.app.ui.responsive.WindowWidthSizeClass.*

/**
 * Responsive design tokens that adapt to screen size.
 *
 * These tokens ensure consistent spacing, sizing, and typography
 * across all device sizes while maintaining visual balance.
 *
 * Usage:
 * ```
 * val spacing = LocalResponsiveSpacing.current
 * Modifier.padding(horizontal = spacing.screenHorizontal)
 * ```
 */

// ═══════════════════════════════════════════════════════════
// SPACING TOKENS
// ═══════════════════════════════════════════════════════════

@Immutable
data class ResponsiveSpacing(
    /** Horizontal padding for screen edges */
    val screenHorizontal: Dp,
    /** Vertical padding for screen edges */
    val screenVertical: Dp,
    /** Padding between sections */
    val sectionGap: Dp,
    /** Padding between items in a list */
    val itemGap: Dp,
    /** Padding inside cards */
    val cardPadding: Dp,
    /** Padding inside cards (compact) */
    val cardPaddingCompact: Dp,
    /** Gap between stats cards */
    val statsCardGap: Dp,
    /** Gap between quick action cards */
    val actionCardGap: Dp,
    /** Icon size for list items */
    val listItemIconSize: Dp,
    /** Icon size for avatars */
    val avatarSize: Dp,
    /** Icon size inside buttons */
    val buttonIconSize: Dp,
    /** FAB size */
    val fabSize: Dp,
    /** Top bar height */
    val topBarHeight: Dp,
    /** Bottom bar height */
    val bottomBarHeight: Dp,
    /** Navigation rail width */
    val navigationRailWidth: Dp,
    /** Min touch target size */
    val minTouchTarget: Dp
)

val CompactSpacing = ResponsiveSpacing(
    screenHorizontal = 16.dp,
    screenVertical = 8.dp,
    sectionGap = 16.dp,
    itemGap = 8.dp,
    cardPadding = 16.dp,
    cardPaddingCompact = 12.dp,
    statsCardGap = 12.dp,
    actionCardGap = 10.dp,
    listItemIconSize = 40.dp,
    avatarSize = 40.dp,
    buttonIconSize = 20.dp,
    fabSize = 56.dp,
    topBarHeight = 64.dp,
    bottomBarHeight = 80.dp,
    navigationRailWidth = 80.dp,
    minTouchTarget = 48.dp
)

val MediumSpacing = ResponsiveSpacing(
    screenHorizontal = 20.dp,
    screenVertical = 12.dp,
    sectionGap = 20.dp,
    itemGap = 10.dp,
    cardPadding = 20.dp,
    cardPaddingCompact = 14.dp,
    statsCardGap = 14.dp,
    actionCardGap = 12.dp,
    listItemIconSize = 44.dp,
    avatarSize = 44.dp,
    buttonIconSize = 22.dp,
    fabSize = 56.dp,
    topBarHeight = 64.dp,
    bottomBarHeight = 80.dp,
    navigationRailWidth = 88.dp,
    minTouchTarget = 48.dp
)

val ExpandedSpacing = ResponsiveSpacing(
    screenHorizontal = 24.dp,
    screenVertical = 16.dp,
    sectionGap = 24.dp,
    itemGap = 12.dp,
    cardPadding = 24.dp,
    cardPaddingCompact = 16.dp,
    statsCardGap = 16.dp,
    actionCardGap = 14.dp,
    listItemIconSize = 48.dp,
    avatarSize = 48.dp,
    buttonIconSize = 24.dp,
    fabSize = 56.dp,
    topBarHeight = 64.dp,
    bottomBarHeight = 80.dp,
    navigationRailWidth = 96.dp,
    minTouchTarget = 48.dp
)

val LargeSpacing = ResponsiveSpacing(
    screenHorizontal = 32.dp,
    screenVertical = 20.dp,
    sectionGap = 28.dp,
    itemGap = 14.dp,
    cardPadding = 28.dp,
    cardPaddingCompact = 18.dp,
    statsCardGap = 18.dp,
    actionCardGap = 16.dp,
    listItemIconSize = 52.dp,
    avatarSize = 52.dp,
    buttonIconSize = 24.dp,
    fabSize = 56.dp,
    topBarHeight = 64.dp,
    bottomBarHeight = 80.dp,
    navigationRailWidth = 104.dp,
    minTouchTarget = 48.dp
)

// ═══════════════════════════════════════════════════════════
// TYPOGRAPHY SCALE TOKENS
// ═══════════════════════════════════════════════════════════

@Immutable
data class ResponsiveTypography(
    /** Display text size */
    val display: TextUnit,
    /** Headline 1 size */
    val h1: TextUnit,
    /** Headline 2 size */
    val h2: TextUnit,
    /** Headline 3 size */
    val h3: TextUnit,
    /** Title large size */
    val titleLg: TextUnit,
    /** Title medium size */
    val titleMd: TextUnit,
    /** Body large size */
    val bodyLg: TextUnit,
    /** Body medium size */
    val bodyMd: TextUnit,
    /** Body small size */
    val bodySm: TextUnit,
    /** Label large size */
    val labelLg: TextUnit,
    /** Label medium size */
    val labelMd: TextUnit,
    /** Label small size */
    val labelSm: TextUnit,
    /** Amount display size */
    val amountLg: TextUnit,
    /** Amount medium size */
    val amountMd: TextUnit,
    /** Amount small size */
    val amountSm: TextUnit
)

val CompactTypography = ResponsiveTypography(
    display = 36.sp,
    h1 = 28.sp,
    h2 = 22.sp,
    h3 = 18.sp,
    titleLg = 18.sp,
    titleMd = 16.sp,
    bodyLg = 16.sp,
    bodyMd = 14.sp,
    bodySm = 12.sp,
    labelLg = 14.sp,
    labelMd = 12.sp,
    labelSm = 10.sp,
    amountLg = 24.sp,
    amountMd = 18.sp,
    amountSm = 14.sp
)

val MediumTypography = ResponsiveTypography(
    display = 40.sp,
    h1 = 30.sp,
    h2 = 24.sp,
    h3 = 20.sp,
    titleLg = 20.sp,
    titleMd = 16.sp,
    bodyLg = 16.sp,
    bodyMd = 14.sp,
    bodySm = 12.sp,
    labelLg = 14.sp,
    labelMd = 12.sp,
    labelSm = 11.sp,
    amountLg = 28.sp,
    amountMd = 20.sp,
    amountSm = 14.sp
)

val ExpandedTypography = ResponsiveTypography(
    display = 44.sp,
    h1 = 32.sp,
    h2 = 26.sp,
    h3 = 22.sp,
    titleLg = 22.sp,
    titleMd = 18.sp,
    bodyLg = 18.sp,
    bodyMd = 16.sp,
    bodySm = 14.sp,
    labelLg = 16.sp,
    labelMd = 14.sp,
    labelSm = 12.sp,
    amountLg = 32.sp,
    amountMd = 22.sp,
    amountSm = 16.sp
)

val LargeTypography = ResponsiveTypography(
    display = 48.sp,
    h1 = 36.sp,
    h2 = 28.sp,
    h3 = 24.sp,
    titleLg = 24.sp,
    titleMd = 20.sp,
    bodyLg = 20.sp,
    bodyMd = 18.sp,
    bodySm = 16.sp,
    labelLg = 18.sp,
    labelMd = 16.sp,
    labelSm = 14.sp,
    amountLg = 36.sp,
    amountMd = 24.sp,
    amountSm = 18.sp
)

// ═══════════════════════════════════════════════════════════
// SIZING TOKENS
// ═══════════════════════════════════════════════════════════

@Immutable
data class ResponsiveSizing(
    /** Card corner radius */
    val cardRadius: Dp,
    /** Button corner radius */
    val buttonRadius: Dp,
    /** Input corner radius */
    val inputRadius: Dp,
    /** Chip corner radius */
    val chipRadius: Dp,
    /** Card elevation */
    val cardElevation: Dp,
    /** Divider thickness */
    val dividerThickness: Dp,
    /** Icon button size */
    val iconButtonSize: Dp,
    /** Switch width */
    val switchWidth: Dp,
    /** Switch height */
    val switchHeight: Dp
)

val CompactSizing = ResponsiveSizing(
    cardRadius = 12.dp,
    buttonRadius = 12.dp,
    inputRadius = 12.dp,
    chipRadius = 8.dp,
    cardElevation = 1.dp,
    dividerThickness = 0.5.dp,
    iconButtonSize = 40.dp,
    switchWidth = 52.dp,
    switchHeight = 32.dp
)

val MediumSizing = ResponsiveSizing(
    cardRadius = 14.dp,
    buttonRadius = 14.dp,
    inputRadius = 14.dp,
    chipRadius = 10.dp,
    cardElevation = 2.dp,
    dividerThickness = 0.5.dp,
    iconButtonSize = 44.dp,
    switchWidth = 52.dp,
    switchHeight = 32.dp
)

val ExpandedSizing = ResponsiveSizing(
    cardRadius = 16.dp,
    buttonRadius = 16.dp,
    inputRadius = 16.dp,
    chipRadius = 12.dp,
    cardElevation = 2.dp,
    dividerThickness = 1.dp,
    iconButtonSize = 48.dp,
    switchWidth = 56.dp,
    switchHeight = 32.dp
)

val LargeSizing = ResponsiveSizing(
    cardRadius = 16.dp,
    buttonRadius = 16.dp,
    inputRadius = 16.dp,
    chipRadius = 12.dp,
    cardElevation = 2.dp,
    dividerThickness = 1.dp,
    iconButtonSize = 48.dp,
    switchWidth = 56.dp,
    switchHeight = 32.dp
)

// ═══════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════

/**
 * Get responsive spacing based on current window size class.
 */
@Composable
fun responsiveSpacing(): ResponsiveSpacing = when (LocalWindowSize.current.widthSizeClass) {
    Large -> LargeSpacing
    Expanded -> ExpandedSpacing
    Medium -> MediumSpacing
    Compact -> CompactSpacing
}

/**
 * Get responsive typography based on current window size class.
 */
@Composable
fun responsiveTypography(): ResponsiveTypography = when (LocalWindowSize.current.widthSizeClass) {
    Large -> LargeTypography
    Expanded -> ExpandedTypography
    Medium -> MediumTypography
    Compact -> CompactTypography
}

/**
 * Get responsive sizing based on current window size class.
 */
@Composable
fun responsiveSizing(): ResponsiveSizing = when (LocalWindowSize.current.widthSizeClass) {
    Large -> LargeSizing
    Expanded -> ExpandedSizing
    Medium -> MediumSizing
    Compact -> CompactSizing
}
