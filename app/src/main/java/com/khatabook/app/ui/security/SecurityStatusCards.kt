package com.khatabook.app.ui.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * SECURITY STATUS CARDS — Safe / Weak / Off indicators
 * ═══════════════════════════════════════════════════════════════════
 *
 * Three equal-width cards showing security status:
 *   ✅ SAFE   — Green border + icon (configured & active)
 *   ⚠️ WEAK   — Orange border + icon (partially configured)
 *   🔴 OFF    — Red border + icon (not configured)
 */
@Composable
fun SecurityStatusCards(
    pinStatus: SecurityViewModel.StatusType,
    biometricStatus: SecurityViewModel.StatusType,
    encryptionStatus: SecurityViewModel.StatusType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatusCard(
            status = pinStatus,
            icon = "🔐",
            label = "PIN",
            detail = when (pinStatus) {
                SecurityViewModel.StatusType.SAFE -> "Set"
                SecurityViewModel.StatusType.WEAK -> "Recommended"
                SecurityViewModel.StatusType.OFF -> "Not set"
            },
            modifier = Modifier.weight(1f)
        )

        StatusCard(
            status = biometricStatus,
            icon = "👆",
            label = "Biometric",
            detail = when (biometricStatus) {
                SecurityViewModel.StatusType.SAFE -> "Active"
                SecurityViewModel.StatusType.WEAK -> "Available"
                SecurityViewModel.StatusType.OFF -> "Off"
            },
            modifier = Modifier.weight(1f)
        )

        StatusCard(
            status = encryptionStatus,
            icon = "🔒",
            label = "Encryption",
            detail = when (encryptionStatus) {
                SecurityViewModel.StatusType.SAFE -> "Active"
                SecurityViewModel.StatusType.WEAK -> "Partial"
                SecurityViewModel.StatusType.OFF -> "Off"
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusCard(
    status: SecurityViewModel.StatusType,
    icon: String,
    label: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when (status) {
        SecurityViewModel.StatusType.SAFE ->
            Color(0xFF4CAF50) to Color(0xFF4CAF50).copy(alpha = 0.08f)
        SecurityViewModel.StatusType.WEAK ->
            Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.08f)
        SecurityViewModel.StatusType.OFF ->
            Color(0xFFD32F2F) to Color(0xFFD32F2F).copy(alpha = 0.08f)
    }

    val cardShape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .shadow(2.dp, cardShape)
            .clip(cardShape)
            .background(statusBg)
            .border(1.5.dp, statusColor.copy(alpha = 0.3f), cardShape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Status icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
        }

        // Status text
        Text(
            text = when (status) {
                SecurityViewModel.StatusType.SAFE -> "SAFE"
                SecurityViewModel.StatusType.WEAK -> "WEAK"
                SecurityViewModel.StatusType.OFF -> "OFF"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = statusColor,
            letterSpacing = 1.sp
        )

        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Detail
        Text(
            text = detail,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
