package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ACTION_SHOW_EVENT_NOTIFICATION") {
            val eventId = intent.getStringExtra("EVENT_ID") ?: "unknown_event"
            val title = intent.getStringExtra("EVENT_TITLE") ?: "Sự kiện quan trọng sắp diễn ra"
            val message = "Sự kiện vĩ mô/on-chain sắp bắt đầu sau 30 phút. Nhấp để xem chi tiết đếm ngược!"
            
            NotificationHelper.showNotification(context, eventId, title, message)
        }
    }
}
