package com.example.noteapp

import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import io.mockk.core.ValueClassSupport.boxedValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRepository : NoteRepository {
    private val noteList = mutableListOf<Note>()
    private val _notesFlow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> {
        return _notesFlow
    }

    override suspend fun getNoteById(id: Long): Note? {
        TODO("Not yet implemented")
    }

    override suspend fun getNoteByName(name: String): Note? {
        return noteList.find { it.name == name }
    }

    override suspend fun insertNote(note: Note): Boolean {
        noteList.add(note)
        _notesFlow.value = noteList.toList() // Emito copia nueva
        return true
    }

    override suspend fun deleteNote(note: Note): Boolean {
        val removed = noteList.remove(note)
        _notesFlow.value = noteList.toList()
        return removed
    }

    override suspend fun shareNote(note: Note) {
        TODO("Not yet implemented")
    }

    override suspend fun exportNote(note: Note) {
        TODO("Not yet implemented")
    }
}