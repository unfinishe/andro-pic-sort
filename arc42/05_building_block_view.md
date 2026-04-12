# 05 Building Block View

## Level 1
### Android App
- Presents setup/progress/report screens.
- Orchestrates sorting execution.

### Gradle Modules
- `:app` contains Android-specific code (Compose UI, Activity lifecycle, SAF/document handling, Android adapters).
- `:core` contains platform-agnostic domain logic and models (for example conflict policy, report model, format checks, folder schema helpers).
- `:app` depends on `:core`, but `:core` must not depend on `:app`.

## Level 2
### UI Layer
- Compose screens for:
  - source/target selection
  - mode selection (`Copy` or `Move`)
  - run progress
  - final report

### Presentation Layer
- ViewModels expose state via `StateFlow`.
- Converts domain results into UI model.

### Domain Layer
- `SortUseCase` for run orchestration.
- `DateResolver` strategy (metadata first, timestamp fallback).
- `FolderSchema` strategy (default: `YYYY/MM MonthName`, extensible later).
- `ConflictResolver` for suffix naming.

### Module Responsibility Mapping
- `:app` owns execution flow and platform integration (for example `AndroidSortUseCase`, folder picker wiring, progress UI).
- `:core` owns reusable domain contracts and value logic (for example `SortReport`, `ConflictResolver`, `ConflictPolicy`, `YearMonthFolderSchema`).
- Rationale: keep Android framework details out of core rules to improve testability and maintainability.

### Data/Platform Layer
- Storage adapter using Android storage APIs.
- Metadata adapter for image date extraction.
- File operation adapter for copy/move.

## Level 3: Core Contracts (MVP)
- `SortUseCase.run(config, onProgress): SortReport`
- `DateResolver.resolve(file): LocalDateTime`
- `FolderSchema.pathFor(date, locale): RelativePath`
- `FileOperator.copyOrMove(source, target, mode): Result`

