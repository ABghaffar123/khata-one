package com.khatabook.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.khatabook.app.R
import com.khatabook.app.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "low_stock_alerts"
    private const val CHANNEL_NAME = "Low Stock Alerts"
    private const val CHANNEL_DESC = "Notifications when product stock falls below threshold"
    private const val NOTIFICATION_ID_BASE = 9000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showLowStockNotification(
        context: Context,
        productName: String,
        currentQty: Double,
        threshold: Double,
        unit: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "low_stock")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE + productName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ Low Stock Alert"
        val text = "$productName is low! Only $currentQty $unit left (threshold: $threshold)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + productName.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission not granted — silently skip
        }
    }

    fun showLowStockSummaryNotification(
        context: Context,
        lowStockCount: Int
    ) {
        if (lowStockCount <= 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "low_stock")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE + 99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ $lowStockCount Products Low on Stock")
            .setContentText("Tap to view low-stock items")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + 99999, notification)
        } catch (e: SecurityException) {
            // Permission not granted — silently skip
        }
    }

    fun cancelNotification(context: Context, productName: String) {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID_BASE + productName.hashCode())
    }

    fun cancelSummaryNotification(context: Context) {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID_BASE + 99999)
    }

    // ═══════════════════ Daily Summary Notification ═══════════════════

    private const val DAILY_SUMMARY_CHANNEL_ID = "daily_summary"
    private const val DAILY_SUMMARY_CHANNEL_NAME = "Daily Summary"
    private const val DAILY_SUMMARY_CHANNEL_DESC = "Daily business summary notification"
    private const val DAILY_SUMMARY_NOTIFICATION_ID = 88001

    fun createDailySummaryChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DAILY_SUMMARY_CHANNEL_ID,
                DAILY_SUMMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = DAILY_SUMMARY_CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showDailySummaryNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "daily_summary")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            DAILY_SUMMARY_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DAILY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 Aaj ka summary tayyar hai!")
            .setContentText("Tap to view your daily business summary")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(DAILY_SUMMARY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted — silently skip
        }
    }
}
