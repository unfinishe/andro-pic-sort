# 02 Architecture Constraints

## Technical Constraints
- Platform: Android 15+.
- Runtime: fully offline, no backend dependency.
- Language: Kotlin.
- UI: Material 3 and Android design guideline compliant.
- Storage access: Android-safe APIs (SAF/DocumentFile and related platform APIs).

## Product Constraints
- MVP must provide `Copy` and `Move`.
- No undo and no saved job presets in MVP.
- Default folder schema is fixed to `YYYY/MM MonthName` for MVP.
- Locale behavior: German system locale -> German month names; otherwise English.

## Documentation Constraints
- `README.md` is the single source of truth for scope/roadmap/testing policy.
- `AGENTS.md` must reference `README.md` and avoid duplication.
- Architecture details live in `arc42/*.md`.

