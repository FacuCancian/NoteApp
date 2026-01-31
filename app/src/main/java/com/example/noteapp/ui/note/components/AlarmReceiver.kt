package com.example.noteapp.ui.note.components


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.noteapp.presentation.util.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("noteTitle") ?: "Alarma"

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("noteTitle", title)
        }
        context.startActivity(activityIntent)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("noteTitle", title)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}


