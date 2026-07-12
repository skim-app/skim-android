package com.example.skim.data

import com.example.skim.model.Recording
import com.example.skim.model.SummaryItem
import com.example.skim.model.SummarySource
import com.example.skim.model.TranscriptChunk

object FakeSkimRepository {
    val recordings = listOf(
        Recording(
            id = "rec-idea-memo",
            title = "앱 아이디어 메모",
            duration = "05:32",
            createdAt = "오늘 오전 9:42",
            status = "요약 완료",
            oneLineSummary = "Skim MVP의 핵심은 AI 요약을 원문 timestamp로 검증하는 경험이다.",
            transcriptChunks = listOf(
                TranscriptChunk(
                    id = "c1",
                    timeRange = "00:40-01:20",
                    text = "첫 MVP는 개인 메모 중심으로 가는 게 맞다. 회의록이나 강의까지 넓히면 화면과 처리 범위가 너무 커진다.",
                ),
                TranscriptChunk(
                    id = "c2",
                    timeRange = "01:45-02:30",
                    text = "핵심은 AI 요약 자체보다 원문 검증 경험이다. 요약 항목을 눌렀을 때 바로 근거 구간으로 이동해야 한다.",
                ),
                TranscriptChunk(
                    id = "c3",
                    timeRange = "02:40-03:10",
                    text = "요약 항목마다 timestamp를 붙이고, 사용자가 의심되는 항목을 탭하면 transcript chunk가 하이라이트되게 만든다.",
                ),
                TranscriptChunk(
                    id = "c4",
                    timeRange = "04:10-04:45",
                    text = "STT와 LLM은 지금 당장 붙이지 말고 더미 데이터로 UX를 먼저 검증한다. 나중에 온디바이스 모델로 교체하면 된다.",
                ),
            ),
            summaryItems = listOf(
                SummaryItem(
                    id = "s1",
                    category = "MVP 범위",
                    text = "첫 버전은 개인 음성 메모 하나의 핵심 흐름에 집중한다.",
                    sources = listOf(SummarySource("c1", "00:40-01:20")),
                ),
                SummaryItem(
                    id = "s2",
                    category = "차별점",
                    text = "Skim은 AI 요약을 원문 timestamp와 연결해 사용자가 근거를 확인할 수 있게 한다.",
                    sources = listOf(
                        SummarySource("c2", "01:45-02:30"),
                        SummarySource("c3", "02:40-03:10"),
                    ),
                ),
                SummaryItem(
                    id = "s3",
                    category = "구현 전략",
                    text = "MVP에서는 STT/LLM을 더미 데이터로 대체하고 순수 Android UX를 먼저 완성한다.",
                    sources = listOf(SummarySource("c4", "04:10-04:45")),
                ),
            ),
        ),
        Recording(
            id = "rec-retrospective",
            title = "작업 회고",
            duration = "03:18",
            createdAt = "어제 오후 11:10",
            status = "요약 완료",
            oneLineSummary = "구현 속도를 높이려면 기능을 줄이고 시연 가능한 vertical slice를 먼저 만든다.",
            transcriptChunks = listOf(
                TranscriptChunk(
                    id = "r2c1",
                    timeRange = "00:15-00:50",
                    text = "오늘은 기능을 넓히기보다 앱이 켜지고 핵심 화면이 이어지는지부터 확인해야 한다.",
                ),
                TranscriptChunk(
                    id = "r2c2",
                    timeRange = "01:05-01:44",
                    text = "빌드가 자주 깨지면 바로 범위를 줄이고 Compose 단일 화면에서 먼저 검증한다.",
                ),
            ),
            summaryItems = listOf(
                SummaryItem(
                    id = "r2s1",
                    category = "실행 원칙",
                    text = "시연 가능한 vertical slice를 먼저 만든다.",
                    sources = listOf(SummarySource("r2c1", "00:15-00:50")),
                ),
                SummaryItem(
                    id = "r2s2",
                    category = "리스크",
                    text = "빌드가 깨지면 구조보다 범위를 줄인다.",
                    sources = listOf(SummarySource("r2c2", "01:05-01:44")),
                ),
            ),
        ),
        Recording(
            id = "rec-mvp-demo-audio",
            title = "MVP 테스트 음성",
            duration = "00:09",
            createdAt = "방금 추가됨",
            status = "내장 오디오",
            oneLineSummary = "앱에 포함된 더미 녹음 파일로, 실제 오디오 asset 연결 상태를 확인합니다.",
            audioAssetName = "skim-demo-voice-note.m4a",
            transcriptChunks = listOf(
                TranscriptChunk(
                    id = "demo-c1",
                    timeRange = "00:00-00:09",
                    text = "안녕하세요. 이것은 Skim MVP 테스트용 더미 음성 메모입니다. 요약의 timestamp를 누르면 해당 원문 근거를 확인할 수 있습니다.",
                ),
            ),
            summaryItems = listOf(
                SummaryItem(
                    id = "demo-s1",
                    category = "테스트 오디오",
                    text = "이 화면은 APK에 포함된 더미 녹음 asset을 보여 줍니다.",
                    sources = listOf(SummarySource("demo-c1", "00:00-00:09")),
                ),
            ),
        ),
    )
}
