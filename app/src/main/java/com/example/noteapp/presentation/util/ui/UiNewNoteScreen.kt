package com.example.noteapp.presentation.util.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.R
import com.example.noteapp.presentation.util.ui.saveDialog.SaveOrOverwriteDialog
import kotlin.text.isLetter
import kotlin.text.isLowerCase
import kotlin.text.uppercaseChar


@Composable
fun NoteEditor(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onLayout: (TextLayoutResult) -> Unit
) {
    //Box(weight + verticalScroll + imePadding) to a god layout
    //Box(weight + verticalScroll + imePadding) to a god layout
    Box(
        modifier = modifier
            .fillMaxWidth()   // 🔥 importante
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .verticalScroll(scrollState)
            .imePadding()
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->

                val oldText = textFieldValue.text
                val newText = newValue.text

                var finalText = newText
                var finalSelection = newValue.selection

                if (newText.length > oldText.length) {

                    val i = newValue.selection.end - 1
                    val lastChar = newText.getOrNull(i)

                    val shouldCapitalize =
                        i == 0 ||
                                newText.getOrNull(i - 1) == '\n' ||
                                (newText.getOrNull(i - 2) == '.' && newText.getOrNull(i - 1) == ' ')

                    if (
                        shouldCapitalize &&
                        lastChar != null &&
                        lastChar.isLetter() &&
                        lastChar.isLowerCase()
                    ) {
                        finalText =
                            newText.substring(0, i) +
                                    lastChar.uppercaseChar() +
                                    newText.substring(i + 1)

                        finalSelection = TextRange(i + 1)
                    }
                }

                onTextChange(
                    newValue.copy(
                        text = finalText,
                        selection = finalSelection
                    )
                )
            }
            ,
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            onTextLayout = { layout ->
                onLayout(layout)//to follow the cursor
            }
        )
    }
}
@Composable
fun NewNoteDialogs(
    showSaveDialog: Boolean,
    showOverwriteDialog: Boolean,
    showUnsavedDialog: Boolean,
    pendingTitle: String,
    onDismissSave: () -> Unit,
    onDismissOverwrite: () -> Unit,
    onDismissUnsaved: () -> Unit,
    onConfirmSaveFromUnsaved: () -> Unit, // 👈 NUEVO
    onBack: () -> Unit,
    onSaveRequested: (String) -> Unit,
    onOverwriteConfirmed: () -> Unit
)
 {
    /* ========= DIÁLOGOS (TU LÓGICA ORIGINAL) ========= */

    SaveOrOverwriteDialog(
        showSaveDialog = showSaveDialog,
        showOverwriteDialog = showOverwriteDialog,
        pendingTitle = pendingTitle,
        onDismissSave = onDismissSave,
        onDismissOverwrite = onDismissOverwrite,
        onSaveRequested = onSaveRequested,
        onOverwriteConfirmed = onOverwriteConfirmed,
    )

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnsaved,
            title = { Text(stringResource(R.string.not_save_note_dialog_title)) },
            text = { Text(stringResource(R.string.not_save_note_dialog_subtitle)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmSaveFromUnsaved()
                }) {
                    Text(stringResource(R.string.dialog_save),color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onDismissUnsaved()
                        onBack()
                    }) {
                        Text(stringResource(R.string.dialog_dismis),color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = onDismissUnsaved) {
                        Text(stringResource(R.string.dialog_cancel),color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        )
    }
}
