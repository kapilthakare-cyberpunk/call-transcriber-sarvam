package com.example.calltranscriber.data.repository

import android.content.Context
import androidx.core.content.edit

class SettingsRepository(private val context: Context) {

    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var sarvamApiKey: String
        get() = sp.getString("sarvam_api_key", "").orEmpty()
        set(value) = sp.edit { putString("sarvam_api_key", value) }

    var summaryProvider: String
        get() = sp.getString("summary_provider", "local").orEmpty()
        set(value) = sp.edit { putString("summary_provider", value) }

    var summaryApiUrl: String
        get() = sp.getString("summary_api_url", "").orEmpty()
        set(value) = sp.edit { putString("summary_api_url", value) }

    var summaryApiKey: String
        get() = sp.getString("summary_api_key", "").orEmpty()
        set(value) = sp.edit { putString("summary_api_key", value) }

    var summaryModel: String
        get() = sp.getString("summary_model", "").orEmpty()
        set(value) = sp.edit { putString("summary_model", value) }

    var telegramBotToken: String
        get() = sp.getString("telegram_bot_token", "").orEmpty()
        set(value) = sp.edit { putString("telegram_bot_token", value) }

    var telegramChatId: String
        get() = sp.getString("telegram_chat_id", "").orEmpty()
        set(value) = sp.edit { putString("telegram_chat_id", value) }

    var ollamaUrl: String
        get() = sp.getString("ollama_url", "http://127.0.0.1:11434").orEmpty()
        set(value) = sp.edit { putString("ollama_url", value) }

    var llmModel: String
        get() = sp.getString("llm_model", "qwen2.5:1.5b").orEmpty()
        set(value) = sp.edit { putString("llm_model", value) }

    var workKeywords: String
        get() = sp.getString("work_keywords", "").orEmpty()
        set(value) = sp.edit { putString("work_keywords", value) }

    var personalKeywords: String
        get() = sp.getString("personal_keywords", "").orEmpty()
        set(value) = sp.edit { putString("personal_keywords", value) }

    var workContacts: String
        get() = sp.getString("work_contacts", "").orEmpty()
        set(value) = sp.edit { putString("work_contacts", value) }

    var personalContacts: String
        get() = sp.getString("personal_contacts", "").orEmpty()
        set(value) = sp.edit { putString("personal_contacts", value) }
}
