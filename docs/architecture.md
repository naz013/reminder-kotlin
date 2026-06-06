# Architecture & Module Dependency

## Overview

The project follows a **multi-module Clean Architecture** approach. Concerns are separated into distinct Gradle modules so that:

- The `domain` and `*-api` modules contain no Android framework dependencies and can be unit-tested without an emulator.
- The `app` module only wires everything together; all business logic lives in lower-layer modules.
- Feature modules depend on *interfaces* (`-api` modules), not on concrete implementations, which keeps them decoupled and swappable.

---

## Module Inventory

| Module | Type | Purpose |
|---|---|---|
| `app` | Android Application | Entry point. Hosts all UI screens (Activities, Fragments, Compose screens), ViewModels, navigation graph, and Koin DI wiring. |
| `domain` | Kotlin library | Pure data models and enums (`Reminder`, `Note`, `Birthday`, `GoogleTask`, `Place`, `ReminderType`, …). Zero Android/framework dependencies. |
| `repository-api` | Android library | Repository interfaces (`ReminderRepository`, `NoteRepository`, `BirthdayRepository`, etc.) and `TableChangeListener` observers. Depends on `domain`. |
| `repository` | Android library | Room-based implementation of `repository-api`. Contains the Room database, DAOs, and entity-to-domain mappers. |
| `cloud-api` | Kotlin library | Interfaces for cloud file access (`CloudFileApi`, `GoogleDriveApi`, `DropboxApi`, `GoogleTasksApi`) and auth managers. |
| `cloud` | Android library | Concrete implementations of `cloud-api` using the Google Drive REST SDK, Google Tasks SDK, and Dropbox Android SDK. |
| `sync` | Kotlin library | Cloud-sync orchestration use-cases: upload, download, delete, and diff-check between local and remote file metadata. Depends on `cloud-api` and `repository-api`. |
| `icalendar` | Android library | iCalendar (`.ics`) serialisation / deserialisation and RRULE evaluation using the lib-recur library. |
| `analytics` | Android library | Firebase Analytics wrapper; provides a single `AnalyticsManager` abstraction over Firebase events. |
| `logging-api` | Kotlin library | `Logger` interface with no implementation. All other modules depend on this so that log calls compile without pulling in a concrete logging library. |
| `logging` | Android library | Logback + SLF4J + Firebase Crashlytics implementation of `logging-api`. |
| `navigation-api` | Android library | `DeepLinkDestination` and screen-navigation contracts shared between `app` and feature/widget modules. |
| `feature-common` | Android library | Shared Kotlin/Android utilities: coroutine `DispatcherProvider`, `SingleLiveEvent`, Flow extensions, `SystemServiceProvider`, etc. |
| `platform-common` | Android library | Android-platform-level helpers: biometric auth, permission helpers, Google sign-in wrappers. |
| `ui-common` | Android library | Shared Compose components, Material 3 theme tokens, reusable composables, and color-picker utilities. |
| `appwidgets` | Android library | All home-screen widget implementations (reminders, notes, birthdays, Google Tasks). Aggregates usecase modules. |
| `usecase:reminders` | Android library | Reminder-specific use cases (`GetActiveRemindersUseCase`, `GetReminderByIdUseCase`, …). |
| `usecase:notes` | Android library | Note-specific use cases (`GetAllNotesUseCase`, `GetNoteByIdUseCase`, `SearchNotesByTextUseCase`, …). |
| `usecase:birthdays` | Android library | Birthday-specific use cases (`GetAllBirthdaysUseCase`, `GetBirthdaysByDayMonthUseCase`). |
| `usecase:googletasks` | Android library | Google-Tasks-specific use cases (`GetAllGoogleTasksUseCase`, `GetGoogleTaskByIdUseCase`, …). |
| `reviews` | Android library | In-app review flow using the Play Core / Play Review API. |
| `reviewsadmin` | Android library | Internal admin tooling for the review flow (debug/test only). |
| `cloudtestadmin` | Android library | Internal admin tooling for testing cloud integrations (debug/test only). |

