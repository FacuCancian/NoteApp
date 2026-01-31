package com.example.noteapp.ui.note.components

import android.content.Intent
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

    private fun stopAlarm() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        startService(intent)
        finish()
    }

    private fun snoozeAlarm() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE
        }
        startService(intent)
        finish()
    }
}
