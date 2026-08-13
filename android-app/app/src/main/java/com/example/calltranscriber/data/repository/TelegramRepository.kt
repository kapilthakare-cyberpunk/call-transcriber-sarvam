package com.example.calltranscriber.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class TelegramRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = "https://api.telegram.org"
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun sendMessage(botToken: String, chatId: String, text: String): Boolean {
        val url = "$baseUrl/bot$botToken/sendMessage"
        val payload = mapOf("chat_id" to chatId, "text" to text)
        val body = moshi.adapter(Map::class.java).toJson(payload).toRequestBody(jsonType)
        val request = Request.Builder().url(url).post(body).build()
        return client.newCall(request).execute().use { it.isSuccessful }
    }
}
