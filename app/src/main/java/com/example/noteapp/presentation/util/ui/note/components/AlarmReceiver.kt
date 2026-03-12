package com.example.noteapp.presentation.util.ui.note.components


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import androidx.core.content.ContextCompat

import com.example.noteapp.domain.data.alarm.AlarmService
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.ACTION_START
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
        val title = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE)
            ?: AlarmConstants.DEFAULT
        Log.d("AlarmReceiver", "onReceive() — noteId=$noteId title=$title")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = ACTION_START
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, title)
        }
        ContextCompat.startForegroundService(context, serviceIntent)


        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        context.startActivity(activityIntent)
    }
}