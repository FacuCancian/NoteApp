package com.example.noteapp.presentation.util

import java.util.Calendar

fun calculateNextAlarmTime(
    hour: Int,
    minute: Int,
    repeatDays: List<Int>?,
    nowOverride: Calendar? = null
): Long {

    val now = nowOverride ?: Calendar.getInstance()


    // Base: today time selected
    val alarmTime = (now.clone() as Calendar).apply {

        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    //  no day selected
    if (repeatDays.isNullOrEmpty()) {

        // time pass -> tomorrow
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        return alarmTime.timeInMillis
    }

    // : repetead
    val today = now.get(Calendar.DAY_OF_WEEK)

    // map calendar
    fun calendarDayToAppDay(day: Int): Int =
        if (day == Calendar.SUNDAY) 7 else day - 1

    val todayAppDay = calendarDayToAppDay(today)

    //next valid day
    for (i in 0..6) {

        val candidate = alarmTime.clone() as Calendar
        candidate.add(Calendar.DAY_OF_MONTH, i)

        val candidateDay =
            calendarDayToAppDay(candidate.get(Calendar.DAY_OF_WEEK))

        if (candidateDay in repeatDays) {

            // for today, verify hour has pass
            if (i == 0 && !candidate.after(now)) {
                continue
            }

            return candidate.timeInMillis
        }
    }

    // security
    alarmTime.add(Calendar.DAY_OF_MONTH, 7)
    return alarmTime.timeInMillis
}