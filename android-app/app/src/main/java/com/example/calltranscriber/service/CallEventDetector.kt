package com.example.calltranscriber.service

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class CallEventDetector(private val context: Context) {
    private val telephonyManager: TelephonyManager =
        ContextCompat.getSystemService(context, TelephonyManager::class.java)
            ?: throw IllegalStateException("TelephonyManager unavailable")

    private var listener: PhoneStateListener? = null
    private var isRunning = false

    fun start(): Flow<PendingRecording> = callbackFlow {
        val callState = CallStateTracker()
        listener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                super.onCallStateChanged(state, incomingNumber)
                val previous = callState.current
                val next = CallState.fromTelephony(state)
                if (previous == CallState.OffHook && next == CallState.Idle) {
                    Timber.i("Call ended; awaiting new recording")
                }
                callState.update(next)
            }
        }

        isRunning = true
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)

        val observer = RecordingObserver(context.contentResolver) { uri ->
            if (!isRunning) return@RecordingObserver
            val resolved = resolve(uri) ?: return@RecordingObserver
            if (!resolved.lowercase().endsWith(".m4a")) return@RecordingObserver
            if (!callState.recentlyEnded()) return@RecordingObserver
            trySend(PendingRecording(Uri.parse(resolved), resolved))
        }

        awaitClose {
            stop()
        }
    }

    fun stop() {
        try {
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
        } catch (t: Throwable) {
            Timber.w(t, "Telephony listen removal failed")
        }
        listener = null
        isRunning = false
    }

    private suspend fun resolve(uri: Uri): String? = with(kotlinx.coroutines.Dispatchers.IO) {
        when {
            uri.scheme == ContentResolver.SCHEME_FILE -> uri.path
            uri.scheme == ContentResolver.SCHEME_CONTENT && uri.host?.equals("media", true) == true -> {
                uri.toString()
            }
            else -> uri.path
        }
    }
}
