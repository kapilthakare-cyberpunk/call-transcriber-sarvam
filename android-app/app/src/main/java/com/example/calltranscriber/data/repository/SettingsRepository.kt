package com.example.calltranscriber.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val sarvamApiKey = stringPreferencesKey("sarvam_api_key")
    private val telegramBotToken = stringPreferencesKey("telegram_bot_token")
    private val telegramChatId = stringPreferencesKey("telegram_chat_id")
    private val sttProvider = stringPreferencesKey("stt_provider")
    private val summaryProvider = stringPreferencesKey("summary_provider")
    private val summaryApiKey = stringPreferencesKey("summary_api_key")
    private val summaryBaseUrl = stringPreferencesKey("summary_base_url")
    private val summaryModel = stringPreferencesKey("summary_model")
    private val workKeywords = stringPreferencesKey("work_keywords")
    private val workContacts = stringPreferencesKey("work_contacts")

    val settings: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        mapOf(
            "sarvamApiKey" to prefs[sarvamApiKey].orEmpty(),
            "telegramBotToken" to prefs[telegramBotToken].orEmpty(),
            "telegramChatId" to prefs[telegramChatId].orEmpty(),
            "sttProvider" to prefs[sttProvider].orEmpty(),
            "summaryProvider" to prefs[summaryProvider].orEmpty(),
            "summaryApiKey" to prefs[summaryApiKey].orEmpty(),
            "summaryBaseUrl" to prefs[summaryBaseUrl].orEmpty(),
            "summaryModel" to prefs[summaryModel].orEmpty(),
            "workKeywords" to prefs[workKeywords].orEmpty(),
            "workContacts" to prefs[workContacts].orEmpty()
        )
    }

    suspend fun update(key: String, value: String) {
        val prefKey = when (key) {
            "sarvamApiKey" -> sarvamApiKey
            "telegramBotToken" -> telegramBotToken
            "telegramChatId" -> telegramChatId
            "sttProvider" -> sttProvider
            "summaryProvider" -> summaryProvider
            "summaryApiKey" -> summaryApiKey
            "summaryBaseUrl" -> summaryBaseUrl
            "summaryModel" -> summaryModel
            "workKeywords" -> workKeywords
            "workContacts" -> workContacts
            else -> null
        } ?: return
        context.dataStore.edit { it[prefKey] = value }
    }
}
