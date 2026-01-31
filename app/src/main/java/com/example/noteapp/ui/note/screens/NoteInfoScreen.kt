package com.example.noteapp.ui.note.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.note.components.TopBarState

import androidx.compose.ui.text.input.TextFieldValue
import com.example.noteapp.presentation.util.ui.NoteInfoDialogs
import com.example.noteapp.presentation.util.ui.NoteInfoEditor
import kotlinx.coroutines.delay

@Composable
fun NoteInfo(
    note: Note,
    onSave: (Note) -> Unit,
    onNoteSaved: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val scrollState = rememberScrollState()

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(note.content))
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges = textFieldValue.text != note.content
    var lastTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }//to follow the cursor

    LaunchedEffect(hasChanges) {
        setTopBar(
            TopBarState(
                title = note.name,
                showBack = true,
                showSave = hasChanges,
                onBack = {
                    if (hasChanges) showUnsavedDialog = true
                    else onNoteSaved()
                },
                onSave = {
                    onSave(note.copy(content = textFieldValue.text))
                    onNoteSaved()
                }
            )
        )
    }

    LaunchedEffect(textFieldValue.selection) {
        val cursorOffset = textFieldValue.selection.end
        delay(16)

        val layout = lastTextLayoutResult ?: return@LaunchedEffect

        if (cursorOffset <= layout.layoutInput.text.length) {
            val cursorRect = layout.getCursorRect(cursorOffset)

            scrollState.animateScrollTo(
                (cursorRect.top - 100).toInt().coerceAtLeast(0)
            )
        }
    }

    BackHandler(enabled = hasChanges) {
        showUnsavedDialog = true
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            NoteInfoEditor(
                modifier = Modifier.weight(1f),
                scrollState = scrollState,
                textFieldValue = textFieldValue,
                onTextChange = { textFieldValue = it },
                onLayout = { lastTextLayoutResult = it }
            )
        }

        NoteInfoDialogs(
            showUnsavedDialog = showUnsavedDialog,
            onDismiss = { showUnsavedDialog = false },
            onSave = {
                onSave(note.copy(content = textFieldValue.text))
                showUnsavedDialog = false
                onNoteSaved()
            },
            onDiscard = {
                showUnsavedDialog = false
                onNoteSaved()
            }
        )
    }
}
