package com.didexcodes.alarms.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.didexcodes.alarms.notification.NotificationService

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val service = NotificationService(context)
        service.showNotification(message)
    }
}