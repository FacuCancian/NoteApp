package com.example.noteapp.domain.useCase

import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.presentation.alarm.AlarmScheduler
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import javax.inject.Inject

class RescheduleAlarmUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val scheduler: AlarmScheduler
) {

    suspend fun execute(noteId: Long) {

        val note = repository.getNoteById(noteId) ?: return

        val nextTime = AlarmTimeUtils.calculateNextFromNote(note)
            ?: return

        val updated = note.copy(reminderDateTime = nextTime)

        scheduler.schedule(updated)
    }
}