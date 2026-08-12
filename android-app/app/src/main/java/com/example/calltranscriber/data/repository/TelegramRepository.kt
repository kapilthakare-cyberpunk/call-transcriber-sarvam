package com.example.calltranscriber.data.repository

import android.util.Log
import com.example.calltranscriber.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramRepository(private val settings: SettingsRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(text: String, replyMarkup: String? = null): Boolean {
        val token = settings.telegramBotToken
        val chatId = settings.telegramChatId
        if (token.isBlank() || chatId.isBlank()) return false
        val body = JSONObject()
            .put("chat_id", chatId)
            .put("text", text)
            .put("parse_mode", "Markdown")
            .put("disable_web_page_preview", true)
            .apply { replyMarkup?.let { put("reply_markup", JSONObject(it)) } }
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$token/sendMessage")
            .post(body)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) true
                else {
                    Log.e("Telegram", "send failed: ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("Telegram", "send error", e)
            false
        }
    }
}
