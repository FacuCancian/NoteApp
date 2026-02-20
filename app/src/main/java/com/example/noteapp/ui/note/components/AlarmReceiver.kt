package com.example.noteapp.ui.note.components


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import com.example.noteapp.presentation.alarm.AlarmScheduler
import com.example.noteapp.presentation.alarm.AlarmService
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

        val noteId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
        val title = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE)
        //create and launch activity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        context.startActivity(activityIntent)
        //launch service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, title)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}


