package com.example.skim.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skim.data.repository.SkimRepository
import com.example.skim.model.Recording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface SkimUiState {
    data object Loading : SkimUiState
    data class Content(val recordings: List<Recording>, val refreshError: String? = null) : SkimUiState
    data class Error(val message: String) : SkimUiState
}

class SkimMainViewModel(
    private val repository: SkimRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val workScope = scope ?: viewModelScope
    private val mutableUiState = MutableStateFlow<SkimUiState>(SkimUiState.Loading)
    val uiState: StateFlow<SkimUiState> = mutableUiState.asStateFlow()

    init {
        workScope.launch {
            repository.recordings.collectLatest { recordings ->
                mutableUiState.value = SkimUiState.Content(recordings)
            }
        }
        sync()
    }

    fun refresh() {
        sync()
    }

    private fun sync() {
        workScope.launch {
            try {
                repository.refreshUntilIdle()
            } catch (throwable: Throwable) {
                val current = mutableUiState.value
                mutableUiState.value = when (current) {
                    is SkimUiState.Content -> current.copy(refreshError = "서버와 연결할 수 없습니다. 저장된 데이터를 표시합니다.")
                    else -> SkimUiState.Error("서버와 연결할 수 없습니다. 다시 시도해 주세요.")
                }
            }
        }
    }

    fun upload(title: String, filename: String, contentType: String, bytes: ByteArray) {
        workScope.launch {
            try {
                repository.upload(title, filename, contentType, bytes)
                repository.refreshUntilIdle()
            } catch (throwable: Throwable) {
                val current = mutableUiState.value
                mutableUiState.value = when (current) {
                    is SkimUiState.Content -> current.copy(refreshError = "업로드에 실패했습니다. 다시 시도해 주세요.")
                    else -> SkimUiState.Error("업로드에 실패했습니다. 다시 시도해 주세요.")
                }
            }
        }
    }
}
