# 08 Crosscutting Concepts

## Localization
- UI strings are localized for English and German.
- Month names come from platform locale/date formatting APIs.
- Locale rule: if system language is German use German; otherwise fallback to English.

## Error Handling
- Per-file failure must not abort the whole run.
- User receives aggregated summary with failure count.
- Domain returns structured error categories for UI/report mapping.

## Timestamp Preservation
- After every copy or move the app uses the following two-strategy approach per file:
- **Primary strategy (OS copy)**: both source and target real on-disk paths are resolved via `Os.readlink("/proc/self/fd/{fd}")`. Then `java.nio.file.Files.copy(src, dst, COPY_ATTRIBUTES, REPLACE_EXISTING)` is called. The OS preserves mtime, atime, and any other attributes the underlying filesystem supports — no manual step needed. For MOVE mode the source is deleted with `Files.delete()` afterwards.
- **Fallback strategy (stream copy)**: if real paths cannot be resolved (e.g. virtual or OTG providers), the file is copied via `InputStream → OutputStream`. A subsequent `File.setLastModified()` call attempts to at least restore mtime.
- **Creation time (btime) cannot be preserved** — the Linux kernel provides no syscall to write a file's birth/creation time. Neither `utimensat` nor any `android.system.Os` method exposes this. This is a hard kernel constraint, not an app limitation.
- **EXIF capture dates are fully preserved**: `DateTimeOriginal` and similar EXIF tags live inside the file content and are copied verbatim regardless of strategy.
- `SortReport` exposes `osCopyUsed`, `streamFallbackUsed`, `timestampPreserved`, and `timestampFailed` so the user receives explicit per-strategy feedback in the report.

## Logging and Observability
- Structured logging in debug builds.
- No sensitive path data in analytics because MVP is offline-only and has no backend telemetry.

## Testing Concept

### Test layer targets
- Unit tests for domain contracts and conflict/path/date rules.
- Flow/coroutine tests for progress and cancellation behavior.
- Robolectric tests for Android-adjacent logic on JVM.
- Instrumentation tests for storage picker, permissions, and end-to-end sorting flow.
- Macrobenchmark for startup and large-folder processing.

### Mandatory coverage rules (agent-enforceable)
These rules apply immediately to any new or modified code. AI agents and contributors must follow them before marking a task done.

**`:core` domain classes**
- Every public `object` or `class` in `core/src/main/kotlin/` must have a corresponding test file in `core/src/test/kotlin/` with the same package.
- Missing test files for existing domain classes are treated as bugs.

**Input validation paths**
- Any function that accepts a `String?` filename or extension must be tested with: lowercase, UPPERCASE, MixedCase, `null`, and blank/empty string.
- This applies to `SupportedImageFormats.isSupported`, `InputFilePolicy.shouldInclude`, `InputFilePolicy.effectiveDateSourceMode`, and any future equivalent.

**Date and locale formatting**
- Any function producing locale-specific date or month output must be tested for all 12 months in every supported locale (currently `Locale.GERMAN` and `Locale.ENGLISH`).
- Month folder names must be verified for zero-padded numbering (`01`–`12`) and capitalised names.

**Report and counter types**
- Any data class representing aggregated output (e.g. `SortReport`) must be tested for: default values, every field independently, and any flag semantics (e.g. `dryRun`).

**Negative and boundary paths**
- Every business rule must have at least one explicit negative-path test (invalid input, boundary condition, or expected rejection).

### ViewModel testing pattern
- `MainViewModel` accepts an `ioDispatcher: CoroutineDispatcher = Dispatchers.IO` parameter.
- In tests, pass `StandardTestDispatcher()` as `ioDispatcher` (and set it as `Dispatchers.Main` via `Dispatchers.setMain`). This puts all ViewModel coroutines under the test scheduler, making `advanceUntilIdle()` deterministic.
- Use `CompletableDeferred<Unit>` as a blocker in fakes when a test needs the sort to remain in-progress (`isRunning = true`) across multiple assertions.
- Use a fresh fake storage instance per ViewModel created inside a test to avoid cross-contamination from the `@Before` ViewModel.
- Robolectric SDK level must match `targetSdk` in `app/build.gradle.kts`; use `@Config(sdk = [35])` and Robolectric ≥ 4.14.

## Security and Privacy
- Offline-only processing.
- Least-privilege storage access via user-selected folders.
- No network permission required for MVP.

