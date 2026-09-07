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
 * SESSION SECURITY CARD — Extra protection toggles
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  📱 Session Security                       │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  ○ Clear data on 3 failed attempts          │
 * │  ○ Hide sensitive data in background        │
 * │  ○ Block screenshots in app                 │
 * └────────────────────────────────────────────┘
 */
@Composable
fun SessionSecurityCard(
    clearOnFailedAttempts: Boolean,
    hideInBackground: Boolean,
    blockScreenshots: Boolean,
    onToggleClearOnFailed: (Boolean) -> Unit,
    onToggleHideInBackground: (Boolean) -> Unit,
    onToggleBlockScreenshots: (Boolean) -> Unit,
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
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                cardShape
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📱", fontSize = 18.sp)
            }

            Text(
                text = "Session Security",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        )

        // Toggle options
        SessionToggleItem(
            title = "Clear data on 3 failed attempts",
            description = "Erase app data after 3 wrong PIN attempts",
            isChecked = clearOnFailedAttempts,
            onToggle = onToggleClearOnFailed,
            accentColor = accentColor
        )

        SessionToggleItem(
            title = "Hide sensitive data in background",
            description = "Blur content when app is in background",
            isChecked = hideInBackground,
            onToggle = onToggleHideInBackground,
            accentColor = accentColor
        )

        SessionToggleItem(
            title = "Block screenshots in app",
            description = "Prevent screen capture of sensitive data",
            isChecked = blockScreenshots,
            onToggle = onToggleBlockScreenshots,
            accentColor = accentColor
        )
    }
}

@Composable
private fun SessionToggleItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Text
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Switch
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}
