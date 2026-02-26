package com.example.noteapp.presentation.login

sealed class RenameState {
    object Idle : RenameState()
    object NoteExists : RenameState()
    object RenameDone : RenameState()
}