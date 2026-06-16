# PROJECT_CONTEXT.md

## Project Summary

This repository contains the Android (Kotlin) frontend for an urgent job matching service.

The app is the client for an MSA-based backend (separate repository). It communicates with backend services through REST HTTP APIs and, for real-time state, WebSocket. The app does not own data; it presents and edits data that the backend services own.

The current repository is in an early frontend setup stage.

The screen-level product definition lives in:

- `docs/UI_Specification.md`

Architecture notes:

- `docs/architecture/package-structure.md`

## Domain

The service domain is urgent job matching.

The app supports two user types:

- 점주 (store owner): posts urgent job openings, manages applicants, manages work progress and settlement.
- 알바생 (worker): browses and instantly applies to openings, manages matched work, checks in by GPS, builds a trust score.

Expected feature areas from the UI specification:

- onboarding / splash / auto-login
- sign up (user type selection, basic info, email/phone verification, business info for owners)
- login (email/password, social login, password recovery)
- owner home and job posting
- applicant management and matching confirmation
- work management (GPS check-in, live location, no-show handling, settlement)
- worker home, job search (list/map/filter), job detail, instant apply
- application status (real-time via WebSocket), matching result, re-matching
- chat between owner and worker
- my page (profile, notifications, account) and trust score

## Current Stage

The project is currently in the initial frontend scaffolding stage.

Current state:

- The repository holds agent documents and the UI specification.
- The app module, feature packages, and build configuration are scaffolded as work begins.
- A repository-wide verification script exists at `docs/scripts/verify.sh`.
- The package structure is defined in `docs/architecture/package-structure.md`.
- Agent work instructions exist in `AGENTS.md`.
- Agent failure memory, decision memory, and checklist memory exist under `docs/agent/`.

## Current Technical State

Decided technology baseline (see `docs/agent/decisions.md`):

- Kotlin
- Android, Jetpack Compose UI
- MVVM architecture (UI / ViewModel / domain / data)
- Hilt for dependency injection
- Retrofit + OkHttp for networking
- Kotlinx Serialization (or Moshi) for JSON
- Coroutines + Flow for async and state
- Jetpack Navigation (Compose)
- Coil for image loading

Exact library versions, `minSdk`, and `targetSdk` are managed in the Gradle version catalog and `build.gradle.kts`. Where this document and the build configuration disagree, the build configuration is authoritative and the mismatch should be reported.

## Feature Map

Planned feature areas, derived from `docs/UI_Specification.md`:

- `auth`: onboarding, sign up, login, social login, token-based auto-login
- `job`: owner job posting, job search (worker), job detail
- `applicant`: applicant list/detail, matching confirmation (owner)
- `matching`: application status, matching result, re-matching (worker)
- `work`: work management, GPS check-in, no-show handling, settlement
- `chat`: owner–worker chat
- `mypage`: profile settings, notifications, account
- `trust`: trust score, no-show risk, reviews

This feature map is provisional. Use `docs/agent/decisions.md` for confirmed decisions that override this document.

## Backend Dependency Notes

- The backend is MSA; service boundaries are provisional on the backend side. The app should not assume a single monolithic API surface beyond what contracts confirm.
- Cross-feature data in the app should be combined in the `domain`/`data` layer, not in Composables.
- Authentication is JWT Access Token + Refresh Token. The app stores tokens securely and refreshes transparently.
- Real-time application status uses WebSocket (see UI spec 3-4).
- Several features depend on device capabilities: GPS/location, camera/gallery, push notifications, maps.

## Near-Term Priorities

Likely next steps:

- Scaffold the app module and feature package structure.
- Define the networking layer (Retrofit service, token interceptor, refresh flow).
- Define common UI state, error, and result handling patterns.
- Build the auth flow (onboarding, sign up, login, auto-login) first, since most flows require an authenticated session.
- Keep project and agent documents aligned as decisions are made.

## Open Questions

These questions are not yet settled in code:

- What are the exact backend endpoint contracts and error code shapes?
- Where and how are tokens stored (EncryptedSharedPreferences, DataStore)?
- Which map SDK is used (Google Maps, Naver Map, Kakao Map)?
- Which social login SDKs are integrated (Kakao, Naver)?
- What is the minimum supported Android version (`minSdk`)?
- Is a multi-module setup adopted, or a single `app` module with feature packages?
