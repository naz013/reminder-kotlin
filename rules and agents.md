# Reminder Kotlin Project Rules

## Architecture: Multi-module Clean Architecture
- **domain**: Pure Kotlin, zero external dependencies. Foundation for all.
- **repository-api**: Interfaces + domain. No implementation.
- **repository**: Room implementation of `repository-api`.
- **cloud-api**: Cloud storage interfaces.
- **cloud**: Implementations (Google Drive/Tasks, Dropbox).
- **usecase:* modules**: Pure logic. Depend ONLY on `repository-api` (never `repository`).
- **sync**: Orchestration via `cloud-api` and `repository-api`.
- **app**: Entry point. Wires implementations via Koin.
- **ui-common**: Shared Compose components + Material 3 tokens.

## Coding Standards
- **Language**: Kotlin.
- **UI**: Jetpack Compose + Material 3.
- **Dependency Injection**: Koin. Use `KoinModule.kt` in each module.
- **State**: MVVM with `StateFlow` / `LiveData`.
- **Log**: Use `logging-api` (`Logger` interface) to avoid concrete coupling.
- **Persistence**: Room (SQLite). Mappings between Entity and Domain are mandatory.
- **JSON**: Every field Gson touches (`Gson().toJson(...)` / `fromJson(...)`, directly or via
  `TypeToken`) needs `@SerializedName` - unannotated Gson field reflection is not safe under
  R8/ProGuard shrinking (see the `@SerializedName` keep rule in `app/proguard-rules.pro`) - it
  caused a production crash (`RecurrenceRule$Weekly` constructor stripped by R8). Two ways to get
  there, pick based on whether the wire shape matches the model:
  - **Annotate the class directly** when the JSON shape is just the model's own fields (e.g.
    `Place`, `ShopItem`, `BuilderSchemeItem`, `GoogleTaskList`, `RecurrenceRule`'s variants,
    `WorkflowTrigger`/`WorkflowAction`/`WorkflowCondition`). Simplest; no translation layer needed.
  - **Define a `*Json` data class** in the module doing the conversion when the wire format
    genuinely diverges from the domain shape (e.g. a sealed class flattened to `type`+`payload`
    columns, `LocalDateTime` written as an epoch-millis string) - see `files/model/*Json.kt` and
    `repository/entity/*Columns` for the pattern.
  - Whichever you pick, a Gson-reflected type getting `fromJson`'d is never allowed to throw
    uncaught into a `List.map { it.toDomain() }` - wrap the parse in `runCatching` with a safe
    fallback (see `ReminderV2Mapper.toRecurrenceRule`), so one bad row can't take down an entire
    list load.

## Dependency Rules
- Arrows: `A -> B` (A depends on B).
- `usecase` -> `repository-api` (NOT `repository`).
- `feature` -> `*-api` modules (for decoupling).
- `domain` is the root (no arrows outgoing).

## Build System
- Gradle Kotlin DSL (`.kts`).
- Version catalog: `libs.versions.toml`.
- Code analysis: `detekt` (all modules via `reminder.detekt` convention plugin; config at `config/detekt/detekt.yml`).
- Coverage: `kover` (all modules via `reminder.kover`; root aggregates JVM modules).
