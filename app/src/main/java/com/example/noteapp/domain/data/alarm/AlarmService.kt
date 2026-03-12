package com.example.noteapp.domain.data.alarm

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
import com.example.noteapp.presentation.util.ui.note.components.AlarmReceiver
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.noteapp.domain.useCase.RescheduleAlarmUseCase
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.ACTION_SNOOZE
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.ACTION_START
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.ACTION_STOP
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.CHANNEL_ID
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.NOTIFICATION_ID
import com.example.noteapp.presentation.util.alarmUtils.AlarmConstants.isRunning
import com.example.noteapp.presentation.util.alarmUtils.AlarmTimeUtils
import com.example.noteapp.presentation.util.ui.note.components.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AlarmService : Service() {
    @Inject
    lateinit var rescheduleAlarmUseCase: RescheduleAlarmUseCase

    private var vibrator: Vibrator? = null
    private var ringtone: Ringtone? = null
    private var alarmTitle: String = AlarmConstants.DEFAULT
    private var userInteracted = false
    private var currentNoteId: Int = -1
    private var autoStopJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun handleMissedAlarm() {
        if (currentNoteId == -1) return

        val nextTime = withContext(Dispatchers.IO) {
            rescheduleAlarmUseCase.execute(currentNoteId.toLong())
        }

        if (nextTime != null) {
            val formattedDate = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            ).format(Date(nextTime))

            Toast.makeText(
                this,
                "Reprogramada para: $formattedDate",
                Toast.LENGTH_LONG
            ).show()
        }

        val notificationManager =
            getSystemService(NotificationManager::class.java)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarma perdida")
            .setContentText("$alarmTitle - no la escuchaste")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(currentNoteId + 2000, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        Log.d("AlarmService", "onStartCommand action=${intent?.action} (extras aún no leídos)")



        when (intent?.action) {
            ACTION_START -> {
                currentNoteId = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: -1
                alarmTitle =
                    intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT
                val id = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1)
                val t = intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE)
                Log.d("AlarmService", "ACTION_START recibido — noteId=$id title=$t")
                startForegroundAlarm(intent)
            }
            ACTION_STOP -> {
                val noteId = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: -1
                val title = intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT
                currentNoteId = noteId
                alarmTitle = title
                userInteracted = true
                serviceScope.launch(Dispatchers.IO) {
                    if (currentNoteId != -1) {
                        val nextTime = rescheduleAlarmUseCase.execute(currentNoteId.toLong())
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
                    withContext(Dispatchers.Main) {
                        stopAlarm()
                    }
                }

                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                currentNoteId = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: -1
                alarmTitle = intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_TITLE) ?: AlarmConstants.DEFAULT
                userInteracted = true
                snoozeAlarm(intent)
                stopAlarm()
                return START_NOT_STICKY
            }

            else -> {
                Log.e("AlarmService", "onStartCommand con action=null, abortando")
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundAlarm(intent: Intent?) {
        isRunning = true
        userInteracted = false
        createChannel()
        val noteId = intent?.getIntExtra(AlarmConstants.EXTRA_NOTE_ID, -1) ?: return
        autoStopJob?.cancel()

        autoStopJob = serviceScope.launch {
            delay(180_000)

            if (!userInteracted) {
                userInteracted = true
                handleMissedAlarm()
                stopAlarm()
            }
        }
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
        val stopPending = PendingIntent.getService(
            this,
            noteId * 10 + 0,
            Intent(this, AlarmService::class.java).apply {
                putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
                putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozePending = PendingIntent.getService(
            this,
            noteId * 10 + 1,
            Intent(this, AlarmService::class.java).apply {
                putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
                putExtra(AlarmConstants.EXTRA_NOTE_TITLE, alarmTitle)
                action = ACTION_SNOOZE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
            .setContentIntent(activityPending)
            .addAction(0, AlarmConstants.STOPALARM, stopPending)
            .addAction(0, AlarmConstants.POS, snoozePending)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startActivity(activityIntent)
        if (ringtone == null) {
            val uri = Settings.System.DEFAULT_ALARM_ALERT_URI
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        }


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
        autoStopJob?.cancel()
        autoStopJob = null

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

        val newTime = System.currentTimeMillis() + 10 * 60_000

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