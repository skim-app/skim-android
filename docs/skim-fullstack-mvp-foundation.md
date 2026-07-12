# Skim 풀스택 MVP — 제품·구현·포트폴리오 기준 문서

> **문서 목적:** Skim을 단순 Compose UX 프로토타입에서, 실제로 시연·검증 가능하고 포트폴리오에 사용할 수 있는 Android 풀스택 MVP로 완성하기 위한 기준 문서다. 이후 기능 구현, PRD 보완, README 작성, 테스트, 데모 제작, 포트폴리오 작성은 이 문서를 우선 기준으로 삼는다.
>
> **문서 상태:** 2026-07-11 대화 및 현재 코드베이스 점검을 반영한 구현 기준. 기존 `portfolio-case-study.md`는 현재 프로토타입의 증거 기록이며, 이 문서는 앞으로 만들 제품의 source of truth다.

---

## 1. 제품 한 문장

**Skim은 음성 메모의 요약을 그대로 믿게 하지 않고, 각 요약 항목을 실제 원문 오디오와 timestamp 근거에 연결하여 사용자가 빠르게 검증하고 실행 항목으로 전환할 수 있게 하는 Android 음성 노트 서비스다.**

### 해결하려는 문제

AI 음성 요약은 빠르지만, 사용자는 다음을 확인하기 어렵다.

- 요약 문장이 실제 원문에서 나온 것인가?
- 중요한 문맥이 누락되거나 왜곡되지 않았는가?
- 전체 녹음을 다시 듣지 않고 필요한 근거만 확인할 수 있는가?
- 확인한 내용을 곧바로 실행 항목으로 저장할 수 있는가?

### 핵심 가설

요약 카드의 source timestamp를 누르면 해당 오디오 구간을 즉시 재생하고, 같은 원문 구간을 강조하면 사용자는 AI 결과를 더 빠르고 신뢰성 있게 확인할 수 있다.

---

## 2. 포트폴리오 목표와 완료 정의

Skim은 화면 캡처만 있는 디자인/UX 과제가 아니라, **Android 앱과 Backend가 실제로 연결되는 풀스택 MVP**로 완성한다.

완성 후 포트폴리오에서 정직하게 말할 수 있는 메시지:

> 음성 파일 업로드부터 비동기 처리 상태, transcript·summary·source timestamp 저장, Android의 실제 오디오 seek/play, 원문 근거 확인과 할 일 저장까지를 하나의 사용자 흐름으로 구현했습니다. AI 요약을 생성하는 것보다 각 결과가 어떤 원문에서 나왔는지 사용자가 검증할 수 있는 데이터 계약과 UX를 우선 설계했습니다.

### 완료의 최소 조건

아래 흐름이 실제 에뮬레이터에서 동작하고, API·핵심 도메인·UI가 테스트되어야 한다.

```text
음성 파일 추가 또는 녹음
→ Android에서 Backend 업로드
→ Backend가 처리 job 생성 및 상태 반환
→ transcript / summary / source timestamp 저장
→ 앱에서 처리 완료 결과 표시
→ summary timestamp 탭
→ 실제 오디오의 해당 위치로 seek 후 재생
→ 연결된 transcript 구간 강조
→ 해당 요약을 할 일로 저장
→ 앱 재시작 후 저장된 데이터 복원
```

### 완료로 간주하지 않는 것

다음만 구현된 상태는 포트폴리오 MVP 완료가 아니다.

- seed data만 표시되는 Compose 화면
- 오디오 파일명을 보여주기만 하고 실제 재생하지 않는 상태
- mock UI만 있고 API/DB 저장이 없는 상태
- summary와 transcript가 실제 timestamp 데이터로 연결되지 않은 상태
- 빌드만 성공하고 핵심 플로우를 테스트/에뮬레이터에서 검증하지 않은 상태

---

## 3. 현재 코드베이스 사실 기록

프로젝트 경로: `/Users/yuseob/Documents/android-project/SkimAndroidCli`

### 현재 구현됨

- Kotlin / Jetpack Compose / Material 3 기반 Android UI
- 녹음 목록 → 상세 → 요약 / 원문 탭 흐름
- `SummaryItem`과 `SummarySource`가 `TranscriptChunk`를 참조하는 모델
- timestamp 선택 시 원문 탭 전환, 해당 chunk 자동 스크롤, `선택한 근거` badge와 highlight
- 3개의 Kotlin local seed recording 및 APK 포함 `.m4a` asset
- Git 저장소 및 포트폴리오 문서 커밋: `e042078 docs: prepare Skim portfolio MVP`
- 2026-07-11 검증: `./gradlew :app:testDebugUnitTest :app:assembleDebug` → `BUILD SUCCESSFUL`
- 2026-07-11 에뮬레이터에서 seed 흐름 검증: 목록 → 요약 timestamp → 원문 highlight → 목록 복귀

