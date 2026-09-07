package com.khatabook.app.ui.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE PREVIEW — Live preview of selected language in phone mockup
 * ═══════════════════════════════════════════════════════════════════
 *
 * Shows a mini phone frame with:
 *   - TopBar with language-specific text
 *   - Stats card
 *   - Two customer list items
 *   - Bottom navigation
 *
 * Updates LIVE as user selects different languages.
 * Flips RTL when Urdu is selected.
 */
@Composable
fun LanguagePreview(
    language: KhataLanguage,
    modifier: Modifier = Modifier
) {
    val phoneShape = RoundedCornerShape(24.dp)
    val accentColor = Color(0xFF1A73E8)
    val primaryColor = Color(0xFF1A73E8)
    val greenAccent = Color(0xFF00BFA5)

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
        val phoneContent: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, phoneShape)
                    .clip(phoneShape)
                    .border(2.dp, primaryColor.copy(alpha = 0.15f), phoneShape)
                    .background(Color(0xFFFAFAFA)),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ── TopBar ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(primaryColor)
                            .padding(horizontal = 12.dp),
                        contentAlignment = if (language.isRtl) Alignment.CenterEnd
                        else Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (language.isRtl) Arrangement.SpaceBetween
                            else Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PreviewText(
                                text = "📒 Khata Book",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                isRtl = language.isRtl
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚙️", fontSize = 10.sp)
                            }
                        }
                    }

                    // ── Body ────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Stats Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F3F4))
                                .padding(10.dp)
                        ) {
                            Column {
                                PreviewText(
                                    text = language.sampleTexts.totalDues,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    isRtl = language.isRtl
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PreviewText(
                                        text = language.sampleTexts.amount,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1C1B1F),
                                        fontWeight = FontWeight.Bold,
                                        isRtl = language.isRtl
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(greenAccent.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "+12%",
                                            fontSize = 7.sp,
                                            color = greenAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Customer Card 1
                        PreviewCustomerCard(
                            name = "Ahmed Khan",
                            amount = "Rs 5,000",
                            isCreditor = true,
                            isRtl = language.isRtl,
                            accentColor = accentColor
                        )

                        // Customer Card 2
                        PreviewCustomerCard(
                            name = "Ali Khan",
                            amount = "Rs 3,000",
                            isCreditor = false,
                            isRtl = language.isRtl,
                            accentColor = greenAccent
                        )
                    }

                    // ── Bottom Navigation ────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(Color(0xFFF1F3F4))
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navItems = listOf("🏠", "👥", "📷", "⚙️")
                        val navLabels = listOf(
                            language.sampleTexts.homeLabel,
                            language.sampleTexts.customersLabel,
                            language.sampleTexts.cameraLabel,
                            language.sampleTexts.settingsLabel
                        )

                        navItems.forEachIndexed { index, icon ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = icon,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = navLabels[index],
                                    fontSize = 5.sp,
                                    color = if (index == 0) primaryColor
                                    else Color.Gray,
                                    fontWeight = if (index == 0) FontWeight.Bold
                                    else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Wrap content with RTL if needed
        if (language.isRtl) {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                phoneContent()
            }
        } else {
            phoneContent()
        }
    }
}

@Composable
private fun PreviewCustomerCard(
    name: String,
    amount: String,
    isCreditor: Boolean,
    isRtl: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(0.5.dp, Color.Black.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Column {
                PreviewText(
                    text = name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    isRtl = isRtl
                )
                PreviewText(
                    text = if (isCreditor) "Credit" else "Payment",
                    fontSize = 6.sp,
                    color = Color.Gray,
                    isRtl = isRtl
                )
            }
        }

        // Amount chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(accentColor.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = amount,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun PreviewText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    isRtl: Boolean,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
            maxLines = 1,
            modifier = modifier
        )
    }

    if (isRtl) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            content()
        }
    } else {
        content()
    }
}
