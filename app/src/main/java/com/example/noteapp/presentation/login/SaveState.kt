package com.example.noteapp.presentation.login

sealed class SaveState {
    object Idle : SaveState()
    object AlreadyExists : SaveState()
    object Done : SaveState()
}