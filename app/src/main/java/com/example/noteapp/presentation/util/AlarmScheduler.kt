package com.example.noteapp.presentation.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.note.components.AlarmReceiver
import android.app.AlarmManager
import android.util.Log
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(note: Note) {
        if (note.reminderDateTime == null) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("noteTitle", note.name)

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
            note.id!!.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("ALARM_DEBUG", "⛔ Canceling alarm for noteId=${note.id}")
        alarmManager.cancel(pendingIntent)
    }

}