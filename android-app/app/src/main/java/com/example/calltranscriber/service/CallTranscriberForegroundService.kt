package com.example.calltranscriber.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calltranscriber.R
import java.io.File

class CallTranscriberForegroundService : Service() {

    private val TAG = "CallTranscriberService"
    private lateinit var telephonyManager: TelephonyManager
    private val listener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.i(TAG, "Call ended, checking for new recording")
                enqueueLatestRecording()
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

    private fun enqueueLatestRecording() {
        val latest = latestRecordingPath() ?: return
        val data = Data.Builder()
            .putString(TranscriptionWorker.KEY_FILE_PATH, latest.absolutePath)
            .build()
        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(this).enqueue(request)
    }

    private fun latestRecordingPath(): File? {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC LIMIT 1"
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val name = it.getString(nameIdx)
                val dir = File(getExternalFilesDir(null), "recordings")
                return File(dir, name)
            }
        }
        return null
    }
}
