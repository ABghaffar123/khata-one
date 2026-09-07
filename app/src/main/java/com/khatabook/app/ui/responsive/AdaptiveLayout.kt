package com.khatabook.app.ui.responsive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Max-width container that prevents content from stretching on large screens.
 *
 * On compact/medium screens: fills available width with screen padding
 * On expanded/large screens: centers content with max width
 */
@Composable
fun MaxWidthContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val windowSize = LocalWindowSize.current
    val spacing = LocalResponsiveSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = spacing.screenHorizontal + windowSize.horizontalMargin
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Responsive horizontal padding modifier.
 */
@Composable
fun Modifier.responsivePadding(
    horizontal: Boolean = true,
    vertical: Boolean = true
): Modifier {
    val spacing = LocalResponsiveSpacing.current
    return this.padding(
        horizontal = if (horizontal) spacing.screenHorizontal else 0.dp,
        vertical = if (vertical) spacing.screenVertical else 0.dp
    )
}

/**
 * Responsive grid that adapts column count to screen size.
 *
 * Compact: 1 column
 * Medium: 2 columns
 * Expanded: 3 columns
 * Large: 4 columns
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    columns: Int? = null, // null = auto from window size
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: LazyGridScope.() -> Unit
) {
    val windowSize = LocalWindowSize.current
    val effectiveColumns = columns ?: windowSize.gridColumns

    LazyVerticalGrid(
        columns = GridCells.Fixed(effectiveColumns),
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * Responsive row that adapts layout based on available width.
 *
 * On compact screens: horizontal row with tighter spacing
 * On medium+ screens: horizontal row with more spacing
 */
@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Responsive section with optional title and content.
 * Adapts spacing and padding based on screen size.
 */
@Composable
fun ResponsiveSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalResponsiveSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.sectionGap / 2),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
    ) {
        if (title != null) {
            SectionHeader(
                title = title,
                actionText = actionText,
                onAction = onAction
            )
        }
        content()
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val typography = LocalResponsiveTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                fontSize = typography.titleMd
            ),
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        if (actionText != null && onAction != null) {
            androidx.compose.material3.Text(
                text = actionText,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                    fontSize = typography.labelLg
                ),
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Two-column stats layout that adapts to screen size.
 *
 * Compact: 2 equal columns
 * Medium: 2 equal columns with more spacing
 * Expanded: 2 equal columns with even more spacing
 * Large: Can be 3 or 4 columns
 */
@Composable
fun StatsRow(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable RowScope.() -> Unit
) {
    val spacing = LocalResponsiveSpacing.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.statsCardGap),
        content = content
    )
}
