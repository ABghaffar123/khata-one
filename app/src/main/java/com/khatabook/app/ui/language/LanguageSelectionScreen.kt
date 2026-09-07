package com.khatabook.app.ui.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE SELECTION SCREEN — Premium language selection page
 * ═══════════════════════════════════════════════════════════════════
 *
 * Layout:
 *   1. Header with title (multilingual)
 *   2. Language cards (3 options with sample previews)
 *   3. Live preview phone mockup
 *   4. Continue button
 *
 * Supports: English, Urdu (RTL), Urdu Roman
 */
@Composable
fun LanguageSelectionScreen(
    viewModel: LanguageSelectionViewModel,
    onContinue: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val accentColor = Color(0xFF1A73E8)

    // Determine if current selection is RTL
    val isCurrentRtl = state.selectedLanguage.isRtl

    val screenContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ═══════════════════════════════════════════════════
            // HEADER
            // ═══════════════════════════════════════════════════
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Globe icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌍", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = "Choose Your Language",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "Apni Zubaan Intekhab Karein\nاپنی زبان منتخب کریں",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ═══════════════════════════════════════════════════
            // LANGUAGE CARDS
            // ═══════════════════════════════════════════════════
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.allLanguages.forEach { language ->
                    LanguageCard(
                        language = language,
                        isSelected = state.selectedLanguageCode == language.code,
                        onClick = { viewModel.selectLanguage(language.code) }
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // LIVE PREVIEW
            // ═══════════════════════════════════════════════════
            LanguagePreview(
                language = state.selectedLanguage
            )

            // ═══════════════════════════════════════════════════
            // CONTINUE BUTTON
            // ═══════════════════════════════════════════════════
            Button(
                onClick = {
                    val selected = viewModel.continueWithSelection()
                    onContinue(selected)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                enabled = state.isLanguageSelected
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Footer
            Text(
                text = "You can change this later in Settings",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
    }

    // Wrap with RTL if current language is RTL
    if (isCurrentRtl) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            screenContent()
        }
    } else {
        screenContent()
    }
}
