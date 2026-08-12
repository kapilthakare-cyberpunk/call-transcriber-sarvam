package com.example.calltranscriber.data.network.llm.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxTokens: Int? = null,
    val stream: Boolean = false,
    @Json(name = "stop")
    val stopSequences: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<ChatChoice>? = null,
    val usage: Usage? = null
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    val index: Int? = null,
    val message: ChatMessage? = null,
    @Json(name = "finish_reason")
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens")
    val promptTokens: Int? = null,
    @Json(name = "completion_tokens")
    val completionTokens: Int? = null,
    @Json(name = "total_tokens")
    val totalTokens: Int? = null
)
