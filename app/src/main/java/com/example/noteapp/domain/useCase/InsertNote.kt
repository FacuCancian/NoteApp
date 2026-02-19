package com.example.noteapp.domain.useCase

import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class InsertNote @Inject constructor(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Long {
        return repository.insertNote(note)
    }
}
