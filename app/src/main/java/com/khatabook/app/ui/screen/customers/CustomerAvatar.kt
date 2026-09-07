package com.khatabook.app.ui.screen.customers

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.ui.theme.KhataGradientPresets

/**
 * Circular customer avatar: shows the customer photo when one is set,
 * otherwise falls back to the auto-generated initials on the app gradient.
 *
 * @param size      circle diameter (use a large value on the detail screen)
 * @param fontSize  text size for the initials fallback
 */
@Composable
fun CustomerAvatar(
    customer: CustomerEntity,
    size: Dp,
    fontSize: TextUnit = MaterialTheme.typography.titleMedium.fontSize
) {
    val context = LocalContext.current
    val gradientTheme = KhataGradientPresets.FireOrange

    val photoUri = customer.photoUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
    if (photoUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(gradientTheme.gradientStart.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = customer.initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = gradientTheme.gradientStart
                )
            )
        }
    }
}