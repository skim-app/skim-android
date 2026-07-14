# skim-evidence-first-implementation - Work Plan

## TL;DR (For humans)
<!-- Fill this LAST, after the detailed plan below is written, so it summarizes the REAL plan. -->
<!-- Plain English for a non-engineer: NO file paths, NO todo numbers, NO wave/agent/tool names. -->

**What you'll get:** Skim becomes an evidence-first listening app: the library makes reviewable evidence easy to scan, and detail makes a summary source lead naturally into the matching transcript and audio.

**Why this approach:** The code already has a strong timestamp-to-source interaction that competing apps usually bury beside separate Summary, Transcript, and AI surfaces. The redesign makes that proven interaction the visual identity instead of adding unbuilt features.

**What it will NOT do:** It will not add recording, meeting bots, chat, templates, sharing, search, collaboration, or backend/model work. It will not copy another app's imagery, branding, or copy.

**Effort:** Medium
**Risk:** Medium - a single Compose file owns the home, library, upload, detail, transcript, and playback states, so visual changes must retain one existing evidence route.
**Decisions I made for you:** Use a warm editorial palette with a high-contrast evidence signal, make the review queue the home experience, preserve the existing two content destinations as Skim and Evidence, and keep file import as the only capture action.

Your next move: run this plan with the existing ulw-loop workflow. Full execution detail follows below.

---

> TL;DR (machine): Medium-risk Android Compose redesign of theme and MainScreen that foregrounds the existing timestamp evidence route, with no product-capability expansion.

## Scope
### Must have
- `DESIGN.md` as the implementation contract: research log, tokens, typography, spacing, shapes, component states, motion, accessibility, and accepted debt.
- Deterministic light/dark Material 3 tokens: warm paper surface, graphite text, deep-moss primary, burnt-orange evidence signal; all text/surface combinations meet the Android 4.5:1 guidance and non-text controls meet 3:1.
- Home/Library review queue that preserves `Home = first three` and `Library = all`, plus the existing recording click path and source counts.
- Detail flow retaining `SummarySource` click -> selected `chunkId` -> Evidence destination -> scroll/highlight -> `startMs` playback seek.
- Accessible vector navigation, descriptive semantics, selected evidence state description, and 48dp touch targets for compact controls.
- Agent-executed instrumentation, build, and Android emulator QA for success plus no-audio/upload/error boundaries.

### Design contract (executor must implement exactly)

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `background` / `surface` | `#FFFBF7` | `#151613` | warm paper canvas |
| `onBackground` / `onSurface` | `#1D1C1A` | `#E9E5DF` | all primary reading text |
| `surfaceVariant` | `#F0ECE6` | `#242522` | recording and transcript cards |
| `onSurfaceVariant` | `#5D5A55` | `#CBC6BF` | metadata and secondary body text |
| `primary` / `onPrimary` | `#38503F` / `#FFFFFF` | `#B8D3BD` / `#1D2B20` | selected navigation, primary action, evidence badge |
| `primaryContainer` / `onPrimaryContainer` | `#D7E9D7` / `#102516` | `#233D2A` / `#B8D3BD` | Home review-queue hero |
| `secondary` / `onSecondary` | `#6D5B2D` / `#FFFFFF` | `#D9C98E` / `#383006` | supporting category label |
| `secondaryContainer` / `onSecondaryContainer` | `#F5E6B8` / `#392A05` | `#54481E` / `#F5E6B8` | Skim summary lead card |
| `tertiary` / `onTertiary` | `#9C3D10` / `#FFFFFF` | `#FFC1A0` / `#48200D` | timestamp/source action only |
| `tertiaryContainer` / `onTertiaryContainer` | `#FFE1CE` / `#52220B` | `#623415` / `#FFDCC7` | selected evidence transcript card |
| `error` / `onError` | `#BA1A1A` / `#FFFFFF` | `#FFB4AB` / `#690005` | error icon and retry emphasis |
| `errorContainer` / `onErrorContainer` | `#FFDAD6` / `#410002` | `#93000A` / `#FFDAD6` | refresh/upload/playback error card |
| `outline` | `#7A756E` | `#938F88` | source-chip border and inactive separator |

