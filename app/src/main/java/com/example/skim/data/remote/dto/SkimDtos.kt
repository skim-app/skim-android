package com.example.skim.data.remote.dto

data class RemoteRecording(
    val id: String,
    val title: String,
    val status: String,
    val durationMs: Long?,
    val createdAt: String,
    val audioAvailable: Boolean = false,
)

data class RemoteRecordingDetail(
    val id: String,
    val title: String,
    val status: String,
    val durationMs: Long?,
    val createdAt: String,
    val chunks: List<RemoteTranscriptChunk>,
    val items: List<RemoteSummaryItem>,
    val audioAvailable: Boolean = false,
)

data class RemoteProcessingStatus(val status: String, val failureReason: String?, val updatedAt: String)
data class RemoteTranscriptResponse(val recordingId: String, val chunks: List<RemoteTranscriptChunk>)
data class RemoteTranscriptChunk(val id: String, val startMs: Long, val endMs: Long, val text: String, val sequence: Int)
data class RemoteSummaryResponse(val recordingId: String, val items: List<RemoteSummaryItem>)
data class RemoteSummaryItem(val id: String, val category: String, val text: String, val sources: List<RemoteSummarySource>)
data class RemoteSummarySource(val transcriptChunkId: String, val startMs: Long, val endMs: Long, val label: String)
