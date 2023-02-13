package com.didexcodes.alarms

import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.didexcodes.alarms.alarm.AlarmItem
import com.didexcodes.alarms.alarm.AndroidAlarmScheduler
import com.didexcodes.alarms.ui.theme.AlarmWorkManagerTheme
import com.didexcodes.alarms.work.TriggerAlarmWorker
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        val triggerAlarmRequest = OneTimeWorkRequestBuilder<TriggerAlarmWorker>()
        val workManager = WorkManager.getInstance(applicationContext)
        setContent {
            val context = LocalContext.current
            // Permissions
            var hasNotificationPermission by context.rememberPostNotificationState()
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    hasNotificationPermission = isGranted
                    if (!isGranted) {
                        shouldShowRequestPermissionRationale("Hey! You cannot use this application without this Notification Permission.")
                    }
                }
            )
            // Content
            AlarmWorkManagerTheme {
                var secondsText by remember {
                    mutableStateOf("")
                }
                var message by remember {
                    mutableStateOf("")
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Trigger alarm in seconds")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Alarm message")
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            if (!hasNotificationPermission && isBuildVersionTiramisuOrHigher()) {
                                permissionLauncher.launch(POST_NOTIFICATIONS)
                            } else {
                                val seconds =
                                    if (secondsText.isNotEmpty() && secondsText.isDigitsOnly()) {
                                        secondsText.toLong()
                                    } else {
                                        5
                                    }
                                alarmItem = AlarmItem(
                                    time = LocalDateTime.now()
                                        .plusSeconds(seconds),
                                    message = message
                                )
                                alarmItem?.let(scheduler::schedule)
                                secondsText = ""
                                message = ""
                            }
                        }) {
                            Text(text = "Set alarm (Alarm Manager)")
                        }
                        Button(onClick = {
                            alarmItem?.let(scheduler::cancel)
                        }) {
                            Text(text = "Cancel alarm (Alarm Manager)")
                        }
                        Button(onClick = {
                            if (!hasNotificationPermission && isBuildVersionTiramisuOrHigher()) {
                                permissionLauncher.launch(POST_NOTIFICATIONS)
                            } else {
                                val seconds =
                                    if (secondsText.isNotEmpty() && secondsText.isDigitsOnly()) {
                                        secondsText.toLong()
                                    } else {
                                        5
                                    }
                                val data = Data.Builder()
                                data.putString("message", message)
                                data.putLong("time", seconds)
                                triggerAlarmRequest.setInputData(data.build())
                                workManager
                                    .beginUniqueWork(
                                        "TriggerAlarm",
                                        ExistingWorkPolicy.KEEP,
                                        triggerAlarmRequest.build()
                                    )
                                    .enqueue()
                                secondsText = ""
                                message = ""
                            }
                        }) {
                            Text(text = "Set alarm (Work Manager)")
                        }
                    }

                }
            }
        }
    }
}