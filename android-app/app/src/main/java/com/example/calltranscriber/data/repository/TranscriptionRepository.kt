package com.example.calltranscriber.data.repository

import android.util.Log
import com.example.calltranscriber.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class TranscriptionRepository(private val settings: SettingsRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun transcribeSarvam(input: File, model: String = "saaras:v3", languageCode: String = "unknown"): String =
        withContext(Dispatchers.IO) {
            val apiKey = settings.sarvamApiKey
            if (apiKey.isBlank()) return@withContext "NO_SARVAM_KEY"

            val wav = File(input.parentFile, "transcribe_${input.nameWithoutExtension}.wav")
            val pb = ProcessBuilder("ffmpeg", "-y", "-i", input.absolutePath, "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", wav.absolutePath)
                .redirectErrorStream(true)
            val rc = pb.start().waitFor()
            if (rc != 0 || !wav.exists()) return@withContext "FFMPEG_FAIL"

            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", wav.name, wav.asRequestBody("audio/wav".toMediaType()))
                .addFormDataPart("model", model)
                .addFormDataPart("mode", "transcribe")
                .addFormDataPart("language_code", languageCode)
                .build()

            val request = Request.Builder()
                .url("https://api.sarvam.ai/speech-to-text")
                .addHeader("api-subscription-key", apiKey)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                wav.delete()
                if (!response.isSuccessful) return@withContext "Sarvam failed: ${response.code}"
                val text = JSONObject(response.body!!.string()).optString("transcript")
                text.ifBlank { "(empty transcript)" }
            }
        }

    suspend fun summarizeText(text: String): String = withContext(Dispatchers.IO) {
        val provider = settings.summaryProvider
        when {
            provider == "none" -> "Summary disabled"
            provider == "local" -> summarizeLocal(text)
            else -> summarizeRemote(text, provider)
        }
    }

    private suspend fun summarizeLocal(text: String): String = withContext(Dispatchers.IO) {
        try {
            val url = settings.ollamaUrl.trim().ifBlank { "http://127.0.0.1:11434" }
            val model = settings.llmModel.trim().ifBlank { "qwen2.5:1.5b" }
            val prompt = "Summarize this phone call in 3-5 bullets; list action-items. Call: \"$text\""
            val body = JSONObject()
                .put("model", model)
                .put("stream", false)
                .put("prompt", prompt)
                .toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url("$url/api/generate").post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "summary_failed"
                JSONObject(response.body!!.string()).optString("response").ifBlank { "summary_failed" }
            }
        } catch (e: Exception) {
            Log.e("Summary", "local failed", e)
            "summary_failed"
        }
    }

    private suspend fun summarizeRemote(text: String, provider: String): String = withContext(Dispatchers.IO) {
        val apiUrl = settings.summaryApiUrl.trim()
        val apiKey = settings.summaryApiKey.trim()
        val model = settings.summaryModel.trim().ifBlank { "auto" }
        if (apiUrl.isBlank() || apiKey.isBlank()) return@withContext "NO_API_KEY"

        val prompt = "Summarize this phone call in 3-5 bullets; list decisions/action-items. Write in the same language as the call.\n\n$text"
        val body = JSONObject()
            .put("model", model)
            .put("messages", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .put("max_tokens", 512)
            .put("temperature", 0.3)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext "summary_failed"
            val json = JSONObject(response.body!!.string())
            val content = json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
            content?.ifBlank { "summary_failed" } ?: "summary_failed"
        }
    }
}
