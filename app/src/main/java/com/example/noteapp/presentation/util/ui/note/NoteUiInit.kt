package com.example.noteapp.presentation.util.ui.note

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.noteapp.R

enum class NoteScreen(@StringRes val title: Int) {
    Start(title = R.string.note_list),
    NoteInfo(title = R.string.note_info),
    New(title = R.string.new_note)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

