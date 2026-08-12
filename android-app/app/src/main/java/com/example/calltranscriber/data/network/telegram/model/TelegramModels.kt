package com.example.calltranscriber.data.network.telegram.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "chat_id")
    val chatId: String,
    val text: String,
    @Json(name = "parse_mode")
    val parseMode: String? = null,
    @Json(name = "disable_web_page_preview")
    val disableWebPagePreview: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class EditMessageTextRequest(
    @Json(name = "chat_id")
    val chatId: String,
    @Json(name = "message_id")
    val messageId: Long,
    val text: String,
    @Json(name = "parse_mode")
    val parseMode: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageResponse(
    @Json(name = "ok")
    val ok: Boolean? = null,
    @Json(name = "result")
    val result: TelegramMessage? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "error_code")
    val errorCode: Int? = null
)

@JsonClass(generateAdapter = true)
data class TelegramMessage(
    @Json(name = "message_id")
    val messageId: Long,
    val text: String? = null
)
