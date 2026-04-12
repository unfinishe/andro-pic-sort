# 08 Crosscutting Concepts

## Localization
- UI strings are localized for English and German.
- Month names come from platform locale/date formatting APIs.
- Locale rule: if system language is German use German; otherwise fallback to English.

## Error Handling
- Per-file failure must not abort the whole run.
- User receives aggregated summary with failure count.
- Domain returns structured error categories for UI/report mapping.

## Logging and Observability
- Structured logging in debug builds.
- No sensitive path data in analytics because MVP is offline-only and has no backend telemetry.

## Testing Concept
- Unit tests for domain contracts and conflict/path/date rules.
- Flow/coroutine tests for progress and cancellation behavior.
- Robolectric tests for Android-adjacent logic on JVM.
- Instrumentation tests for storage picker, permissions, and end-to-end sorting flow.
- Macrobenchmark for startup and large-folder processing.

## Security and Privacy
- Offline-only processing.
- Least-privilege storage access via user-selected folders.
- No network permission required for MVP.

