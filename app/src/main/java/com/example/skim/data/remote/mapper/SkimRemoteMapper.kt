package com.example.skim.data.remote.mapper

import com.example.skim.data.remote.dto.RemoteRecordingDetail
import com.example.skim.model.Recording
import com.example.skim.model.SummaryItem
import com.example.skim.model.SummarySource
import com.example.skim.model.TranscriptChunk

fun RemoteRecordingDetail.toDomain() = Recording(
    id = id,
    title = title,
    duration = durationMs?.let(::formatDuration) ?: "--:--",
    createdAt = createdAt,
    status = status,
    audioAvailable = audioAvailable,
    oneLineSummary = items.firstOrNull()?.text ?: "아직 요약 결과가 없습니다.",
    summaryItems = items.map { item ->
        SummaryItem(item.id, item.category, item.text, item.sources.map { source ->
            SummarySource(source.transcriptChunkId, source.label, source.startMs, source.endMs)
        })
    },
    transcriptChunks = chunks.sortedBy { it.sequence }.map { chunk ->
        TranscriptChunk(chunk.id, "${formatDuration(chunk.startMs)}–${formatDuration(chunk.endMs)}", chunk.text, chunk.startMs, chunk.endMs)
    },
)

private fun formatDuration(millis: Long): String = "%02d:%02d".format(millis / 60000, (millis / 1000) % 60)
