package com.khatabook.app.ui.security

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * AUTO-LOCK CARD — Timeout duration selection
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  ⏱️ Auto-Lock                           ●  │
 * │  Lock app automatically when not in use    │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  ○  Immediately                              │
 * │  ●  After 1 minute                          │
 * │  ○  After 5 minutes                         │
 * │  ○  After 15 minutes                        │
 * │  ○  After 30 minutes                        │
 * │  ○  Never                                   │
 * └────────────────────────────────────────────┘
 */
@Composable
fun AutoLockCard(
    isEnabled: Boolean,
    selectedDuration: SecurityViewModel.AutoLockDuration,
    onToggle: (Boolean) -> Unit,
    onDurationSelected: (SecurityViewModel.AutoLockDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val accentColor = Color(0xFF1A73E8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isEnabled) accentColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                cardShape
            )
            .animateContentSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header row with title + switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⏱️", fontSize = 18.sp)
                }

                Column {
                    Text(
                        text = "Auto-Lock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lock app automatically when not in use",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }

        // Divider
        if (isEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            )
        }

        // Duration options (when enabled)
        if (isEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SecurityViewModel.AutoLockDuration.entries.forEach { duration ->
                    DurationOption(
                        duration = duration,
                        isSelected = selectedDuration == duration,
                        onClick = { onDurationSelected(duration) },
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationOption(
    duration: SecurityViewModel.AutoLockDuration,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.08f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "duration_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) accentColor
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "duration_text"
    )

    val icon = when (duration) {
        SecurityViewModel.AutoLockDuration.IMMEDIATELY -> "⚡"
        SecurityViewModel.AutoLockDuration.NEVER -> "🚫"
        else -> "⏱️"
    }

    val label = when (duration) {
        SecurityViewModel.AutoLockDuration.IMMEDIATELY -> "Immediately"
        SecurityViewModel.AutoLockDuration.ONE_MINUTE -> "After 1 minute"
        SecurityViewModel.AutoLockDuration.FIVE_MINUTES -> "After 5 minutes"
        SecurityViewModel.AutoLockDuration.FIFTEEN_MINUTES -> "After 15 minutes"
        SecurityViewModel.AutoLockDuration.THIRTY_MINUTES -> "After 30 minutes"
        SecurityViewModel.AutoLockDuration.NEVER -> "Never"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Radio button
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.background(accentColor)
                    } else {
                        Modifier.border(1.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // Icon
        Text(text = icon, fontSize = 14.sp)

        // Label
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
