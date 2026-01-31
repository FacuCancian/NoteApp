package com.example.noteapp.ui.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.R
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.ui.note.components.SaveNoteDialog
import com.example.noteapp.ui.note.components.SaveOrOverwriteDialog
import kotlinx.coroutines.launch

@Composable
fun NewNote(
    viewModel: NoteListViewModel = hiltViewModel(),
    back: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var pendingTitle by remember { mutableStateOf("") }
    var showOverwriteDialog by remember { mutableStateOf(false) }

    val hasChanges = text != ""

    BackHandler(enabled = hasChanges) {
        showUnsavedDialog = true
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (text.isNotBlank()) {
                        showSaveDialog = true
                    }
                },
                modifier = Modifier.imePadding(),//to not be under keyboard
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.floatting_action_button_content)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),//to not be under keyboard
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        stringResource(R.string.button_placeholder),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                textStyle = TextStyle(fontSize = 18.sp),
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ), colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
        SaveOrOverwriteDialog(
            showSaveDialog = showSaveDialog,
            showOverwriteDialog = showOverwriteDialog,
            pendingTitle = pendingTitle,
            onDismissSave = { showSaveDialog = false },
            onDismissOverwrite = { showOverwriteDialog = false },
            onSaveRequested = { title ->
                viewModel.viewModelScope.launch {
                    val exists = viewModel.doesNoteExist(title)
                    if (exists) {
                        pendingTitle = title
                        showSaveDialog = false
                        showOverwriteDialog = true
                    } else {
                        viewModel.insertNote(content = text, name = title, id = null)
                        showSaveDialog = false
                        back()
                    }
                }
            },
            onOverwriteConfirmed = {
                viewModel.insertNote(content = text, name = pendingTitle, id = null)
                showOverwriteDialog = false
                back()
            }
        )

        if (showUnsavedDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedDialog = false },
                title = { Text(stringResource(R.string.not_save_note_dialog_title)) },
                text = { Text(stringResource(R.string.not_save_note_dialog_subtitle)) },
                confirmButton = {
                    TextButton(onClick = {
                        showSaveDialog = true
                        showUnsavedDialog = false
                    }) {
                        Text(
                            stringResource(R.string.dialog_save),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            showUnsavedDialog = false
                            back()
                        }) {
                            Text(
                                stringResource(R.string.dialog_dismis),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(onClick = { showUnsavedDialog = false }) {
                            Text(
                                stringResource(R.string.dialog_cancel),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    }
}
