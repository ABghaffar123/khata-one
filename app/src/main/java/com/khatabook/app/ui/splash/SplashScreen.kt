package com.khatabook.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * SPLASH SCREEN — Premium app launch screen
 * ═══════════════════════════════════════════════════════════════════
 *
 * Layout:
 *   1. Logo container (icon + shadow)
 *   2. App name + Urdu subtitle
 *   3. Loading progress bar
 *   4. Loading text (pulsing)
 *   5. Version footer
 *
 * Supports: Light/Dark themes, all languages
 */
@Composable
fun SplashScreen(
    isLoading: Boolean = true,
    loadingText: String = "Loading...",
    versionText: String = "1.0.0",
    onSplashComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val background = MaterialTheme.colorScheme.background

    // Pulse animation for loading text
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Logo scale animation
    val scaleTransition = rememberInfiniteTransition(label = "scale")
    val logoScale by scaleTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ═══════════════════════════════════════════════════
            // 1. LOGO CONTAINER
            // ═══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = primaryColor.copy(alpha = 0.15f),
                        spotColor = primaryColor.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.1f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📒",
                    fontSize = 44.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════
            // 2. APP NAME
            // ═══════════════════════════════════════════════════
            Text(
                text = "Khata Book",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "خ账簿",
                fontSize = 16.sp,
                color = onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ═══════════════════════════════════════════════════
            // 3. LOADING PROGRESS
            // ═══════════════════════════════════════════════════
            if (isLoading) {
                // Progress bar track
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                ) {
                    // Animated progress indicator
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = primaryColor,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ═══════════════════════════════════════════════════
                // 4. LOADING TEXT
                // ═══════════════════════════════════════════════════
                Text(
                    text = loadingText,
                    fontSize = 12.sp,
                    color = onSurfaceVariant.copy(alpha = pulseAlpha),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ═══════════════════════════════════════════════════
            // 5. VERSION FOOTER
            // ═══════════════════════════════════════════════════
            Column(
                modifier = Modifier.padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Version $versionText",
                    fontSize = 11.sp,
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Made with ❤️ for Pakistani Shopkeepers",
                    fontSize = 11.sp,
                    color = onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


/**
 * GraphicsLayer extension for scale animation.
 */
private fun androidx.compose.ui.graphics.GraphicsLayerScope.graphicsLayer(
    block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit
) {
    block()
}
