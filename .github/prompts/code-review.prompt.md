---
mode: 'agent'
description: 'Review staged or recently changed code for correctness, style, and architecture compliance'
---

## Task

Perform a thorough code review of the changes described below. Focus on correctness, architecture compliance, and Kotlin best practices. Do **not** flag purely stylistic nits that are already handled by detekt-formatting (indentation, spacing, import order, etc.).

## What to Review

${input:changes_description:Describe the changes to review, or paste the diff/file paths here.}

## Review Criteria

### Architecture Compliance (see `docs/architecture.md` and `rules and agents.md`)

- [ ] No circular module dependencies introduced.
- [ ] `domain` module remains free of ANY external dependencies.
- [ ] `*-api` modules contain only interfaces and data types; no implementations.
- [ ] `usecase:*` modules depend only on `repository-api`, not `repository`.
- [ ] Concrete DI bindings are registered only in `app` or the module's own `KoinModule.kt`.
- [ ] Room Entities in `repository` are correctly mapped to/from Domain models before leaving the repository layer.

### Kotlin & Android Correctness

- [ ] No memory leaks: no `Context` held in static fields; no uncancelled coroutines.
- [ ] Coroutines launched on the correct dispatcher via `DispatcherProvider`; not hardcoded to `Dispatchers.IO`.
- [ ] `StateFlow` / `LiveData` updated only from the correct thread / dispatcher.
- [ ] Room queries annotated correctly; no query on the main thread.
- [ ] No `runBlocking` in production code (acceptable in tests only).
- [ ] `try/catch` around all network and database calls; errors logged via `Logger`.

### Jetpack Compose

- [ ] Composables are stateless where possible (state hoisted to ViewModel).
- [ ] Side effects use the correct effect handler (`LaunchedEffect`, `SideEffect`, `DisposableEffect`).
- [ ] No expensive computations inside `@Composable` functions without `remember { }`.
- [ ] `collectAsStateWithLifecycle()` used instead of `collectAsState()` for lifecycle-aware collection.

### Testing

- [ ] New business logic is covered by unit tests.
- [ ] Tests use MockK; no Mockito.
- [ ] Test names are descriptive English sentences.

### General

- [ ] Public APIs have KDoc comments.
- [ ] No hardcoded strings that should be resources (`R.string.*`).
- [ ] No `TODO` or `FIXME` comments left without a corresponding issue reference.
- [ ] Sensitive data (API keys, tokens) not committed to source.

## Output Format

For each issue found, provide:
1. **File & line** (if applicable)
2. **Severity**: Critical / Major / Minor
3. **Description** of the problem
4. **Suggested fix** (code snippet if helpful)

Summarise with an overall assessment: ✅ Approve / 🔄 Needs changes / ❌ Reject.
