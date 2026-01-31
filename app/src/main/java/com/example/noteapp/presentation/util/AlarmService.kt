package com.example.noteapp.presentation.util


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Context

import com.example.noteapp.ui.note.components.AlarmReceiver
import android.provider.Settings
class AlarmService : Service() {

    companion object {
        const val ACTION_STOP = "STOP_ALARM"
        const val ACTION_SNOOZE = "SNOOZE_ALARM"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var ringtone: Ringtone? = null
    private var alarmTitle: String = "Alarma"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        alarmTitle = intent?.getStringExtra("noteTitle") ?: "Alarma"

        when (intent?.action) {
            ACTION_STOP -> {
                stopAlarm()
                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                snoozeAlarm()
                stopAlarm()
                return START_NOT_STICKY
            }

            else -> startForegroundAlarm()
        }

        return START_STICKY
    }

    private fun startForegroundAlarm() {
        createChannel()

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE
        }

        val stopPending =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val snoozePending =
            PendingIntent.getService(this, 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmTitle)
            .setContentText("Sonando…")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopPending)
            .addAction(android.R.drawable.ic_media_pause, "Posponer 10 min", snoozePending)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        if (ringtone == null) {
            val uri = Settings.System.DEFAULT_ALARM_ALERT_URI
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        ringtone = null
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm() {
        val newTime = System.currentTimeMillis() + 10 * 60_000

        val intent = Intent(this, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            this,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, newTime, pending)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
