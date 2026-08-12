package com.example.calltranscriber.data.network.telegram.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendMessageBody(
    val chat_id: String,
    val text: String,
    @Json(name = "parse_mode") val parseMode: String = "Markdown",
    @Json(name = "disable_web_page_preview") val disableWebPagePreview: Boolean = true,
    @Json(name = "reply_markup") val replyMarkup: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramResponse(
    val ok: Boolean,
    val result: Map<String, Any>? = null
)
