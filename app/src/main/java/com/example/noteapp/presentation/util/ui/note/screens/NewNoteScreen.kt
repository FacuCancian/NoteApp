package com.example.noteapp.presentation.util.ui.note.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.TextLayoutResult

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.noteList.NoteListViewModel

import androidx.compose.ui.text.input.TextFieldValue
import com.example.noteapp.presentation.login.SaveState
import com.example.noteapp.presentation.util.ui.NewNoteDialogs
import com.example.noteapp.presentation.util.ui.NoteEditor
import com.example.noteapp.presentation.util.ui.note.components.TopBarState
import kotlinx.coroutines.delay

private const val DEFAULT_TITLE = "Nueva nota"
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
    val saveState by viewModel.saveState.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }

// Helper
    val hasCustomTitle = noteTitle.isNotBlank()
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var noteToOverwrite by remember { mutableStateOf<Note?>(null) }

    val hasChanges = textFieldValue.text.isNotBlank()
    var lastTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }//to follow the cursor
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.AlreadyExists -> {
                showSaveDialog = false
                showOverwriteDialog = true
                viewModel.resetSaveState()
            }
            is SaveState.Done -> {
                showSaveDialog = false
                back()
                viewModel.resetSaveState()
            }
            is SaveState.Idle -> Unit
        }
    }
    // Reemplazar LaunchedEffect(hasChanges) por:
    LaunchedEffect(hasChanges, noteTitle) {
        setTopBar(
            TopBarState(
                title = noteTitle,
                editableTitle = true,
                onTitleChange = { noteTitle = it },
                showBack = true,
                showSave = hasChanges,
                onBack = {
                    if (hasChanges) {
                        if (hasCustomTitle) viewModel.trySaveNote(noteTitle, textFieldValue.text)
                        else showUnsavedDialog = true
                    } else back()
                },
                onSave = {
                    if (hasCustomTitle) viewModel.trySaveNote(noteTitle, textFieldValue.text)
                    else showSaveDialog = true
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
        if (hasChanges) {
            if (hasCustomTitle) viewModel.trySaveNote(noteTitle, textFieldValue.text)
            else showUnsavedDialog = true
        } else back()
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
            pendingTitle = noteTitle,
            onDismissSave = { showSaveDialog = false },
            onDismissOverwrite = { showOverwriteDialog = false },
            onDismissUnsaved = { showUnsavedDialog = false},
            onBack = back,
            onSaveRequested = { title ->
                noteTitle = title
                noteToOverwrite = Note(id = null, content = textFieldValue.text, name = title)
                viewModel.trySaveNote(title, textFieldValue.text)
            },
            onOverwriteConfirmed = {
                noteToOverwrite?.let { viewModel.insertNote(it) }
                showOverwriteDialog = false
                back()
            },
            onConfirmSaveFromUnsaved = {
                showUnsavedDialog = false
                if (hasCustomTitle) viewModel.trySaveNote(noteTitle, textFieldValue.text)
                else showSaveDialog = true
            }
        )
    }
}