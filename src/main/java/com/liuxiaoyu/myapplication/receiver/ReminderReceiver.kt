package com.liuxiaoyu.myapplication.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.liuxiaoyu.myapplication.R
import com.liuxiaoyu.myapplication.utils.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("event_id", 0)
        val title = intent.getStringExtra("event_title") ?: "日程提醒"
        val description = intent.getStringExtra("event_description") ?: ""
        val time = intent.getStringExtra("event_time") ?: ""

        println("收到提醒: $title")
        showNotification(context, title, description, time)
    }

    private fun showNotification(context: Context, title: String, description: String, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📅 日程提醒: $title")
            .setContentText("时间: $time")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$description\n\n⏰ 时间: $time"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
    }
}