---

## Dependency Graph

The arrows below read **"depends on"** (i.e., `A → B` means module A has a compile dependency on module B).

```
app
 ├── domain
 ├── logging-api ←─────────────── logging
 ├── repository-api ←────────────── repository
 │     └── domain
 ├── cloud-api ←──────────────────── cloud
 │     ├── logging-api
 │     └── domain
 ├── sync
 │     ├── cloud-api
 │     ├── repository-api
 │     ├── domain
 │     └── logging-api
 ├── icalendar
 │     ├── domain
 │     └── logging-api
 ├── analytics
 │     ├── domain
 │     └── logging-api
 ├── navigation-api
 │     └── logging-api
 ├── feature-common
 │     ├── domain
 │     └── logging-api
 ├── platform-common
 │     ├── feature-common
 │     └── logging-api
 ├── ui-common
 │     ├── domain
 │     ├── logging-api
 │     ├── navigation-api
 │     └── platform-common
 ├── appwidgets
 │     ├── domain / logging-api / navigation-api / cloud-api
 │     ├── analytics / icalendar
 │     ├── feature-common / platform-common / ui-common
 │     └── usecase:reminders / usecase:notes / usecase:birthdays / usecase:googletasks
 ├── usecase:reminders ──┐
 ├── usecase:notes      ──┤── domain, repository-api, logging-api
 ├── usecase:birthdays  ──┤
 ├── usecase:googletasks─┘
 ├── reviews
 └── (debug) cloudtestadmin / reviewsadmin
```

### Key architectural rules

1. **`domain` has zero external dependencies.** It is the foundation everything else builds on.
2. **`*-api` modules contain only interfaces and data types.** Implementations live in the corresponding non-`-api` module.
3. **`usecase:*` modules only depend on `repository-api` (not `repository`).** They never touch Room or the database directly.
4. **`sync` is cloud-provider agnostic.** It operates through `cloud-api` interfaces and is never aware of whether storage is Google Drive or Dropbox.
5. **`app` is the only module that wires concrete implementations** to their interfaces via Koin `KoinModule` objects.

---

## Layered Architecture (per feature)

Each major feature (reminders, notes, birthdays, Google Tasks) follows the same vertical slice:

```
┌─────────────────────────────────────────────────┐
│  app  (ViewModel + Compose/Fragment UI)          │
│       uses usecase:* for read operations         │
│       calls repository-api directly for writes  │
└────────────────────┬────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │   usecase:*          │
          │  (pure read logic)   │
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │   repository-api     │
          │   (interfaces)       │
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │   repository         │
          │   (Room + DAOs)      │
          └─────────────────────┘
```

---

## Dependency Injection (Koin)

Each module that exposes objects declares a `KoinModule.kt` file containing a Koin `module { }` block. The `app` module collects all `KoinModule` lists and passes them to `startKoin { }` in the Application class.

Modules that provide DI configuration:

- `cloud/KoinModule.kt`
- `repository/KoinModule.kt` (inferred from Room setup)
- `usecase:reminders/KoinModule.kt`, `usecase:notes/KoinModule.kt`, etc.
- `analytics/KoinModule.kt` (inferred)
- `navigation-api/KoinModule.kt`
- `feature-common/KoinModule.kt`
- `app/core/utils/KoinModule.kt` (top-level wiring)

---

## Build System

The project uses **Gradle with Kotlin DSL** (`build.gradle.kts`).

- A shared `libs.versions.toml` version catalog centralises all dependency versions.
- `ktlint` is applied to the `app` module for code-style enforcement.
- Firebase is integrated via the `google-services` and `crashlytics-gradle` plugins.
- Jetpack Compose compiler metrics/reports are generated automatically by the `compose.compiler` plugin.
- CI runs on GitHub Actions (`.github/workflows/build_and_test.yml`) and a release pipeline (`.github/workflows/build_and_publish_release.yml`).