- Typography: display `32sp/40sp Bold`; screen title `24sp/30sp Bold`; card title `20sp/28sp SemiBold`; section `16sp/22sp SemiBold`; body `16sp/24sp Normal`; label `13sp/18sp Medium`; metadata `12sp/16sp Medium`. Keep Korean text at these `sp` sizes or larger.
- Spacing: `4, 8, 12, 16, 24, 32dp`; card padding `20dp`; screen horizontal padding `20dp`; list gap `12dp`; card radii `20dp`; hero radius `28dp`; compact status pill is fully rounded. Do not introduce a spacing value outside this scale.
- Component states: default card = `surfaceVariant`; pressed/selected uses `tertiaryContainer` plus an explicit text badge; disabled/unavailable uses `onSurfaceVariant` text and no implied action; error uses `errorContainer` plus text. Motion is only `animateColorAsState` for selected transcript background, `150ms` default, no decorative loop/scale animation.
- Accessibility proof: add `SkimThemeContrastTest` with relative-luminance contrast assertions for every foreground/background pair in this table: normal text >= `4.5`, non-text state/selection pairs >= `3.0`. Test every compact action with `getUnclippedBoundsInRoot()` against `48.dp` converted through the rule density.
- Required semantics: navigation descriptions exactly `홈`, `음성 파일 추가`, `라이브러리`, `설정`; every source action description exactly `근거 듣기 <timestamp>`; selected chunk `stateDescription` exactly `선택한 근거`; unavailable audio text remains `오디오가 없어 재생할 수 없습니다.`. Tests must locate these semantics, not only visual text.

### Deterministic device fixture

- Add `scripts/qa/skim-fixture-server.mjs`, a QA-only Node built-in `http` server with no npm dependency. It serves `GET /v1/recordings`, `GET /v1/recordings/{id}`, `/transcript`, `/summary`, and `/audio` at port `8081`.
- Fixture `evidence-audio`: `audioAvailable:true`, one summary source `chunk-evidence` (`00:00–00:06`, `startMs:0`, `endMs:6000`), one transcript chunk with that ID, and `/audio` streams `app/src/main/assets/skim-demo-voice-note.m4a`. Fixture `review-ready`: a second `audioAvailable:true` recording with source/transcript ID `chunk-review` and the same local audio asset. Fixture `without-audio`: a third recording with the same source/transcript shape, `audioAvailable:false`, and no audio endpoint. `GET /v1/recordings` returns the deterministic ordered list `evidence-audio`, `review-ready`, `without-audio`; detail, summary, transcript, and audio routes only expose the assets valid for the requested ID.
- Device builds always use `./gradlew -PskimBaseUrl=http://10.0.2.2:8081/ :app:assembleDebug`; QA starts the fixture with `node scripts/qa/skim-fixture-server.mjs --port 8081 > "$ATTEMPT_DIR/fixture-server.txt" 2>&1 & FIXTURE_PID=$!`, verifies `curl -fsS http://127.0.0.1:8081/v1/recordings`, and cleans it with `kill "$FIXTURE_PID"; ! kill -0 "$FIXTURE_PID"`.
- Create-or-reuse one clean AVD exactly as follows: `PRE_AVDS=$(mktemp); android emulator list > "$PRE_AVDS"; if rg -qx "Pixel_10_Pro" "$PRE_AVDS"; then AVD_NAME=Pixel_10_Pro; else android emulator create medium_phone; AVD_NAME=$(comm -13 <(sort "$PRE_AVDS") <(android emulator list | sort) | head -n 1); test -n "$AVD_NAME"; fi; rm -f "$PRE_AVDS"`. Start it using `android emulator start --cold "$AVD_NAME"`; because this command waits for readiness, discover exactly one emulator using `SERIAL=$(adb devices | awk '/^emulator-[0-9]+[[:space:]]+device$/{print $1}'); test "$(printf '%s\n' "$SERIAL" | sed '/^$/d' | wc -l | tr -d ' ')" -eq 1; printf '%s\n' "$SERIAL" > "$ATTEMPT_DIR/device.txt"`; clear cache and Room state after install and before launch with `adb -s "$SERIAL" shell pm clear com.example.skim`.
- Except for the one initial `adb devices` discovery command above, every device command in this plan begins with `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")` and uses either `adb -s "$SERIAL"` or `ANDROID_SERIAL="$SERIAL" android screen capture`; no angle-bracket device placeholder or unqualified device action is permitted. Cleanup stops only an AVD created by this QA run; reused `Pixel_10_Pro` remains running unless the worker started it from stopped state, which must be recorded in `$ATTEMPT_DIR/device-lifecycle.txt`.
### Must NOT have (guardrails, anti-slop, scope boundaries)
- No microphone capture, meeting bot, AI chat, collaboration, templates, confidence correction, search, sharing/export, or data/API/schema change.
- No competitor assets, copied copy, logo, or pixel tracing; no blue/purple SaaS gradient or Unicode/emoji navigation symbols.
- No new navigation framework, screen model, or abstraction beyond the smallest existing `MainScreen.kt` changes.

