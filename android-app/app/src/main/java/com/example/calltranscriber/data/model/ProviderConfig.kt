package com.example.calltranscriber.data.model

data class ProviderConfig(
    val sttProvider: SttProvider = SttProvider.SARVAM,
    val sarvamApiKey: String = "",
    val summaryProvider: SummaryProvider = SummaryProvider.NONE,
    val summaryApiKey: String = "",
    val summaryBaseUrl: String = "",
    val summaryModel: String = ""
) {
    enum class SttProvider { SARVAM, LOCAL_WHISPER, NONE }
    enum class SummaryProvider { NONE, OPENROUTER, GROQ, CEREBRAS, MISTRAL, OPENCODE_ZEN, OLLAMA }
}
