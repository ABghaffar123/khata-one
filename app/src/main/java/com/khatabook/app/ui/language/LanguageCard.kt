package com.khatabook.app.ui.language

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE CARD — Single language option with sample preview
 * ═══════════════════════════════════════════════════════════════════
 *
 * ┌────────────────────────────────────────────┐
 * │  🇬🇧  English                       ●     │
 * │  ─────────────────────────────────────     │
 * │                                              │
 * │  ┌──────────────────────────────────┐      │
 * │  │  Welcome to Khata Book           │      │
 * │  │  Total Dues: Rs 45,000           │      │
 * │  │  Ahmed Khan owes Rs 5,000        │      │
 * │  └──────────────────────────────────┘      │
 * └────────────────────────────────────────────┘
 */
@Composable
fun LanguageCard(
    language: KhataLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val accentColor = Color(0xFF1A73E8)

    // Animation states
    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 8f else 2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_elevation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor
        else Color.Gray.copy(alpha = 0.12f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "border_color"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.03f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bg_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation.dp, cardShape)
            .clip(cardShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header: Flag + Name + Radio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flag circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.1f)
                        else Color.Gray.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.flagEmoji,
                    fontSize = 22.sp
                )
            }

            // Language name column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = language.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) accentColor
                    else MaterialTheme.colorScheme.onSurface
                )
                if (language.nativeName != language.displayName) {
                    Text(
                        text = language.nativeName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Radio indicator
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(accentColor)
                        } else {
                            Modifier.border(2.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        )

        // Sample preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    0.5.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sample texts (RTL-aware)
                SampleText(
                    text = language.sampleTexts.welcome,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = language.textAlign,
                    isRtl = language.isRtl
                )
                SampleText(
                    text = "${language.sampleTexts.totalDues}: ${language.sampleTexts.amount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = language.textAlign,
                    isRtl = language.isRtl
                )
                SampleText(
                    text = language.sampleTexts.customerOwes,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = language.textAlign,
                    isRtl = language.isRtl
                )
            }
        }
    }
}

@Composable
private fun SampleText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color,
    textAlign: TextAlign,
    isRtl: Boolean,
    modifier: Modifier = Modifier
) {
    // Use composition local for layout direction
    val content: @Composable () -> Unit = {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign,
            modifier = modifier.fillMaxWidth()
        )
    }

    if (isRtl) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            content()
        }
    } else {
        content()
    }
}

// CompositionLocalProvider import
@Composable
private fun CompositionLocalProvider(
    provider: androidx.compose.runtime.ProvidedValue<*>,
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(provider, content = content)
}
