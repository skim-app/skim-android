package com.example.skim.data.repository

import com.example.skim.data.remote.dto.RemoteRecording
import com.example.skim.data.remote.dto.RemoteRecordingDetail
import com.example.skim.data.remote.dto.RemoteProcessingStatus
import com.example.skim.data.remote.dto.RemoteSummaryItem
import com.example.skim.data.remote.dto.RemoteSummarySource
import com.example.skim.data.remote.dto.RemoteTranscriptChunk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSkimRepositoryTest {
    @Test
    fun `refresh fetches the server result and replaces the offline cache`() = runTest {
        val cache = FakeCache()
        val repository = DefaultSkimRepository(FakeRemote(), cache)

        repository.refresh()

        val recording = repository.recordings.first().single()
        assertEquals("recording-1", recording.id)
        assertEquals("근거를 연결한다", recording.oneLineSummary)
        assertEquals(105000, recording.summaryItems.single().sources.single().startMs)
    }

    @Test
    fun `refresh until idle keeps polling while the server reports processing`() = runTest {
        val cache = FakeCache()
        val remote = FakeRemote(statuses = ArrayDeque(listOf("TRANSCRIBING", "COMPLETED")))
        val repository = DefaultSkimRepository(remote, cache, pollingIntervalMillis = 0)

        repository.refreshUntilIdle()

        assertEquals(2, remote.listCalls)
        assertEquals("COMPLETED", repository.recordings.first().single().status)
    }
}

private class FakeRemote(
    private val statuses: ArrayDeque<String> = ArrayDeque(listOf("COMPLETED")),
) : SkimRemoteDataSource {
    var listCalls = 0
    private var status = "COMPLETED"

    override suspend fun listRecordings(): List<RemoteRecording> {
        listCalls++
        status = statuses.removeFirstOrNull() ?: "COMPLETED"
        return listOf(RemoteRecording("recording-1", "앱 아이디어 메모", status, 332000, "2026-07-11T00:42:00Z"))
    }
    override suspend fun recordingDetail(id: String) = RemoteRecordingDetail(
        id, "앱 아이디어 메모", status, 332000, "2026-07-11T00:42:00Z",
        listOf(RemoteTranscriptChunk("chunk-1", 105000, 150000, "원문", 0)),
        listOf(RemoteSummaryItem("summary-1", "차별점", "근거를 연결한다", listOf(RemoteSummarySource("chunk-1", 105000, 150000, "01:45–02:30")))),
    )
    override suspend fun processingStatus(id: String) = RemoteProcessingStatus(status, null, "2026-07-11T00:42:00Z")
    override suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray) = Unit
}

private class FakeCache : SkimLocalDataSource {
    private val values = MutableStateFlow(emptyList<com.example.skim.model.Recording>())
    override fun observe(): Flow<List<com.example.skim.model.Recording>> = values
    override suspend fun replace(recordings: List<com.example.skim.model.Recording>) { values.value = recordings }
}
