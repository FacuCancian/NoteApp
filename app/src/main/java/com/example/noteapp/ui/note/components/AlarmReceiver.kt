package com.example.noteapp.ui.note.components


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.presentation.util.AlarmScheduler
import com.example.noteapp.presentation.util.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var noteRepository: NoteRepository

    //AlarmTime has come...
    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getIntExtra("noteId", -1)
        val title = intent.getStringExtra("noteTitle")
        //create and launch activity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("noteTitle", title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        context.startActivity(activityIntent)
        //launch service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("noteTitle", title)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}


