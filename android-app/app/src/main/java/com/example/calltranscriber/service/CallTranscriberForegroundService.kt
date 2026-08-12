package com.example.calltranscriber.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calltranscriber.R

class CallEventService : Service() {

    private val TAG = "CallEventService"
    private lateinit var telephonyManager: TelephonyManager
    private val listener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.i(TAG, "Call ended, checking for new recording")
                enqueueTranscriptionWorker()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())
        telephonyManager = getSystemService(TelephonyManager::class.java)
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
    }

    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "call_transcriber",
                "Call Transcriber",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "call_transcriber")
            .setContentTitle("Call Transcriber")
            .setContentText("Watching for new recordings")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    private fun enqueueTranscriptionWorker() {
        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>().build()
        WorkManager.getInstance(this).enqueue(request)
    }
}
