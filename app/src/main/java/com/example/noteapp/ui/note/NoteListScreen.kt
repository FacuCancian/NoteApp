package com.example.noteapp.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.shareNoteAsTxt
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
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItemList(
    note: Note,
    onCardClick: (note: Note) -> Unit,
    onDelete: (Note) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var pendingName by remember { mutableStateOf("") }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCardClick(note) },
                onLongClick = { showChangeNameDialog = true }
            )

    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = note.name, fontSize = 18.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.note_Item_list_button_description),
                    tint = Color.Red
                )
            }
            IconButton(onClick = {  shareNoteAsTxt(context, note) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.share_note_text),
                    tint = Color(0xFF64B5F6)
                )
            }
        }
    }

    SaveOrOverwriteDialog(
        showSaveDialog = showChangeNameDialog,
        showOverwriteDialog = showOverwriteDialog,
        pendingTitle = pendingName,
        onDismissSave = { showChangeNameDialog = false },
        onDismissOverwrite = { showOverwriteDialog = false },
        onSaveRequested = { newName ->
            viewModel.viewModelScope.launch {
                val exists = viewModel.doesNoteExist(newName)
                if (exists) {
                    pendingName = newName
                    showChangeNameDialog = false
                    showOverwriteDialog = true
                } else {
                    viewModel.renameNote(note, newName)
                    showChangeNameDialog = false
                }
            }
        },
        onOverwriteConfirmed = {
            viewModel.renameNote(note, pendingName)
            showOverwriteDialog = false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_sub_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(note)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}
