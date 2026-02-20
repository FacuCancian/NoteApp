package com.example.noteapp.presentation.util.ui.saveDialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.noteapp.R

@Composable
fun SaveOrOverwriteDialog(
    showSaveDialog: Boolean,
    showOverwriteDialog: Boolean,
    pendingTitle: String,
    onDismissSave: () -> Unit,
    onDismissOverwrite: () -> Unit,
    onSaveRequested: (String) -> Unit,
    onOverwriteConfirmed: () -> Unit,
) {
    if (showSaveDialog) {
        SaveNoteDialog(
            pendingTitle = pendingTitle,
            onDismiss = onDismissSave,
            onSave = { title -> onSaveRequested(title) }
        )
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = onDismissOverwrite,
            title = { Text(stringResource(R.string.showOverwriteDialog_title)) },
            text = {
                Text(stringResource(R.string.showOverwriteDialog_sub_title, pendingTitle))
            },
            confirmButton = {
                TextButton(onClick = onOverwriteConfirmed) {
                    Text(
                        stringResource(R.string.showOverwriteDialog_confirm_button),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissOverwrite) {
                    Text(
                        stringResource(R.string.showOverwriteDialog_cancel_button),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }

}
