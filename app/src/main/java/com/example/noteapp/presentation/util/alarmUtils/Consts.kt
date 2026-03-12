package com.example.noteapp.presentation.util.alarmUtils

object AlarmConstants {
    const val EXTRA_NOTE_ID = "extra_note_id"
    const val EXTRA_NOTE_TITLE = "extra_note_title"
    const val DEFAULT = "ALARMA"
    const val STOPALARM = "PARAR"
    const val POS = "POSPONER"
    const val SOUND = "SONANDO"
    const val DEFAULTCHANNEL = "ALARMS"


    const val ACTION_STOP = "STOP_ALARM"
    const val ACTION_START = "START_ALARM"
    const val ACTION_SNOOZE = "SNOOZE_ALARM"
    const val CHANNEL_ID = "alarm_channel"
    const val NOTIFICATION_ID = 1001
    var isRunning = false
}
