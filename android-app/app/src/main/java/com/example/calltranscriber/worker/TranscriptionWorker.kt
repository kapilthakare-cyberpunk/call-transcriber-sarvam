package com.example.calltranscriber.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calltranscriber.data.repository.SettingsRepository
import com.example.calltranscriber.data.repository.TelegramRepository
import com.example.calltranscriber.data.repository.TranscriptionRepository
import java.io.File

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val file = File(filePath)
        if (!file.exists()) return Result.success()

        val settingsRepo = SettingsRepository(applicationContext)
        val transcriptionRepo = TranscriptionRepository(settingsRepo)
        val telegramRepo = TelegramRepository(settingsRepo)

        val transcript = transcriptionRepo.transcribeSarvam(file)
        val summary = transcriptionRepo.summarizeText(transcript)
        val verdict = if (transcript.contains("NO_SARVAM_KEY") || transcript.contains("FFMPEG_FAIL") || transcript.contains("Sarvam failed")) "UNKNOWN" else "UNKNOWN"

        val message = buildString {
            appendLine("Call: ${file.nameWithoutExtension}")
            appendLine("Transcript: $transcript")
            appendLine("Summary: $summary")
            appendLine("Verdict: $verdict")
        }
        telegramRepo.sendMessage(message)

        Result.success()
    } catch (e: Exception) {
        Log.e("TranscriptionWorker", "Pipeline failed", e)
        Result.retry()
    }

    companion object {
        const val KEY_FILE_PATH = "file_path"
    }
}
