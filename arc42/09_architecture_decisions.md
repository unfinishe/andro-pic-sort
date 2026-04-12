# 09 Architecture Decisions

## ADR-001 Native Android over Python Runtime
- Status: Accepted
- Decision: Build a native Android app in Kotlin instead of Python/Pydroid runtime.
- Rationale: Better UX, maintainability, platform integration, and distribution path.

## ADR-002 Offline-Only Processing
- Status: Accepted
- Decision: No backend dependency and no cloud sync in MVP.
- Rationale: Privacy, simplicity, reliability without network.

## ADR-003 Storage Access via Android-Safe APIs
- Status: Accepted
- Decision: Use SAF-based folder/file access patterns for user-selected folders.
- Rationale: Works with modern Android storage restrictions and user intent.

## ADR-004 Extensible Folder Schema
- Status: Accepted
- Decision: Implement folder schema via interface/strategy; default schema is `YYYY/MM MonthName`.
- Rationale: Preserve MVP behavior while enabling future schema options.

## ADR-005 Date Fallback Strategy
- Status: Accepted
- Decision: Use metadata date first, file timestamp fallback.
- Rationale: Mirrors proven prototype behavior and handles missing metadata.

## ADR-006 Test Pyramid for Android
- Status: Accepted
- Decision: Domain-heavy unit tests + targeted instrumentation + macrobenchmark.
- Rationale: Fast feedback with realistic device-path validation.

## ADR-007 Separate `:app` and `:core` Modules
- Status: Accepted
- Decision: Keep Android-specific implementation in `:app` and platform-agnostic domain logic in `:core`.
- Rationale: Improves separation of concerns, enables faster JVM tests for domain rules, and reduces coupling between UI/platform code and business logic.

