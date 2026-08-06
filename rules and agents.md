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
