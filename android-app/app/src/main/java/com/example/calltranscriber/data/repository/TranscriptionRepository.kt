package com.example.calltranscriber.data.repository

import com.example.calltranscriber.data.model.TranscriptionEvent

class TranscriptionRepository {
    suspend fun save(event: TranscriptionEvent) {
        // TODO: persist to Room or DataStore-backed history
    }

    suspend fun updateStatus(id: String, status: TranscriptionEvent.Status) {
        // TODO: update event status in storage
    }
}
