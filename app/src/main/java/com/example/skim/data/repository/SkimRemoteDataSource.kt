package com.example.skim.data.repository

import com.example.skim.data.remote.SkimApi
import com.example.skim.data.remote.CreateRecordingRequest
import com.example.skim.data.remote.dto.RemoteRecording
import com.example.skim.data.remote.dto.RemoteRecordingDetail
import com.example.skim.data.remote.dto.RemoteProcessingStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType

interface SkimRemoteDataSource {
    suspend fun listRecordings(): List<RemoteRecording>
    suspend fun recordingDetail(id: String): RemoteRecordingDetail
    suspend fun processingStatus(id: String): RemoteProcessingStatus
    suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray)
}

class RetrofitSkimRemoteDataSource(private val api: SkimApi) : SkimRemoteDataSource {
    override suspend fun listRecordings(): List<RemoteRecording> = api.recordings()

    override suspend fun recordingDetail(id: String): RemoteRecordingDetail {
        val recording = api.recording(id)
        val transcript = api.transcript(id)
        val summary = api.summary(id)
        return RemoteRecordingDetail(
            recording.id,
            recording.title,
            recording.status,
            recording.durationMs,
            recording.createdAt,
            transcript.chunks,
            summary.items,
            recording.audioAvailable,
        )
    }

    override suspend fun processingStatus(id: String): RemoteProcessingStatus = api.processingStatus(id)

    override suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray) {
        val recording = api.createRecording(CreateRecordingRequest(title))
        val body = bytes.toRequestBody(contentType.toMediaType())
        api.uploadAudio(recording.id, MultipartBody.Part.createFormData("file", filename, body))
        api.process(recording.id)
    }
}
