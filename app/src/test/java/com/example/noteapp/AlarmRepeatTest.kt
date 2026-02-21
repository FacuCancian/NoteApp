package com.example.noteapp

import com.example.noteapp.presentation.alarm.calculateNextAlarmTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlarmRepeatTest {

    @Test
    fun whenTodayIsWednesday_andAlarmIsWednesdayAndFriday_nextShouldBeFriday() {

        val fakeNow = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18)
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
            set(2026, Calendar.FEBRUARY, 20)
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
    @Test
    fun `alarm should move sequentially through all selected days`() {

        val repeatAll = listOf(1,2,3,4,5,6,7)

        var now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 11, 20, 0) // sábado
            set(Calendar.MILLISECOND, 0)
        }

        var next = calculateNextAlarmTime(
            hour = 11,
            minute = 29,
            repeatDays = repeatAll,
            nowOverride = now
        )

        var expected = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 11, 29, 0) // sábado
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(expected, next)

        now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 11, 35, 0)
            set(Calendar.MILLISECOND, 0)
        }

        next = calculateNextAlarmTime(
            hour = 11,
            minute = 29,
            repeatDays = repeatAll,
            nowOverride = now
        )

        expected = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 8, 11, 29, 0) // domingo
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(expected, next)
    }
    @Test
    fun `alarm should cycle through all weekdays in order`() {

        val repeatAll = listOf(1,2,3,4,5,6,7)
        val triggeredDays = mutableListOf<Int>()

        var now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 12, 0, 0) // sábado 12:00
            set(Calendar.MILLISECOND, 0)
        }

        repeat(7) {

            val next = calculateNextAlarmTime(
                hour = 11,
                minute = 0,
                repeatDays = repeatAll,
                nowOverride = now
            )

            val cal = Calendar.getInstance().apply {
                timeInMillis = next
            }

            triggeredDays.add(cal.get(Calendar.DAY_OF_WEEK))

            // Simulamos que ya sonó y pasó la hora
            now = Calendar.getInstance().apply {
                timeInMillis = next
                add(Calendar.MINUTE, 1)
            }
        }

        val expectedOrder = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )
        fun Int.toDayName(): String = when(this) {
            Calendar.SUNDAY -> "DOMINGO"
            Calendar.MONDAY -> "LUNES"
            Calendar.TUESDAY -> "MARTES"
            Calendar.WEDNESDAY -> "MIERCOLES"
            Calendar.THURSDAY -> "JUEVES"
            Calendar.FRIDAY -> "VIERNES"
            Calendar.SATURDAY -> "SABADO"
            else -> "?"
        }
        println(triggeredDays.map { it.toDayName() })
        assertEquals(expectedOrder, triggeredDays)
    }
    @Test
    fun `repeating alarm should keep rescheduling to valid future days`() {

       //mapper where sunday = 7 else day -1 (monday = 1...)
        val repeat = listOf(
           1,7
        )

        // beggining of the date test
        var now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 14, 21, 0) // Saturday
            set(Calendar.MILLISECOND, 0)
        }

        var lastTrigger: Long? = null

        repeat(repeat.size) {

            val next = calculateNextAlarmTime(
                hour = 14,
                minute = 21,
                repeatDays = repeat,
                nowOverride = now
            )

            val cal = Calendar.getInstance().apply {
                timeInMillis = next
            }

            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

            // valid days
            val validDays = listOf(
                Calendar.MONDAY,
                Calendar.SUNDAY
            )
            assertTrue(validDays.contains(dayOfWeek))

            // ✅ avance in time
            if (lastTrigger != null) {
                assertTrue(next > lastTrigger!!)
            }

            // stopped alarm
            now = Calendar.getInstance().apply {
                timeInMillis = next
                add(Calendar.MINUTE, 1)
            }

            lastTrigger = next
        }
    }
}
