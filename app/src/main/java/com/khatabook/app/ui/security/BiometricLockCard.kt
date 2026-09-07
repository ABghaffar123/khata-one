package com.khatabook.app.ui.security

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
 * BIOMETRIC LOCK CARD — Fingerprint / Face unlock configuration
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  👆 Biometric Lock                      ●  │
 * │  Use fingerprint or face to unlock          │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  ┌──────────────────────────────────┐      │
 * │  │  ✅ Active (2 fingerprints saved)│      │
 * │  └──────────────────────────────────┘      │
 * │                                              │
 * │  [ Test Biometric ]                          │
 * └────────────────────────────────────────────┘
 */
@Composable
fun BiometricLockCard(
    isEnabled: Boolean,
    isAvailable: Boolean,
    savedFingerprints: Int,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val accentColor = Color(0xFF1A73E8)
    val greenColor = Color(0xFF4CAF50)

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
                    Text(text = "👆", fontSize = 18.sp)
                }

                Column {
                    Text(
                        text = "Biometric Lock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isAvailable) "Use fingerprint or face to unlock"
                        else "Not available on this device",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                enabled = isAvailable,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
                    disabledCheckedTrackColor = accentColor.copy(alpha = 0.4f),
                    disabledUncheckedTrackColor = Color.Gray.copy(alpha = 0.15f)
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

        // Status pill (when enabled)
        if (isEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(greenColor.copy(alpha = 0.1f))
                        .border(1.dp, greenColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(greenColor)
                        )
                        Text(
                            text = "Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = greenColor
                        )
                        if (savedFingerprints > 0) {
                            Text(
                                text = "($savedFingerprints fingerprint${if (savedFingerprints > 1) "s" else ""} saved)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Test button
            OutlinedButton(
                onClick = onTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor
                ),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Test Biometric",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
