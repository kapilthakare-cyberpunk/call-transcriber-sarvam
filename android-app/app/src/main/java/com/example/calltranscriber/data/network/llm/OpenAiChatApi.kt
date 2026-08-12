package com.example.calltranscriber.data.network.llm

import com.example.calltranscriber.data.network.ApiModule
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface OpenAiChatApi {
    @POST
    suspend fun chatCompletions(
        @Url completionUrl: String,
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest,
    ): ChatCompletionResponse
}

object OpenAiChatApiFactory {
    fun create(baseUrl: String = "https://openrouter.ai/"): OpenAiChatApi =
        ApiModule.create(baseUrl = baseUrl, factory = object : OpenAiChatApi {}) { this }
}
