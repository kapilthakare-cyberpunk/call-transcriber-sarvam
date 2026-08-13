package com.example.calltranscriber.data.model

data class TranscriptionEvent(
    val id: String,
    val callTimestampMillis: Long,
    val filePath: String,
    val status: Status,
    val provider: String?,
    val transcript: String?,
    val summary: String?,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    enum class Status { PENDING, PROCESSING, APPROVAL_NEEDED, COMPLETED, FAILED }
}
