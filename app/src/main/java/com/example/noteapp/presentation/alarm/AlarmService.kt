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
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.noteapp.ui.note.components.AlarmReceiver
import android.provider.Settings
import android.widget.Toast
import com.example.noteapp.domain.useCase.RescheduleAlarmUseCase
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import com.example.noteapp.ui.note.components.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {
    @Inject
    lateinit var rescheduleAlarmUseCase: RescheduleAlarmUseCase

    companion object {
        const val ACTION_STOP = "STOP_ALARM"
        const val ACTION_SNOOZE = "SNOOZE_ALARM"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001

        var isRunning = false

    }
    private var vibrator: Vibrator? = null
    private var ringtone: Ringtone? = null
    private var alarmTitle: String = AlarmConstants.DEFAULT

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId =
            intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: return START_NOT_STICKY
        alarmTitle =
            intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT

        when (intent?.action) {
            ACTION_STOP -> {
                val noteId = intent.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
                CoroutineScope(Dispatchers.IO).launch {
                    if (noteId != -1) {
                        val nextTime = rescheduleAlarmUseCase.execute(noteId.toLong())
                        nextTime?.let {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@AlarmService,
                                    "Próxima alarma: ${AlarmTimeUtils.formatTimeUntil(it)}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    // 👇 ahora sí paramos la alarma
                    withContext(Dispatchers.Main) {
                        stopAlarm()
                    }
                }

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
            action = ACTION_STOP
        }

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
            action = ACTION_SNOOZE
        }


        PendingIntent.getService(
            this,
            stopRequestCode,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        PendingIntent.getService(
            this,
            snoozeRequestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        PendingIntent.getActivity(
            this,
            noteId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val activityPending = PendingIntent.getActivity(
            this,
            noteId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmTitle)
            .setContentText("Sonando...")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(activityPending) // 👈 ESTA ES LA CLAVE
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startActivity(activityIntent)
        if (ringtone == null) {
            val uri = Settings.System.DEFAULT_ALARM_ALERT_URI
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        }

// 🔥 VIBRACIÓN SIEMPRE ACTIVA
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 500)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0) // 0 = loop infinito
            )
        } else {
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null

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