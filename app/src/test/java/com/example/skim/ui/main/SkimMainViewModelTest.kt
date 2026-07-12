package com.example.skim.ui.main

import com.example.skim.data.repository.SkimRepository
import com.example.skim.model.Recording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SkimMainViewModelTest {
    @Test
    fun `refreshes server data and exposes cached recordings`() = runTest {
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSkimRepository()
            val viewModel = SkimMainViewModel(repository, scope)

            advanceUntilIdle()

            assertEquals(1, repository.refreshCalls)
            assertEquals("서버 녹음", (viewModel.uiState.value as SkimUiState.Content).recordings.single().title)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `shows an empty content state when server has no recordings`() = runTest {
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeSkimRepository(refreshResult = emptyList())
            val viewModel = SkimMainViewModel(repository, scope)

            advanceUntilIdle()

            assertEquals(SkimUiState.Content(emptyList()), viewModel.uiState.value)
        } finally {
            scope.cancel()
        }
    }
}

private class FakeSkimRepository(
    private val refreshResult: List<Recording> = listOf(
        Recording("server-1", "서버 녹음", "00:09", "방금", "COMPLETED", "서버 데이터", summaryItems = emptyList(), transcriptChunks = emptyList()),
    ),
) : SkimRepository {
    private val values = MutableStateFlow(emptyList<Recording>())
    override val recordings: Flow<List<Recording>> = values
    var refreshCalls = 0

    override suspend fun refresh() {
        refreshCalls++
        values.value = refreshResult
    }
    override suspend fun refreshUntilIdle() = refresh()
    override suspend fun upload(title: String, filename: String, contentType: String, bytes: ByteArray) = Unit
}
