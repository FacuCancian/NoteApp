package com.example.noteapp.ui.note.components

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noteapp.presentation.util.AlarmService
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.noteapp.R
import com.example.noteapp.presentation.util.AlarmConstants

class AlarmActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()

        if (!AlarmService.isRunning) {
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Text(
                    text = "⏰ $title",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = { stopAlarm() }
                    ) {
                        Text(stringResource(R.string.stop_alarm))
                    }

                    Button(
                        onClick = { snoozeAlarm() }
                    ) {
                        Text(stringResource(R.string.pos_alarm))
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun stopAlarm() {
        val currentId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
            putExtra(AlarmConstants.EXTRA_NOTE_ID, currentId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, title)
        }
        startService(intent)
        finish()
    }

    private fun snoozeAlarm() {
        val currentId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
        val noteTitle = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE)
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE
            putExtra(AlarmConstants.EXTRA_NOTE_ID, currentId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, noteTitle)
        }
        startService(intent)
        finish()
    }
}
