package com.khatabook.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * THEME PREVIEW — Live preview of selected gradient theme in a phone mockup
 * ═══════════════════════════════════════════════════════════════════
 *
 * Shows a mini phone frame with:
 *   - TopBar (gradient)
 *   - Stats card
 *   - Two customer list items
 *   - Bottom navigation bar
 *
 * Updates LIVE as user selects different gradient themes.
 */
@Composable
fun ThemePreview(
    theme: KhataGradientPreset,
    modifier: Modifier = Modifier
) {
    val phoneShape = RoundedCornerShape(24.dp)
    val phoneBorderWidth = 2.dp

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Text(
            text = "Live Preview",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Phone frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = phoneShape,
                    ambientColor = theme.gradientStart.copy(alpha = 0.15f),
                    spotColor = theme.gradientStart.copy(alpha = 0.2f)
                )
                .clip(phoneShape)
                .border(phoneBorderWidth, theme.gradientStart.copy(alpha = 0.2f), phoneShape)
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── TopBar (Gradient) ──────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(theme.gradient)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📒 Khata Book",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // ── Body ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Dues Stats Card (gradient subtle)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.subtleGradient)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Total Dues",
                                fontSize = 10.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Rs 45,000",
                                    fontSize = 18.sp,
                                    color = Color(0xFF1A1A1A),
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(theme.gradientStart.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "+12%",
                                        fontSize = 8.sp,
                                        color = theme.gradientStart,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFE0E0E0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.67f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(theme.gradient)
                                )
                            }
                        }
                    }

                    // Customer Card 1
                    PreviewCustomerCard(
                        name = "Ahmed Khan",
                        amount = "Rs 5,000",
                        theme = theme,
                        isCreditor = true
                    )

                    // Customer Card 2
                    PreviewCustomerCard(
                        name = "Ali Khan",
                        amount = "Rs 3,000",
                        theme = theme,
                        isCreditor = false
                    )
                }

                // ── Bottom Navigation ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf("🏠", "👥", "📷", "⚙️")
                    val navLabels = listOf("Home", "Khata", "Camera", "Settings")

                    navItems.forEachIndexed { index, icon ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = icon,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .then(
                                        if (index == 0) {
                                            Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.gradientStart.copy(alpha = 0.12f))
                                                .padding(2.dp)
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                            Text(
                                text = navLabels[index],
                                fontSize = 6.sp,
                                color = if (index == 0) theme.gradientStart
                                else Color.Gray,
                                fontWeight = if (index == 0) FontWeight.Bold
                                else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCustomerCard(
    name: String,
    amount: String,
    theme: KhataGradientPreset,
    isCreditor: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                width = 0.5.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCreditor) theme.gradientStart.copy(alpha = 0.12f)
                        else theme.gradientEnd.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCreditor) theme.gradientStart else theme.gradientEnd
                )
            }

            Column {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = if (isCreditor) "Credit" else "Payment",
                    fontSize = 7.sp,
                    color = Color(0xFF999999)
                )
            }
        }

        // Amount chip (gradient)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(theme.subtleGradient)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = amount,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = theme.gradientStart
            )
        }
    }
}
