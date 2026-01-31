package com.example.noteapp.presentation.util

import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
fun requestExactAlarmPermission(context: Context) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)

    if (!alarmManager.canScheduleExactAlarms()) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
