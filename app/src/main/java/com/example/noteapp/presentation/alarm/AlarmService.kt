package com.example.noteapp.presentation.alarm

import android.R
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
import com.example.noteapp.ui.note.components.AlarmReceiver
import android.provider.Settings
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants

class AlarmService : Service() {

    companion object {
        const val ACTION_STOP = "STOP_ALARM"
        const val ACTION_SNOOZE = "SNOOZE_ALARM"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001

        var isRunning = false

    }

    private var ringtone: Ringtone? = null
    private var alarmTitle: String = AlarmConstants.DEFAULT
    //next here, no coection, go to startForegroundAlarm()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId =
            intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: return START_NOT_STICKY
        alarmTitle =
            intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT


        when (intent?.action) {
            ACTION_STOP -> {
                stopAlarm()
                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                snoozeAlarm(intent)
                stopAlarm()
                return START_NOT_STICKY
            }

            else -> startForegroundAlarm(intent)
        }

        return START_STICKY
    }

    private fun startForegroundAlarm(intent: Intent?) {
        isRunning = true
        createChannel()
        val noteId = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: return

        // Stop y Snooze for requestCode unique per note
        val stopRequestCode = (noteId * 10 + 0)
        val snoozeRequestCode = (noteId * 10 + 1)

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
            action  = ACTION_STOP
        }

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
            action  = ACTION_SNOOZE
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
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmTitle)
            .setContentText(AlarmConstants.SOUND)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(R.drawable.ic_media_pause, AlarmConstants.STOPALARM, stopPending)
            .addAction(R.drawable.ic_media_pause, AlarmConstants.POS, snoozePending)
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
        isRunning = false

        stopSelf()
    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                AlarmConstants.DEFAULTCHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm(intent: Intent) {
        val noteId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
        if (noteId == -1) return

        val newTime = System.currentTimeMillis() + 10 * 60_000 // 10 min

        val pending = PendingIntent.getBroadcast(
            this,
            noteId, // unique per note
            Intent(this, AlarmReceiver::class.java).apply {
                putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
                putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            newTime,
            pending
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}