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
import android.util.Log

class AlarmService : Service() {

    companion object {
        const val ACTION_STOP = "STOP_ALARM"
        const val ACTION_SNOOZE = "SNOOZE_ALARM"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001

        var isRunning = false

    }

    private var ringtone: Ringtone? = null
    private var alarmTitle: String = "Alarma"
    //next here, no coection, go to startForegroundAlarm()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId = intent?.getIntExtra("noteId", -1) ?: return START_NOT_STICKY
        alarmTitle = intent?.getStringExtra("noteTitle") ?: "Alarma"


        when (intent?.action) {
            ACTION_STOP -> {
                stopAlarm(noteId)
                Log.d("ALARM_DEBUG", "⛔ Alarm stopped for noteId=$noteId")
                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                snoozeAlarm(intent)
                stopAlarm(noteId)
                Log.d("ALARM_DEBUG", "⏰ Alarm snoozed for noteId=$noteId") // ⚡

                return START_NOT_STICKY
            }

            else -> startForegroundAlarm(intent)
        }

        return START_STICKY
    }

    private fun startForegroundAlarm(intent: Intent?) {
        isRunning = true
        createChannel()
        val noteId = intent?.getIntExtra("noteId", -1) ?: return

        // Stop y Snooze for requestCode unique per note
        val stopRequestCode = (noteId * 10 + 0)
        val snoozeRequestCode = (noteId * 10 + 1)

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("noteTitle", alarmTitle)
            action = ACTION_STOP
        }

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("noteTitle", alarmTitle)
            action = ACTION_SNOOZE
        }


        val stopPending =
            PendingIntent.getService(
                this,
                stopRequestCode,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        val snoozePending =
            PendingIntent.getService(
                this,
                snoozeRequestCode,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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

    private fun stopAlarm(noteId: Int) {
        ringtone?.stop()
        ringtone = null
        isRunning = false

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
    private fun snoozeAlarm(intent: Intent) {
        val noteId = intent.getIntExtra("noteId", -1)
        if (noteId == -1) return

        val newTime = System.currentTimeMillis() + 10 * 60_000 // 10 minutos

        val pending = PendingIntent.getBroadcast(
            this,
            noteId, // único por nota
            Intent(this, AlarmReceiver::class.java).apply {
                putExtra("noteId", noteId)
                putExtra("noteTitle", alarmTitle)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            newTime,
            pending
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}