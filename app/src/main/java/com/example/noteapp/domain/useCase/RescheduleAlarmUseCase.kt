package com.example.noteapp.domain.useCase

import android.util.Log
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.data.alarm.AlarmScheduler
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class RescheduleAlarmUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val scheduler: AlarmScheduler
) {

    suspend fun execute(noteId: Long): Long? {
        Log.d("RescheduleUseCase", "execute() noteId=$noteId")
        val note = repository.getNoteById(noteId) ?: return null
        Log.d("RescheduleUseCase", "note recuperada: repeatDays=${note?.repeatDays} forever=${note?.repeatForever}")
        val days = note.repeatDays?.toMutableList()

        if (days.isNullOrEmpty()) {
            val updated = note.copy(
                hasReminder = false,
                reminderDateTime = null
            )
            repository.insertNote(updated)
            return null
        }

        val todayCalendar = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val today = AlarmTimeUtils.reverseDayMap[todayCalendar] ?: return null

        val updatedDays = if (!note.repeatForever) {
            days.apply { remove(today) }
        } else {
            days
        }

        if (updatedDays.isEmpty()) {
            val updated = note.copy(
                hasReminder = false,
                reminderDateTime = null,
                repeatDays = null
            )
            repository.insertNote(updated)
            return null
        }

        val nextTime = AlarmTimeUtils.calculateNextFromNote(
            note.copy(repeatDays = updatedDays)
        ) ?: return null

        val updated = note.copy(
            reminderDateTime = nextTime,
            repeatDays = updatedDays
        )

        repository.insertNote(updated)
        scheduler.schedule(updated)
        Log.d("RescheduleUseCase", "próxima alarma en: ${nextTime?.let {
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(it))
        }}")
        return nextTime
    }
}