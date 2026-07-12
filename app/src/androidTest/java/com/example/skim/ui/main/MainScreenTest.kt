package com.example.skim.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
