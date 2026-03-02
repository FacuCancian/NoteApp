package com.example.noteapp.presentation.util.ui.note.components

data class TopBarState(
    val title: String = "",
    val showBack: Boolean = false,
    val showSave: Boolean = false,
    val onBack: (() -> Unit)? = null,
    val onSave: (() -> Unit)? = null
)
