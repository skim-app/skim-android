package com.example.skim.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.example.skim.model.Recording
import com.example.skim.model.SummaryItem
import com.example.skim.model.SummarySource
import com.example.skim.model.TranscriptChunk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Smoke test for the repository-provided recording list. */
class MainScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreen(
        uiState = SkimUiState.Content(
          recordings = listOf(
            com.example.skim.model.Recording(
              id = "recording-1",
              title = "서버에서 받은 녹음",
              duration = "00:09",
              createdAt = "방금",
              status = "COMPLETED",
              oneLineSummary = "서버 데이터",
              summaryItems = emptyList(),
              transcriptChunks = emptyList(),
            ),
            Recording(
              id = "without-audio",
              title = "오디오 없는 녹음",
              duration = "00:06",
              createdAt = "방금",
              status = "COMPLETED",
              oneLineSummary = "오디오 없이도 근거는 확인할 수 있습니다.",
              audioAvailable = false,
              summaryItems = listOf(SummaryItem("summary-1", "핵심", "근거", listOf(SummarySource("chunk-1", "00:00–00:06")))),
              transcriptChunks = listOf(TranscriptChunk("chunk-1", "00:00–00:06", "선택된 원문")),
            ),
          ),
        ),
        onRefresh = {},
      )
    }
  }

  @Test
  fun repositoryRecordings_areDisplayed() {
    composeTestRule.onNodeWithText("서버에서 받은 녹음").assertExists()
  }

  @Test
  fun addDestination_exposesTheUploadEntryPoint() {
    composeTestRule.onNodeWithText("추가").performClick()

    composeTestRule.onNodeWithText("음성 파일을 선택해 업로드하세요").assertExists()
  }

  @Test
  fun unavailableAudio_keepsTheSelectedTranscriptVisible() {
    composeTestRule.onNodeWithText("오디오 없는 녹음").performClick()
    composeTestRule.onNodeWithContentDescription("근거 듣기 00:00–00:06").performClick()

    composeTestRule.onNodeWithText("오디오가 없어 재생할 수 없습니다.").assertExists()
    composeTestRule.onNodeWithText("선택한 근거").assertExists()
  }
}
