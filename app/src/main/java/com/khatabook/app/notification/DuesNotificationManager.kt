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
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.ui.MainActivity
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.util.toCurrency

/**
 * Manages local notifications for customer due dates.
 * Checks for overdue transactions and sends notifications.
 */
object DuesNotificationManager {

    private const val CHANNEL_ID = "dues_reminders"
    private const val CHANNEL_NAME = "Customer Dues Reminders"
    private const val CHANNEL_DESC = "Notifications when customer due dates arrive or are overdue"
    private const val NOTIFICATION_ID_BASE = 7000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Send a notification for a single overdue/due transaction.
     */
    fun showDuesNotification(
        context: Context,
        customerName: String,
        amount: Double,
        daysOverdue: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "due_customers")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE + customerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (daysOverdue > 0) {
            "⏰ $customerName ka due hai!"
        } else {
            "📅 $customerName ka due aaj hai!"
        }

        val text = if (daysOverdue > 0) {
            "$customerName ka ${amount.toCurrency()} ka due $daysOverdue din pehle ho gaya hai"
        } else {
            "$customerName ka ${amount.toCurrency()} ka due aaj hai"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + customerName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted — silently skip
        }
    }

    /**
     * Send a summary notification if multiple customers have dues.
     */
    fun showDuesSummaryNotification(
        context: Context,
        overdueCount: Int,
        totalOverdueAmount: Double
    ) {
        if (overdueCount <= 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "due_customers")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE + 99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $overdueCount customers ka due hai!")
            .setContentText("Total overdue: ${totalOverdueAmount.toCurrency()} — Tap to view")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + 99999, notification)
        } catch (_: SecurityException) {
            // Permission not granted — silently skip
        }
    }

    /**
     * Check transactions for overdue dues and send notifications.
     * Call this from Application startup or periodic worker.
     */
    fun checkAndNotifyOverdueDues(
        context: Context,
        transactions: List<TransactionEntity>
    ) {
        val nowUtc = PkDateTime.nowUtc()
        val overdueTransactions = transactions.filter { txn ->
            txn.type == TransactionType.CREDIT &&
                txn.dueDate != null &&
                txn.dueDate <= nowUtc
        }

        if (overdueTransactions.isEmpty()) return

        // Group by customer
        val groupedByCustomer = overdueTransactions.groupBy { it.customerName }

        var totalOverdueAmount = 0.0
        for ((customerName, txns) in groupedByCustomer) {
            val totalDue = txns.sumOf { it.amount }
            totalOverdueAmount += totalDue

            val latestDueDate = txns.maxOfOrNull { it.dueDate ?: 0L } ?: 0L
            val daysOverdue = ((nowUtc - latestDueDate) / (1000 * 60 * 60 * 24)).toInt()

            showDuesNotification(
                context = context,
                customerName = customerName,
                amount = totalDue,
                daysOverdue = daysOverdue
            )
        }

        // If multiple customers, also show summary
        if (groupedByCustomer.size > 1) {
            showDuesSummaryNotification(
                context = context,
                overdueCount = groupedByCustomer.size,
                totalOverdueAmount = totalOverdueAmount
            )
        }
    }

    fun cancelAllDuesNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + 99999)
    }
}
