package com.example.noteapp.presentation.noteList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.repository.NoteRepositoryImpl
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.presentation.alarm.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    application: Application,
    private val getAllNotes: GetAllNotes,
    private val insert: InsertNote,
    private val delete: DeleteNote,
    private val search: GetNoteByName,
    private val alarmScheduler: AlarmScheduler,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _searchedNote = MutableStateFlow<Note?>(null)
    val searchedNote: StateFlow<Note?> = _searchedNote
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredNotes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getAllNotes.get() // Flow<List<Note>>
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

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
            insert.invoke(updateNote)
        }
    }

    public fun insertNote(note: Note) {
        viewModelScope.launch {
            insert.invoke(note)
        }

    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            val success = delete.delete(note)
            if (success == true) {
                //TODO MSJ
            }
        }
    }

    fun saveNoteWithAlarm(note: Note) {
        viewModelScope.launch {
            val generatedId = insert.invoke(note) // get a Long
            val savedNote = note.copy(id = generatedId.toInt())
            alarmScheduler.schedule(savedNote)
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