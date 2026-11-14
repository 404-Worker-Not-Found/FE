# Decision Memory

This file records decisions the project has already made.

Decisions may be product, architecture, technology, repository, verification, or agent-operation decisions. Do not use this file for current-state summaries, repeated mistake reports, or task checklists.

Use this format:

```md
## YYYY-MM-DD - Short Title

Decision:
- What was decided.

Reason:
- Why this decision was made.

Implication for agents:
- What future agents should do or avoid because of this decision.

Related files:
- Optional file paths.
```

## 2026-06-16 - Frontend Repository Scope

Decision:
- This repository contains only the Android (Kotlin) frontend client.
- The backend lives in a separate repository and owns all data and APIs.

Reason:
- Frontend and backend are developed and released independently.

Implication for agents:
- Do not add backend service code to this repository.
- Do not invent backend endpoints, payloads, or error codes; confirm against the backend contract or ask.
- Treat the app as a presentation client over backend-owned data.

Related files:
- `docs/PROJECT_CONTEXT.md`

## 2026-06-16 - Technology Baseline

Decision:
- Use Kotlin.
- Use Android with Jetpack Compose for UI.
- Use MVVM as the app architecture.
- Use Hilt for dependency injection.
- Use Retrofit + OkHttp for networking.
- Use Coroutines and Flow for async work and UI state.
- Use Jetpack Navigation (Compose) for navigation.
- Use Coil for image loading.

Reason:
- This is the current, well-supported Android stack and keeps the team aligned on one set of patterns.

Implication for agents:
- Prefer these libraries and patterns for new code.
- Do not introduce a competing library (for example, a different DI or networking framework) without justification and a recorded decision.
- Manage versions through the Gradle version catalog when present.
- If a stack change is proposed, explain the migration reason and expected impact.

Related files:
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `docs/PROJECT_CONTEXT.md`

## 2026-06-16 - App Architecture and Layering

Decision:
- Use MVVM with a clear separation of UI, ViewModel, domain, and data layers.
- UI (Composables) observes immutable UI state exposed by the ViewModel.
- ViewModels expose state through `StateFlow` and receive user intent through functions.
- Domain layer holds business/use-case logic and domain models.
- Data layer holds repositories, remote (Retrofit) data sources, and local data sources.
- Repositories are exposed to the domain/ViewModel layer through interfaces.
- Network DTOs stay in the data layer and are mapped to domain models; raw DTOs do not reach the UI.

Reason:
- A consistent layering keeps screens testable, keeps network details out of the UI, and makes changes easier to review.

Implication for agents:
- Put business logic in the ViewModel/domain layer, not in Composables.
- Do not call Retrofit services directly from a Composable or ViewModel; go through a repository.
- Keep Composables free of Android framework side effects beyond what Compose provides; collect state with lifecycle-aware APIs.

Related files:
- `docs/architecture/package-structure.md`

## 2026-06-16 - Package Structure

Decision:
- Use the package structure defined in `docs/architecture/package-structure.md`.
- Organize code by feature under `feature/{feature}` with `ui`, `viewmodel` (or state), `domain`, and `data` responsibilities.
- Use `core` for shared infrastructure (network, design system, common UI, navigation, util).
- Do not create empty packages or classes before they are needed.

Reason:
- Feature-first packaging keeps related screen code together and maps naturally to the UI specification.

Implication for agents:
- Read `docs/architecture/package-structure.md` before creating or changing package structure.
- Apply the documented structure to new code unless the user explicitly approves a different structure.
- If a different structure is needed, explain the reason and record the decision.

Related files:
- `docs/architecture/package-structure.md`
- `AGENTS.md`
- `docs/PROJECT_CONTEXT.md`

## 2026-06-16 - Authentication Strategy

Decision:
- Use Access Token + Refresh Token authentication, consistent with the backend.
- Store tokens securely on device.
- Refresh the access token transparently when it expires, then retry the original request.

Reason:
- The backend issues JWT access and refresh tokens; the client must store and renew them without exposing them.

Implication for agents:
- Attach the access token through an OkHttp interceptor, not per-call boilerplate.
- Handle refresh and re-auth in one place (an authenticator/interceptor or a token repository).
- Do not log tokens, authorization headers, passwords, or secrets.
- Do not store tokens in plain `SharedPreferences`; use encrypted storage or DataStore with appropriate protection.
- Auto-login (UI spec 1-1) relies on a valid stored session.

Related files:
- `docs/PROJECT_CONTEXT.md`

## 2026-06-16 - UI Specification as Source of Truth

