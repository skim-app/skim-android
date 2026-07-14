---
slug: skim-evidence-first-implementation
status: drafting
intent: unclear
pending-action: write .omo/plans/skim-evidence-first-implementation.md
approach: <fill: the approach you intend to plan>
---

# Draft: skim-evidence-first-implementation

## Components (topology ledger)
<!-- Lock the SHAPE before depth. One row per top-level component that can succeed or fail independently. -->
<!-- id | outcome (one line) | status: active|deferred | evidence path -->
| D1 | Root design contract makes Evidence-first listening desk tokens, motion, states, and accessibility executable. | active | DESIGN.md |
| D2 | Deterministic Material 3 theme and vector navigation remove the generic purple/default and text-symbol UI. | active | app/src/main/java/com/example/skim/theme/*; app/build.gradle.kts |
| D3 | Home and Library become a compact evidence-review queue without changing records, IDs, or upload flow. | active | app/src/main/java/com/example/skim/ui/main/MainScreen.kt |
| D4 | Detail makes the existing source-to-transcript-to-playback route feel continuous and keeps no-audio behavior. | active | app/src/main/java/com/example/skim/ui/main/MainScreen.kt; MainScreenTest.kt |
| D5 | Add, Settings, loading, empty, refresh error, and playback error use the same system and remain usable. | active | app/src/main/java/com/example/skim/ui/main/MainScreen.kt |
| D6 | Instrumentation and emulator evidence prove the preserved route and accessibility-visible controls. | active | app/src/androidTest/java/com/example/skim/ui/main/MainScreenTest.kt; .omo/evidence/ |

## Open assumptions (announced defaults)
<!-- Intent is UNCLEAR: research resolves ambiguity, defaults are adopted (not asked), and each is surfaced in the plan's human TL;DR for veto. -->
<!-- assumption | adopted default | rationale | reversible? -->
| Visual language | Warm paper, graphite, deep moss primary, and burnt-orange evidence signal; no blue/purple SaaS gradient. | Distinct from the reviewed market screens and suited to reading/listening trust. | Yes |
| Theme policy | `SkimTheme` defaults to deterministic brand tokens while retaining the explicit dark-theme branch. | Dynamic system colors erase Skim's product identity. | Yes |
| Information hierarchy | Home shows a review queue and evidence count before generic AI messaging; Library keeps all recordings. | Reuses current data without a schema/API change. | Yes |
| Detail navigation | Keep the two sibling destinations but label/present them as Skim and Evidence; a source action still changes to evidence, highlights, scrolls, and seeks. | Preserves existing testable behavior while differentiating interaction. | Yes |
| Icon source | Add official Compose Material Icons Extended under the existing Compose BOM; do not use Unicode text symbols. | No current vector-icon dependency or vector resources exist. | Yes |

## Findings (cited - path:lines)

- Product promise and required source contract: README.md:1-15; app/src/main/java/com/example/skim/model/SkimModels.kt:3-35.
- Preserve source click / tab switch / seek and transcript highlight: app/src/main/java/com/example/skim/ui/main/MainScreen.kt:450-464, 539-617.
- Preserve Home three-item versus Library all-item behavior and Add upload route: app/src/main/java/com/example/skim/ui/main/MainScreen.kt:81-92, 242-275.
- Current token limitation: app/src/main/java/com/example/skim/theme/Theme.kt:13-49; Color.kt:5-11; Type.kt:10-36.
- Existing test guard: app/src/androidTest/java/com/example/skim/ui/main/MainScreenTest.kt:53-72.
- Android accessibility baseline: https://developer.android.com/design/ui/mobile/guides/foundations/accessibility ; https://developer.android.com/develop/ui/compose/accessibility/api-defaults ; https://developer.android.com/develop/ui/compose/components/tabs .

## Decisions (with rationale)

- Use an editorial listening surface, not a copied competitor template: make evidence counts, timestamps, source chips, and selected-evidence state the focal vocabulary.
- Do not add confidence/correction UI because the current `Recording`/`TranscriptChunk` model has no confidence or edit state.
- Keep source behavior in one existing `MainScreen.kt` flow; do not introduce a screen-navigation layer or new domain abstraction for a design change.

## Scope IN

- Root `DESIGN.md`, Compose theme tokens and typography, Material vector navigation icons, the full `MainScreen` visual hierarchy, Compose semantics/tests, and emulator QA.

## Scope OUT (Must NOT have)

- Microphone recording, meeting bot, collaboration, AI chat, templates, search, export/share, backend/API/Room schema changes, model-confidence data, and competitor asset/copy replication.

## Open questions

- None. The defaults above are reversible and the approved design brief plus verified code resolves the implementation path.

## Approval gate
status: approved-by-user-via-ulw-loop
<!-- When exploration is exhausted and unknowns are answered, set status: awaiting-approval. -->
<!-- That durable record is the loop guard: on a later turn read it and resume at the gate instead of re-running exploration. -->
