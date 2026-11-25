package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "crypto_events_notifications"
    private const val CHANNEL_NAME = "Crypto Market Alerts"
    private const val BTC_CHANNEL_ID = "btc_price_alerts"
    private const val BTC_CHANNEL_NAME = "Bitcoin Price Alerts"

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

                // Channel 1: Macro & Calendar Event Alerts
                val eventChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Get notified before high-impact FOMC, CPI, and token events."
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(eventChannel)

                // Channel 2: Bitcoin Price Fluctuation Alerts
                val btcChannel = NotificationChannel(
                    BTC_CHANNEL_ID,
                    BTC_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent alerts when Bitcoin (BTC) price surges or drops significantly."
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(btcChannel)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun showBtcPriceAlert(
        context: Context,
        currentPrice: Double,
        previousPrice: Double,
        changePct: Double,
        isTest: Boolean = false
    ) {
        val prefs = context.getSharedPreferences("crypto_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "en") ?: "en"

        val isPositive = changePct >= 0
        val formattedChange = String.format(java.util.Locale.US, "%+.2f", changePct)
        val formattedCurrent = String.format(java.util.Locale.US, "%,.0f", currentPrice)
        val formattedPrev = String.format(java.util.Locale.US, "%,.0f", previousPrice)

        val title = if (isTest) {
            if (lang == "vi") "🔔 [Thử nghiệm] Cảnh báo biến động BTC ($formattedChange%)"
            else "🔔 [Test Alert] BTC Fluctuation ($formattedChange%)"
        } else if (isPositive) {
            if (lang == "vi") "🚀 Bitcoin tăng mạnh $formattedChange%!"
            else "🚀 Bitcoin Surged $formattedChange%!"
        } else {
            if (lang == "vi") "📉 Bitcoin giảm mạnh $formattedChange%!"
            else "📉 Bitcoin Dropped $formattedChange%!"
        }

        val message = if (lang == "vi") {
            "Giá BTC hiện tại: $$formattedCurrent (mốc trước: $$formattedPrev). Chạm để xem cập nhật thị trường!"
        } else {
            "BTC price is now $$formattedCurrent (from $$formattedPrev). Tap to view market updates!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_PRICE_ALERT", true)
            putExtra("BTC_PRICE", currentPrice)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, BTC_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            notificationManager.notify(2002, builder.build())
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun showNotification(context: Context, eventId: String, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EVENT_ID", eventId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard system info icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            notificationManager.notify(eventId.hashCode(), builder.build())
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun scheduleEventAlarm(context: Context, eventId: String, title: String, triggerTimeMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_SHOW_EVENT_NOTIFICATION"
            putExtra("EVENT_ID", eventId)
            putExtra("EVENT_TITLE", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    fun cancelEventAlarm(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_SHOW_EVENT_NOTIFICATION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
