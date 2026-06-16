# Checklists

This file records verification checklists. Use it to check whether decisions and recurring process requirements are being followed.

Do not add one-off task notes. Add or update a checklist only when the check is likely to recur.

## Documentation Update Check

At the end of a task, check:

- Was a new decision made?
- Did a previous decision change?
- Did the user identify a repeated AI mistake?
- Did the current project state significantly change?
- Is a new recurring verification checklist needed?

If no update is needed, report:

```text
No agent document update needed.
```

## Repository Verification

For repository-wide verification, run:

```bash
./docs/scripts/verify.sh
```

If verification fails, check:

- What command was run?
- Which module or step failed?
- What is the likely cause, if known?
- Is the failure related to the current task?

## Architecture Check

When a task changes architecture or package structure, check:

- Does the change keep the feature-first package structure in `docs/architecture/package-structure.md`?
- Is business logic kept out of Composables (in ViewModel/domain)?
- Do Composables avoid calling Retrofit services or repositories directly?
- Are network DTOs mapped to domain models in the data layer instead of reaching the UI?
- Are repositories exposed through interfaces?
- If a different structure is proposed, is the reason explained and recorded as a decision?

## Technology Baseline Check

When a task changes build configuration, dependencies, or module scaffolding, check:

- Does the change align with Kotlin + Jetpack Compose?
- Does it keep MVVM with UI / ViewModel / domain / data layering?
- Does it use Hilt for DI, Retrofit/OkHttp for networking, and Coroutines/Flow for async?
- Are versions managed through the Gradle version catalog when present?
- If current scaffolding differs from the baseline, is the mismatch reported?
- If an incompatible stack change is proposed, is the justification explicit?

## UI Specification Check

When a task implements or changes a screen, check:

- Was the relevant section of `docs/UI_Specification.md` read first?
- Do screen names, fields, and behavior match the specification?
- Are validation rules (name length, email/password rules, verification timers, etc.) consistent with the specification?
- Are user-type differences (점주 vs 알바생) handled as the specification describes?
- If the implementation must diverge from the specification, is the conflict reported instead of silently changed?

## Authentication Strategy Check

When a task changes authentication or session behavior, check:

- Does the design assume Access Token + Refresh Token authentication?
- Are tokens stored securely (encrypted storage / DataStore), not plain SharedPreferences?
- Is token attachment and refresh handled in one place (interceptor/authenticator/token repository)?
- Does auto-login rely on a valid stored session as in UI spec 1-1?
- Are passwords, tokens, and authorization headers kept out of logs?

## Backend Contract Check

When a task depends on backend behavior, check:

- Are endpoint paths, request/response shapes, and error codes confirmed against the backend contract rather than invented?
- Is the dependency isolated behind a repository interface?
- If a contract is unknown, is the assumption stated explicitly?

## Permissions and Device Capability Check

When a task uses device capabilities (location, camera, notifications), check:

- Are the required runtime permissions requested and handled, including denial?
- Is the manifest updated with the necessary permissions?
- Is graceful behavior provided when a capability is unavailable or denied?
- Are GPS-dependent features (check-in, distance, live location) guarded for missing location?

## Agent Memory Check

When agent documents are updated, check:

- Does `AGENTS.md` contain AI behavior rules only?
- Does `docs/PROJECT_CONTEXT.md` describe current project state only?
- Does `docs/agent/decisions.md` record decisions?
- Does `docs/agent/failure-memory.md` record only user-identified repeated mistakes?
- Does `docs/agent/checklists.md` contain verification checklists?
- Does `docs/agent/coding-rules.md` contain AI-facing frontend coding rules?

## Collaboration Workflow Check

When starting or preparing a new unit of work, check:

- Was an issue created first?
- Does the issue title follow `[type] title`, such as `[chore] 스타일 시스템 추가`?
- Are the issue title and body written in Korean?
- Does the issue use the repository issue template?
- Was the work branch created from `develop`?
- Does the branch name follow `{type}/#{issue-number}-{short-description}`, such as `feature/#2-login-screen`?
- Does the branch type match the work type, such as `feature/*`, `fix/*`, `docs/*`, `refactor/*`, `test/*`, `build/*`, `ci/*`, `chore/*`, or `environment/*`?

When committing, check:

- Does each commit follow `type: 작업 내용`, such as `feat: 로그인 화면`?
- Is the commit type one of `feat`, `fix`, `docs`, `refactor`, `test`, `build`, `ci`, `chore`, or `environment`?

When opening or updating a PR, check:

- Does the PR target `develop`?
- Does the PR title follow `[type] PR 제목`, such as `[feat] 로그인 화면`?
- Are the PR title and body written in Korean?
- Is the PR author assigned as the assignee in PR metadata?
- Does the PR use the repository PR template?
- Is the related issue linked?
- Does the related issue section avoid auto-closing keywords such as `Close`, `Closes`, `Closed`, `Fix`, `Fixes`, `Fixed`, `Resolve`, `Resolves`, or `Resolved`?
- Is repository verification handled in the agent's final report instead of being written into the issue or PR body?
- Does the issue or PR body avoid command-specific AI verification notes such as `./gradlew test passed`, `./docs/scripts/verify.sh passed`, or similar agent execution notes?
- Does the PR template keep generic human-facing checklist items such as `테스트를 통과했나요?` when useful?

When merging a PR, check:

- Did one teammate review and approve, or were requested changes addressed?
- Is squash-and-merge being used?
- Will the branch be deleted after merge?
- Will the next unit of work start from a fresh branch?
