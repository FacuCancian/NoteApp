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

        val nextTime = AlarmTimeUtils.calculateNextFromNote(note)
            ?: return null

        val updated = note.copy(reminderDateTime = nextTime)
        repository.insertNote(updated)
        scheduler.schedule(updated)

        return nextTime
    }
}