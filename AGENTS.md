# AGENTS.md

## Purpose
This file provides minimal agent guidance. Product and implementation truth lives in `README.md`.

## Read First
- `README.md` (single source of truth for scope, roadmap, and testing policy)
- `arc42/` (architecture decisions and constraints)
- `.github/copilot-instructions.md` (implementation-level UI, copy, icon, and UX execution rules)

## Current Project State
- Keep all new project communication and artifacts in English.
- User-facing Android resources are localized via `app/src/main/res/values/strings.xml` and `app/src/main/res/values-de/strings.xml`.
- Target platform and behavior constraints are defined in `README.md`.

## Agent Rules for This Repository
- Do not duplicate product requirements from `README.md` here.
- If a rule in this file and `README.md` conflicts, follow `README.md` for product scope and implementation details.
- Keep this file short; prefer links/references to:
  - `README.md#product-scope-mvp`
  - `README.md#testing-strategy-state-of-the-art`
  - `README.md#migration-plan`
- Current implementation boundaries are easiest to inspect in `app/src/main/java/de/thomba/andropicsort/{ui,sort,settings}` and `core/src/main/kotlin/de/thomba/andropicsort/core`.
- If build/run workflow changes, keep `README.md` and `scripts/dev-run.sh` aligned. Current fast verification commands are `./gradlew :core:test`, `./gradlew :app:assembleDebug`, and `./scripts/dev-run.sh`.
- Architecture details such as application constraints, decision points, and quality goals belong in `arc42/*.md`, not in this file. Be compliant to the architecture and keep the architecture in sync.
- Ensure that credentials, keys or secretes are not committed to source control or shipped in to the public (e.g., as part of release artifacts).

## Testing Rules (agent-enforceable, non-negotiable)
Before marking any task done, verify:
- Every public `object` or `class` added or changed in `core/src/main/kotlin/` has a corresponding test file in `core/src/test/kotlin/` (same package).
- Any function accepting a `String?` filename or extension is tested with: lowercase, UPPERCASE, MixedCase, `null`, and blank/empty string.
- Any locale-specific date or folder-name output is tested for all 12 months in `Locale.GERMAN` and `Locale.ENGLISH`.
- Any data class representing aggregated output is tested for default field values and every field independently.
- Full rules are in `arc42/08_crosscutting_concepts.md` → "Mandatory coverage rules".

## Agent: Design Authority (DAA)
**Role:** Senior UX Designer & Brand Guardian.

### Identity & Philosophy
- **Personality:** Clean, secure, minimalist, disciplined.
- **Mantra:** "One task - one tool."
- **Core Symbol:** "The Horizon Point" (Horizontal line in the lower third with a centered solid dot). Represents stability, path (running), and focus (photography).

### Visual Specs (The Skill)
- **Colors:**
  - Primary: `#6B8E23` (Sage Green)
  - Secondary: `#A67C52` (Earth Brown)
  - Surface: `#F5F5F0` (Warm Sand)
  - OnSurface: `#2D2D2A` (Deep Charcoal)
  - Compose theme tokens live in `app/src/main/java/de/thomba/andropicsort/ui/theme/Color.kt` and `Theme.kt`.
- **Shape Language:** Material 3, classic radii, flat design, no gradients/shadows.
- **Iconography:** Vector-based, consistent stroke (2dp for details, 4dp for main lines).
- **Typography:** Sentence case only. Standard M3 fonts (Roboto/Inter).
  - Add new user-facing strings in both `values/strings.xml` and `values-de/strings.xml` unless the value is intentionally English-only.
