package com.example.noteapp

import com.example.noteapp.presentation.alarm.calculateNextAlarmTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AlarmRepeatTest {

    @Test
    fun whenTodayIsWednesday_andAlarmIsWednesdayAndFriday_nextShouldBeFriday() {

        // Simulamos que HOY es miércoles 18 Feb 2026
        val fakeNow = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18) // Miércoles real
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE,10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = listOf(3, 5) // 3 = miércoles, 5 = viernes

        val nextTime = calculateNextAlarmTime(
            hour = 15,
            minute = 10,
            repeatDays = repeatDays,
            nowOverride = fakeNow
        )

        val expected = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 20) // Viernes 20 Feb 2026
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE,10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(expected.timeInMillis, nextTime)
    }
    @Test
    fun whenTodayIsFriday_andAlarmIsWednesdayAndFriday_nextShouldBeNextWednesday() {

        val fakeNow = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 20) // Viernes
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE,10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = listOf(3, 5)

        val nextTime = calculateNextAlarmTime(
            hour = 15,
            minute = 10,
            repeatDays = repeatDays,
            nowOverride = fakeNow
        )

        val expected = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 25) // Miércoles siguiente
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(expected.timeInMillis, nextTime)
    }
    @Test
    fun repeats_monday_wednesday_sunday_sequence() {

        val repeatDays = listOf(1,3,7)

        // Empezamos en lunes 16 Feb 2026 10:00
        var current = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18, 14, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        val results = mutableListOf<Int>()

        repeat(6) {

            val nextMillis = calculateNextAlarmTime(
                hour = 15,
                minute = 5,
                repeatDays = repeatDays,
                nowOverride = current
            )

            current = Calendar.getInstance().apply {
                timeInMillis = nextMillis
            }

            val day = if (current.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                7
            else
                current.get(Calendar.DAY_OF_WEEK) - 1

            results.add(day)
        }

        assertEquals(
            listOf(3, 7, 1, 3, 7, 1),
            results
        )
    }

}
