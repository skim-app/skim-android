# Skim evidence-first redesign — durable draft

status: awaiting-approval
intent: unclear
review_required: false
classification: architecture
pending_action: write `.omo/plans/skim-evidence-first-redesign.md` and run the UNCLEAR-path Metis + dual high-accuracy review; do not implement product code.

## Routing

I treated the request as open-ended: it asks for a redesigned concept but does not prescribe a visual language. I therefore adopted the evidence-backed defaults below. If a specific desired outcome exists, the user can override one or more defaults at the approval gate.

## Evidence ledger

- Skim's product promise is not generic AI summarization: a `SummarySource` connects a summary item to a transcript `chunkId`, timestamp label, and millisecond range. `README.md:1-15`; `app/src/main/java/com/example/skim/model/SkimModels.kt:3-29`.
- The live interaction already executes summary-source tap -> selected transcript chunk -> transcript tab -> automatic scroll -> playback seek. `app/src/main/java/com/example/skim/ui/main/MainScreen.kt:450-464`, `555-617`.
- Current home/list gives the differentiator only one hero card and compact `근거 N개` metadata on recording cards. `app/src/main/java/com/example/skim/ui/main/MainScreen.kt:288-335`.
- Current theme is default/dynamic Material 3 with purple/pink fallback tokens and only a `bodyLarge` type override. `app/src/main/java/com/example/skim/theme/Theme.kt:13-49`, `Color.kt:5-11`, `Type.kt:10-36`.
- The current screen implementation has Home, Add (audio file picker/upload), Library, and Settings in one Compose file. It does not implement microphone capture, collaboration, search, export, or a reusable component system. `app/src/main/java/com/example/skim/ui/main/MainScreen.kt:71-92`, `242-275`.
- Official App Store screenshot research on 2026-07-14: Otter, Notta, Fireflies, and Plaud visibly use explicit Summary/Transcript/AI tabs or separate AI surfaces, a visually dominant record/import action, transcript time blocks, and modal/sheet sharing or export. Sources: https://apps.apple.com/us/app/otter-transcribe-voice-notes/id1276437113 ; https://apps.apple.com/in/app/notta-transcribe-voice-to-text/id1480649572 ; https://apps.apple.com/us/app/fireflies-ai-notetaker/id6463164203 ; https://apps.apple.com/us/app/plaud-ai-note-taker/id6450364080.
- Real-product screenshot search through Lazyweb was attempted with the documented anonymous-token recipe on 2026-07-14. The endpoint responded that paid access is required, so no Lazyweb images were downloaded or used. This is an external research limitation, not a product dependency.

## Components topology lock

1. Root design contract: create a project `DESIGN.md` from the current Material 3 surface and this concept before UI code changes.
2. Theme and primitive layer: deterministic colors, type, spacing, shapes, and accessible states replace the current default/dynamic token dependence where brand consistency matters.
3. Review-first library: Home and Library make unresolved/available evidence the main scanning signal without changing recording data or upload behavior.
4. Evidence-first detail: summary, source time ranges, transcript highlight, and audio playback become one progressive review flow while preserving existing `SummarySource` routing.
5. Peripheral states: Add, loading, empty, error, processing, no-audio, and Settings follow the same language without adding new features.
6. Visual QA and regression: test the existing timestamp-to-transcript path and inspect Android screen states on an emulator at phone widths.

## Decisions I made for you

| Decision | Default | Why | Reversible |
| --- | --- | --- | --- |
| Product concept | **Evidence-first listening desk** (`듣고, 훑고, 확인한다`) | Makes Skim's timestamp-grounded verification the primary experience, instead of competing as another meeting bot, transcript archive, or AI chat. | Yes |
| Visual character | Quiet editorial recording surface: warm paper/near-white background, graphite text, one high-contrast signal accent reserved for evidence and playback; no blue/purple SaaS gradient. | Market screenshots cluster around white + blue/purple AI surfaces; restrained editorial contrast supports reading and trust. | Yes |
| Home hierarchy | Replace the generic promise hero with a compact review queue and recording cards that surface `근거` count, status, duration, and one-line takeaway as scannable evidence. | The current cards already expose these facts; reordering creates differentiation without a data-model change. | Yes |
| Detail interaction | Keep the existing summary-source-to-transcript route, but present it as a progressive **Skim -> Evidence** journey: source chips open an in-context evidence focus with matching timestamp, highlight, and playback before the full transcript. | Competitors visibly separate Summary/Transcript/AI into parallel destinations; Skim's proof path should feel continuous. | Yes |
| Capture affordance | Retain audio-file import/upload as the sole Add action and name it accurately. | The repository has no microphone permission/recording flow; visual redesign must not imply a capability that does not exist. | Yes |
| Confidence UX | Do not add low-confidence/correction UI in this redesign. | It needs model confidence data and editing behavior not present in `Recording`/`TranscriptChunk`; it is outside a design-only change. | Yes |

## Must NOT have

- No competitor logo, branded color system, copied copy, screenshot tracing, or pixel-level imitation.
- No microphone recording, meeting bot, collaboration, AI chat, templates, search, share/export, or backend/data-schema expansion.
- No dynamic color as the only brand identity; system dark mode remains supported.
- No emoji icons; use Android/Material vector assets and semantic labels.
- No visual treatment that hides the existing loading, error, no-audio, upload-processing, or timestamp-navigation states.

## Proposed implementation approach after approval

Create the design contract first, then make the smallest Compose change set centered on `theme/*` and `ui/main/MainScreen.kt`. Preserve `Recording`, `SummaryItem`, `SummarySource`, `TranscriptChunk`, `SkimMainViewModel`, and the timestamp navigation contract. Prove the existing interaction with the instrumentation test plus an emulator-driven manual walkthrough, including source tap, transcript highlight, playback/no-audio edge state, and light/dark visual checks.

## Approval gate

Approve this approach to authorize only the writing and review of the executable plan. It will not authorize implementation. You can override any listed default in the same reply.
