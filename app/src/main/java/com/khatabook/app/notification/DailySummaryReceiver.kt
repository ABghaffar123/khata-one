package com.khatabook.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.khatabook.app.ui.MainActivity
import java.util.Calendar

/**
 * BroadcastReceiver that fires at the configured time (default 9:00 PM PKT)
 * to show the daily summary notification.
 */
class DailySummaryReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_SUMMARY = "com.khatabook.app.DAILY_SUMMARY"
        private const val ALARM_REQUEST_CODE = 77001
        private const val PREFS_NAME = "khata_prefs"
        private const val KEY_SUMMARY_HOUR = "summary_hour"
        private const val KEY_SUMMARY_MINUTE = "summary_minute"

        /** Schedule the daily summary alarm at the configured hour/minute. */
        fun scheduleAlarm(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hour = prefs.getInt(KEY_SUMMARY_HOUR, 21) // default 9 PM
            val minute = prefs.getInt(KEY_SUMMARY_MINUTE, 0)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailySummaryReceiver::class.java).apply {
                action = ACTION_DAILY_SUMMARY
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If the time has already passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        /** Cancel the daily summary alarm. */
        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailySummaryReceiver::class.java).apply {
                action = ACTION_DAILY_SUMMARY
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_DAILY_SUMMARY) {
            NotificationHelper.showDailySummaryNotification(context)
            // Re-schedule for next day
            scheduleAlarm(context)
        }
    }
}
