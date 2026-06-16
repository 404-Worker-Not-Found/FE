# AGENTS.md

## Purpose

This file defines how AI agents should work in this repository.

This repository is the Android (Kotlin) frontend for the urgent job matching service. The backend lives in a separate repository.

Keep this file focused on AI behavior rules.

## Document Map

- `docs/PROJECT_CONTEXT.md`: current project state and context
- `docs/UI_Specification.md`: screen-by-screen UI specification (source of truth for screens and fields)
- `docs/agent/decisions.md`: decisions the project has already made
- `docs/agent/failure-memory.md`: repeated or high-risk mistakes identified by the user
- `docs/agent/checklists.md`: verification checklists
- `docs/agent/coding-rules.md`: AI-facing frontend coding rules
- `docs/architecture/package-structure.md`: app package/feature structure
- `docs/scripts/verify.sh`: repository-wide verification entry point

## Required Reading

Before starting a code-change task, agents must read:

- `docs/agent/decisions.md`
- `docs/agent/failure-memory.md`

These two are short and high-value; read them every code-change task. Consult the following as needed instead of loading them every task:

- `docs/PROJECT_CONTEXT.md`: read once to orient when unfamiliar with the project, or when the task touches project scope, stage, or backend dependencies.
- `docs/agent/checklists.md`: open the relevant checklist when the task matches it, and run the `Documentation Update Check` before completing.

When the task implements or changes a screen, read the relevant section of `docs/UI_Specification.md` and treat it as the source of truth for screen names, fields, and behavior.

If the task affects a specific feature, inspect that feature's source code, Gradle build files, and resources before editing.

For frontend implementation work, follow `docs/agent/coding-rules.md`. Consult it when the task involves Composable structure, ViewModel/state handling, naming, or error handling, or when the rule is not already clear from the current context.

For app structure work, inspect as needed:

- `build.gradle.kts` (project and module level)
- `gradle/libs.versions.toml` (version catalog, if present)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java` (Kotlin source)
- `app/src/test` and `app/src/androidTest`

## Work Principles

- Keep changes scoped to the user's request.
- Do not modify unrelated files.
- Prefer existing project conventions over new patterns.
- Do not introduce new architecture without a clear reason.
- Do not hardcode secrets, API keys, tokens, or local-only values. Use `local.properties`, `BuildConfig`, or Gradle secrets handling.
- Do not hardcode user-facing strings, dimensions, or colors; use resources / theme where the project already does.
- Explain assumptions when project context is incomplete.
- Prefer small, reversible changes when requirements are ambiguous.
- Preserve user changes already present in the working tree.

## Parallel Work and Shared Files

Multiple developers run agents in parallel from separate branches off `develop`. Keep feature work inside its own `feature/{feature}` package so parallel changes do not overlap.

Some files are shared edit points that cause merge conflicts when two features change them at once. When a task must touch one of these, make additive, self-contained changes and do not reformat, reorder, or bump unrelated entries:

- `core/navigation`: add the feature's routes/graph entries; do not restructure existing routes.
- `di` / Hilt modules: prefer a feature-scoped module over editing a shared one.
- `gradle/libs.versions.toml` and `build.gradle.kts`: append new entries; do not reorder or change unrelated versions.
- `app/src/main/AndroidManifest.xml`: add only the permissions/components the task needs.
- Shared `strings.xml`, theme, and design-system files: append new resources; leave unrelated entries untouched.

When two tasks would edit the same shared file, prefer splitting the work (for example, a per-feature navigation graph or a feature-scoped DI module) over serializing on one file. When appending to a shared agent document (`docs/agent/decisions.md`, `docs/agent/failure-memory.md`), add new entries at the end of the file to minimize conflicts.

## Decision Priority

When `docs/PROJECT_CONTEXT.md` and `docs/agent/decisions.md` conflict:

- Follow `docs/agent/decisions.md`.
- Report the conflict.

When `docs/UI_Specification.md` and the current implementation conflict on screen behavior, report the conflict instead of silently choosing one. The UI specification is the design source of truth; the backend contract may constrain what is achievable.

## Verification

Use `./docs/scripts/verify.sh` as the repository-wide verification command.

During iterative work, a smaller Gradle command may be used first. Before completing work, run repository-wide verification unless the user explicitly asks not to or the current task is documentation-only.

If verification cannot be run or fails, report:

- the command that was run
- the failing module or step
- the likely cause, if known
- whether the failure is related to the current task

## Backend Contract Boundary

This repository does not own the API. When a task depends on backend behavior:

- Do not invent endpoint paths, request/response shapes, or error codes. Confirm them against the backend contract or ask.
- Model network responses as DTOs in the `data` layer and map them to domain models; do not let raw DTOs reach the UI.
- Assume Access Token + Refresh Token authentication, consistent with the backend (see `docs/agent/decisions.md`).
- When a contract is unknown, state the assumption and keep the dependency isolated behind a repository interface.

## Agent Document Updates

Follow the `Documentation Update Check` in `docs/agent/checklists.md`.

When the user identifies a repeated mistake, propose an entry for `docs/agent/failure-memory.md`.

Only add or update `docs/agent/failure-memory.md` after the user asks for the entry to be recorded or clearly approves the proposed entry.

At the end of a task, briefly report whether any agent document update is needed.
If none is needed, say: "No agent document update needed."

## Completion Routine

Before reporting completion:

- Confirm relevant files were inspected.
- Confirm changes are scoped to the request.
- Run required verification, or report why it was not run.
- Confirm no secrets were added.
- Check whether `docs/PROJECT_CONTEXT.md`, `docs/agent/decisions.md`, `docs/agent/failure-memory.md`, `docs/agent/checklists.md`, or `docs/agent/coding-rules.md` needs an update.
