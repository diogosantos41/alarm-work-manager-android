package com.didexcodes.alarms.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.didexcodes.alarms.notification.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TriggerAlarmWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val message = inputData.getString("message") ?: "Work Manager Notification"
        val seconds = inputData.getLong("time", 5000)
        delay(seconds * 1000)
        return withContext(Dispatchers.IO) {
            val service = NotificationService(context)
            service.showNotification(message)
            Result.success()
        }
    }
}