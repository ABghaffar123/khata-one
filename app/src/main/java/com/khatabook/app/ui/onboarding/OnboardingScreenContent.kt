package com.khatabook.app.ui.onboarding

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatabook.app.ui.language.KhataLanguage
import com.khatabook.app.ui.language.KhataLanguages

/**
 * ═══════════════════════════════════════════════════════════════════
 * ONBOARDING SCREEN CONTENT — Content for a single onboarding page
 * ═══════════════════════════════════════════════════════════════════
 *
 * Layout:
 *   1. Illustration (top)
 *   2. Title + Subtitle (center)
 *   3. Features list or Language selector (bottom)
 */
@Composable
fun OnboardingScreenContent(
    screen: OnboardingScreen,
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRtl = selectedLanguageCode == "ur"
    val language = KhataLanguages.getByCode(selectedLanguageCode)

    val content: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════
            // 1. ILLUSTRATION
            // ═══════════════════════════════════════════════════
            OnboardingIllustration(
                type = screen.illustrationType,
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════
            // 2. TITLE
            // ═══════════════════════════════════════════════════
            Text(
                text = if (selectedLanguageCode == "ur") screen.titleUrdu else screen.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════════════════════════════════════════════
            // 3. SUBTITLE
            // ═══════════════════════════════════════════════════
            Text(
                text = if (selectedLanguageCode == "ur") screen.subtitleUrdu else screen.subtitle,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ═══════════════════════════════════════════════════
            // 4. FEATURES or LANGUAGE SELECTOR
            // ═══════════════════════════════════════════════════
            if (screen.showLanguageSelector) {
                // Language selector cards
                LanguageSelectorCompact(
                    selectedCode = selectedLanguageCode,
                    onLanguageSelected = onLanguageSelected
                )
            } else if (screen.features.isNotEmpty()) {
                // Feature checklist
                FeatureChecklist(
                    features = if (selectedLanguageCode == "ur") screen.featuresUrdu else screen.features
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Wrap with RTL if needed
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


/**
 * Compact language selector for onboarding screen 1.
 *
 * Three cards in a row: English, Urdu, Roman Urdu
 */
@Composable
private fun LanguageSelectorCompact(
    selectedCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF1A73E8)
    val languages = KhataLanguages.allLanguages

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        languages.forEach { language ->
            LanguageChip(
                language = language,
                isSelected = selectedCode == language.code,
                onClick = { onLanguageSelected(language.code) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageChip(
    language: KhataLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF1A73E8)
    val chipShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(chipShape)
            .background(
                if (isSelected) accentColor.copy(alpha = 0.06f)
                else Color.White
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.4f)
                else Color.Gray.copy(alpha = 0.15f),
                shape = chipShape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Flag
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) accentColor.copy(alpha = 0.1f)
                    else Color.Gray.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = language.flagEmoji, fontSize = 16.sp)
        }

        // Name
        Text(
            text = when (language.code) {
                "en" -> "English"
                "ur" -> "اردو"
                "ur-roman" -> "Roman"
                else -> language.displayName
            },
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) accentColor else Color.Gray,
            maxLines = 1
        )

        // Radio
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.background(accentColor)
                    } else {
                        Modifier.border(1.5.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}


/**
 * Feature checklist for onboarding screens 2-4.
 */
@Composable
private fun FeatureChecklist(
    features: List<String>,
    modifier: Modifier = Modifier
) {
    val greenColor = Color(0xFF4CAF50)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        features.forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Checkmark
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(greenColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor
                    )
                }

                // Feature text
                Text(
                    text = feature,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
