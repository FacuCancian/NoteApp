package com.example.noteapp.presentation.util.ui.note.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.login.NoteListUiState
import com.example.noteapp.presentation.login.RenameState
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.share.shareNoteAsTxt
import com.example.noteapp.presentation.util.ui.AlarmBottomSheet
import com.example.noteapp.presentation.util.ui.DeleteNoteDialog
import com.example.noteapp.presentation.util.ui.NoteCardContent
import com.example.noteapp.presentation.util.ui.saveDialog.SaveOrOverwriteDialog

@Composable
fun NoteList(
    noteListViewModel: NoteListViewModel,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit,
    onButtonClick: () -> Unit
) {
    //collectAsState() each state change reload the view
    val uiState by noteListViewModel.uiState.collectAsState()
    val searchQuery by noteListViewModel.searchQuery.collectAsState()
    val renameState by noteListViewModel.renameState.collectAsState()

    // local state not for item
    var noteBeingRenamed by remember { mutableStateOf<Note?>(null) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }

    LaunchedEffect(renameState) {
        when (renameState) {
            is RenameState.NoteExists -> {
                showChangeNameDialog = false
                showOverwriteDialog = true
                noteListViewModel.resetRenameState()
            }
            is RenameState.RenameDone -> {
                showChangeNameDialog = false
                noteListViewModel.resetRenameState()
            }
            is RenameState.Idle -> Unit
        }
    }

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
        Column() {
            NoteSearchField(
                query = searchQuery,
                onQueryChange = { noteListViewModel.updateSearchQuery(it) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            when (uiState) {
                is NoteListUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is NoteListUiState.Success -> {
                    val notes = (uiState as NoteListUiState.Success).notes
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            NoteItemList(
                                note = note,
                                onCardClick = { onNoteClick(note) },
                                onDelete = { noteListViewModel.deleteNote(it) },
                                onLongClick = {
                                    noteBeingRenamed = note
                                    editedTitle = note.name
                                    showChangeNameDialog = true
                                }
                            )
                        }
                    }
                }

                is NoteListUiState.Error -> {
                    val message = (uiState as NoteListUiState.Error).message
                    Text(text = message, color = Color.Red)
                }
            }
            if (showChangeNameDialog || showOverwriteDialog) {
                noteBeingRenamed?.let { note ->
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
                            editedTitle = newName
                            noteListViewModel.tryRenameNote(note, newName)
                        },
                        onOverwriteConfirmed = {
                            noteListViewModel.renameNote(note, editedTitle)
                            showOverwriteDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteItemList(
    note: Note,
    onCardClick: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onLongClick: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAlarmSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    NoteCardContent(
        note = note,
        onClick = { onCardClick(note) },
        onLongClick = onLongClick,
        onDeleteClick = { showDeleteDialog = true },
        onAlarmClick = { showAlarmSheet = true },
        onShareClick = { shareNoteAsTxt(context, note) }
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
}

@Composable
fun NoteSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_note)) },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}