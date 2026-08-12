package com.example.calltranscriber.data.network.telegram

import com.example.calltranscriber.data.network.ApiModule
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface TelegramApi {
    @POST("bot{token}/sendMessage")
    suspend fun sendMessage(
        @retrofit2.http.Path("token") token: String,
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @POST("bot{token}/editMessageText")
    suspend fun editMessageText(
        @retrofit2.http.Path("token") token: String,
        @Body request: EditMessageTextRequest
    ): SendMessageResponse
}

object TelegramApiFactory {
    fun create(): TelegramApi =
        ApiModule.create(baseUrl = "https://api.telegram.org/", factory = object : TelegramApi {}) { this }
}
