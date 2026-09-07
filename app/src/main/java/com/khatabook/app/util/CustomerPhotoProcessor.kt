package com.khatabook.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Handles importing, resizing, compressing and persisting customer profile photos.
 *
 * The cropper (uCrop) and image pickers return images in temporary locations
 * (cache dir, content:// URIs with transient read permission). This class copies
 * the image into app-private storage as a small JPEG so the photo survives
 * app restarts and doesn't bloat the database.
 */
object CustomerPhotoProcessor {

    const val MAX_DIMENSION = 300
    private const val MAX_BYTES = 100 * 1024 // 100 KB
    private const val MIN_QUALITY = 55

    private const val PHOTO_DIR = "customer_photos"

    /**
     * Import a photo from any source (content://, file://) into app-private
     * storage, resized to at most [MAX_DIMENSION]×[MAX_DIMENSION] and compressed
     * to under 100 KB. Returns the absolute file path, or null on failure.
     *
     * Transparent corners from the circular crop are filled with white so the
     * JPEG (which has no alpha) looks correct on any background.
     */
    fun importPhoto(context: Context, sourceUri: Uri): String? {
        return try {
            val bitmap = decodeSampledBitmap(context, sourceUri, MAX_DIMENSION) ?: return null

            // Scale keeping aspect ratio within MAX_DIMENSION
            val scaled = scaleWithin(bitmap, MAX_DIMENSION)
            if (scaled != bitmap) bitmap.recycle()

            // Draw onto a white square canvas (handles transparent corners)
            val square = Bitmap.createBitmap(MAX_DIMENSION, MAX_DIMENSION, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(square)
            canvas.drawColor(Color.WHITE)
            val left = (MAX_DIMENSION - scaled.width) / 2f
            val top = (MAX_DIMENSION - scaled.height) / 2f
            canvas.drawBitmap(scaled, left, top, null)

            // Compress with quality loop until under 100 KB
            var quality = 92
            var bytes: ByteArray
            do {
                val bos = ByteArrayOutputStream()
                square.compress(Bitmap.CompressFormat.JPEG, quality, bos)
                bytes = bos.toByteArray()
                quality -= 8
            } while (bytes.size > MAX_BYTES && quality >= MIN_QUALITY)

            val dir = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
            val output = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            output.outputStream().use { it.write(bytes) }
            output.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete a previously saved customer photo. Only deletes files inside the
     * app's private [PHOTO_DIR] to avoid touching arbitrary paths.
     */
    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            if (file.exists() && file.parentFile?.name == PHOTO_DIR) {
                file.delete()
            }
        }
    }

    private fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                // First pass: read bounds only
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, bounds)
                input.close()

                // Compute inSampleSize (power of 2) to keep memory low
                var sampleSize = 1
                while (bounds.outWidth / (sampleSize * 2) >= maxDimension &&
                    bounds.outHeight / (sampleSize * 2) >= maxDimension
                ) {
                    sampleSize *= 2
                }

                // Second pass: decode sampled
                context.contentResolver.openInputStream(uri)?.use { input2 ->
                    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeStream(input2, null, options)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleWithin(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt().coerceAtLeast(1)
        val newHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}