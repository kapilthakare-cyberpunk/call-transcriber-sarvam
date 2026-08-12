package com.example.calltranscriber.data.network.sarvam.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SarvamSpeechRequest(
    @param:Json(name = "language_code")
    val languageCode: String = "en-IN",
    @param:Json(name = "model")
    val model: String = "saaras:v2.5",
    @param:Json(name = "audio_format")
    val audioFormat: String = "wav",
    @param:Json(name = "sample_rate")
    val sampleRateHz: Int = 16000,
    @param:Json(name = "channels")
    val channels: Int = 1,
    @param:Json(name = "no_audio_normalize")
    val noAudioNormalize: Boolean = false,
    @param:Json(name = "enable_diarization")
    val enableDiarization: Boolean = true,
    @param:Json(name = "max_diarization_speakers")
    val maxDiarizationSpeakers: Int = 2,
    @param:Json(name = "chunk_id")
    val chunkId: String,
    @param:Json(name = "audio_content")
    val audioContent: String
)

@JsonClass(generateAdapter = true)
data class SarvamSpeechResponse(
    @param:Json(name = "request_id")
    val requestId: String? = null,
    @param:Json(name = "transcript")
    val transcript: String? = null,
    @param:Json(name = "language_code")
    val languageCode: String? = null,
    @param:Json(name = "diarized_transcript")
    val diarizedTranscript: List<DiarizedLine>? = null,
    @param:Json(name = "status")
    val status: String? = null,
    @param:Json(name = "error")
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class DiarizedLine(
    @param:Json(name = "speaker")
    val speaker: String? = null,
    @param:Json(name = "start_time")
    val startTimeMs: Long? = null,
    @param:Json(name = "end_time")
    val endTimeMs: Long? = null,
    @param:Json(name = "text")
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class SarvamStitchRequest(
    @param:Json(name = "language_code")
    val languageCode: String = "en-IN",
    @param:Json(name = "transcripts")
    val transcripts: List<SarvamSpeechResponse>,
    @param:Json(name = "merge_strategy")
    val mergeStrategy: String = "concatenate"
)

@JsonClass(generateAdapter = true)
data class SarvamStitchResponse(
    @param:Json(name = "request_id")
    val requestId: String? = null,
    @param:Json(name = "transcript")
    val transcript: String? = null,
    @param:Json(name = "language_code")
    val languageCode: String? = null,
    @param:Json(name = "status")
    val status: String? = null,
    @param:Json(name = "error")
    val error: String? = null
)
