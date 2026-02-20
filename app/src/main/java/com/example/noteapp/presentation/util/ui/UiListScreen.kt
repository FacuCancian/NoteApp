package com.example.noteapp.presentation.util.ui

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.AlarmConstants
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import com.example.noteapp.presentation.util.calculateNextAlarmTime
import com.example.noteapp.presentation.util.requestExactAlarmPermission
import java.util.Calendar
import kotlin.collections.ifEmpty
object WeekUtils {
    val weekLetters = listOf("L","M","MI","J","V","S","D")
}
@Composable
fun NoteCardContent(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () ->Unit,
    onDeleteClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(Modifier.padding(16.dp)) {
            Text(note.name, modifier = Modifier.weight(1f), fontSize = 18.sp)

            IconButton(onClick = onAlarmClick) {
                if (note.hasReminder == true) {
                    Icon(
                        painter = painterResource(R.drawable.bell_active_icon),
                        contentDescription = stringResource(R.string.bell_init),
                        tint = Color(0xFF64B5F6)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.bell_icon),
                        contentDescription = stringResource(R.string.bell_title),
                        tint = Color(0xFF64B5F6)
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.note_Item_list_button_description),
                    tint = Color.Red
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.share_note_text),
                    tint = Color(0xFF64B5F6)
                )
            }

        }
    }
}
@Composable
fun DeleteNoteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_title)) },
        text = { Text(stringResource(R.string.dialog_delete_sub_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_delete), color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    note: Note,
    viewModel: NoteListViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedDays by remember { mutableStateOf(note.repeatDays ?: emptyList()) }

    val (initialHour, initialMinute) =
        AlarmTimeUtils.extractHourMinute(note.reminderDateTime)

    val timeState = rememberTimePickerState(
        initialHour,
        initialMinute,
        true
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(onDismissRequest = onDismiss,sheetState = sheetState) {

        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(AlarmConstants.DEFAULT, style = MaterialTheme.typography.titleLarge)

            TimePicker(state = timeState)

            RepeatDaysRow(
                selectedDays = selectedDays,
                onChange = { selectedDays = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = {
                        viewModel.cancelAlarm(note)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val triggerTime = calculateNextAlarmTime(
                            timeState.hour,
                            timeState.minute,
                            selectedDays
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestExactAlarmPermission(context)
                        }
                        val noteWithTime = note.copy(
                            reminderDateTime = triggerTime,
                            repeatDays = selectedDays.ifEmpty { null },
                            hasReminder = true
                        )
                        viewModel.saveNoteWithAlarm(noteWithTime)

                        val message =
                            "Alarma programada en ${AlarmTimeUtils.formatTimeUntil(triggerTime)}"
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.dialog_save))
                }
            }
        }
    }
}

@Composable
fun RepeatDaysRow(
    selectedDays: List<Int>,
    onChange: (List<Int>) -> Unit
) {
    val dayLetters = WeekUtils.weekLetters

    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
        dayLetters.forEachIndexed { index, letter ->
            val dayIndex = index + 1
            val isSelected = selectedDays.contains(dayIndex)

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
                    .clickable {
                        onChange(
                            if (isSelected) selectedDays - dayIndex
                            else selectedDays + dayIndex
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(letter, color = if (isSelected) Color.White else Color.Black)
            }
        }
    }
}

