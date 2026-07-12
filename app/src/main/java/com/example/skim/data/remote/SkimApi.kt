package com.example.skim.data.remote

import com.example.skim.data.remote.dto.RemoteRecording
import com.example.skim.data.remote.dto.RemoteProcessingStatus
import com.example.skim.data.remote.dto.RemoteSummaryResponse
import com.example.skim.data.remote.dto.RemoteTranscriptResponse
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody

data class CreateRecordingRequest(val title: String)

interface SkimApi {
    @POST("v1/recordings")
    suspend fun createRecording(@Body request: CreateRecordingRequest): RemoteRecording

    @Multipart
    @POST("v1/recordings/{recordingId}/audio")
    suspend fun uploadAudio(@Path("recordingId") recordingId: String, @Part file: MultipartBody.Part): RemoteRecording

    @POST("v1/recordings/{recordingId}/process")
    suspend fun process(@Path("recordingId") recordingId: String): RemoteRecording

    @GET("v1/recordings")
    suspend fun recordings(): List<RemoteRecording>

    @GET("v1/recordings/{recordingId}")
    suspend fun recording(@Path("recordingId") recordingId: String): RemoteRecording

    @GET("v1/recordings/{recordingId}/processing-status")
    suspend fun processingStatus(@Path("recordingId") recordingId: String): RemoteProcessingStatus

    @GET("v1/recordings/{recordingId}/transcript")
    suspend fun transcript(@Path("recordingId") recordingId: String): RemoteTranscriptResponse

    @GET("v1/recordings/{recordingId}/summary")
    suspend fun summary(@Path("recordingId") recordingId: String): RemoteSummaryResponse
}
