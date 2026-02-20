package com.example.noteapp.domain.repository

import com.example.noteapp.data.local.entities.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?

    suspend fun getNoteByName(name: String):Note?
    suspend fun insertNote(note: Note): Long
    suspend fun deleteNote(note: Note):Boolean
    suspend fun shareNote(note:Note)
    suspend fun exportNote(note:Note)

    suspend fun searchNotes(query: String): Flow<List<Note>>

}