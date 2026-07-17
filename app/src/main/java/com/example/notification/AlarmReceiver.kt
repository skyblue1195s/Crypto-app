package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.presentation.Translations

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ACTION_SHOW_EVENT_NOTIFICATION") {
            val eventId = intent.getStringExtra("EVENT_ID") ?: "unknown_event"
            
            val prefs = context.getSharedPreferences("crypto_prefs", Context.MODE_PRIVATE)
            val lang = prefs.getString("app_language", "en") ?: "en"
            
            val fallbackTitle = Translations.getString("notification_fallback_title", lang)
            val title = intent.getStringExtra("EVENT_TITLE") ?: fallbackTitle
            val message = Translations.getString("notification_message", lang)
            
            NotificationHelper.showNotification(context, eventId, title, message)
        }
    }
}