### 현재 구현되지 않음

- 실제 오디오 재생·pause·seek
- 마이크 녹음 및 오디오 파일 import
- Backend / REST API / OpenAPI 문서
- PostgreSQL 및 영속 데이터
- 실제 upload / processing job / polling
- 실제 STT 및 LLM summary
- Todo 영속 저장
- 앱의 Home / Add / Library / Settings 구조
- timestamp seek, 저장 복원, upload flow에 대한 핵심 테스트

### 현재 주요 코드 위치

| 책임 | 현재 경로 |
| --- | --- |
| UI 상태와 목록/상세/요약/원문 화면 | `app/src/main/java/com/example/skim/ui/main/MainScreen.kt` |
| UI 모델 | `app/src/main/java/com/example/skim/model/SkimModels.kt` |
| 임시 seed 데이터 | `app/src/main/java/com/example/skim/data/FakeSkimRepository.kt` |
| Compose smoke test | `app/src/androidTest/java/com/example/skim/ui/main/MainScreenTest.kt` |
| 현재 UX 근거 기록 | `docs/portfolio-case-study.md` |

> 현재 `MainScreen.kt`와 `FakeSkimRepository`는 프로토타입 구현이다. 풀스택 MVP에서는 UI, ViewModel, repository, network/local data source, player를 분리한다.

---

## 4. 확정된 MVP 범위

### 4.1 Android 앱

#### 큰 화면 구조

```text
Bottom navigation
├─ 홈: 최근 녹음, 처리 상태, 이어 듣기
├─ 추가: 파일 가져오기 / 녹음 시작
├─ 라이브러리: 전체 녹음 목록과 최소 검색/정렬
└─ 설정: 앱 정보 및 처리 정책 안내 (최소 구현)

Detail (별도 depth)
├─ 요약
├─ 원문
└─ 할 일
```

#### Android 필수 기능

1. 오디오 파일 선택 및 업로드
2. 마이크 녹음 후 업로드
3. 처리 상태 표시: `UPLOADED`, `TRANSCRIBING`, `SUMMARIZING`, `COMPLETED`, `FAILED`
4. 처리 완료된 recording 목록 및 상세 조회
5. Media3 기반 재생, pause, 진행 위치 표시
6. summary source 선택 시 `startMs`로 seek하고 재생
7. 재생·선택된 위치와 transcript chunk highlight 연결
8. summary에서 Todo 생성 / 완료 상태 변경
9. 네트워크 오류·처리 실패·빈 결과의 명확한 UI
10. Room cache로 최근 결과와 Todo를 앱 재시작 후 복원

### 4.2 Backend

Backend는 Android가 직접 DB나 AI 공급자에 접근하지 않도록 하는 신뢰 경계다. 파일, 처리 job, 데이터 계약, 상태 전이, API 오류를 Backend가 담당한다.

#### Backend 필수 역할

1. recording 메타데이터 생성·조회
2. audio upload 수신 및 서버 파일 저장
3. processing job 생성 및 상태 전이
4. transcript / summary / source timestamp / Todo의 PostgreSQL 영속화
5. Android용 REST API 제공
6. OpenAPI/Swagger 문서 자동 생성
7. 유효성 검증, 표준 오류 응답, 처리 실패 상태 기록
8. 개발 환경에서 Docker Compose로 실행 가능

#### Backend 구현 권장 스택

| 영역 | 선택 |
| --- | --- |
| Language / Framework | Kotlin + Spring Boot |
| DB | PostgreSQL |
| ORM / Migration | Spring Data JPA + Flyway |
| API 문서 | springdoc-openapi / Swagger UI |
| 비동기 처리 | job table 기반 상태 전이 + `@Async` worker 또는 명시적 worker |
| 파일 저장 | 개발 단계는 Docker volume의 local file storage |
| 검증 | JUnit 5, MockMvc, Testcontainers 또는 Docker PostgreSQL |
| 실행 | Docker Compose |

> 로그인, 다중 사용자, 클라우드 배포는 MVP 필수 범위가 아니다. 단, 모든 API는 나중에 user scope를 붙일 수 있도록 resource ID와 request/response 경계를 명확하게 설계한다.

