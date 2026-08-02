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
| `appfunctions` | Android library | Exposes reminders/notes/birthdays/Google Tasks capabilities to Gemini and other on-device assistants via the Android `androidx.appfunctions` platform API. **PRO-only**: wired into `app` via `"proImplementation"(project(":appfunctions"))`, never a plain `implementation` — see "Flavor-gated modules" below. For on-device/`adb` testing, see [appfunctions-testing.md](appfunctions-testing.md). |
| `usecase:reminders` | Android library | Reminder-specific use cases (`GetActiveRemindersV2UseCase`, `GetReminderV2ByIdUseCase`, …). |
| `usecase:notes` | Android library | Note-specific use cases (`GetAllNotesUseCase`, `GetNoteByIdUseCase`, `SearchNotesByTextUseCase`, …). |
| `usecase:birthdays` | Android library | Birthday-specific use cases (`GetAllBirthdaysUseCase`, `GetBirthdaysByDayMonthUseCase`). |
| `usecase:googletasks` | Android library | Google-Tasks-specific use cases (`GetAllGoogleTasksUseCase`, `GetGoogleTaskByIdUseCase`, …). |
| `reviews` | Android library | In-app review flow using the Play Core / Play Review API. |
| `reviewsadmin` | Android library | Internal admin tooling for the review flow (debug/test only). |
| `cloudtestadmin` | Android library | Internal admin tooling for testing cloud integrations (debug/test only). |
| `tags-api` | Kotlin library | Public contract for the Tags feature: `Tag`, `TaggedItemType`, `TagRepository`, `TagAssignmentRepository`. Depends on `domain`. |
| `tags` | Android library (Compose) | Room-backed implementation of `tags-api` in its own database (`tags_db`, isolated from the shared `AppDb`) plus the Tags Compose UI (`TagChipPicker`, manage/edit screens) and Nav3 entries. Both flavors. |
| `insights` | Android library (Compose) | **PRO-only** Streaks & Insights dashboard. Purely computed from `repository-api` (`EventHistoryRepository`, `ReminderV2Repository`) — no schema of its own. Gated at runtime via `BuildInfo.isPro`, not a build-time flavor split. |
| `localbackup` | Android library (Compose) | **PRO-only** local encrypted backup/restore. Owns the crypto (PBKDF2 + AES-GCM), the archive framing (reusing `files-api`'s `DataConverter`), and the passphrase Compose UI. Gated at runtime via `BuildInfo.isPro`. |

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
 ├── appfunctions (PRO flavor only — see "Flavor-gated modules" below)
 │     ├── domain / repository-api / logging-api / analytics
 │     ├── platform-common / date-calculations
 │     ├── cloud-api / cloud
 │     └── usecase:reminders / usecase:notes / usecase:birthdays / usecase:googletasks
 ├── usecase:reminders ──┐
 ├── usecase:notes      ──┤── domain, repository-api, logging-api
 ├── usecase:birthdays  ──┤
 ├── usecase:googletasks─┘
 ├── reviews
 ├── tags-api
 │     └── domain
 ├── tags
 │     ├── tags-api / domain / logging-api / feature-common / ui-common
 │     └── own Room database (tags_db, isolated from AppDb)
 ├── insights (PRO at runtime — see "Runtime vs. build-time PRO gating" below)
 │     ├── domain / repository-api / logging-api / feature-common / ui-common
 │     └── no persistent storage of its own
 ├── localbackup (PRO at runtime — see "Runtime vs. build-time PRO gating" below)
 │     ├── domain / repository-api / files-api / logging-api / feature-common / ui-common
 │     └── javax.crypto / java.security only (no new crypto dependency)
 └── (debug) cloudtestadmin / reviewsadmin
```

### Key architectural rules

1. **`domain` has zero external dependencies.** It is the foundation everything else builds on.
2. **`*-api` modules contain only interfaces and data types.** Implementations live in the corresponding non-`-api` module.
3. **`usecase:*` modules only depend on `repository-api` (not `repository`).** They never touch Room or the database directly.
4. **`sync` is cloud-provider agnostic.** It operates through `cloud-api` interfaces and is never aware of whether storage is Google Drive or Dropbox.
5. **`app` is the only module that wires concrete implementations** to their interfaces via Koin `KoinModule` objects.
6. **Flavor-gated modules** (currently just `appfunctions`) are added in `app/build.gradle.kts` via `"proImplementation"(project(":x"))` / `"freeImplementation"(project(":x"))`, never a plain `implementation` — see "Flavor-gated modules" below for how `main`-sourceset code still references them.

---

### Flavor-gated modules

A module needed by only one product flavor (e.g. `appfunctions`, PRO-only) is added with `"proImplementation"(project(":appfunctions"))` in `app/build.gradle.kts` instead of a plain `implementation`, so the `free` APK never contains its code, its manifest entries, or its transitive dependencies.

Because `main`-sourceset code (e.g. `ReminderApp.kt`) can't reference types from a dependency that only one flavor sees, each flavor provides its own same-named shim class — e.g. `app/src/pro/.../AppFunctionsInitializer.kt` (real implementation, calls `loadKoinModules(appFunctionsModule)`) and `app/src/free/.../AppFunctionsInitializer.kt` (empty no-op) — mirroring the older `AdsProvider` free/pro split. `main` code calls the shim unconditionally; Gradle compiles whichever flavor's version is on the classpath for that variant. The flavor-gated module's own `KoinModule.kt` is therefore loaded at runtime via `loadKoinModules(...)` from the pro-flavor shim, not included in `ReminderApp.kt`'s static `startKoin { modules(listOf(...)) }` call like every other module's.

### Runtime vs. build-time PRO gating

`insights` and `localbackup` are **PRO features gated at runtime**, not build-time flavor splits like `appfunctions`: both modules are plain `implementation(project(":x"))` dependencies present in every build, their Koin modules are registered unconditionally in `ReminderApp.kt`, and their Nav3 entries are always reachable in the graph. The only PRO check is a `buildInfo.isPro` read in `SettingsHubViewModel` that hides their Settings entry points on the free flavor (`SettingsHubState.isInsightsVisible` / `isLocalBackupVisible`). This was a deliberate simplicity trade-off — these two features have no free-flavor-only code paths or extra manifest entries worth stripping from the free APK, unlike `appfunctions`, so the Gradle-flavor-split machinery wasn't worth the extra complexity.

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
- `tags/KoinModule.kt`, `insights/KoinModule.kt`, `localbackup/KoinModule.kt` — all added to `startKoin { modules(listOf(...)) }` like any other module (see "Runtime vs. build-time PRO gating" above for why `insights`/`localbackup` aren't loaded conditionally despite being PRO features)
- `appfunctions/KoinModule.kt` — the exception: loaded at runtime via `loadKoinModules(...)` from `app/src/pro`'s `AppFunctionsInitializer`, not added to `startKoin { modules(listOf(...)) }` (see "Flavor-gated modules" above)

---

## Build System

The project uses **Gradle with Kotlin DSL** (`build.gradle.kts`).

- A shared `libs.versions.toml` version catalog centralises all dependency versions.
- **Detekt** (`reminder.detekt` convention plugin) is applied to all modules for static analysis and formatting (wraps ktlint rules via `detekt-formatting`; config in `config/detekt/detekt.yml`).
- **Kover** (`reminder.kover` convention plugin) is applied to all modules for coverage; the root project aggregates JVM-module coverage into `build/reports/kover/xml/report.xml`.
- Firebase is integrated via the `google-services` and `crashlytics-gradle` plugins.
- Jetpack Compose compiler metrics/reports are generated automatically by the `compose.compiler` plugin.
- CI runs on GitHub Actions (`.github/workflows/build_and_test.yml`) and a release pipeline (`.github/workflows/build_and_publish_release.yml`).
