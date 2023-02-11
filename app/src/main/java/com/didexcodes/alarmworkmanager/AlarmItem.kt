package com.didexcodes.alarmworkmanager

import java.time.LocalDateTime

data class AlarmItem(
    val time: LocalDateTime,
    val message: String
)