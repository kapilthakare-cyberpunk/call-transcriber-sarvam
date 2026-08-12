package com.example.calltranscriber.worker

import android.content.Context
import com.example.calltranscriber.data.local.AppDatabase
import com.example.calltranscriber.data.repository.SettingsRepository
import com.example.calltranscriber.data.repository.TelegramRepository
import com.example.calltranscriber.data.repository.TranscriptionRepository

object SettingsRepositoryProvider {
    private var instance: SettingsRepository? = null

    fun init(repo: SettingsRepository) { instance = repo }
    fun get(context: Context): SettingsRepository = instance ?: throw IllegalStateException("SettingsRepositoryProvider not initialized")
}

object TranscriptionRepositoryProvider {
    private var instance: TranscriptionRepository? = null

    fun init(repo: TranscriptionRepository) { instance = repo }
    fun get(context: Context): TranscriptionRepository = instance ?: throw IllegalStateException("TranscriptionRepositoryProvider not initialized")
}

object TelegramRepositoryProvider {
    private var instance: TelegramRepository? = null

    fun init(repo: TelegramRepository) { instance = repo }
    fun get(context: Context): TelegramRepository = instance ?: throw IllegalStateException("TelegramRepositoryProvider not initialized")
}

object AppDatabaseProvider {
    private var instance: AppDatabase? = null

    fun init(db: AppDatabase) { instance = db }
    fun get(context: Context): AppDatabase = instance ?: throw IllegalStateException("AppDatabaseProvider not initialized")
}
