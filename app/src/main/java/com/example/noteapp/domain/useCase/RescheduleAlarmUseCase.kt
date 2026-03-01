package com.example.noteapp.domain.useCase

import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.presentation.alarm.AlarmScheduler
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import javax.inject.Inject

class RescheduleAlarmUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val scheduler: AlarmScheduler
) {

    suspend fun execute(noteId: Long): Long? {

        val note = repository.getNoteById(noteId) ?: return null

        val days = note.repeatDays?.toMutableList()

        if (days.isNullOrEmpty()) {
            val updated = note.copy(
                hasReminder = false,
                reminderDateTime = null
            )
            repository.insertNote(updated)
            return null
        }

        val today = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_WEEK)

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

        return nextTime
    }
}