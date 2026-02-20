package com.example.noteapp.presentation.util.ui.saveDialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.noteapp.R

@Composable
fun SaveNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    pendingTitle : String
) {
    var noteTitle by remember { mutableStateOf(pendingTitle) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.dialog_save)) },
        text = {
            Column {
                Text(stringResource(R.string.save_note_dialog_sub_title))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    placeholder = { Text(stringResource(R.string.save_note_dialog_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteTitle.isNotBlank()) {
                        onSave(noteTitle)
                    }
                }
            ) {
                Text(
                    stringResource(R.string.dialog_save),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.dialog_cancel),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}