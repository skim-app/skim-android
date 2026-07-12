package com.example.skim.data.remote

import com.example.skim.data.remote.dto.RemoteRecordingDetail
import com.example.skim.data.remote.dto.RemoteSummaryItem
import com.example.skim.data.remote.dto.RemoteSummarySource
import com.example.skim.data.remote.dto.RemoteTranscriptChunk
import com.example.skim.data.remote.mapper.toDomain
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SkimRemoteMapperTest {
    @Test
    fun `maps millisecond source timestamps to domain labels without parsing display text`() {
        val recording = RemoteRecordingFixture.recording(
            chunks = listOf(RemoteTranscriptChunk("chunk-1", 105000, 150000, "원문", 0)),
            items = listOf(RemoteSummaryItem(
                id = "summary-1",
                category = "차별점",
                text = "근거를 연결한다",
                sources = listOf(RemoteSummarySource("chunk-1", 105000, 150000, "01:45–02:30")),
            )),
        ).toDomain()

        assertEquals(105000, recording.summaryItems.single().sources.single().startMs)
        assertEquals("01:45–02:30", recording.summaryItems.single().sources.single().label)
        assertEquals(150000, recording.transcriptChunks.single().endMs)
    }
}

private object RemoteRecordingFixture {
    fun recording(chunks: List<RemoteTranscriptChunk>, items: List<RemoteSummaryItem>) = RemoteRecordingDetail(
        id = "recording-1",
        title = "앱 아이디어 메모",
        status = "COMPLETED",
        durationMs = 332000,
        createdAt = "2026-07-11T00:42:00Z",
        chunks = chunks,
        items = items,
    )
}
