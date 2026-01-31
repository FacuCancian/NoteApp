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
import kotlin.text.isLetter
import kotlin.text.isLowerCase
import kotlin.text.uppercaseChar

@Composable
fun NoteInfoEditor(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onLayout: (TextLayoutResult) -> Unit
) {
    //Box(weight + verticalScroll + imePadding) to a god layout
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp) // padding interno// 👈 ocupa el resto
            .verticalScroll(scrollState)
            .imePadding()               // 👈 CLAVE
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
            },
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            onTextLayout = { layout ->
                onLayout(layout)
            }
        )
    }
}
@Composable
fun NoteInfoDialogs(
    showUnsavedDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.not_save_note_dialog_title)) },
            text = { Text(stringResource(R.string.not_save_note_dialog_subtitle)) },
            confirmButton = {
                TextButton(onClick = onSave) {
                    Text(
                        stringResource(R.string.dialog_save),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = onDiscard) {
                        Text(
                            stringResource(R.string.dialog_dismis),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = onDismiss) {
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