## Verification strategy
> Zero human intervention - all verification is agent-executed.
- Test decision: tests-after for pure design documentation; TDD for every Compose behavior/selectable-semantic change using existing Compose UI instrumentation tests.
- Evidence: obtain `ATTEMPT_DIR` from `omo ulw-loop status --json`; write every test transcript, Android layout dump, screenshot, and review receipt below `ATTEMPT_DIR`.
- Baseline/RED rule: before each behavior change, run the exact failing test or faithful emulator action against the prior UI; record why it fails, then make the smallest change and capture GREEN. Pure prose in `DESIGN.md` has no fake grep test and is reviewed by read.

## Execution strategy
### Parallel execution waves
> Target 5-8 todos per wave. Fewer than 3 (except the final) means you under-split.

Wave 1 (baseline and contract): Todos 1-3. Todo 2 blocks all visual code; Todo 3 can start after the theme contract but must preserve existing navigation.

Wave 2 (shared-file serial UI changes): Todos 4-6. They all modify `MainScreen.kt`, so run serially in that order to avoid conflicts; each worker receives the prior worker's tested state.

Wave 3 (proof): Todos 7-8 after all UI changes. Todo 8 is the real Android surface gate and depends on Todo 7's green instrumentation suite.

### Dependency matrix
| Todo | Depends on | Blocks | Can parallelize with |
| --- | --- | --- | --- |
| 1 | none | 2-8 | 2 after baseline capture |
| 2 | 1 | 3-8 | 1 final documentation review |
| 3 | 2 | 4-8 | none (shared UI entry/navigation) |
| 4 | 2, 3 | 5-8 | none (same `MainScreen.kt`) |
| 5 | 2, 3, 4 | 6-8 | none (same `MainScreen.kt`) |
| 6 | 2-5 | 7-8 | none (same `MainScreen.kt`) |
| 7 | 2-6 | 8 | none (test surface follows UI) |
| 8 | 7 | final verification | Final plan/compliance review only |

