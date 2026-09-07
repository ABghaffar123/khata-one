package com.khatabook.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════
// GLOBAL SUCCESS TOAST — Khata One
// ═══════════════════════════════════════════════════════════
// Slides down from the top of the screen, auto-dismisses after
// ~2.5s, and can be dismissed by tapping or swiping up.
// Trigger from anywhere: SuccessToast.show("Customer Added")

/** Global channel for showing a success toast from any screen. */
object SuccessToast {
    var message by mutableStateOf<String?>(null)
        private set

    fun show(msg: String) {
        message = msg
    }

    fun dismiss() {
        message = null
    }
}

/**
 * Place ONCE at the app root (above the NavHost) so toasts float
 * over every screen, below the status bar.
 */
@Composable
fun SuccessToastHost(modifier: Modifier = Modifier) {
    val message = SuccessToast.message
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Keep the last non-null message so the exit animation still has text
    var renderedText by remember { mutableStateOf(message) }
    if (message != null) {
        renderedText = message
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            // Auto-dismiss after 2.5s (only if this message is still showing)
            LaunchedEffect(renderedText) {
                val shown = renderedText ?: return@LaunchedEffect
                delay(2500)
                if (SuccessToast.message == shown) {
                    SuccessToast.message = null
                }
            }

            SuccessToastCard(
                text = renderedText ?: "",
                onDismiss = { SuccessToast.dismiss() },
                modifier = Modifier.padding(top = statusBarPadding + 12.dp)
            )
        }
    }
}

@Composable
private fun SuccessToastCard(
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .background(Color(0xFF221410), RoundedCornerShape(16.dp))
            .clickable(onClick = onDismiss)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    totalDrag += dragAmount
                    if (totalDrag < -120f) {
                        onDismiss()
                        totalDrag = 0f
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .widthIn(max = 380.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Green circle with checkmark
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF4CAF50), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = text,
            color = Color(0xFFF5F1EC),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}