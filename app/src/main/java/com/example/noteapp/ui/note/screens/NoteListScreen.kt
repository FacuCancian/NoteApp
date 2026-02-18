package com.example.noteapp.ui.note.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.shareNoteAsTxt
import com.example.noteapp.presentation.util.ui.AlarmBottomSheet
import com.example.noteapp.presentation.util.ui.DeleteNoteDialog
import com.example.noteapp.presentation.util.ui.NoteCardContent
import com.example.noteapp.presentation.util.ui.saveDialog.SaveOrOverwriteDialog
import kotlinx.coroutines.launch


@Composable
fun NoteList(
    noteListViewModel: NoteListViewModel,
    modifier: Modifier,
    onNoteClick: (Note) -> Unit,
    onButtonClick: () -> Unit
) {
    val notes by noteListViewModel.notes.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onButtonClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(R.drawable.pen_icon),
                    contentDescription = stringResource(R.string.note_button),
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier.padding(innerPadding).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes) { note ->
                NoteItemList(
                    note = note,
                    onCardClick = { onNoteClick(note) },
                    onDelete = { noteToDelete ->
                        noteListViewModel.deleteNote(noteToDelete)
                    },
                )
            }
        }
    }
}

@Composable
fun NoteItemList(
    note: Note,
    onCardClick: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAlarmSheet by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(note.name) }
    val context = LocalContext.current
    NoteCardContent(
        note = note,
        onClick = { onCardClick(note) },
        onLongClick = { showChangeNameDialog = true },
        onDeleteClick = { showDeleteDialog = true },
        onAlarmClick = { showAlarmSheet = true },
        onShareClick = { shareNoteAsTxt(context, note) },

        )

    if (showDeleteDialog) {
        DeleteNoteDialog(
            onConfirm = {
                onDelete(note)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showAlarmSheet) {
        AlarmBottomSheet(
            note = note,
            viewModel = viewModel,
            onDismiss = { showAlarmSheet = false }
        )
    }


    if (showChangeNameDialog || showOverwriteDialog) {
        SaveOrOverwriteDialog(
            showSaveDialog = showChangeNameDialog,
            showOverwriteDialog = showOverwriteDialog,
            pendingTitle = editedTitle,
            onDismissSave = { showChangeNameDialog = false },
            onDismissOverwrite = {
                showOverwriteDialog = false
                showChangeNameDialog = false
            },
            onSaveRequested = { newName ->
                viewModel.viewModelScope.launch {
                    val exists = viewModel.doesNoteExist(newName)
                    if (exists) {
                        editedTitle = newName
                        showChangeNameDialog = false
                        showOverwriteDialog = true
                    } else {
                        viewModel.renameNote(note, newName)
                        showChangeNameDialog = false
                    }
                }
            },
            onOverwriteConfirmed = {
                viewModel.renameNote(note, editedTitle)
                showOverwriteDialog = false
            }
        )
    }

}