Decision:
- `docs/UI_Specification.md` is the source of truth for screens, fields, and screen behavior.

Reason:
- A single screen definition keeps both AI agents and human teammates building the same screens.

Implication for agents:
- Before implementing or changing a screen, read the relevant section of the UI specification.
- If the implementation must diverge from the specification, report the conflict instead of silently changing behavior.
- Keep validation rules (for example, password 8~20 with letters/numbers/special characters) consistent with the specification.

Related files:
- `docs/UI_Specification.md`

## 2026-06-16 - Resource and Localization Conventions

Decision:
- Keep user-facing text in string resources.
- Keep colors, typography, and dimensions in the Compose theme / design system, not hardcoded in screens.
- The product language is Korean; default strings are Korean.

Reason:
- Centralized resources keep UI consistent and make future changes safe.

Implication for agents:
- Do not hardcode display strings, colors, or dimensions in Composables when a resource or theme value exists.
- Add new shared UI values to the design system rather than duplicating literals.

Related files:
- `docs/architecture/package-structure.md`

## 2026-06-16 - Repository Verification Entry Point

Decision:
- Use `./docs/scripts/verify.sh` as the repository-wide verification command.

Reason:
- A single verification entry point lets humans, agents, and future CI run the same check.

Implication for agents:
- Run `./docs/scripts/verify.sh` before completing non-documentation tasks unless the user explicitly asks not to.
- Add new checks (lint, unit tests, instrumented test smoke) to `docs/scripts/verify.sh` as the project grows.
- If verification fails, report the failing step and whether it appears related to the current task.

Related files:
- `docs/scripts/verify.sh`
- `docs/agent/checklists.md`
- `AGENTS.md`

## 2026-06-16 - Failure Memory Requires User Identification

Decision:
- Do not let agents independently decide that a mistake should be recorded in `docs/agent/failure-memory.md`.
- When the user identifies a repeated mistake, propose an entry and record it only after user approval.

Reason:
- Agents often cannot reliably judge whether their own behavior is a recurring mistake. User confirmation prevents incorrect or noisy rules from accumulating.

Implication for agents:
- Propose failure-memory entries only when the user identifies a repeated mistake.
- Do not update `docs/agent/failure-memory.md` without explicit user request or clear approval.

Related files:
- `docs/agent/failure-memory.md`
- `AGENTS.md`

## 2026-06-16 - Agent Document Responsibilities

Decision:
- `AGENTS.md` defines AI behavior rules.
- `docs/PROJECT_CONTEXT.md` describes the current project state.
- `docs/agent/decisions.md` records decisions.
- `docs/agent/failure-memory.md` records repeated mistakes identified by the user.
- `docs/agent/checklists.md` records verification checklists.
- `docs/agent/coding-rules.md` records AI-facing frontend coding rules.
- `docs/architecture/package-structure.md` records app structure.

Reason:
- Each document has a different purpose and update cadence.

Implication for agents:
- Put new information in the document matching its purpose.
- Do not duplicate the same rule across multiple documents unless a short reference is needed.

Related files:
- `AGENTS.md`
- `docs/PROJECT_CONTEXT.md`
- `docs/agent/decisions.md`
- `docs/agent/failure-memory.md`
- `docs/agent/checklists.md`
- `docs/agent/coding-rules.md`

## 2026-06-16 - Decision Priority

Decision:
- When `docs/PROJECT_CONTEXT.md` and `docs/agent/decisions.md` conflict, follow `docs/agent/decisions.md` and report the conflict.

Reason:
- `docs/PROJECT_CONTEXT.md` describes current state and may become stale. `docs/agent/decisions.md` records what has been decided.

Implication for agents:
- Treat `docs/agent/decisions.md` as the higher-priority source for decisions.
- Do not silently resolve conflicts.

Related files:
- `docs/PROJECT_CONTEXT.md`
- `docs/agent/decisions.md`

## 2026-06-16 - Branch Strategy

Decision:
- Use Git Flow for collaboration.
- Use `develop` as the integration branch for development work.
- Create work branches from `develop`.
- Match the branch type to the work type.
- Include the issue number in the branch name using `{type}/#{issue-number}-{short-description}`, such as `feature/#2-login-screen`.

Reason:
- A consistent branch flow keeps work isolated and makes integration testing through `develop` predictable.

Implication for agents:
- Do not work directly on `develop` unless explicitly requested.
- For a new unit of work, follow this flow: create issue, create issue-number branch from `develop`, work locally, commit and push, then open a PR to `develop`.
- Use branch types such as `feature/*`, `fix/*`, `docs/*`, `refactor/*`, `test/*`, `build/*`, `ci/*`, `chore/*`, or `environment/*` according to the work type.
- For chore tasks, use `chore/*` branches unless the user explicitly says otherwise.

