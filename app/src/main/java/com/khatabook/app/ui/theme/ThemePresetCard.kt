package com.khatabook.app.ui.theme

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * GRADIENT THEME CARD — Mini preview card for a single gradient theme
 * ═══════════════════════════════════════════════════════════════════
 *
 * Visual structure:
 * ┌────────────────────────┐
 * │ ████████████████████   │  ← Gradient TopBar preview
 * │ █  Khata Book    █    │
 * │ ████████████████████   │
 * ├────────────────────────┤
 * │ ░░░░░░░░░░░░░░░░░░░░  │  ← Body
 * │ ┌──────────────────┐  │
 * │ │ ████████    5000 │  │  ← Card items
 * │ └──────────────────┘  │
 * │ ┌──────────────────┐  │
 * │ │ ████████    3000 │  │
 * │ └──────────────────┘  │
 * ├────────────────────────┤
 * │ ░░░░  ░░░░  ░░░░  ░░ │  ← Bottom nav
 * └────────────────────────┘
 *       ● Ocean Blue        ← Name + radio indicator
 */
@Composable
fun GradientThemeCard(
    preset: KhataGradientPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) preset.gradientStart else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "border_color"
    )

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .width(110.dp)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card with gradient preview
        Box(
            modifier = Modifier
                .width(102.dp)
                .height(130.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 2.dp,
                    shape = cardShape,
                    ambientColor = preset.gradientStart.copy(alpha = 0.15f),
                    spotColor = preset.gradientStart.copy(alpha = 0.25f)
                )
                .clip(cardShape)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = borderColor,
                    shape = cardShape
                )
                .background(preset.gradient)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Mini UI Preview
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // TopBar (white semi-transparent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Khata Book",
                        color = Color.White,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Body content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Card item 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .height(2.5.dp)
                                .width(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                        )
                    }

                    // Card item 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .height(2.5.dp)
                                .width(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                        )
                    }
                }

                // Bottom Nav
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == 0) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            // Selection overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selection indicator + theme name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Radio indicator
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(preset.gradient)
                        } else {
                            Modifier
                                .border(1.5.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Theme name
            Text(
                text = preset.nameResKey
                    .removePrefix("theme_")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    },
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) preset.gradientStart else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
