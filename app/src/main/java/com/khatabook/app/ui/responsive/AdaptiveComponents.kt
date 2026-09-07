package com.khatabook.app.ui.responsive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Responsive card that adapts padding, corner radius, and elevation.
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val sizing = LocalResponsiveSizing.current
    val spacing = LocalResponsiveSpacing.current

    if (onClick != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(sizing.cardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = sizing.cardElevation)
        ) {
            Box(
                modifier = Modifier.padding(
                    if (LocalWindowSize.current.isCompact) spacing.cardPaddingCompact
                    else spacing.cardPadding
                )
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(sizing.cardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = sizing.cardElevation)
        ) {
            Box(
                modifier = Modifier.padding(
                    if (LocalWindowSize.current.isCompact) spacing.cardPaddingCompact
                    else spacing.cardPadding
                )
            ) {
                content()
            }
        }
    }
}

/**
 * Responsive stat card with icon, title, and amount.
 * Amount text has overflow protection.
 */
@Composable
fun ResponsiveStatCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color
) {
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current

    ResponsiveCard(
        modifier = modifier,
        containerColor = backgroundColor
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.listItemIconSize)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(spacing.buttonIconSize)
                    )
                }
                Spacer(modifier = Modifier.width(spacing.itemGap))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = typography.bodySm
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(spacing.itemGap))
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = typography.amountLg
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Responsive list item with avatar, title, subtitle, amount, and trailing content.
 * All text has overflow protection.
 */
@Composable
fun ResponsiveListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    amount: String? = null,
    avatar: String? = null,
    avatarColor: Color = MaterialTheme.colorScheme.primary,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    ResponsiveCard(
        modifier = modifier.then(clickableModifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.avatarSize)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatar ?: title.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = typography.labelMd
                        ),
                        color = avatarColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(spacing.itemGap + 4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = typography.bodyLg
                        ),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = typography.bodySm
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // Trailing: amount or custom content
            if (amount != null) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = typography.amountSm
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

/**
 * Responsive quick action card.
 * Minimum height ensures consistent card sizes even with short labels.
 */
@Composable
fun ResponsiveActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val sizing = LocalResponsiveSizing.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current

    ResponsiveCard(
        modifier = modifier.heightIn(min = 88.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.listItemIconSize + 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(spacing.buttonIconSize)
                )
            }
            Spacer(modifier = Modifier.height(spacing.itemGap))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = typography.labelMd
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Responsive settings item with icon, title, subtitle, and trailing chevron.
 * Divider is aligned to start after icon width for consistent visual hierarchy.
 */
@Composable
fun ResponsiveSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showTrailingIcon: Boolean = true,
    onClick: () -> Unit
) {
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val sizing = LocalResponsiveSizing.current

    val iconEndOffset = spacing.listItemIconSize + spacing.itemGap + 4.dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = spacing.minTouchTarget)
                .clickable(onClick = onClick)
                .padding(vertical = spacing.itemGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(spacing.listItemIconSize - 8.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(spacing.itemGap + 4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = typography.bodyLg
                    ),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = typography.bodySm
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (showTrailingIcon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(spacing.listItemIconSize - 16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = iconEndOffset),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = sizing.dividerThickness
        )
    }
}
