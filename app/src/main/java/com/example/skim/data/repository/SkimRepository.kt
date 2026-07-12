package com.example.skim.data.repository

import com.example.skim.data.remote.mapper.toDomain
import com.example.skim.model.Recording
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay

interface SkimRepository {
    val recordings: Flow<List<Recording>>
    suspend fun refresh()
    suspend fun refreshUntilIdle()
    suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray)
}

class DefaultSkimRepository(
    private val remote: SkimRemoteDataSource,
    private val local: SkimLocalDataSource,
    private val pollingIntervalMillis: Long = 1_000,
) : SkimRepository {
    override val recordings: Flow<List<Recording>> = local.observe()

    override suspend fun refresh() {
        refreshAndHasActiveProcessing()
    }

    override suspend fun refreshUntilIdle() {
        while (refreshAndHasActiveProcessing()) {
            delay(pollingIntervalMillis)
        }
    }

    private suspend fun refreshAndHasActiveProcessing(): Boolean {
        val serverRecordings = remote.listRecordings()
        local.replace(serverRecordings.map { remote.recordingDetail(it.id).toDomain() })
        return serverRecordings
            .filter { it.status in activeStatuses }
            .any { remote.processingStatus(it.id).status in activeStatuses }
    }

    override suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray) {
        remote.upload(title, filename, contentType, bytes)
    }

    private companion object {
        val activeStatuses = setOf("TRANSCRIBING", "SUMMARIZING")
    }
}
