package com.example.noteapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.noteapp.data.local.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note:Note): Int

    @Query("SELECT * FROM Notes")
    fun getAllNotes(): Flow<List<Note>>//flow is native for corrutines, it use flow in corrutine by default

    @Query("SELECT * FROM Notes WHERE name = :name")
    suspend fun getNoteByName(name: String): Note?

    @Query("SELECT * FROM Notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): Note?

    //live find
    @Query("SELECT * FROM Notes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchNotesByName(query: String): Flow<List<Note>>

}