Related files:
- `docs/agent/checklists.md`

## 2026-06-16 - Commit Convention

Decision:
- Use `type: 작업 내용` commit messages.
- Common types are `feat`, `fix`, `docs`, and `refactor`.
- Additional allowed types are `test`, `build`, `ci`, `chore`, and `environment`.

Reason:
- A consistent commit convention keeps history readable and makes review easier.

Implication for agents:
- Use commit messages like `feat: 로그인 화면`.
- Use `docs` for documentation-only changes.
- Pick the most specific type for the change.

Related files:
- `docs/agent/checklists.md`

## 2026-06-16 - Issue Convention

Decision:
- Use issue titles in the format `[type] title`, such as `[chore] 스타일 시스템 추가`.
- Use the repository issue template for issue content.
- Write issue titles and bodies in Korean.

Reason:
- Consistent issue titles and content make planned work easier to scan and track.

Implication for agents:
- Match the issue type to the work type, such as `[feature]`, `[fix]`, `[docs]`, `[refactor]`, `[test]`, `[build]`, `[ci]`, `[chore]`, or `[environment]`.
- When creating or proposing issues, include issue type, feature description, task checklist, and reference links when available.
- Follow the issue template instead of inventing a new format.
- Use Korean for issue titles and body content unless the user explicitly requests another language.

Related files:
- `.github/ISSUE_TEMPLATE/task.md`
- `docs/agent/checklists.md`

## 2026-06-16 - Pull Request Convention

Decision:
- Use PR titles in the format `[type] PR 제목`, such as `[feat] 로그인 화면`.
- Target `develop` for development PRs.
- Assign the PR author as the assignee in PR metadata.
- Use the repository PR template for PR content.
- Write PR titles and bodies in Korean.
- In the related issue section, reference issue numbers without auto-closing keywords such as `Close`, `Fixes`, or `Resolves`.
- Do not put command-specific AI verification notes, such as `./gradlew test passed`, in issue or PR bodies.
- Generic checklist text such as `테스트를 통과했나요?` is allowed in PR templates.

Reason:
- Consistent PR metadata makes review ownership and change intent clear.

Implication for agents:
- When creating or proposing PRs, use the `[type] title` format.
- Set the PR assignee to the PR author through PR metadata when tool access allows it.
- Include related issue, work purpose, work contents, optional screenshots, and checklist.
- Use plain issue references such as `- #2` instead of `Close #2` in PR bodies unless the user explicitly asks to auto-close the issue.
- Report command-specific verification results in the agent's final response, not as AI-flavored prose in issue or PR templates.
- Use Korean for PR titles and body content unless the user explicitly requests another language.

Related files:
- `.github/pull_request_template.md`
- `docs/agent/checklists.md`

## 2026-06-16 - Review and Merge Strategy

Decision:
- One teammate reviews each PR.
- After approval or after requested changes are addressed, the PR author merges the PR.
- Use squash-and-merge.
- Delete the branch after the PR is merged.
- For a new unit of work, recreate a fresh branch from the appropriate base branch.

Reason:
- This keeps review responsibility clear and keeps branch history tidy.

Implication for agents:
- Do not merge without review approval unless explicitly instructed.
- Prefer squash-and-merge when completing PRs.
- After merge, expect the work branch to be deleted before starting new work.

Related files:
- `docs/agent/checklists.md`

## 2026-06-16 - Application ID and Root Package

Decision:
- Use `com.workernotfound.app` as the Kotlin root package and Gradle `applicationId`.
- This aligns with the backend organization namespace `com.workernotfound`.

Reason:
- Parallel agents must scaffold against the same package and `applicationId`. If they diverge, generated code, imports, and the manifest conflict on the first merge.

Implication for agents:
- Use `com.workernotfound.app` when scaffolding the app module, packages, and `applicationId`.
- If a different identifier is required, report it and record a new decision before scaffolding.

Related files:
- `docs/architecture/package-structure.md`
- `build.gradle.kts`

## 2026-06-16 - SDK Levels Must Be Decided Before Scaffolding

Decision:
- `minSdk`, `targetSdk`, and `compileSdk` are not yet chosen.
- These values must be decided and recorded in this file before the app module is scaffolded.

Reason:
- If parallel agents each pick their own SDK levels, the version catalog and `build.gradle.kts` conflict immediately and runtime behavior diverges across branches.

