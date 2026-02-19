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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()

        if (!AlarmService.isRunning) {
            Log.d("ALARM_DEBUG", "📴 Service not running, closing activity")
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("noteTitle") ?: "Alarma"

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                // --- TÍTULO CENTRADO ---
                Text(
                    text = "⏰ $title",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                // --- BOTONES ABAJO ---
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
                        Text("Apagar")
                    }

                    Button(
                        onClick = { snoozeAlarm() }
                    ) {
                        Text("Posponer")
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 🔥 MUY IMPORTANTE

        val newId = intent.getIntExtra("noteId", -1)
        Log.d("ALARM_DEBUG", "🔄 onNewIntent received, noteId=$newId")
    }
    private fun stopAlarm() {
        val currentId = intent.getIntExtra("noteId", -1)
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
            putExtra("noteId", currentId)
            putExtra("noteTitle", title)
        }
        Log.d("ALARM_DEBUG", "🛑 stopAlarm called, noteId=$currentId")  // ✅ log para debug

        startService(intent)
        finish()
    }

    private fun snoozeAlarm() {
        val currentId = intent.getIntExtra("noteId", -1)
        val noteTitle = intent.getStringExtra("noteTitle")
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE
            putExtra("noteId", currentId)
            putExtra("noteTitle", noteTitle)
        }
        startService(intent)
        finish()
    }
}
