package com.example.noteapp.presentation.util.ui.note

import androidx.annotation.StringRes
import com.example.noteapp.R

enum class NoteScreen(@StringRes val title: Int) {
    Start(title = R.string.note_list),
    NoteInfo(title = R.string.note_info),
    New(title = R.string.new_note)
}

