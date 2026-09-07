package com.khatabook.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * DISPLAY MODE TOGGLE — Light / Dark / System selector
 * ═══════════════════════════════════════════════════════════════════
 */
@Composable
fun DisplayModeToggle(
    selectedMode: KhataGradientPresets.DisplayMode,
    accentColor: Color,
    onModeSelected: (KhataGradientPresets.DisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Display Mode",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, cardShape)
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = cardShape
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KhataGradientPresets.DisplayMode.entries.forEach { mode ->
                ModeOptionCard(
                    mode = mode,
                    isSelected = selectedMode == mode,
                    accentColor = accentColor,
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    mode: KhataGradientPresets.DisplayMode,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (mode) {
        KhataGradientPresets.DisplayMode.LIGHT -> Icons.Outlined.LightMode
        KhataGradientPresets.DisplayMode.DARK -> Icons.Outlined.DarkMode
        KhataGradientPresets.DisplayMode.SYSTEM -> Icons.Outlined.SettingsBrightness
    }

    val label = when (mode) {
        KhataGradientPresets.DisplayMode.LIGHT -> "Light"
        KhataGradientPresets.DisplayMode.DARK -> "Dark"
        KhataGradientPresets.DisplayMode.SYSTEM -> "System"
    }

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mode_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Gray,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mode_icon"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.4f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mode_border"
    )

    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (isSelected) {
                        Modifier.background(accentColor)
                    } else {
                        Modifier.background(Color.Gray.copy(alpha = 0.1f))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )
    }
}
