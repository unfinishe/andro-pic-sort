# 06 Runtime View

## Runtime Scenario: Start Sort Run
1. User selects source and target folders.
2. User selects mode (`Copy` or `Move`).
3. User starts run.
4. `SortUseCase` requests recursive listing from storage adapter.
5. For each supported file:
   - Resolve date via metadata; fallback to file timestamp.
   - Build destination path from schema and locale month names.
   - Resolve conflicts (`_1`, `_2`, ...).
   - Execute copy or move.
   - Emit progress update.
6. On completion, app displays summary report.

## Runtime Scenario: Metadata Failure
1. Metadata read fails or has no valid date.
2. `DateResolver` logs/marks fallback path.
3. File timestamp is used.
4. Processing continues.

## Runtime Scenario: Partial Errors
1. Individual file operation fails (permission/path/corruption).
2. Error is counted in report and processing continues for next file.
3. Final report includes failure count.

