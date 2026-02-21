package com.example.noteapp.presentation.alarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.note.components.AlarmReceiver
import android.app.AlarmManager
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(note: Note) {
        if (note.reminderDateTime == null || note.id == null) return

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
        //call the manager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderDateTime,//alarm time
            pendingIntent
        )
    }

    fun cancel(note: Note) {
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