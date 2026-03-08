package com.example.noteapp.presentation.util.ui.note.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.share.shareNoteAsTxt
import com.example.noteapp.presentation.util.ui.AlarmBottomSheet
import com.example.noteapp.presentation.util.ui.DeleteNoteDialog
import com.example.noteapp.presentation.util.ui.NoteCardContent
import com.example.noteapp.presentation.util.ui.NoteGridCard
import com.example.noteapp.presentation.util.ui.note.components.TopBarState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteList(
    noteListViewModel: NoteListViewModel,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit,
    onButtonClick: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val uiState by noteListViewModel.uiState.collectAsState()
    val searchQuery by noteListViewModel.searchQuery.collectAsState()
    val isGridView by noteListViewModel.isGridView.collectAsState()
    val appName = stringResource(R.string.app_name)

    rememberLazyListState()
    LaunchedEffect(isGridView) {
        setTopBar(TopBarState(
            title = appName,
            showGridToggle = true,
            isGridView = isGridView,
            onToggleGrid = { noteListViewModel.toggleViewMode() }
        ))
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
        Column {
            NoteSearchField(
                query = searchQuery,
                onQueryChange = { noteListViewModel.updateSearchQuery(it) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            when (uiState) {
                is NoteListUiState.Success -> {
                    val notes = (uiState as NoteListUiState.Success).notes
                    var localNotes by remember(notes) { mutableStateOf(notes) }

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(localNotes, key = { it.id!! }) { note ->  // 👈 reemplaza el items anterior
                                var showDeleteDialog by remember { mutableStateOf(false) }
                                var showAlarmSheet by remember { mutableStateOf(false) }
                                val context = LocalContext.current

                                NoteGridCard(
                                    note = note,
                                    onClick = { onNoteClick(note) },
                                    onDeleteClick = { showDeleteDialog = true },
                                    onAlarmClick = { showAlarmSheet = true },
                                    onShareClick = { shareNoteAsTxt(context, note) }
                                )

                                if (showDeleteDialog) {
                                    DeleteNoteDialog(
                                        onConfirm = {
                                            noteListViewModel.deleteNote(note)
                                            showDeleteDialog = false
                                        },
                                        onDismiss = { showDeleteDialog = false }
                                    )
                                }
                                if (showAlarmSheet) {
                                    AlarmBottomSheet(
                                        note = note,
                                        viewModel = noteListViewModel,
                                        onDismiss = { showAlarmSheet = false }
                                    )
                                }
                            }
                        }
                    } else {
                        val lazyListState = rememberLazyListState()
                        val reorderState = rememberReorderableLazyListState(
                            lazyListState = lazyListState,
                            onMove = { from, to ->
                                localNotes = localNotes.toMutableList().apply {
                                    add(to.index, removeAt(from.index))
                                }
                            }
                        )
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(localNotes, key = { it.id!! }) { note ->
                                ReorderableItem(reorderState, key = note.id!!) { isDragging ->
                                    NoteItemList(
                                        note = note,
                                        onCardClick = { onNoteClick(note) },
                                        onDelete = { noteListViewModel.deleteNote(it) },
                                        reorderModifier = Modifier.longPressDraggableHandle(
                                            onDragStopped = {
                                                noteListViewModel.updateNoteOrder(localNotes)
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                is NoteListUiState.Error -> {
                    val message = (uiState as NoteListUiState.Error).message
                    Text(text = message, color = Color.Red)
                }

                NoteListUiState.Loading -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun NoteItemList(
    note: Note,
    onCardClick: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    reorderModifier: Modifier = Modifier,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAlarmSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    NoteCardContent(
        note = note,
        reorderModifier = reorderModifier,
        onClick = { onCardClick(note) },
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