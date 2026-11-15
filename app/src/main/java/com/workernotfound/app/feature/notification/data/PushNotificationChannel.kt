package com.workernotfound.app.feature.notification.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.workernotfound.app.R

object PushNotificationChannel {
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.push_notification_channel_id),
                context.getString(R.string.notifications_title),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }
}