## Todos
> Implementation + Test = ONE todo. Never separate.
<!-- APPEND TASK BATCHES BELOW THIS LINE WITH edit/apply_patch - never rewrite the headers above. -->
- [ ] 1. Capture the existing evidence flow and create the design contract.
  What to do / Must NOT do: Capture current Home, Add, detail source tap, and no-audio screenshots/layout dumps before code changes; create root `DESIGN.md` by copying the complete `Design contract` above and include a research log for the reviewed market/app sources. Add the QA-only fixture server defined above, but do not change Kotlin, Gradle, production API, or production backend behavior in this todo.
  Parallelization: Wave 1 | Blocked by: none | Blocks: 2-8
  References (executor has NO interview context - be exhaustive): `.omo/drafts/skim-evidence-first-redesign.md:23-53`; `README.md:9-15`; `MainScreen.kt:81-92,242-275,450-464,539-617`; Android accessibility guidance at https://developer.android.com/develop/ui/compose/accessibility/api-defaults.
  Acceptance criteria (agent-executable): `test -s DESIGN.md`; `node scripts/qa/skim-fixture-server.mjs --port 8081` serves the three fixture IDs and the `.m4a` asset; `./gradlew -PskimBaseUrl=http://10.0.2.2:8081/ :app:assembleDebug` exits 0. Use the exact create-or-reuse AVD algorithm in `Deterministic device fixture`, then record its sole serial in `$ATTEMPT_DIR/device.txt` and clear `com.example.skim` after installing and before launching.
  QA scenarios (name the exact tool + invocation): start the fixture command in the deterministic fixture section, set `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`, run `android run --debug --apks app/build/outputs/apk/debug/app-debug.apk --activity com.example.skim.MainActivity --device "$SERIAL"`, inspect `android layout --device "$SERIAL" --pretty`, then capture `ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-1-baseline.png"`. Failure proof: source control currently reads `▶ 00:00–00:06`, not the planned `근거 듣기 00:00–00:06` semantic action. Evidence `$ATTEMPT_DIR/task-1-baseline.{png,json,txt}` plus fixture transcript.
  Commit: Y | `docs(design): define evidence-first visual contract`

- [ ] 2. Implement deterministic Material 3 evidence tokens and typography.
  What to do / Must NOT do: Update `Color.kt`, `Theme.kt`, and `Type.kt` with every exact token/type value from the `Design contract`; default `SkimTheme` to its own tokens rather than wallpaper-driven dynamic color while retaining explicit dark-theme support. Add the Compose-BOM-managed Material Icons Extended dependency through `gradle/libs.versions.toml` and `app/build.gradle.kts`. Do not alter models, repository, or playback behavior.
  Parallelization: Wave 1 | Blocked by: 1 | Blocks: 3-8
  References (executor has NO interview context - be exhaustive): `app/src/main/java/com/example/skim/theme/Theme.kt:13-49`, `Color.kt:5-11`, `Type.kt:10-36`; `app/build.gradle.kts:49-96`; `gradle/libs.versions.toml:21-49`; `DESIGN.md` tokens.
  Acceptance criteria (agent-executable): write `SkimThemeContrastTest` RED with the listed pair thresholds, then GREEN with the exact tokens; `./gradlew :app:testDebugUnitTest :app:assembleDebug` exits 0. Verify both `SkimTheme(darkTheme = false)` and `SkimTheme(darkTheme = true)` render without default purple tokens in fixture-backed emulator screenshots.
  QA scenarios (name the exact tool + invocation): set `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`; run `adb -s "$SERIAL" shell cmd uimode night no; ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-2-light.png"`; then run `adb -s "$SERIAL" shell cmd uimode night yes; ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-2-dark.png"`. PASS when both images show readable text, distinctive evidence color, and no system-wallpaper palette drift. Evidence `$ATTEMPT_DIR/task-2-theme-build.txt`, `task-2-light.png`, and `task-2-dark.png`.
  Commit: Y | `feat(theme): add evidence-first color and type tokens`

