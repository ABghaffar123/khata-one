package com.khatabook.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatabook.app.ui.responsive.LocalWindowSize
import com.khatabook.app.ui.responsive.WindowWidthSizeClass
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.KhataTheme
import com.khatabook.app.ui.theme.SplashBackground
import com.khatabook.app.util.Constants
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KhataTheme {
                SplashScreen(
                    onSplashComplete = {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val gradientTheme = KhataGradientPresets.FireOrange

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }
    val glowAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val accentWidth = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )

        glowAlpha.animateTo(
            targetValue = 0.6f,
            animationSpec = tween(durationMillis = 300)
        )

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )

        accentWidth.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200)
        )

        delay(200)
        contentAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 200)
        )

        onSplashComplete()
    }

    val windowSize = LocalWindowSize.current

    val logoSize = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Large -> 180.dp
        WindowWidthSizeClass.Expanded -> 160.dp
        WindowWidthSizeClass.Medium -> 140.dp
        WindowWidthSizeClass.Compact -> 120.dp
    }

    val logoTextSize = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Large -> 72.sp
        WindowWidthSizeClass.Expanded -> 64.sp
        WindowWidthSizeClass.Medium -> 56.sp
        WindowWidthSizeClass.Compact -> 48.sp
    }

    val appNameSize = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Large -> 36.sp
        WindowWidthSizeClass.Expanded -> 32.sp
        WindowWidthSizeClass.Medium -> 28.sp
        WindowWidthSizeClass.Compact -> 24.sp
    }

    val taglineSize = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Large -> 16.sp
        WindowWidthSizeClass.Expanded -> 15.sp
        WindowWidthSizeClass.Medium -> 14.sp
        WindowWidthSizeClass.Compact -> 12.sp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                gradientTheme.gradient
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(contentAlpha.value)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(logoSize * 1.2f)
            ) {
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .scale(1.3f)
                        .alpha(glowAlpha.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    gradientTheme.gradientStart.copy(alpha = 0.4f),
                                    gradientTheme.gradientStart.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                Text(
                    text = "K₁",
                    fontSize = logoTextSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .alpha(logoAlpha.value)
                        .scale(logoScale.value)
                )
            }

            Spacer(modifier = Modifier.height(logoSize * 0.2f))

            Text(
                text = Constants.APP_NAME,
                fontSize = appNameSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (appNameSize.value * 0.07).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(appNameSize.value.dp * 0.25))

            Text(
                text = "Digital Khata for Shopkeepers",
                fontSize = taglineSize,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = (taglineSize.value * 0.08).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(logoSize * 0.3f))

            val accentWidthDp = logoSize * 0.8f
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .offset(
                        x = (-accentWidthDp / 2) + (accentWidthDp * accentWidth.value)
                    )
                    .alpha(accentWidth.value)
                    .size(width = accentWidthDp, height = 2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                gradientTheme.gradientEnd,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
