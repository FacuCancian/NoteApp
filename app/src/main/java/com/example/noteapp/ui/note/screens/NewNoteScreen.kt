package com.example.noteapp.ui.note.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.TextLayoutResult

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.noteList.NoteListViewModel

import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import com.example.noteapp.presentation.util.ui.NewNoteDialogs
import com.example.noteapp.presentation.util.ui.NoteEditor
import com.example.noteapp.ui.note.components.TopBarState
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewNote(
    viewModel: NoteListViewModel = hiltViewModel(),
    back: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val scrollState = rememberScrollState()

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {//to follow cursor
        mutableStateOf(TextFieldValue(""))
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var pendingTitle by remember { mutableStateOf("") }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var noteToOverwrite by remember { mutableStateOf<Note?>(null) }

    val hasChanges = textFieldValue.text.isNotBlank()
    var lastTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }//to follow the cursor
    LaunchedEffect(hasChanges) {
        setTopBar(
            TopBarState(
                title = "Nueva nota",
                showBack = true,
                showSave = hasChanges,
                onBack = {
                    if (hasChanges) showUnsavedDialog = true
                    else back()
                },
                onSave = {
                    showSaveDialog = true
                }
            )
        )
    }
    LaunchedEffect(textFieldValue.selection) {
        val cursorOffset = textFieldValue.selection.end

        // esperamos un frame para que layout esté listo
        delay(16)

        val layout = lastTextLayoutResult ?: return@LaunchedEffect

        if (cursorOffset <= layout.layoutInput.text.length) {
            val cursorRect = layout.getCursorRect(cursorOffset)

            scrollState.animateScrollTo(
                (cursorRect.top - 100).toInt().coerceAtLeast(0)
            )
        }
    }
    BackHandler {
        if (hasChanges) showUnsavedDialog = true
        else back()
    }
    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            NoteEditor(
                modifier = Modifier.weight(1f),
                scrollState = scrollState,
                textFieldValue = textFieldValue,
                onTextChange = { textFieldValue = it },
                onLayout = { lastTextLayoutResult = it }
            )
        }

        NewNoteDialogs(
            showSaveDialog = showSaveDialog,
            showOverwriteDialog = showOverwriteDialog,
            showUnsavedDialog = showUnsavedDialog,
            pendingTitle = pendingTitle,
            onDismissSave = { showSaveDialog = false },
            onDismissOverwrite = { showOverwriteDialog = false },
            onDismissUnsaved = { showUnsavedDialog = false},
            onBack = back,
            onSaveRequested = { title ->
                viewModel.viewModelScope.launch {
                    val exists = viewModel.doesNoteExist(title)
                    if (exists) {
                        noteToOverwrite = Note(
                            id = null,
                            content = textFieldValue.text,
                            name = title
                        )
                        pendingTitle = title
                        showSaveDialog = false
                        showOverwriteDialog = true
                    } else {
                        viewModel.insertNote(
                            Note(
                                id = null,
                                content = textFieldValue.text,
                                name = title
                            )
                        )
                        showSaveDialog = false
                        back()
                    }
                }
            },
            onOverwriteConfirmed = {
                noteToOverwrite?.let { viewModel.insertNote(it) }
                showOverwriteDialog = false
                back()
            },
            onConfirmSaveFromUnsaved = {
                showUnsavedDialog = false
                showSaveDialog = true
            }
        )
    }
}