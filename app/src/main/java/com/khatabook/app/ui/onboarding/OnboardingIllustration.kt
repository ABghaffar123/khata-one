package com.khatabook.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * ONBOARDING ILLUSTRATIONS — Premium geometric illustrations
 * ═══════════════════════════════════════════════════════════════════
 *
 * Style: Modern flat with subtle gradients
 * Colors: Primary (#1A73E8) + Accent (#00BFA5)
 * Animation: Subtle float on entry
 */
@Composable
fun OnboardingIllustration(
    type: IllustrationType,
    modifier: Modifier = Modifier
) {
    // Float animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val primaryColor = Color(0xFF1A73E8)
    val accentColor = Color(0xFF00BFA5)
    val bgColor = primaryColor.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .size(180.dp)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when (type) {
                IllustrationType.GLOBE -> GlobeIllustration(primaryColor, accentColor)
                IllustrationType.LEDGER_BOOK -> LedgerIllustration(primaryColor, accentColor)
                IllustrationType.CAMERA_SCAN -> CameraIllustration(primaryColor, accentColor)
                IllustrationType.SHIELD_LOCK -> ShieldIllustration(primaryColor, accentColor)
            }
        }
    }
}

@Composable
private fun GlobeIllustration(
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main globe
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.15f))
                .border(2.dp, primary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌍", fontSize = 48.sp)
        }

        // Orbiting dots
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = -40.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🇬🇧", fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = -55.dp, y = 30.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🇵🇰", fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = 50.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🗣️", fontSize = 10.sp)
        }
    }
}

@Composable
private fun LedgerIllustration(
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val bookShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Book shadow
        Box(
            modifier = Modifier
                .offset(x = 4.dp, y = 4.dp)
                .size(100.dp, 130.dp)
                .clip(bookShape)
                .background(Color.Black.copy(alpha = 0.05f))
        )

        // Main book
        Box(
            modifier = Modifier
                .size(100.dp, 130.dp)
                .clip(bookShape)
                .background(primary)
                .shadow(8.dp, bookShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "📒",
                    fontSize = 32.sp
                )
                Text(
                    text = "Khata",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating ledger pages
        Box(
            modifier = Modifier
                .offset(x = 55.dp, y = -30.dp)
                .size(50.dp, 60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(6.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.height(3.dp).fillMaxWidth().background(accent.copy(alpha = 0.3f)).clip(RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.height(3.dp).fillMaxWidth(0.7f).background(primary.copy(alpha = 0.2f)).clip(RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.height(3.dp).fillMaxWidth(0.5f).background(accent.copy(alpha = 0.2f)).clip(RoundedCornerShape(2.dp)))
            }
        }

        // Amount badges
        Box(
            modifier = Modifier
                .offset(x = -50.dp, y = 40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "₹5,000",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = 50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(primary.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "₹3,000",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
}

@Composable
private fun CameraIllustration(
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    // Scanning line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_offset"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Camera body
        Box(
            modifier = Modifier
                .size(120.dp, 100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(primary)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Lens
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }

        // Document/page below camera
        Box(
            modifier = Modifier
                .offset(y = 65.dp)
                .size(80.dp, 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.5.dp, primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .fillMaxWidth(if (it % 2 == 0) 0.9f else 0.6f)
                            .background(primary.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // Scanning line
        Box(
            modifier = Modifier
                .offset(y = scanOffset.dp)
                .width(70.dp)
                .height(2.dp)
                .background(accent)
                .shadow(4.dp)
        )

        // AI sparkle badge
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = -55.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🤖 AI",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun ShieldIllustration(
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glow behind shield
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.08f))
        )

        // Main shield
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(primary)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 40.sp
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Floating security badges
        Box(
            modifier = Modifier
                .offset(x = 65.dp, y = -30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(primary.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(text = "🔒 PIN", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = primary)
        }

        Box(
            modifier = Modifier
                .offset(x = -60.dp, y = 20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(text = "👆 Bio", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = accent)
        }

        Box(
            modifier = Modifier
                .offset(x = 55.dp, y = 45.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(primary.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(text = "💾 Backup", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = primary)
        }
    }
}
