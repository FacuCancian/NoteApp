package com.example.noteapp.presentation.alarm

import java.util.Calendar

fun calculateNextAlarmTime(
    hour: Int,
    minute: Int,
    repeatDays: List<Int>? = null,
    nowOverride: Calendar? = null
): Long? {
    //day time
    val now = (nowOverride ?: Calendar.getInstance()).apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    //user time
    val alarmTime = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    //if only ring 1 day a week
    if (repeatDays.isNullOrEmpty()) {
        // no reprogram if pass
        if (alarmTime <= now) return null
        return alarmTime.timeInMillis
    }

    // Map repeatDays (1=Lunes ... 7=Domingo) -> Calendar.DAY_OF_WEEK (1=Domingo ... 7=Sábado)
    val dayMap = mapOf(
        1 to Calendar.MONDAY,
        2 to Calendar.TUESDAY,
        3 to Calendar.WEDNESDAY,
        4 to Calendar.THURSDAY,
        5 to Calendar.FRIDAY,
        6 to Calendar.SATURDAY,
        7 to Calendar.SUNDAY
    )
    val repeatCalendarDays = repeatDays.map { dayMap[it] ?: it }

    for (i in 0..6) {
        //first valid day, today...
        val candidate = (alarmTime.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
        val day = candidate.get(Calendar.DAY_OF_WEEK)
        if (day in repeatCalendarDays) {
            if (i == 0 && candidate <= now) continue
            return candidate.timeInMillis
        }
    }
    //if not a valid day. in a week ring...
    alarmTime.add(Calendar.DAY_OF_MONTH, 7)
    return alarmTime.timeInMillis
}