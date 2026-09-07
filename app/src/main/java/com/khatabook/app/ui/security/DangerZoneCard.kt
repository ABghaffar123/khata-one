package com.khatabook.app.ui.security

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
 * DANGER ZONE CARD — Destructive actions with red styling
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  ⚠️ Danger Zone                             │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  [ Remove All Security ]    🔴              │
 * │  [ Reset App Data ]         🔴              │
 * └────────────────────────────────────────────┘
 */
@Composable
fun DangerZoneCard(
    onRemoveSecurity: () -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val dangerColor = Color(0xFFD32F2F)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, cardShape)
            .clip(cardShape)
            .background(dangerColor.copy(alpha = 0.03f))
            .border(1.dp, dangerColor.copy(alpha = 0.15f), cardShape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(dangerColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 18.sp)
            }

            Column {
                Text(
                    text = "Danger Zone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dangerColor
                )
                Text(
                    text = "These actions cannot be undone",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dangerColor.copy(alpha = 0.1f))
        )

        // Remove All Security button
        OutlinedButton(
            onClick = onRemoveSecurity,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = dangerColor
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(dangerColor.copy(alpha = 0.4f))
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🔴", fontSize = 14.sp)
                Text(
                    text = "Remove All Security",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Reset App Data button
        OutlinedButton(
            onClick = onResetData,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = dangerColor
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(dangerColor.copy(alpha = 0.4f))
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🔴", fontSize = 14.sp)
                Text(
                    text = "Reset App Data",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
