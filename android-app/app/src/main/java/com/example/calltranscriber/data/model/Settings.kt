package com.example.calltranscriber.data.model

data class Settings(
    val sarvamApiKey: String = "",
    val summaryProvider: String = "local",
    val summaryApiUrl: String = "",
    val summaryApiKey: String = "",
    val summaryModel: String = "",
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val ollamaUrl: String = "http://127.0.0.1:11434",
    val llmModel: String = "qwen2.5:1.5b",
    val workKeywords: String = "",
    val personalKeywords: String = "",
    val workContacts: String = "",
    val personalContacts: String = ""
)