---

## 5. 데이터 계약

`SummaryItem → SummarySource → TranscriptChunk` 관계는 Skim의 제품 핵심이며, Android와 Backend에서 동일한 의미를 가져야 한다.

### 5.1 핵심 엔터티

```text
Recording
├─ id
├─ title
├─ audioStorageKey
├─ durationMs
├─ status
├─ failureReason?
├─ createdAt / updatedAt
├─ transcriptChunks[]
├─ summaryItems[]
└─ todos[]

TranscriptChunk
├─ id
├─ recordingId
├─ startMs
├─ endMs
├─ text
└─ sequence

SummaryItem
├─ id
├─ recordingId
├─ category
├─ text
├─ sourceConfidence?       # 후속 확장 가능
└─ sources[]

SummarySource
├─ id
├─ summaryItemId
├─ transcriptChunkId
├─ startMs
└─ endMs

Todo
├─ id
├─ recordingId
├─ summaryItemId?
├─ title
├─ isCompleted
├─ sourceStartMs?
├─ sourceEndMs?
└─ createdAt
```

### 5.2 중요한 불변 조건

1. `SummarySource`는 반드시 같은 recording의 transcript를 참조한다.
2. `SummarySource.startMs/endMs`는 참조 transcript의 범위 안에 있어야 한다.
3. source가 없는 summary는 `근거 확인 가능`한 AI 요약으로 표시하지 않는다.
4. audio seek의 기준은 표시용 문자열이 아니라 정수 밀리초 `startMs`다.
5. Android UI는 `timeRange` 같은 표시 문자열을 다시 파싱하지 않는다.
6. Todo가 summary에서 생성되었다면 원래 summary와 source timestamp 연결을 보존한다.
7. 실패한 처리 job은 실패 원인을 기록하며, 완료 데이터와 섞어 표시하지 않는다.

---

## 6. REST API 초안

API path와 JSON 구조는 구현 전에 OpenAPI로 확정하고, Android Retrofit client는 이 계약을 기준으로 작성한다.

### Recording

```http
POST /v1/recordings
GET  /v1/recordings
GET  /v1/recordings/{recordingId}
```

`POST /v1/recordings` request:

```json
{
  "title": "앱 아이디어 메모"
}
```

response:

```json
{
  "id": "rec_123",
  "title": "앱 아이디어 메모",
  "status": "UPLOADED",
  "durationMs": null,
  "createdAt": "2026-07-11T12:00:00Z"
}
```

### Audio upload 및 처리 시작

```http
POST /v1/recordings/{recordingId}/audio
POST /v1/recordings/{recordingId}/process
GET  /v1/recordings/{recordingId}/processing-status
```

- audio upload: `multipart/form-data`, 허용 포맷과 크기를 서버에서 검증한다.
- processing start: 중복 시작을 막고, 이미 실행 중이면 idempotent하게 현재 job 상태를 반환한다.
- status response에는 `status`, `failureReason`, `updatedAt`을 포함한다.

### 결과 조회

```http
GET /v1/recordings/{recordingId}/transcript
GET /v1/recordings/{recordingId}/summary
GET /v1/recordings/{recordingId}/audio
```

summary response 예시:

```json
{
  "recordingId": "rec_123",
  "items": [
    {
      "id": "sum_1",
      "category": "결정 사항",
      "text": "첫 MVP는 개인 음성 메모 흐름에 집중한다.",
      "sources": [
        {
          "transcriptChunkId": "chunk_1",
          "startMs": 40000,
          "endMs": 80000,
          "label": "00:40–01:20"
        }
      ]
    }
  ]
}
```

### Todo

```http
POST  /v1/todos
GET   /v1/todos?recordingId={recordingId}
PATCH /v1/todos/{todoId}
```

`POST /v1/todos` request 예시:

```json
{
  "recordingId": "rec_123",
  "summaryItemId": "sum_1",
  "title": "MVP 화면 범위를 개인 음성 메모로 확정",
  "sourceStartMs": 40000,
  "sourceEndMs": 80000
}
```

---

## 7. 처리 파이프라인 전략

### MVP 처리 상태

```text
UPLOADED
  → TRANSCRIBING
  → SUMMARIZING
  → COMPLETED

어느 단계에서든 실패
  → FAILED (failureReason 기록)
```

### 단계적 구현 원칙

#### Phase A — 실제 full-stack 흐름을 먼저 증명

