package com.khatabook.app.ui.security

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
 * PRIVACY SCORE CARD — Overall security health with checklist
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  🛡️  Your data is protected                 │
 * │                                              │
 * │  ●  Encryption       Active                  │
 * │  ●  App Lock         PIN + Biometric         │
 * │  ●  Auto-Lock        5 minutes               │
 * │  ○  Backup           Not configured          │
 * │                                              │
 * │  ████████████████████░░░░░░░░  75%           │
 * └────────────────────────────────────────────┘
 */
@Composable
fun PrivacyScoreCard(
    score: Int,
    level: SecurityViewModel.SecurityLevel,
    isPinEnabled: Boolean,
    isBiometricEnabled: Boolean,
    isAutoLockEnabled: Boolean,
    isEncryptionEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    val (levelColor, levelLabel) = when (level) {
        SecurityViewModel.SecurityLevel.EXCELLENT ->
            Color(0xFF4CAF50) to "Excellent"
        SecurityViewModel.SecurityLevel.GOOD ->
            Color(0xFF1A73E8) to "Good"
        SecurityViewModel.SecurityLevel.FAIR ->
            Color(0xFFFF9800) to "Fair"
        SecurityViewModel.SecurityLevel.WEAK ->
            Color(0xFFD32F2F) to "Weak"
    }

    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, levelColor.copy(alpha = 0.2f), cardShape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Shield icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 20.sp)
            }

            Column {
                Text(
                    text = "Security Score",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (level) {
                        SecurityViewModel.SecurityLevel.EXCELLENT -> "Excellent — Your data is well protected"
                        SecurityViewModel.SecurityLevel.GOOD -> "Good — Consider enabling more features"
                        SecurityViewModel.SecurityLevel.FAIR -> "Fair — Some protections are missing"
                        SecurityViewModel.SecurityLevel.WEAK -> "Weak — Enable more security features"
                    },
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
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        )

        // Checklist
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecurityCheckItem(
                label = "Encryption",
                isActive = isEncryptionEnabled,
                activeText = "Active",
                inactiveText = "Not configured"
            )
            SecurityCheckItem(
                label = "App Lock",
                isActive = isPinEnabled || isBiometricEnabled,
                activeText = if (isPinEnabled && isBiometricEnabled) "PIN + Biometric"
                else if (isPinEnabled) "PIN Only"
                else "Biometric Only",
                inactiveText = "Not configured"
            )
            SecurityCheckItem(
                label = "Auto-Lock",
                isActive = isAutoLockEnabled,
                activeText = "Enabled",
                inactiveText = "Disabled"
            )
            SecurityCheckItem(
                label = "Backup",
                isActive = false, // Would be connected to backup system
                activeText = "Configured",
                inactiveText = "Not configured"
            )
        }

        // Progress bar
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = levelLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = levelColor
                )
                Text(
                    text = "$score%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(levelColor)
                )
            }
        }
    }
}

@Composable
private fun SecurityCheckItem(
    label: String,
    isActive: Boolean,
    activeText: String,
    inactiveText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Circle indicator
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isActive) {
                        Modifier.background(Color(0xFF4CAF50))
                    } else {
                        Modifier
                            .border(1.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Label
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Status text
        Text(
            text = if (isActive) activeText else inactiveText,
            fontSize = 12.sp,
            color = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