Implication for agents:
- Do not invent `minSdk` / `targetSdk` / `compileSdk` values. Ask the team for the target and record the decision here before generating Gradle configuration.
- Until decided, treat any task that needs these values as blocked on this decision.

Related files:
- `docs/PROJECT_CONTEXT.md`
- `gradle/libs.versions.toml`
- `build.gradle.kts`

## 2026-06-16 - Visual Design System Source of Truth

Decision:
- `docs/agent/style_guide.md` is the source of truth for the visual layer: colors, typography, shape/radius, elevation vs. border, spacing, and component styling (tab bar, badges, chips, toggles, custom map view).
- The app is light-mode only. Do not implement dark-mode variants or `values-night` resources.
- The two owner/worker roles are color-coded: owner uses orange (`#FF7E36`), worker uses yellow (`#FFB703` / emphasis `#CC8F00`). Urgency is signaled with the red accent.

Reason:
- `docs/UI_Specification.md` defines screens, fields, and behavior but not the visual style. A single style source keeps screens visually consistent across parallel feature work and agents.

Implication for agents:
- When implementing or changing a screen, apply `docs/agent/style_guide.md` values through the Compose theme / design system in `core` (see Resource and Localization Conventions), not as hardcoded literals in screens.
- `docs/UI_Specification.md` wins on what a screen contains and does; `docs/agent/style_guide.md` wins on how it looks. If they conflict, report it instead of silently choosing.
- Do not add dark-mode color sets.

Related files:
- `docs/agent/style_guide.md`
- `docs/UI_Specification.md`
- `docs/architecture/package-structure.md`

## 2026-06-16 - SDK Levels Decided

Decision:
- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` (Android 8.0).
- This supersedes the open question in "SDK Levels Must Be Decided Before Scaffolding"; SDK levels are now decided and scaffolding is unblocked.

Reason:
- minSdk 26 gives stable foreground-location / GPS behavior, broad device coverage (~95%+), and avoids legacy permission edge cases. compile/target 35 is the current Android baseline.

Implication for agents:
- Use these exact values in `gradle/libs.versions.toml` and `build.gradle.kts` when scaffolding.
- If a change is needed, record a new decision before editing Gradle config.

Related files:
- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `docs/PROJECT_CONTEXT.md`

## 2026-06-16 - Map SDK: Naver Map

Decision:
- Use the Naver Map SDK for map views (worker job-search map, owner live-location, pins).
- This resolves the "Which map SDK" open question.

Reason:
- Domestic (Korea) service; Naver Map has good local coverage and straightforward key issuance.

Implication for agents:
- The Naver Cloud Platform Maps client ID is a secret-style value: read it from `local.properties` / `BuildConfig` (or a manifest meta-data placeholder fed by Gradle), never hardcode or commit it.
- Style guide's "custom map view" colors apply to non-Naver map placeholders/mini-maps; the search map uses the Naver SDK.

Related files:
- `docs/agent/style_guide.md`
- `local.properties`
- `app/src/main/AndroidManifest.xml`

## 2026-11-01 - Backend Contract Priority During Integration

Decision:
- Integrate the existing Android screens with the deployed backend before implementing APIs for screens that do not exist yet.
- When screen behavior or API fields conflict with the confirmed backend contract, adapt the frontend to the backend and record the difference. This supersedes the earlier UI-specification priority for backend integration conflicts.
- The backend reference repository is `https://github.com/404-Worker-Not-Found/BE.git`; confirm contracts against the deployed OpenAPI documents and backend implementation. Report disagreements between those sources rather than inventing behavior.
- Keep UI-only implementation work on its own branch, separate from API integration.

Reason:
- The user explicitly chose backend compatibility as the integration priority and requested separate API and UI work.

Implication for agents:
- Inspect existing screens first and distinguish implemented APIs, missing frontend screens, missing backend support, and unverified integration flows.
- Do not fabricate server fields, successful mutations, account credentials, or unsupported endpoint paths.
- Request backend-provided test accounts and required business/payment setup for authenticated end-to-end verification.
- Record any screen-specification differences in the integration status documentation. Preserve the existing visual design system.

Related files:
- `docs/UI_Specification.md`
- `docs/PROJECT_CONTEXT.md`
- `docs/backend-integration.md`

## 2026-11-09 - Owner Posting and Navigation UX

Decision:
- 공고관리 opens current postings by default and provides a separate 지난 공고 tab. Payment-pending/funding-blocked cards open their own publication/payment state without asking the owner to enter an ID.
- New postings reuse the registered business address and coordinates. The form groups store, schedule, work, and wage information; date/time fields use pickers rather than duplicate text inputs.
- Keep one back affordance in the top bar. Chat is accessed from the bottom navigation; remove the duplicate owner my-page chat action.

