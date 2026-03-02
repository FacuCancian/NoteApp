import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.RescheduleAlarmUseCase
import com.example.noteapp.presentation.alarm.AlarmScheduler
import com.example.noteapp.presentation.alarm.calculateNextAlarmTime
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CalculateNextAlarmTimeTest {
    @Test
    fun `should ring today if repeat days includes today and time has not passed`() {
        // Arrange
        val now = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }

        // Act
        val result = calculateNextAlarmTime(
            hour = 10,
            minute = 0,
            repeatDays = listOf(1, 2, 3, 4, 5, 6, 7),
            nowOverride = now
        )

        // Assert
        val resultCalendar = Calendar.getInstance().apply {
            timeInMillis = result!!
        }
        assertEquals(Calendar.MONDAY, resultCalendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(10, resultCalendar.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `should ring today if repeat days includes today and time has not passed tuesday`() {
        // Arrange
        val now = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }

        // Act
        val result = calculateNextAlarmTime(
            hour = 8,
            minute = 0,
            repeatDays = listOf(1, 2, 3, 4, 5, 6, 7),
            nowOverride = now
        )

        // Assert
        val resultCalendar = Calendar.getInstance().apply {
            timeInMillis = result!!
        }
        assertEquals(Calendar.WEDNESDAY, resultCalendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(8, resultCalendar.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `should schedule alarm on next valid day`() {
        data class TestCase(
            val nowDay: Int,
            val nowHour: Int,
            val expectedDay: Int
        )

        val cases = listOf(
            TestCase(Calendar.MONDAY, 9, Calendar.MONDAY),
            TestCase(Calendar.MONDAY, 11, Calendar.WEDNESDAY),
            TestCase(Calendar.TUESDAY, 9, Calendar.WEDNESDAY),
            TestCase(Calendar.WEDNESDAY, 9, Calendar.WEDNESDAY),
            TestCase(Calendar.WEDNESDAY, 11, Calendar.FRIDAY),
            TestCase(Calendar.THURSDAY, 9, Calendar.FRIDAY),
            TestCase(Calendar.FRIDAY, 9, Calendar.FRIDAY),
            TestCase(Calendar.FRIDAY, 11, Calendar.SUNDAY),
            TestCase(Calendar.SATURDAY, 9, Calendar.SUNDAY),
            TestCase(Calendar.SUNDAY, 9, Calendar.SUNDAY),
            TestCase(Calendar.SUNDAY, 11, Calendar.MONDAY),
            )

        cases.forEach { case ->
            // Arrange
            val now = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, case.nowDay)
                set(Calendar.HOUR_OF_DAY, case.nowHour)
                set(Calendar.MINUTE, 0)
            }

            // Act
            val result = calculateNextAlarmTime(
                hour = 10,
                minute = 0,
                repeatDays = listOf(1, 3, 5, 7),
                nowOverride = now
            )

            // Assert
            val resultCalendar = Calendar.getInstance().apply {
                timeInMillis = result!!
            }
            assertEquals(
                "Falló para día ${case.nowDay} hora ${case.nowHour}",
                case.expectedDay,
                resultCalendar.get(Calendar.DAY_OF_WEEK)
            )
        }
    }

}