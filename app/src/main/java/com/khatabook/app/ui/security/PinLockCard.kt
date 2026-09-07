package com.khatabook.app.ui.security

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
 * PIN LOCK CARD — PIN configuration with dots and actions
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  🔐 PIN Lock                            ●  │
 * │  Secure your app with a 4-6 digit PIN      │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐     │
 * │  │  ●   │ │  ●   │ │  ●   │ │  ○   │     │
 * │  └──────┘ └──────┘ └──────┘ └──────┘     │
 * │                                              │
 * │  [ Change PIN ]    [ Remove PIN ]            │
 * └────────────────────────────────────────────┘
 */
@Composable
fun PinLockCard(
    isEnabled: Boolean,
    pinDots: List<Boolean>,
    onToggle: (Boolean) -> Unit,
    onChangePin: () -> Unit,
    onRemovePin: () -> Unit,
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
                    Text(text = "🔐", fontSize = 18.sp)
                }

                Column {
                    Text(
                        text = "PIN Lock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Secure your app with a 4-6 digit PIN",
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

        // PIN dots (only when enabled)
        if (isEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show 4 PIN dots
                val dotCount = pinDots.size.coerceAtMost(4)
                repeat(dotCount) { index ->
                    PinDot(
                        isFilled = pinDots.getOrElse(index) { false },
                        accentColor = accentColor
                    )
                    if (index < dotCount - 1) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onChangePin,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    ),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Change PIN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onRemovePin,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Remove PIN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFilled) accentColor.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            )
            .border(
                1.5.dp,
                if (isFilled) accentColor.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}