Reason:
- The user identified missing current listings, repeated entry of stored information, internal ID inputs, and duplicated navigation as inconsistent owner UX.

Implication for agents:
- Preserve required backend fields behind understandable labels. Do not expose internal IDs or raw status codes as ordinary user inputs or primary progress messages.
- Verify creation → list → detail and pending-payment recovery, not just individual screen rendering.

Related files:
- `docs/UI_Specification.md`
- `app/src/main/java/com/workernotfound/app/feature/owner/ui/OwnerPostingManagementScreen.kt`
- `app/src/main/java/com/workernotfound/app/feature/job/ui/JobPublicationScreen.kt`

## 2026-11-09 - Single Server-Connected Build

Decision:
- The app has one build, connected to the deployed backend. There is no build flag or local fixture data that replaces server responses.
- Features in `docs/UI_Specification.md` without a confirmed backend contract keep their UI and report an unavailable state through `BackendAccess.unsupported()`.

Reason:
- The user chose to keep only the server-connected app.

Implication for agents:
- Do not add fixture data sources, data-source switching flags or simulated success. Isolate a missing contract behind the repository and return an unavailable error.
- Verify with `./docs/scripts/verify.sh`.

Related files:
- `app/build.gradle.kts`
- `app/src/main/java/com/workernotfound/app/core/network/BackendAccess.kt`


## 2026-11-09 - Profile controls and overnight time selection

Decision:
- Keep profile avatars but hide photo editing controls for both roles. Move name and contact information into account management; place logout last. Show the owner's stored address below the store name.
- Use compact 30-minute dropdowns. End options show elapsed duration; infer next-day end when end <= start (equal times represent 24 hours). This supersedes the explicit next-day picker decision.

Reason:
- User requested these screen changes and automatic overnight calculation.

Implication for agents:
- Preserve the backend nextDay contract and apply inference when either time changes.

## 2026-11-09 - Signup business type comes from the job category catalog

Decision:
- Signup business type is a dropdown over `GET /api/jobs/categories`; the catalog name is sent as the free-text `businessType` / `preferredBusinessTypes` the backend accepts.
- Job posting reuses the owner's stored `businessType`: exact catalog name first, otherwise a single unambiguous word match (for older free-text values). The picker appears only when nothing matches.

Reason:
- The backend stores member business type as free text but requires a catalog `categoryId` for jobs; the user asked not to choose the business type twice.

Implication for agents:
- Do not reintroduce free-text business type input or a second business type question in posting. Never guess a category ID when the match is ambiguous.

## 2026-11-14 - Show all worker job search results

Decision:
- The worker home and search list show all server job search results in an "전체 공고" section, including urgent and already applied postings. Do not filter this section by the worker's preferred business type, wage, time, or activity radius.
- Keep the separate urgent highlight and the opt-in "즉시 지원 가능" toggle. The toggle applies open, deadline, not-applied, and activity-radius checks.

Reason:
- The user requested that every job appear in the former recommendation section without personal preference conditions.

Implication for agents:
- Preserve the server search result in the default worker list. Apply only explicit search filters or the availability toggle, and show server-backed application status on each card.

## 2026-11-14 - Do not offer reapplication after cancellation

Decision:
- A worker can submit only once for a job posting. The matching backend returns `REAPPLICATION_NOT_ALLOWED` (HTTP 409) when a canceled or terminal application is submitted again.
- Show those postings in worker job lists, but label their action `재지원 불가` and disable it. Keep active `APPLIED` applications labeled `지원완료` on job cards.

Reason:
- The user encountered a conflict screen after the frontend incorrectly treated a canceled application as available for a new submission. The backend service and its `rejectsReapplicationAfterCancellation` test confirm the rule.

Implication for agents:
- Include canceled and terminal server application records when deriving job apply status. Do not offer another POST for the same worker and job unless the backend contract changes.

## 2026-11-14 - Make cancellation consequences explicit and separate confirmed work

Decision:
- On an actively applied job detail, show `지원 취소` and confirm with the warning that reapplication is impossible. Use the same warning in work management and application status.
- In work management, label an active application `지원됨`. Show matched shifts before their end time only in the work section; show matched applications after their end time in the applied section as `출근 완료`.

Reason:
- The user requested a clear cancellation choice and one location for each matched shift.

Implication for agents:
- Use the server application ID for cancellation and the job/work schedule in Asia/Seoul for end-time classification. Keep canceled and rejected applications in the applied history.
