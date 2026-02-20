package com.example.noteapp.presentation.util.alarmUtils

import java.util.Calendar

object AlarmTimeUtils {

    fun extractHourMinute(timestamp: Long?): Pair<Int, Int> {
        if (timestamp == null) return 12 to 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return hour to minute
    }
    fun formatTimeUntil(triggerTime: Long): String {
        val diffMillis = triggerTime - System.currentTimeMillis()

        if (diffMillis <= 0) return "menos de un minuto"

        val totalMinutes = diffMillis / 1000 / 60
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 24

        val minutes = totalMinutes % 60
        val hours = totalHours % 24

        return when {
            totalDays > 0 -> {
                if (hours > 0)
                    "${totalDays}d ${hours}h"
                else
                    "${totalDays}d"
            }
            totalHours > 0 -> "${totalHours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