- [ ] 3. Replace character navigation with accessible Material vector navigation.
  What to do / Must NOT do: Replace the `MainDestination.icon` string glyphs in `MainScreen.kt` with `Icon`/official Material vectors and exactly the required navigation descriptions in `Design contract`; use `Modifier.minimumInteractiveComponentSize()` on each NavigationBarItem. Keep the four existing Home/Add/Library/Settings destinations and selection state. Do not add a navigation graph or a fifth destination.
  Parallelization: Wave 1 | Blocked by: 2 | Blocks: 4-8
  References (executor has NO interview context - be exhaustive): `MainScreen.kt:71-72,115-149,188-215,280-286`; https://developer.android.com/develop/ui/compose/accessibility/semantics; https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns.
  Acceptance criteria (agent-executable): write a failing Compose test that locates `음성 파일 추가` by content description, asserts a 48dp minimum bounds height/width with test-rule density, and taps it; then implement it and make it green. Existing `addDestination_exposesTheUploadEntryPoint` remains green.
  QA scenarios (name the exact tool + invocation): `SERIAL=$(cat "$ATTEMPT_DIR/device.txt"); android layout --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-3-nav-layout.json"`; PASS when four clickable primary destinations expose text/content descriptions and the Add tap still opens upload entry. Evidence `$ATTEMPT_DIR/task-3-nav-layout.json` plus instrumentation transcript.
  Commit: Y | `feat(navigation): use semantic material icons`

- [ ] 4. Make Home and Library a review-first evidence queue.
  What to do / Must NOT do: Recompose `HeroCard`, the Home/Library header, and `RecordingCard` so evidence count, status, duration, and one-line takeaway scan before generic marketing copy. Home still shows the first three recordings; Library still shows all; each card still opens the selected record by ID. Use shared theme tokens and restrained surfaces; do not add sorting, search, filters, empty-data schema, or new API fields.
  Parallelization: Wave 2 | Blocked by: 2, 3 | Blocks: 5-8
  References (executor has NO interview context - be exhaustive): `MainScreen.kt:225,242-256,288-335`; `Recording` in `SkimModels.kt:3-17`; screenshot baseline `$ATTEMPT_DIR/task-1-baseline.png`.
  Acceptance criteria (agent-executable): first add a failing Compose test for the review-queue heading and evidence-count semantics; GREEN requires that test plus `repositoryRecordings_areDisplayed` pass and the recording click still reaches detail.
  QA scenarios (name the exact tool + invocation): set `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`; capture Home with `ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-4-home.png"`; write `android layout --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-4-home-layout.json"`; derive the Library target with `COORDS=$(node -e 'const n=require(process.argv[1]).find(x=>x.contentDesc==="라이브러리"); if(!n) process.exit(1); console.log(String(n.center).replace(/[\[\],]/g," "))' "$ATTEMPT_DIR/task-4-home-layout.json")`; tap `adb -s "$SERIAL" shell input tap $COORDS`; then run `ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-4-library.png"` and `android layout --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-4-library-layout.json"`. PASS when Home has exactly three visible recording entries and Library exposes all three seeded entries. Evidence `$ATTEMPT_DIR/task-4-home.png`, `task-4-library.png`, `task-4-home-layout.json`, and `task-4-library-layout.json`.
  Commit: Y | `feat(library): prioritize reviewable evidence`

