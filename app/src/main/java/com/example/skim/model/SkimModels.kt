package com.example.skim.model

data class Recording(
    val id: String,
    val title: String,
    val duration: String,
    val createdAt: String,
    val status: String,
    val oneLineSummary: String,
    val audioAssetName: String? = null,
    val audioAvailable: Boolean = false,
    val summaryItems: List<SummaryItem>,
    val transcriptChunks: List<TranscriptChunk>,
)

data class SummaryItem(
    val id: String,
    val category: String,
    val text: String,
    val sources: List<SummarySource>,
)

data class SummarySource(
    val chunkId: String,
    val label: String,
    val startMs: Long = 0,
    val endMs: Long = 0,
)

data class TranscriptChunk(
    val id: String,
    val timeRange: String,
    val text: String,
    val startMs: Long = 0,
    val endMs: Long = 0,
)
