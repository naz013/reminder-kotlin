# GitHub Copilot Instructions — Reminder (reminder-kotlin)

## Project Overview

This is a Kotlin Android application. It is primarily a **reminder / task manager** but also includes Notes, Birthday tracking, Google Tasks integration, and cloud backup to Google Drive and Dropbox. See `docs/app-overview.md` for a full feature description, `docs/architecture.md` for the module structure, and `rules and agents.md` for core architectural rules.

---

## Module Structure

The project uses a **multi-module Clean Architecture** layout. Key rules:

- `domain` — pure Kotlin data models; **zero** external dependencies.
- `*-api` modules — interfaces only (e.g. `repository-api`, `cloud-api`, `logging-api`, `navigation-api`). Never add implementations here.
- `repository` — Room implementation of `repository-api`.
- `cloud` — Implementations of `cloud-api` (Google Drive/Tasks, Dropbox).
- `usecase:*` modules — pure logic; **only** depend on `repository-api`, never on `repository` directly.
- `sync` — Orchestration via `cloud-api` and `repository-api`.
- `app` — the only module that wires concrete Koin DI bindings.
- `ui-common` — Shared Compose components + Material 3 tokens.
- Never add circular dependencies between modules.

---

## Coding Standards

### Kotlin

- Use Kotlin idioms: data classes, sealed classes, extension functions, `when` expressions.
- Prefer `val` over `var`; use immutable collections where possible.
- Use Kotlin Coroutines (`suspend` functions, `Flow`, `StateFlow`) for all async work; never use `AsyncTask` or raw threads.
- Use `DispatcherProvider` (from `feature-common`) for coroutine dispatchers to keep code testable.

### Functions & Classes

- Add descriptive **KDoc comments** to all public functions and classes.
- Include input validation and use **early returns** for error conditions.
- Use meaningful variable names; avoid single-letter names outside lambda parameters.
- Keep functions short and focused on a single responsibility.

### Error Handling & Persistence

- Wrap external API calls (cloud, Room) in `try/catch` and log errors via the `Logger` interface from `logging-api`.
- Never swallow exceptions silently; always log or propagate.
- **Persistence**: Room (SQLite). Mappings between Entity (repository) and Domain (domain) are mandatory.
- **JSON serialization**: Never call `Gson().toJson(...)` / `fromJson(...)` on a domain (or any non-`*Json`) data class directly. Define a corresponding `*Json` data class with `@SerializedName` on every field, in the module performing the conversion, and map explicitly between it and the domain model. Unannotated Gson field reflection is not safe under R8/ProGuard shrinking — see the `@SerializedName` keep rule in `app/proguard-rules.pro` and the `RecurrenceRule$Weekly` production crash it caused.

---

## Android UI Guidelines

- Follow **Material 3** (Material You / Expressive) design guidelines.
- All new screens should use **Jetpack Compose**. Legacy XML View screens may remain but should not be extended.
- Break Compose screens into **small, reusable `@Composable` functions**; avoid large monolithic composables.
- Ensure layouts are responsive across phone and tablet screen sizes and both orientations.
- Use **ViewModels** (`androidx.lifecycle.ViewModel`) for all UI state; expose state via `StateFlow` or `LiveData`.
- Reuse shared components from `ui-common` before creating new ones.

---

## Dependency Injection

- All DI is done with **Koin**. Each module that provides objects must declare a `KoinModule.kt` with a `module { }` block.
- Inject dependencies through constructor parameters, not field injection.
- Do not call `GlobalContext.get()` inside non-DI code.

---

## Testing

- Use **JUnit 4** as the test runner.
- Use **MockK** for mocking; never use Mockito.
- Follow the **AAA pattern** (Arrange / Act / Assert) and write descriptive test names using plain English with spaces.
- Unit tests belong in `src/test/`; instrumented tests in `src/androidTest/`.
- See `.github/prompts/generate-unit-tests.prompt.md` for the unit-test generation prompt.

---

## Documentation

When creating Markdown documentation files:

- Use proper Markdown syntax with headings, subheadings, and tables where appropriate.
- Use bullet points or numbered lists for clarity.
- Add links to relevant sections or external resources.
- Place project-level documentation in the `docs/` folder.

---

## Git & Pull Requests

- Use conventional commit messages: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`.
- Keep PRs focused; one logical change per PR.
- Ensure all existing tests pass before opening a PR.
