package com.khatabook.app.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Spacing tokens — 4dp grid system
 */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val base = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

/**
 * Corner radius tokens
 */
object Radius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val full = 9999.dp
}

/**
 * Elevation tokens
 */
object Elevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 16.dp
}

/**
 * Typography size tokens
 */
object TextSize {
    val display = 40.sp
    val h1 = 32.sp
    val h2 = 24.sp
    val h3 = 20.sp
    val bodyLg = 16.sp
    val bodyMd = 14.sp
    val bodySm = 12.sp
    val labelLg = 14.sp
    val labelMd = 12.sp
    val labelSm = 10.sp
}

/**
 * Animation duration tokens (ms)
 */
object Duration {
    const val instant = 50
    const val fast = 150
    const val normal = 250
    const val slow = 400
    const val extra = 600
}

/**
 * Animation easing curves
 */
object Curve {
    val standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val decelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val accelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    fun <T> spring(
        dampingFraction: Float = 0.8f,
        stiffness: Float = Spring.StiffnessMedium
    ) = spring<T>(
        dampingRatio = dampingFraction,
        stiffness = stiffness
    )
}