- audio upload, DB, job status, polling, transcript/summary 저장을 실제 구현한다.
- transcript/summary는 deterministic demo processor가 생성해도 된다.
- 이 단계의 목적은 API 계약, 상태 전이, 영속 데이터, Android 처리 UX를 검증하는 것이다.

#### Phase B — 실제 STT 연결

- Whisper API 또는 self-hosted Whisper 중 하나를 선택한다.
- 실제 audio에서 `startMs/endMs`를 갖는 transcript chunk를 생성한다.
- 지원 포맷, 최대 길이, timeout, 실패, 재시도 정책을 명시한다.

#### Phase C — 실제 summary 연결

- transcript를 summary item과 action item으로 변환한다.
- 모든 summary에 source timestamp를 강제한다.
- source를 확정할 수 없는 결과는 생성하지 않거나 `근거 없음`으로 명시한다.

> STT/LLM 공급자, API key, 비용은 구현 전 별도 결정한다. 이를 결정하기 전에도 Phase A는 완성 가능하다.

---

## 8. 구현 순서

### Milestone 0 — 기준선 고정

- 현재 prototype의 build/test를 기록한다.
- `docs/portfolio-case-study.md`는 현재 UX 프로토타입의 증거로 유지한다.
- 이 문서 기준으로 API schema와 완료 조건을 합의한다.

### Milestone 1 — Backend skeleton 및 계약

1. 별도 저장소 `skim-server`에 Kotlin Spring Boot 프로젝트를 유지한다. Android 저장소에는 backend 복사본을 두지 않는다.
2. `skim-server`의 Docker Compose로 PostgreSQL과 Backend를 실행한다.
3. Flyway migration으로 `recordings`, `transcript_chunks`, `summary_items`, `summary_sources`, `todos`, `processing_jobs` 생성
4. OpenAPI 문서 및 API error schema 작성
5. recording / status / transcript / summary / todo API unit/integration test 작성

**완료 기준:** Docker Compose 실행, Swagger UI 접근, recording 생성·조회 API 테스트 통과.

### Milestone 2 — Android network / local architecture

1. 기존 `FakeSkimRepository` 의존을 interface 기반 repository로 교체
2. Retrofit API client와 DTO/domain mapper 작성
3. Room cache와 migration 작성
4. ViewModel, UI state, loading/error state 분리
5. Home / Add / Library / Settings navigation 구조 도입

**완료 기준:** Backend의 seed result를 Android가 받아 목록·상세에 렌더링하며, 앱 재시작 뒤 cache가 유지된다.

### Milestone 3 — 파일·녹음·upload

1. Android Storage Access Framework 파일 선택 구현
2. `RECORD_AUDIO` 권한 및 AudioRecord/MediaRecorder 기반 녹음 구현
3. 생성한 audio를 Backend multipart upload로 전송
4. 업로드 실패, 취소, 포맷/용량 제한 UI 처리

**완료 기준:** 실제 파일 하나와 직접 녹음 하나를 업로드하여 Backend storage 및 DB에 저장할 수 있다.

### Milestone 4 — 처리 상태와 실제 데이터 흐름

1. processing job 생성과 polling 구현
2. deterministic demo processor로 transcript/summary/source 생성
3. Android에서 `UPLOADED → TRANSCRIBING → SUMMARIZING → COMPLETED` 표시
4. `FAILED` 상태 및 재시도 UI 구현

**완료 기준:** 새 audio가 업로드된 뒤 app이 polling을 통해 완료 결과를 표시한다.

### Milestone 5 — Media3 기반 근거 재생

1. Backend audio download/stream endpoint 구현
2. Android Media3 player 구현
3. play/pause, duration, progress UI 구현
4. summary source 선택 시 `startMs` seek + 자동 재생
5. 재생 위치와 transcript chunk highlight 연결

**완료 기준:** summary timestamp 탭 한 번으로 실제 오디오가 정확한 구간에서 재생되고 연결된 원문이 강조된다.

### Milestone 6 — Todo 및 품질 보강

1. summary 기반 Todo 생성/완료 처리
2. source timestamp와 summary 연결 보존
3. Room/Backend 동기화 규칙을 MVP 범위에서 단순화
4. 빈 상태, network error, processing failure, transcript 없음 UI 보강

**완료 기준:** 사용자는 요약을 근거와 함께 확인하고 Todo로 저장한 뒤 다시 열어볼 수 있다.

### Milestone 7 — STT/LLM 및 발표 자료

