package com.example.calltranscriber.data.network.sarvam

import com.example.calltranscriber.data.network.ApiModule
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SarvamSpeechApi {
    @POST("speech-to-text")
    suspend fun transcribeChunk(
        @Header("api-subscription-key") apiKey: String,
        @Body request: SarvamSpeechRequest,
    ): SarvamSpeechResponse

    @POST("speech-to-text/stitch")
    suspend fun stitchTranscripts(
        @Header("api-subscription-key") apiKey: String,
        @Body request: SarvamStitchRequest,
    ): SarvamStitchResponse
}

object SarvamSpeechApiFactory {
    fun create(baseUrl: String = "https://api.sarvam.ai/"): SarvamSpeechApi =
        ApiModule.create(
            baseUrl = baseUrl,
            factory = object : SarvamSpeechApi {}
        ) { this }
}
