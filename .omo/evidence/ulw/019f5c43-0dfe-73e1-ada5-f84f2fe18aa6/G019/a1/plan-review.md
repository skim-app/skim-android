# Independent final plan re-review

## BLOCKERS

1. **The device-QA commands still violate the plan’s own no-placeholder/selected-device rule.** The fixture section requires `SERIAL` for every device command and forbids unqualified `adb` (`.omo/plans/skim-evidence-first-implementation.md:61-62`), but Todo 2 uses unqualified `adb shell` and an unresolved `<light|dark>` filename (`:112`), Todo 4 uses unqualified `adb shell input tap <coordinates ...>` (`:128`), and Todo 8 leaves `<route>` in both capture paths (`:160`). Replace these with concrete, repeated light/dark and named-route invocations, and use `adb -s "$SERIAL"` plus an `ANDROID_SERIAL="$SERIAL"`-scoped screen-capture command (or an equivalent verified selected-device mechanism). Reuse the exact layout-JSON-to-coordinate command pattern already specified in Todo 5 (`:136`) for the Library tap.

2. **The fixture cannot satisfy the required Home-count QA.** The fixture defines only `evidence-audio` and `without-audio` (`:58-60`) and Todo 8 requires the list endpoint to return exactly those two IDs (`:159`), while Todo 4 requires Home to show exactly three recording entries (`:128`). Add a third deterministic fixture record or change the acceptance criterion to the supported two-entry fixture. As written, a correct implementation cannot meet both criteria.
