# Android Pic Sort (Native Android App)

## Core Function
Android Pic Sort organizes photos from a selected source folder into a selected target folder by date.
It supports `Copy` or `Move`, optional `Dry run`, and creates a `YYYY/MM MonthName` folder structure.
Optionally, it can also sort non-image files (for example videos), using file date mode automatically for those files.

## Status
- Current state: first usable Android MVP scaffold implemented.
- UX refresh applied: modernized Material 3 layout and launcher icon.
- Target platform: Android 15+.
- Runtime model: fully offline, on-device processing.
- UI language policy: app follows system locale; German if device language is German, otherwise English fallback.

## Android Development Quick Start
Prerequisites:
- Android Studio (latest stable)
- Android SDK Platform 35
- JDK 21 with `javac` available (full JDK, not JRE)

Build and test commands:
```bash
./gradlew :core:test
./gradlew :app:assembleDebug
```

If SDK/JDK are not auto-detected in your shell session:
```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew :core:test :app:assembleDebug
```

Run on device/emulator:
- Open project in Android Studio.
- Select an Android 15 emulator or device.
- Run the `app` configuration.

## One-click Run Workflow
### Android Studio (recommended)
- Open the project in Android Studio.
- Start an emulator from Device Manager.
- Select run configuration `app`.
- Click Run (green play button).

### Terminal helper script
If you prefer CLI, use the helper script from project root:

```bash
./scripts/dev-run.sh
```

Optional: pass a specific device serial if multiple devices are connected:

```bash
./scripts/dev-run.sh emulator-5554
```

Known local environment limitation:
- If only a JRE is installed, Gradle cannot compile Kotlin/JVM tests. Install a full JDK 21.

## Product Scope (MVP)
- Select a source folder on device storage.
- Select a target folder on device storage.
- Choose operation mode: `Copy` or `Move`.
- Optional `Dry run` mode for safe preview without file changes.
- Scan and process all supported image formats recursively.
- Optional toggle to include non-image files; non-images use file date mode automatically.
- Select date source mode:
  - Metadata date (fallback: file date)
  - File date only (faster)
- Organize files into `YYYY/MM MonthName` (default schema).
- Resolve filename conflicts by policy: `Rename` (for example `name_1.ext`) or `Overwrite`.
- Show a final report (processed, copied/moved, failed, skipped, planned, renamed, categorized error buckets).

## Out of Scope (MVP)
- Undo functionality.
- Saved jobs or scheduling.
- Cloud sync or online services.
- Advanced rule builder UI.

## Format Support
MVP should support Android-typical image formats at minimum:
- `jpg`, `jpeg`, `png`, `webp`, `gif`, `bmp`, `tiff`, `heic`, `heif`, `dng`

If a format cannot expose metadata consistently on a specific device, processing must fall back gracefully to file timestamp.

## Architecture Direction
- Native Android app in Kotlin.
- Material 3 UI with Android design guideline compliance.
- Clean separation:
  - Domain rules (date extraction strategy, folder schema, conflict resolution)
  - Android adapters (SAF/MediaStore access, EXIF reader integration)
  - UI + ViewModel orchestration
- Folder schema is extensible by design; default implementation remains `YYYY/MM MonthName`.

See architecture details in `arc42/`.

## Testing Strategy (State of the Art)
Use a layered Android test stack:
- Unit tests: JUnit 5 + AssertJ/Kotest for domain logic.
- Coroutine/Flow tests: `kotlinx-coroutines-test` + Turbine.
- Android JVM tests: Robolectric for storage/date edge cases not requiring device.
- UI tests: Jetpack Compose UI Test + Espresso interop where needed.
- End-to-end device tests: UI Automator for storage picker/user flows.
- Performance: Macrobenchmark + Baseline Profiles for startup and long-running sort jobs.
- Static quality gates: Detekt, ktlint, Android Lint, dependency audit in CI.

Definition of done for each feature includes:
- unit coverage for business rules,
- at least one UI/instrumentation path for critical flows,
- negative-path test for storage and metadata fallback behavior.

## Migration Plan
### Phase 0 - Foundation (current)
- [x] Confirm product scope and constraints.
- [x] Create arc42 architecture documentation.
- [x] Bootstrap Android project structure (Gradle, app module, core module).

### Phase 1 - Domain Parity MVP
- [x] Implement folder traversal and format filter.
- [x] Implement date strategy: EXIF -> filesystem fallback.
- [x] Implement schema `YYYY/MM MonthName`.
- [x] Implement conflict naming policy.
- [x] Implement copy/move engine with progress callbacks.
- [x] Implement simple summary report.

### Phase 2 - Android UX + Storage
- [x] Folder selection UX via Android Storage Access Framework.
- [x] Permissions and URI persistence flow.
- [x] Material 3 screens (setup, progress, report).
- [ ] Cancel-safe and error-resilient long-running execution.

### Phase 3 - Quality Hardening
- [ ] Complete unit/instrumentation/macrobenchmark suite.
- [ ] Baseline profile generation and startup/perf tuning.
- [ ] CI gates for lint, tests, and release checks.

## Documentation Governance
- `README.md` is the single source of truth for scope, workflows, and implementation progress.
- `AGENTS.md` must stay short and reference this README instead of duplicating details.
- Architecture documentation is maintained in `arc42/*.md`.
