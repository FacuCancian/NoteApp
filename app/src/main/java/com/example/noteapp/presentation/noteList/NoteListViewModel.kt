package com.example.noteapp.presentation.noteList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.repository.ViewPreferencesRepository
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.domain.data.alarm.AlarmScheduler
import com.example.noteapp.presentation.login.NoteListUiState
import com.example.noteapp.presentation.login.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    val uiState: StateFlow<NoteListUiState> = _searchQuery
        //run diff paralel searchs
        .flatMapLatest { rawQuery ->
            val normalizedQuery = rawQuery.trim()
            if (normalizedQuery.isBlank()) {
                getAllNotes.get()
            } else {
                repository.searchNotes(normalizedQuery)
            }
        }
        //control of what came
        .map<List<Note>, NoteListUiState> { notes ->
            NoteListUiState.Success(notes)
        }
        .catch { e ->
            emit(NoteListUiState.Error(e.message ?: "Error desconocido"))
        }
        //change in a StateFLow to start in loading. clod flow to StateFlow to be watch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = NoteListUiState.Loading
        )

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState
    private suspend fun doesNoteExist(title: String): Boolean {
        return search.get(title) != null
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    // en NoteListViewModel
    private val viewPrefsRepo = ViewPreferencesRepository(application)

    val isGridView: StateFlow<Boolean> = viewPrefsRepo.isGridView
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun toggleViewMode() {
        viewModelScope.launch {
            viewPrefsRepo.setGridView(!isGridView.value)
        }
    }
    fun trySaveNote(title: String, content: String) {
        viewModelScope.launch {
            val exists = doesNoteExist(title)
            if (exists) {
                _saveState.value = SaveState.AlreadyExists
            } else {
                insertNote(Note(id = null, content = content, name = title))
                _saveState.value = SaveState.Done
            }
        }
    }

    private fun rescheduleIfNeeded(note: Note) {
        if (note.hasReminder) {
            alarmScheduler.schedule(note)
        }
    }

    fun renameNote(note: Note, newName: String) {
        val updateNote = note.copy(name = newName)
        viewModelScope.launch {
            insert.invoke(updateNote)
            rescheduleIfNeeded(updateNote)
        }

    }

    fun insertNote(note: Note) {
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


    fun cancelAlarm(note: Note) {
        alarmScheduler.cancel(note)

        val updated = note.copy(
            hasReminder = false,
            reminderDateTime = null,
            repeatDays = null
        )
        insertNote(updated)
    }
    fun updateNoteOrder(notes: List<Note>) {
        viewModelScope.launch {
            val reordered = notes.mapIndexed { index, note -> note.copy(position = index) }
            repository.updateNoteOrder(reordered)
        }
    }
}