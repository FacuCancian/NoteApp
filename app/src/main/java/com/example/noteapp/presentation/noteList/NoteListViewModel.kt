package com.example.noteapp.presentation.noteList

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.presentation.util.AlarmScheduler
import com.example.noteapp.ui.note.components.AlarmReceiver
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    application: Application,
    private val getAllNotes: GetAllNotes,
    private val insert: InsertNote,
    private val delete: DeleteNote,
    private val search: GetNoteByName,
    private val alarmScheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _searchedNote = MutableStateFlow<Note?>(null)
    val searchedNote: StateFlow<Note?> = _searchedNote

    init {
        viewModelScope.launch {
            getAllNotes.get().collect { noteList ->
                _notes.value = noteList
            }
        }
    }

    public fun renameNote(note: Note, newName: String) {
        val updateNote = note.copy(name = newName)
        viewModelScope.launch {
            insert.get(updateNote)
        }
    }

    public fun insertNote(note: Note) {
        viewModelScope.launch {
            insert.get(note)
        }

    }

    public fun deleteNote(note: Note) {
        viewModelScope.launch {
            val success = delete.delete(note)
            if (success == true) {
                //TODO MSJ
            }
        }
    }

    suspend fun doesNoteExist(title: String): Boolean {
        val note = search.get(title)
        return note != null
    }

    public fun getNoteByName(name: String) {
        viewModelScope.launch {
            val noteFind = search.get(name)
            _searchedNote.value = noteFind
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(note: Note) {
        if (note.reminderDateTime == null) return

        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("noteTitle", note.name)
            putExtra("content", note.content)
            putExtra("repeatDays", note.repeatDays?.toIntArray())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderDateTime,
            pendingIntent
        )
    }
    fun snoozeNote(note: Note, minutes: Int) {
        alarmScheduler.snooze(note, minutes)
    }
    fun cancelAlarm(note: Note) {
        alarmScheduler.cancel(note)

        val updated = note.copy(
            hasReminder = false,
            reminderDateTime = null,
            repeatDays = null
        )

        insertNote(updated)
    }

}