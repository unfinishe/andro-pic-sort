# 01 Introduction and Goals

## Requirements Overview
Android Pic Sort is an offline Android app (Android 15+) that organizes photos from a selected source folder into a selected target folder.

MVP goals:
- Allow user-selected source and target folders.
- Support both `Copy` and `Move` mode.
- Organize by default schema: `YYYY/MM MonthName`.
- Derive date from metadata first, fallback to file timestamp.
- Show a simple result report.

## Quality Goals
1. **Reliability:** No data loss in normal operation; robust fallback behavior.
2. **Usability:** Minimal setup, clear progress, clear result report.
3. **Performance:** Reasonable throughput on large camera folders.
4. **Maintainability:** Clear modular architecture and testable domain rules.

## Stakeholders
- End user: wants easy, safe photo organization on device.
- Maintainer/developer: needs clean architecture, testability, and clear docs.

