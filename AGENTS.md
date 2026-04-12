# AGENTS.md

## Purpose
This file provides minimal agent guidance. Product and implementation truth lives in `README.md`.

## Read First
- `README.md` (single source of truth for scope, roadmap, and testing policy)
- `arc42/` (architecture decisions and constraints)

## Current Project State
- Repo has migrated from a Python CLI prototype to a native Android app.
- Keep all new project communication and artifacts in English.
- Target platform and behavior constraints are defined in `README.md`.

## Agent Rules for This Repository
- Do not duplicate product requirements from `README.md` here.
- If a rule in this file and `README.md` conflicts, follow `README.md` for product scope and implementation details.
- Keep this file short; prefer links/references to:
  - `README.md#product-scope-mvp`
  - `README.md#testing-strategy-state-of-the-art`
  - `README.md#migration-plan`
- Architecture details belong in `arc42/*.md`, not in this file.

## Legacy Python Note
- Python prototype artifacts were removed.
- Historical migration status is tracked in `README.md`.
