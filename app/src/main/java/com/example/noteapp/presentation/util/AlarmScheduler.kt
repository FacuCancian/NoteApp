package com.example.noteapp.presentation.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.note.components.AlarmReceiver
import android.app.AlarmManager
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(note: Note) {
        if (note.reminderDateTime == null) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteTitle", note.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id!!.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderDateTime,
            pendingIntent
        )
    }
    fun snooze(note: Note, minutes: Int) {
        val newTime = System.currentTimeMillis() + minutes * 60_000

        val updated = note.copy(
            reminderDateTime = newTime,
            hasReminder = true
        )

        schedule(updated)
    }
    fun cancel(note: Note) {
        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id!!.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

}
