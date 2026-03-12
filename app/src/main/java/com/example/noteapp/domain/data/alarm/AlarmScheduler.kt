package com.example.noteapp.domain.data.alarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.presentation.util.ui.note.components.AlarmReceiver
import android.app.AlarmManager
import android.util.Log
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

open class AlarmScheduler @Inject constructor(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    open fun schedule(note: Note) {
        if (note.reminderDateTime == null || note.id == null){
            Log.e("AlarmScheduler", "schedule() abortado — id=${note.id} time=${note.reminderDateTime}")
            return
        }
        Log.d("AlarmScheduler", """
        schedule() llamado
        noteId     = ${note.id}
        title      = ${note.name}
        triggerAt  = ${SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(note.reminderDateTime))}
        repeatDays = ${note.repeatDays}
        forever    = ${note.repeatForever}
        hasReminder= ${note.hasReminder}
    """.trimIndent())

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, note.id)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, note.name)

        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id!!,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        //call the manager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderDateTime,//alarm time
            pendingIntent
        )
    }

    open fun cancel(note: Note) {
        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id!!,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

}