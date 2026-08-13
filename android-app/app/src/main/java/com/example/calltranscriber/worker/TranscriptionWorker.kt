package com.example.calltranscriber.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calltranscriber.data.repository.SettingsRepository

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString("file_path") ?: return Result.failure()
        val settingsRepo = SettingsRepository(applicationContext)
        val settings = settingsRepo.settings

        // Placeholder: provider dispatch, transcription, storage, and Telegram notification
        return Result.success()
    }
}
