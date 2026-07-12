# Skim UX Case Study — AI 요약을 검증 가능한 경험으로 바꾸기

## 한 문장 소개

**Skim은 AI 음성 요약의 각 항목을 원문 timestamp와 연결해, 사용자가 결과의 근거를 즉시 확인할 수 있게 만든 Kotlin/Jetpack Compose Android MVP입니다.**

## 문제

음성 메모를 AI가 요약해도 사용자는 다음을 확인하기 어렵습니다.

- 이 문장이 실제로 원문에 있었는가?
- 중요한 문맥이 빠지거나 왜곡되지 않았는가?
- 전체 녹음을 다시 듣지 않고 필요한 근거만 확인할 수 있는가?

따라서 첫 MVP의 목표를 “STT/LLM 품질 경쟁”이 아니라 **요약 결과와 원문 근거 사이의 이동 비용을 줄이는 UX 검증**으로 제한했습니다.

## 핵심 사용자 흐름

```text
녹음 목록
  → 녹음 상세의 요약 탭
  → summary item 아래 timestamp chip 선택
  → 원문 탭으로 전환
  → 연결된 transcript chunk 자동 스크롤·시각적 강조
```

`SummarySource`는 `chunkId`와 표시 timestamp를 가진 모델이며, summary item이 하나 이상의 원문 chunk를 참조할 수 있도록 설계했습니다. UI는 timestamp를 누르면 해당 `chunkId`를 selected state로 저장하고, 원문 `LazyColumn`을 그 위치로 이동시킨 뒤 `선택한 근거` badge를 노출합니다.

## 구현 근거

| 항목 | 구현/검증 근거 |
| --- | --- |
| summary ↔ transcript mapping | `model/SkimModels.kt`의 `SummarySource`, `data/FakeSkimRepository.kt`의 seed mapping |
| timestamp 기반 탭 전환 | `ui/main/MainScreen.kt`의 `onSourceClick`에서 selected tab과 highlighted chunk 갱신 |
| 원문 자동 이동·강조 | `TranscriptTab`의 `LaunchedEffect` + `animateScrollToItem`, `TranscriptChunkCard`의 badge/color state |
| demo data 경계 | 3개 recording과 transcript/summary는 모두 local Kotlin seed data |
| build/test | `./gradlew :app:testDebugUnitTest :app:assembleDebug` 성공 |
| runtime path | `emulator-5554`에서 목록 → 요약 → `01:45–02:30` 탭 → 원문 강조 → 목록 복귀를 UI hierarchy와 screenshot으로 검증 |

## 실행 화면

| 1. 녹음 목록 | 2. 요약 및 timestamp | 3. 원문 근거 강조 |
| --- | --- | --- |
| ![목록](screenshots/01-recording-list.png) | ![요약](screenshots/02-summary-detail.png) | ![원문 강조](screenshots/03-transcript-highlight.png) |

## 구현됨 / 설계됨 / 후순위

| 구분 | 내용 |
| --- | --- |
| **구현됨** | Compose 기반 목록·상세 UI, summary cards, timestamp chip, 원문 탭 전환, source chunk automatic scroll/highlight, local seed model, debug APK |
| **설계 방향** | 동일 모델 계약을 유지한 STT/LLM 결과 주입, summary source 신뢰도와 복수 근거 지원 |
| **후순위** | 실제 녹음·Media3 seek/play·STT/LLM·Room/WorkManager·로그인/동기화·공유/export |

## 기술적 판단

1. **AI 파이프라인보다 검증 UX를 먼저 고정했습니다.** 데이터가 가짜여도 timestamp를 눌러 원문으로 가는 상호작용이 유효한지, 화면 상태가 충분히 명확한지 먼저 확인할 수 있습니다.
2. **모델·seed data·UI를 분리했습니다.** 향후 local repository를 Room 또는 remote/STT/LLM pipeline으로 바꿔도 UI가 `Recording → SummaryItem → SummarySource → TranscriptChunk` 계약을 유지할 수 있습니다.
3. **과장하지 않았습니다.** 현재 앱은 실제 음성 녹음·재생·AI 요약 생성 앱이 아니라, 그 결과를 검증하는 핵심 Android UX 프로토타입입니다.

## 포트폴리오에서 전달할 메시지

> AI 기능을 추가하는 데서 멈추지 않고, 사용자가 결과의 근거를 확인할 수 있도록 데이터 관계와 UI 흐름을 설계·구현·실기기 환경에서 검증했습니다.
