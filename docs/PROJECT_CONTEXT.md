# PROJECT_CONTEXT.md

## Project Summary

This repository contains the Android (Kotlin) frontend for an urgent job matching service.

The app is the client for an MSA-based backend (separate repository). It communicates with backend services through REST HTTP APIs and, for real-time state, WebSocket. The app does not own data; it presents and edits data that the backend services own.

The app module and the owner/worker demo screens are implemented. Work is now moving from mock data to the deployed backend, alongside a separate UI-only completion branch.

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

The project is in the UI completion and backend integration stage.

Current state:

- A single `app` module is scaffolded with Kotlin, Compose, Hilt, navigation, and feature repository interfaces.
- Owner screens cover home, job posting, applicants, work management, settlement, no-show/rematching, past postings, and my page. Their presence does not imply production API integration.
- Worker screens cover home, search/detail, application status, matching result, work management, chat, my page, and trust/history. Several device-backed and account actions remain mock or incomplete.
- The explicit demo entry point is a role switcher. Live mode starts with authentication; see the 2026-11-02 auth/job checkpoint below. Onboarding slides remain unimplemented.
- Map views and real-time updates remain placeholders/simulations; do not treat them as connected map SDK, push, or WebSocket infrastructure. Live GPS check-in now uses device location and the server attendance policy, but its full real-server success flow needs assigned test work.
- API integration status and confirmed backend differences are tracked in `docs/backend-integration.md`. UI-only changes maintain a separate screen inventory in their branch.
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
- Gson with the Retrofit Gson converter for the current API implementation
- Coroutines + Flow for async and state
- Jetpack Navigation (Compose)
- Coil for image loading

The current module uses `compileSdk = 35`, `targetSdk = 35`, and `minSdk = 26`. Naver Map is the selected map SDK, but the map UI currently uses placeholders. These choices are already recorded in the decision memory.

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

This is the conceptual feature map. Existing screens are primarily grouped under `feature/owner`, `feature/worker`, and `feature/job`; consult the actual source before choosing a package. Use `docs/agent/decisions.md` for confirmed decisions that override this document.

## Backend Dependency Notes

- Backend repository: `https://github.com/404-Worker-Not-Found/BE.git`.
- Deployed API base: `https://api.15-165-106-227.sslip.io/`. Service Swagger documents live under `/docs/{auth,member,job,matching,payment,work,chat,notification}/swagger-ui/index.html` and require separate documentation credentials. Never commit those credentials.
- The user chose the confirmed backend contract over conflicting frontend fields/behavior during integration. Start with existing screens and track missing screens as follow-up work.
- Backend `e24145f` removes the former `FRONTEND_ORIGIN` dependency and web frontend CORS configuration for the native Android deployment. Native Android HTTP calls do not need a hosted frontend origin. OAuth redirect URIs and Android App Links are separate feature-specific setup.
- Cross-feature data in the app should be combined in the `domain`/`data` layer, not in Composables.
- Authentication is JWT Access Token + Refresh Token. The app stores tokens securely and refreshes transparently.
- WebSocket application-status updates are a UI requirement (see UI spec 3-4), not proof that the backend exposes a corresponding endpoint. Confirm service contracts before implementing them.
- Several features depend on device capabilities: GPS/location, camera/gallery, push notifications, maps.

## Near-Term Priorities

Current priorities:

- Map existing screens to confirmed backend contracts and replace mock repositories incrementally.
- Keep explicit demo behavior separate from live mode; do not report mock data or simulated mutations as server success.
- Obtain backend-provided worker/owner test accounts, a registered owner business, and payment test configuration for authenticated end-to-end verification.
- Complete missing behavior in existing owner/worker UIs on the UI-only branch.
- Verify the connected auth/signup/reset screens with real accounts and complete the pending social provider/callback and onboarding setup.
- Keep project and agent documents aligned as decisions are made.

## Open Questions

These questions are not yet settled in code:

- Which test accounts/businesses and payment sandbox settings are available on the deployed server?
- Which screen requirements lack backend support or need frontend changes? Consult the integration inventory, and distinguish implemented adapters from flows tested against the live server.
- What OAuth provider redirect URIs and app identifiers will be registered when social login is implemented?
- What map, push, and real-time infrastructure remains necessary after the current UI work?


## 2026-11-02 Auth and Job Integration Checkpoint (#21)

- Live mode now includes login, validated stored-session entry, OWNER/WORKER routing, email/SMS verification, role-specific signup, and code-based password reset. Existing encrypted session storage and refresh infrastructure are reused. Social login awaits provider/callback registration.
- Live job creation uses the authenticated owner's profile ID/business location, prepares the actual required creation fields behind a repository. New creation stays disabled until a real category catalog contract is available; users never type internal category IDs. The demo posting form remains separate.
- Creation returns a real job ID; payment-order and payment order queries keep order creation, deposit status, and server-confirmed publication separate. The app preserves the created ID for recovery and does not automatically retry a non-idempotent creation request.
- No public category-list endpoint was found. A confirmed category selection contract is needed before creation can be enabled. Toss checkout and callback/approval execution await confirmed test client configuration; no payment is executed by the new UI.
- The #19 integration branch now includes the friend's #21 checkpoint `8f8f2d5` together with GPS, settlements, and past postings. Combined live/demo verification passes 76 tests. Native emulator QA confirms actual WORKER login, stored-session restart, authenticated empty-work/profile views, and logout persisting across restart.
- This does not establish successful live signup, owner creation, assigned-work check-in, or payment. See `docs/agent/friend-progress.md` for the friend's original checkpoint and `docs/agent/work-handoff.md` for integration verification and remaining dependencies.