1. Phase B/C 실제 처리 공급자 선택 및 연결
2. time-range / source validation 강화
3. API, Android, end-to-end test 보강
4. emulator 시연 영상, 스크린샷, README, architecture diagram 작성
5. 2~3페이지 Skim project case study 제작

---

## 9. 테스트와 검증 기준

### Backend

- API request validation: 빈 title, 존재하지 않는 recording, 잘못된 file type/size
- 상태 전이: 유효하지 않은 전이 거부, 중복 process 요청 idempotency
- data integrity: 다른 recording의 transcript를 source로 연결하는 요청 거부
- summary source range validation
- Todo가 source metadata를 보존하는지
- OpenAPI contract와 MockMvc/Testcontainers integration test

### Android

- timestamp 선택 시 올바른 `startMs` seek command 생성
- 선택한 source의 transcript chunk만 highlight
- recording upload 상태별 UI
- network failure / job failure UI
- Room cache 저장과 재시작 뒤 복원
- Todo 생성 및 완료 상태

### End-to-end

실제 emulator/device에서 최소 한 개의 audio에 대해 아래를 영상과 screenshot으로 검증한다.

```text
파일 또는 녹음 추가
→ upload 성공
→ 처리 상태 변화
→ 요약 표시
→ timestamp 선택
→ 실제 음성 seek/play
→ 원문 highlight
→ Todo 저장
→ 앱 재시작 뒤 기록/Todo 확인
```

### 최종 명령 예시

실제 프로젝트 구조가 확정된 뒤 README에 최신 명령을 기록한다.

```bash
# Backend (separate skim-server repository)
cd /Users/yuseob/Documents/other-project/skim-server
./gradlew test
docker compose up --build

# Android
cd /Users/yuseob/Documents/android-project/skim-android
./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:assembleDebug
```

---

## 10. 의도적으로 제외하는 범위

아래는 MVP 완성 후의 확장이다. MVP 구현 중 새 기능으로 추가하지 않는다.

- 회원가입, 로그인, 다중 사용자 및 팀 협업
- 클라우드 배포, production monitoring, 결제
- 실시간 streaming transcription
- 장시간/무제한 파일 처리
- 공유, export, 캘린더 연동
- RAG, 온디바이스 LLM, 모델 학습
- 정교한 검색/태그/추천

---

## 11. 포트폴리오 산출물 기준

기능이 완료된 뒤에만 포트폴리오를 작성한다. 현재의 Markdown은 제출용 포트폴리오가 아니라 프로토타입 근거 기록이다.

### 프로젝트 상세 2~3페이지에 포함할 것

1. **문제와 한 문장 가치 제안**
2. **전체 사용자 흐름**: upload → process → verify → todo
3. **아키텍처 다이어그램**: Android / Backend / PostgreSQL / processing provider
4. **핵심 데이터 계약**: SummaryItem → SummarySource → TranscriptChunk
5. **실제 화면 4~6장**: add, processing, summary, source seek/play, transcript, todo
6. **기술적 판단**: 왜 source를 강제하는지, 왜 Backend를 trust boundary로 두는지, 왜 local cache를 두는지
7. **검증 근거**: API test, Android test, emulator flow, Docker 실행
8. **정직한 범위 표기**: 구현됨 / demo processor / 후속 확장
9. **GitHub, demo video, 실행 방법 링크**

### 표현 원칙

- 실제 STT/LLM이 연결되기 전에는 ‘AI가 자동 생성한다’고 쓰지 않는다.
- demo processor는 ‘deterministic demo processor’라고 명시한다.
- 테스트/성능 수치는 실행 로그가 있을 때만 쓴다.
- 구현하지 않은 login, cloud deployment, team collaboration, real-time processing을 경험한 것처럼 쓰지 않는다.

---

## 12. 구현 시작 전 결정이 필요한 항목

다음 항목은 기술 선택을 위해 구현 시작 시 확정한다.

1. Backend repository 위치: monorepo `SkimAndroidCli/backend` 또는 별도 repository
2. 실제 STT 제공자: Whisper API vs self-hosted Whisper
3. 실제 LLM 제공자 및 비용/secret 관리 방식
4. Android 녹음 구현: MediaRecorder vs AudioRecord
5. 파일 허용 범위: MVP는 m4a/mp3/wav 중 어떤 포맷을 지원할지
6. 최대 오디오 길이·용량, timeout, retry 정책
7. 로컬 Docker 환경의 port와 storage directory

이 결정이 나지 않아도 Milestone 1~5의 API/상태/UX 구조는 이 문서의 계약을 유지한다.