- [ ] 5. Turn detail into a continuous Skim-to-Evidence review flow.
  What to do / Must NOT do: Rework only the presentation around `PlaybackControls`, the two tabs, `SummaryTab`, `SummaryCard`, `TranscriptTab`, and `TranscriptChunkCard`. Label the destinations exactly `Skim` and `근거`; source actions must expose `근거 듣기 <timestamp>`; selected chunk must expose `선택한 근거`; selected-evidence card continues to show the matching transcript highlight. Preserve the exact source handler side effects: set `highlightedChunkId`, select the evidence destination, call `playFrom(source.startMs)`, scroll to the matched chunk, and release `ExoPlayer` on disposal.
  Parallelization: Wave 2 | Blocked by: 2-4 | Blocks: 6-8
  References (executor has NO interview context - be exhaustive): `MainScreen.kt:350-410,435-464,470-617`; `SummarySource`/`TranscriptChunk` in `SkimModels.kt:23-35`; existing no-audio test `MainScreenTest.kt:65-72`.
  Acceptance criteria (agent-executable): update selectors and write a failing behavior test that taps content description `근거 듣기 00:00–00:06`, then asserts tab `근거`, state description `선택한 근거`, and the exact unavailable-audio message for the no-audio fixture. GREEN requires this test and `unavailableAudio_keepsTheSelectedTranscriptVisible` equivalent pass.
  QA scenarios (name the exact tool + invocation): with `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`, write `android layout --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-5-before.json"`; set `COORDS=$(node -e 'const n=require(process.argv[1]).find(x=>x.contentDesc==="근거 듣기 00:00–00:06"); if(!n) process.exit(1); console.log(String(n.center).replace(/[\[\],]/g," "))' "$ATTEMPT_DIR/task-5-before.json")`; tap with `adb -s "$SERIAL" shell input tap $COORDS`; then run `android layout --diff --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-5-layout-diff.json"` and `ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-5-evidence.png"`. PASS when selected evidence is visible and the matching transcript is selected; `without-audio` additionally displays its error text. Evidence `$ATTEMPT_DIR/task-5-evidence.png`, `task-5-layout-diff.json`.
  Commit: Y | `feat(detail): connect skim and evidence review`

- [ ] 6. Apply the system to peripheral states and semantics.
  What to do / Must NOT do: Restyle Loading, Error, empty list, refresh error, Add/import, Settings, playback unavailable/error, and selected transcript semantics in `MainScreen.kt`; state labels must not rely on color alone: `선택한 근거` badge plus state description identifies selection, and unavailable audio retains its explicit text. Keep error retry and upload callbacks wired exactly as today. Do not introduce microphone actions or fake disabled controls.
  Parallelization: Wave 2 | Blocked by: 2-5 | Blocks: 7-8
  References (executor has NO interview context - be exhaustive): `MainScreen.kt:96-112,152-174,225-240,258-274,470-513,569-617`; Android touch/semantic guidance at https://developer.android.com/develop/ui/compose/accessibility/api-defaults.
  Acceptance criteria (agent-executable): add failing Compose tests for `음성 파일 추가`, `선택한 근거`, and `오디오가 없어 재생할 수 없습니다.` semantics/content; GREEN requires all MainScreen instrumentation tests to pass.
  QA scenarios (name the exact tool + invocation): set `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`; use `android layout --device "$SERIAL" --pretty` on Add and no-audio flows; PASS when upload entry, explanatory state text, and selected-evidence state are each exposed by the UI hierarchy. Evidence `$ATTEMPT_DIR/task-6-add-layout.json`, `task-6-no-audio-layout.json`.
  Commit: Y | `feat(states): complete evidence-first system states`

- [ ] 7. Lock behavior with focused Compose instrumentation and build verification.
  What to do / Must NOT do: Extend `MainScreenTest.kt` only for the new observable review queue, vector navigation semantics, 48dp target bounds, source-to-evidence route, no-audio error, and upload entry; add `SkimThemeContrastTest.kt` for the token thresholds. Keep model/repository tests untouched except if the compiler requires a selector update. Do not use text-presence-only tests for a purely visual color decision.
  Parallelization: Wave 3 | Blocked by: 2-6 | Blocks: 8
  References (executor has NO interview context - be exhaustive): `MainScreenTest.kt:16-72`; `SkimModels.kt:3-35`; `MainScreen.kt:455-464,555-617`.
  Acceptance criteria (agent-executable): `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:assembleDebug` exits 0 with no skipped/newly disabled tests.
  QA scenarios (name the exact tool + invocation): write `./gradlew` output to `$ATTEMPT_DIR/task-7-gradle.txt`; PASS when all three named tasks exit 0 and the source-route test reports success. Evidence `$ATTEMPT_DIR/task-7-gradle.txt`.
  Commit: Y | `test(ui): cover evidence-first review flow`

