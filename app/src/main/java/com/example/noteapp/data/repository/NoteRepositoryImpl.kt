package com.example.noteapp.data.repository

import com.example.noteapp.data.local.dao.NoteDao
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(private val dao: NoteDao) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return dao.getAllNotes()
    }

    override suspend fun getNoteById(id: Long): Note? {
        return dao.getNoteById(id)
    }

    override suspend fun getNoteByName(name: String): Note? {
        return dao.getNoteByName(name)
    }

    override suspend fun insertNote(note: Note): Boolean {
        return dao.insertNote(note) != -1L
    }

    override suspend fun deleteNote(note: Note): Boolean {
        return dao.deleteNote(note) > 0
    }

    override suspend fun shareNote(note: Note) {
        TODO("Not yet implemented")
    }

    override suspend fun exportNote(note: Note) {
        TODO("Not yet implemented")
    }

}