# Coding Rules

This file records AI-facing coding rules for frontend (Kotlin / Android / Compose) implementation work.

## Kotlin Rules

- Prefer `val` over `var`; keep state immutable where possible.
- Use data classes for models and UI state.
- Use sealed classes / sealed interfaces for closed sets of states or events (for example, UI state, navigation events).
- Prefer expression bodies and standard library functions over manual loops when they read more clearly.
- Avoid `!!`; handle nullability explicitly.
- Do not block the main thread; use coroutines with the appropriate dispatcher.

## Function Rules

- Functions should have one clear responsibility.
- Keep functions within 30 lines when possible.
- Prefer readability over strict line limits for simple mapping code or test setup code.
- Minimize nested control-flow depth.
- Extract long conditions into well-named functions or properties.

## Composable Rules

- Composable function names use PascalCase, such as `LoginScreen`, `JobCard`.
- A Composable should be stateless when possible; hoist state to the caller or ViewModel.
- Separate a screen-level Composable (wires ViewModel and state) from content Composables (take plain parameters and lambdas).
- Pass events up through lambda parameters; do not call ViewModel methods deep inside leaf Composables.
- Do not perform network or repository calls inside a Composable.
- Use `remember` / `rememberSaveable` correctly for local UI state; do not store business state in Composables.
- Provide a `Modifier` parameter as the first optional parameter for reusable Composables.
- Collect flows with lifecycle-aware APIs (for example, `collectAsStateWithLifecycle`).

## ViewModel and State Rules

- Expose UI state as a single immutable state object through `StateFlow`.
- Receive user intent through ViewModel functions; do not expose mutable state to the UI.
- Keep one-time events (navigation, toasts) separate from persistent state.
- Do not reference Android UI types (Context-dependent views, Activities) from the ViewModel where avoidable.

## Naming Rules

- Use names with clear meaning. Avoid unnecessary abbreviations.
- Use `is` or `has` prefixes for boolean variables and functions.
- Name UI state classes `{Screen}UiState`, such as `LoginUiState`.
- Name repositories `{Feature}Repository` with an interface plus an implementation, such as `JobRepository` / `JobRepositoryImpl`.
- Name remote DTOs with a `Dto` (or `Response`/`Request`) suffix and keep them in the data layer.
- Name mapping functions clearly, such as `toDomain()` / `toUiState()`.

## Networking and Data Rules

- Call backend APIs only through repositories; Composables and ViewModels do not touch Retrofit services directly.
- Map DTOs to domain models in the data layer; do not pass DTOs to the UI.
- Represent the result of a data operation explicitly (for example, a `Result`-style wrapper or sealed result), including loading and error states.
- Do not hardcode base URLs, API keys, or tokens; read them from build configuration or secure storage.

## Error Handling Rules

- Surface failures as UI state (error message / retry), not silent failures.
- Use clear, user-appropriate messages for expected errors; keep technical detail in logs.
- Define a shared error model for mapping network/throwable failures to user-facing messages.
- Do not swallow exceptions silently. Do not wrap everything in broad try/catch that hides the cause.
- Do not log tokens, authorization headers, passwords, or secrets.

## Resource Rules

- Keep user-facing text in string resources.
- Keep colors, typography, and spacing in the Compose theme / design system.
- Do not hardcode display strings, colors, or dimensions in screens when a resource or theme value exists.