- [ ] 8. Run Android device visual QA and capture the final evidence package.
  What to do / Must NOT do: Start the deterministic fixture server, build against its `10.0.2.2:8081` endpoint, then install the final APK on the AVD recorded by Todo 1. Exercise Home, Library, Add, successful `evidence-audio` source route, `without-audio` source route, light theme, and dark theme. Inspect screenshots visually and layout semantics; tear down app, fixture process, and an emulator started by QA. Do not claim visual parity from Compose preview alone.
  Parallelization: Wave 3 | Blocked by: 7 | Blocks: final verification
  References (executor has NO interview context - be exhaustive): `README.md:84-95`; this plan Todos 1-7; Android CLI interaction guidance in `/Users/yuseob/.agents/skills/android-cli/references/interact.md`.
  Acceptance criteria (agent-executable): `curl -fsS http://127.0.0.1:8081/v1/recordings` returns exactly `evidence-audio`, `review-ready`, and `without-audio`; `SERIAL=$(cat "$ATTEMPT_DIR/device.txt"); android run --debug --apks app/build/outputs/apk/debug/app-debug.apk --activity com.example.skim.MainActivity --device "$SERIAL"` launches after `adb -s "$SERIAL" shell pm clear com.example.skim`; each named route has an inspected screenshot and layout dump; final `adb -s "$SERIAL" shell am force-stop com.example.skim`, `kill "$FIXTURE_PID"`, `! kill -0 "$FIXTURE_PID"`, and the lifecycle-dependent `android emulator stop "$AVD_NAME"` all succeed.
  QA scenarios (name the exact tool + invocation): set `SERIAL=$(cat "$ATTEMPT_DIR/device.txt")`; for each exact route name in `home library add evidence-audio without-audio dark`, run `ANDROID_SERIAL="$SERIAL" android screen capture --output "$ATTEMPT_DIR/task-8-$ROUTE.png"` and `android layout --device "$SERIAL" --pretty > "$ATTEMPT_DIR/task-8-$ROUTE.json"` after driving that named route; use `ROUTE=home; ...`, then `ROUTE=library; ...`, `ROUTE=add; ...`, `ROUTE=evidence-audio; ...`, `ROUTE=without-audio; ...`, and `ROUTE=dark; ...`—never an angle-bracket placeholder. PASS when each required state is visibly and semantically present. Evidence `$ATTEMPT_DIR/task-8-{home,library,add,evidence-audio,without-audio,dark}.{png,json}`; cleanup receipt records all app, fixture, and lifecycle-dependent emulator teardown commands.
  Commit: N | device QA only; record the capture commit in ulw-loop evidence.

## Final verification wave
> Runs in parallel after ALL todos. ALL must APPROVE. Surface results and wait for the user's explicit okay before declaring complete.
- [ ] F1. Plan compliance audit
- [ ] F2. Code quality review
- [ ] F3. Real manual QA
- [ ] F4. Scope fidelity

## Commit strategy

Commit every completed tracked work unit atomically after its test/build evidence: `docs(design)`, `feat(theme)`, `feat(navigation)`, `feat(library)`, `feat(detail)`, `feat(states)`, and `test(ui)`. Before staging, inspect touched-path history with `git log -- <path>`; never stage the user's existing `README.md`, `.idea/`, or unrelated `.omo/` artifacts. Each commit footer names this plan: `Plan: .omo/plans/skim-evidence-first-implementation.md`.

## Success criteria

1. The plan covers D1-D6 with exact paths, preserved behavior, acceptance criteria, agent-run QA, evidence locations, dependency relationships, and commits.
2. The plan prevents unsupported-feature scope creep and explicitly retains every source-to-evidence behavior and user-visible edge state.
3. An independent plan review approves the resulting plan after any cited gaps are corrected.
