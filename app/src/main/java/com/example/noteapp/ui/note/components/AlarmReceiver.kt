package com.example.noteapp.ui.note.components


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.presentation.util.AlarmScheduler
import com.example.noteapp.presentation.util.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var noteRepository: NoteRepository

    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getLongExtra("noteId", -1L)
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("noteId", noteId)
        }
        context.startActivity(activityIntent)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("noteId", noteId)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}


