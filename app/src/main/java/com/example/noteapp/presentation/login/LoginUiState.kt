package com.example.noteapp.presentation.login

import com.example.noteapp.data.local.entities.Note

sealed class NoteListUiState {
object Loading : NoteListUiState()
data class Success(val notes: List<Note>) : NoteListUiState()
data class Error(val message: String) : NoteListUiState()
}