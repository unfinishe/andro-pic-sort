# 03 Context and Scope

## Business Context
The app replaces a Python/Pydroid-based workflow with a native Android user experience.

Input:
- User-selected source folder containing unsorted photos.

Output:
- Target folder with organized structure `YYYY/MM MonthName`.
- Simple run report (counts for processed, copied/moved, failed, skipped).

## Technical Context
External systems and boundaries:
- Android OS storage framework for folder/file access.
- Local filesystem/media metadata only (no network integration).
- Device locale service for language-dependent month naming.

## Scope Boundary
In scope:
- Folder selection, sorting engine, progress/report UI, localization behavior.

Out of scope (MVP):
- Remote/cloud systems.
- Undo history.
- Persisted schedules or saved sorting profiles.

