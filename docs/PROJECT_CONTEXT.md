# PROJECT_CONTEXT.md

## Project Summary

This repository contains the Android (Kotlin) frontend for an urgent job matching service.

The app is the client for an MSA-based backend (separate repository). It communicates with backend services through REST HTTP APIs and, for real-time state, WebSocket. The app does not own data; it presents and edits data that the backend services own.

The app module and the owner/worker screens are implemented and connected to the deployed backend.

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
- Worker screens cover home, search/detail, application status, matching result, work management, chat, my page, and trust/history. Actions without a confirmed backend contract show an unavailable state instead of simulating success.
- The app starts with splash/first-run onboarding and authentication; existing sessions skip onboarding and undergo real token validation.
- Features without a backend contract (owner no-show/re-matching, settlement payment approval, worker trust score, profile photo upload, notification preferences) keep their UI and report an unavailable state.
- Job maps, owner posting-location maps, and owner work-detail workplace maps use the Naver SDK with guarded empty-key behavior. Search supports actual GPS-based filtering. Live chat uses WSS/STOMP plus REST sync/read. Device push and continuous worker tracking are not connected. Populated work/chat/check-in success flows still need test data.
- API integration status and confirmed backend differences are tracked in `docs/backend-integration.md`. UI-only changes maintain a separate screen inventory in their branch.
- A repository-wide verification script exists at `docs/scripts/verify.sh`.
- External registrations, backend inputs, and remaining frontend work are inventoried in `docs/integration-readiness-audit.md`. Passing integration tests does not imply maps, push, social login, payments, or all specified screens are complete.
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

The current module uses `compileSdk = 35`, `targetSdk = 35`, and `minSdk = 26`. Naver Map is the selected map SDK. Worker search and job detail use real Naver tiles and address-derived pins when the local client ID is configured.

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

- Map remaining screens to confirmed backend contracts. Do not simulate server results; show an unavailable state until a contract exists.
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

- The app now includes login, validated stored-session entry, OWNER/WORKER routing, email/SMS verification, role-specific signup, and code-based password reset. Existing encrypted session storage and refresh infrastructure are reused. Social login awaits provider/callback registration.
- Job creation uses the authenticated owner's profile ID/business location, prepares the actual required creation fields behind a repository. New creation stays disabled until a real category catalog contract is available; users never type internal category IDs.
- Creation returns a real job ID; payment-order and payment order queries keep order creation, deposit status, and server-confirmed publication separate. The app preserves the created ID for recovery and does not automatically retry a non-idempotent creation request.
- No public category-list endpoint was found. A confirmed category selection contract is needed before creation can be enabled. Toss checkout and callback/approval execution await confirmed test client configuration; no payment is executed by the new UI.
- The #19 integration branch now includes the friend's #21 checkpoint `8f8f2d5` together with GPS, settlements, and past postings. Combined verification passes 76 tests. Native emulator QA confirms actual WORKER login, stored-session restart, authenticated empty-work/profile views, and logout persisting across restart.
- This does not establish successful live signup, owner creation, assigned-work check-in, or payment. See `docs/agent/friend-progress.md` for the friend's original checkpoint and `docs/agent/work-handoff.md` for integration verification and remaining dependencies.

## 2026-11-02 integration checkpoint

- Main integration branch: `feature/#19-api-integration`; durable recovery state: `docs/agent/work-handoff.md`. Original develop is preserved. Friend auth/job branch is already included through `8f8f2d5`.
- Connected: notification REST/read; contact change/withdrawal asynchronous commands; real review lists/write; work cancellation agreement; owner/worker WSS-STOMP chat/sync/read; worker terminal work/participant settlement history; first-run onboarding; Naver SDK/configuration/GPS search/address-derived pins/detail/workplace maps; publication calendar/time/location inputs.
- Current verification: 98 tests, lint and debug APK passed. A passing build does not verify real map tiles, two-account chat, populated matching/check-in/settlement or payment. Four backend test accounts/roles were checked; business lists are empty. No credentials are recorded here.
- Ignored local map key and actual emulator tiles were confirmed during subsequent QA. Remaining external prerequisites and corresponding frontend work: `docs/human-setup-guide.md` and `docs/integration-readiness-audit.md`. In particular category catalog, Toss checkout/callback, social OAuth, push delivery/device registration, continuous worker tracking, trust summary/contracts and live profile photo upload are incomplete.
- Resume automation: same chat, Nov 3 00:45 KST, one run. It inspects dirty work/logs/latest checkpoint before continuing, so a quota interruption requires no manual status handoff.

## 2026-11-03 follow-up

Debug builds now show a copyable diagnostic report on authentication failures (login, signup, verification and reset): UTC time, build version, server host, HTTP method/path/status, an allowlisted server code, and the error response body with a warning that it may contain personal data. Request headers, query parameters, request bodies and credentials are excluded. Release builds do not expose the report. End-to-end authentication still requires real verified accounts.

Backend 212b81d adds the hierarchical public category catalog. Owner publication now fetches/selects real IDs with retry and malformed-ID checks; the deployed route still returns 400, so catalog/publication E2E awaits deployment confirmation. A provided Toss test public key is configured locally via toss.clientKey/BuildConfig with a test-only guard. Checkout/callback/confirm execution still awaits agreed return URLs and paired backend test settings. No keys are recorded in tracked files.

## 2026-11-09 owner UX checkpoint

- Owner 공고관리 now defaults to current postings and separates past history. Pending payment cards on home/management open publication state by selected job ID; manual ID entry was removed.
- The shared posting form reuses the registered business address/coordinates, groups inputs, uses calendar/30-minute time selection, and explains invalid wage/count/schedule/deadline values. Owner my-page no longer duplicates the chat tab.
- Production publication/payment success still requires authenticated server verification.
