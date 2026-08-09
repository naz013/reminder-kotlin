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
| `feature-googletask` | Android library (Compose) | Fully extracted Google Tasks feature: screens, ViewModels, `GoogleTasksNavKey`/`GoogleTasksNavGraph`, feature-private sync/upload/download orchestration use cases, own `KoinModule`. See "Feature Modules" below. |
| `ui-googletask` | Android library (Compose) | Small reusable Compose pieces for Google Tasks: item-state adapter, Google Sign-In Compose helper (`rememberGoogleTasksLogin`). No navigation, no ViewModels. |
| `logic-googletask` | Kotlin library | Scaffolded sibling of `feature-googletask` for cross-feature Google Tasks logic. Currently has no source — not yet populated (see "Feature Modules" below). |
| `feature-tags` | Android library (Compose) | Fully extracted Tags feature: screens, ViewModels, `TagsNavKey`/`TagsNavGraph`, own `KoinModule`. Tag data now lives in the shared `AppDb` via `repository-api`'s `TagRepository`/`TagAssignmentRepository` — there is no separate `tags_db` any more. Both flavors. |
| `ui-tag` | Android library (Compose) | Reusable Tags Compose building blocks (chip picker, item styling) usable outside `feature-tags`. |
| `logic-tag` | Kotlin library | Cross-feature tag logic, e.g. `ToggleTagAssignmentUseCase`, usable by any `feature-*` module that assigns tags. |
| `feature-insights` | Android library (Compose) | Fully extracted **PRO-only** Streaks & Insights dashboard: screens, ViewModel, `InsightsNavKey`/`InsightsNavGraph`, aggregator classes (`ReminderStreakCalculator`, `CompletionStatsCalculator`), own `KoinModule`. Purely computed from `repository-api` — no schema of its own. Gated at runtime via `BuildInfo.isPro`, not a build-time flavor split. No sibling `ui-*`/`logic-*` module yet (nothing shared elsewhere). |
| `feature-note` | Android library (Compose) | Partial extraction: only shared Compose components (`NoteListItemCard`) and adapters moved out of `app`; the Notes screens/ViewModels still live in `app` (`com.elementary.tasks.notes.*`). No nav graph of its own. |
| `feature-reminder` | Android library (Compose) | Scaffolded, not yet populated — module and Gradle dependencies exist and are wired into `app`, but no source yet. Reminder screens still live in `app`. |
| `ui-reminder` | Android library (Compose) | Scaffolded sibling of `feature-reminder`; no source yet. |
| `logic-reminder` | Kotlin library | Populated ahead of `feature-reminder`'s extraction: reminder behavior strategies, occurrence calculators (`*OccurrenceCalculatorV2`), save/delete/activate/pause use cases. Already consumed cross-feature by `feature-googletask` (`CompleteRelatedGoogleTaskUseCase`). |
| `logic-schedule` | Kotlin library | Background-work scheduling use cases (`ScheduleBackgroundWorkUseCase`, sync/upload/delete `BackgroundTask` impls) shared by multiple features via `work-api`. |
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
 ├── ui-googletask
 │     └── domain / logging-api / cloud-api / ui-common / platform-common
 ├── logic-googletask (scaffolded, no source yet)
 ├── feature-googletask
 │     ├── domain / repository-api / usecase:googletasks / cloud-api / work-api / appwidgets-api
 │     ├── feature-common / ui-common / platform-common / platform-api / analytics / date-calculations
 │     └── ui-googletask / logic-googletask / logic-reminder
 ├── ui-tag
 │     └── domain / logging-api / ui-common / platform-api / platform-common
 ├── logic-tag
 │     └── domain / repository-api / logging-api / files-api / logic-schedule
 ├── feature-tags
 │     ├── domain / repository-api / logging-api / feature-common / ui-common
 │     └── ui-tag / logic-tag
 ├── feature-insights (PRO at runtime — see "Runtime vs. build-time PRO gating" below)
 │     ├── domain / repository-api / logging-api / feature-common / platform-common / ui-common
 │     └── no persistent storage of its own; no sibling ui-*/logic-* module yet
 ├── feature-note (partial extraction — screens/ViewModels still in app)
 │     └── domain / logging-api / platform-common / ui-common
 ├── feature-reminder (scaffolded, no source yet)
 │     └── domain / logging-api / platform-common / ui-common / logic-reminder / ui-reminder / analytics / appwidgets-api / date-calculations
 ├── ui-reminder (scaffolded, no source yet)
 ├── logic-reminder
 │     ├── domain / repository-api / logging-api / files-api / icalendar-api
 │     ├── appwidgets-api / scheduler-api / location-api / notification-api / googlecalendar-api / work-api / platform-api
 │     └── logic-schedule / date-calculations
 ├── logic-schedule
 │     └── domain / logging-api / repository-api / work-api / (own scheduler use cases)
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
7. **Newer features are extracted into a `feature-*` (+ optional `ui-*` / `logic-*`) module family instead of living in `app`.** Only `app` may depend on a `feature-*` module; `feature-*` modules never depend on `app` or on each other directly — a feature that needs another feature's logic depends on that feature's `logic-*` module instead (e.g. `feature-googletask` → `logic-reminder`, not → `feature-reminder`). See "Feature Modules" below.

---

### Flavor-gated modules

A module needed by only one product flavor (e.g. `appfunctions`, PRO-only) is added with `"proImplementation"(project(":appfunctions"))` in `app/build.gradle.kts` instead of a plain `implementation`, so the `free` APK never contains its code, its manifest entries, or its transitive dependencies.

Because `main`-sourceset code (e.g. `ReminderApp.kt`) can't reference types from a dependency that only one flavor sees, each flavor provides its own same-named shim class — e.g. `app/src/pro/.../AppFunctionsInitializer.kt` (real implementation, calls `loadKoinModules(appFunctionsModule)`) and `app/src/free/.../AppFunctionsInitializer.kt` (empty no-op) — mirroring the older `AdsProvider` free/pro split. `main` code calls the shim unconditionally; Gradle compiles whichever flavor's version is on the classpath for that variant. The flavor-gated module's own `KoinModule.kt` is therefore loaded at runtime via `loadKoinModules(...)` from the pro-flavor shim, not included in `ReminderApp.kt`'s static `startKoin { modules(listOf(...)) }` call like every other module's.

### Runtime vs. build-time PRO gating

`feature-insights` and `localbackup` are **PRO features gated at runtime**, not build-time flavor splits like `appfunctions`: both modules are plain `implementation(project(":x"))` dependencies present in every build, their Koin modules are registered unconditionally in `ReminderApp.kt`, and their Nav3 entries are always reachable in the graph. The only PRO check is a `buildInfo.isPro` read in `SettingsHubViewModel` that hides their Settings entry points on the free flavor (`SettingsHubState.isInsightsVisible` / `isLocalBackupVisible`). This was a deliberate simplicity trade-off — these two features have no free-flavor-only code paths or extra manifest entries worth stripping from the free APK, unlike `appfunctions`, so the Gradle-flavor-split machinery wasn't worth the extra complexity.

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

## Feature Modules: `feature-*` / `ui-*` / `logic-*`

Newer features (Google Tasks, Tags, Insights) are being extracted out of `app` into their own module
family instead of living entirely inside `app`. This is an **ongoing, incremental migration** — not
every feature has been fully split yet, and not every split feature has all three module kinds. Check
what actually exists before assuming a feature follows the full pattern (see "Current extraction
status" below).

### The three module kinds

| Module kind | Example | Contains | Depends on | Consumed by |
|---|---|---|---|---|
| `logic-<feature>` | `logic-reminder`, `logic-tag`, `logic-schedule` | Pure/Android-library business logic and use cases for the feature that **other features also need**. No Compose, no ViewModels, no navigation. | `domain`, `repository-api`, `logging-api`, whichever `*-api` modules it needs (`files-api`, `work-api`, `icalendar-api`, …), sometimes another `logic-*` module (`logic-tag` → `logic-schedule`) | Its own `feature-<feature>` module **and** any other `feature-*` module that needs that logic — e.g. `feature-googletask` depends on `logic-reminder` for `CompleteRelatedGoogleTaskUseCase`, not on `feature-reminder` |
| `ui-<feature>` | `ui-googletask`, `ui-tag`, `ui-reminder` | Small, reusable Compose building blocks for the feature's domain type — item-state adapters, list-item composables, auth/login Compose helpers. Deliberately free of navigation and ViewModels. | `domain`, `logging-api`, `ui-common`, `platform-common`/`platform-api`, occasionally `cloud-api` (e.g. `ui-googletask`'s Google Sign-In helper) | Its own `feature-<feature>` module **and** any other module that wants to render that feature's items |
| `feature-<feature>` | `feature-googletask`, `feature-tags`, `feature-insights` | The screens: ViewModels, Compose screens, a sealed `<Feature>NavKey : NavKey` per destination, a `<Feature>NavGraph.kt` exposing `fun EntryProviderScope<NavKey>.xyzEntries(backStack, ...)`, the feature's own `KoinModule.kt`, and any use cases that are genuinely feature-private orchestration (not reused elsewhere). | `domain`, `repository-api` or the relevant `usecase:*` module for reads, its own `ui-<feature>`/`logic-<feature>`, other `logic-*` modules it needs, `feature-common`, `ui-common`, `platform-common`/`platform-api`, `analytics`, `date-calculations` as needed | `app` **only** — wired into `ReminderApp.kt`'s `startKoin { modules(...) }` and into `AppNavGraph.kt`'s `entryProvider { }` via its `xyzEntries(backStack)` call |

`feature-*` modules never depend on `app` or on each other — see rule 7 above.

`feature-googletask` shows the full shape: it owns `GoogleTasksNavKey`/`GoogleTasksNavGraph` (five
destinations — list, task-list, task preview, task edit, list edit), five ViewModels, its own
`KoinModule.kt` (`featureGoogleTaskModule`), and a `usecase/` package of feature-private orchestration
(`SyncGoogleTaskList`, `UploadGoogleTask`, `AddNewTaskList`, `DownloadGoogleTasks`, …) that wrap
`cloud-api`/`repository-api` calls — these aren't shared with any other feature, so they live inside
`feature-googletask` rather than in `logic-googletask`. It pulls in `ui-googletask` for the Google
Sign-In Compose helper (`rememberGoogleTasksLogin`) and item-state adapter, and — notably —
`logic-reminder`, not `logic-googletask` (which is currently an empty scaffold), for completing a
reminder linked to a task.

### Why the split (ui vs. logic vs. feature)

Before adding code to a `feature-*` module, ask: **would another feature module ever need this?**

- Reusable domain logic / use cases → `logic-<feature>` (plain Kotlin/Android library, no Compose).
- Reusable Compose UI for the feature's domain type → `ui-<feature>` (Compose, but no navigation/ViewModels).
- Screen-specific ViewModel/UI/navigation, or orchestration only that screen needs → stays in `feature-<feature>`.

Splitting out `ui-*`/`logic-*` siblings isn't required up front — `feature-note` and `feature-insights`
currently have no sibling `ui-*`/`logic-*` module because nothing inside them is reused elsewhere yet.
Add the sibling module when a second consumer actually appears; don't scaffold empty modules ahead of
need on spec alone.

### Current extraction status

This is a migration in progress, not a finished pattern applied uniformly:

- **Fully extracted** — screens, ViewModels, nav graph, and Koin module all live in the `feature-*`
  module, wired via `xyzEntries()` in `AppNavGraph.kt`: `feature-googletask`, `feature-tags`,
  `feature-insights`.
- **Partially extracted** — only shared Compose components/adapters moved out; the actual screens and
  ViewModels still live in `app` (e.g. `com.elementary.tasks.notes.*`), and there is no
  `*NavGraph.kt`/`*NavKey.kt`: `feature-note`.
- **Scaffolded, not yet populated** — the module and its Gradle dependencies exist and are already
  wired into `app`'s dependency list, but there is no source yet; the dependency was declared ahead of
  an extraction that hasn't happened: `feature-reminder`, `ui-reminder`, `logic-googletask`.

Don't assume every `feature-*` module owns a full vertical slice — check whether it has its own
`*NavGraph.kt`/`*NavKey.kt` before assuming its screens are wired outside `app`.

### Creating a new feature module

1. **Module skeleton.** Add `feature-<name>` (and `ui-<name>`/`logic-<name>` only once something is
   actually shared — see "Why the split" above) to `settings.gradle.kts`. Give it a `build.gradle.kts`
   using `id("reminder.android.library.compose")` (add `alias(libs.plugins.kotlin.serialization)` if it
   defines `@Serializable` `NavKey`s) and `android { namespace = "com.github.naz013.feature.<name>" }`.
2. **Dependencies.** Only what the feature needs directly: `domain`, `repository-api` (or the relevant
   `usecase:*` module for reads), `logging-api`, `feature-common`, `ui-common`,
   `platform-common`/`platform-api`, plus its own `ui-<name>`/`logic-<name>` and any cross-feature
   `logic-*` modules it calls into. Never depend on `repository` (the Room implementation) directly — go
   through `repository-api`.
3. **NavKey + NavGraph.** A `sealed interface <Feature>NavKey : NavKey` with one `@Serializable`
   destination per screen, and a `<Feature>NavGraph.kt` exposing
   `fun EntryProviderScope<NavKey>.xyzEntries(backStack: MutableList<NavKey>, ...)` that registers one
   `entry<...>` per destination (see `GoogleTasksNavGraph.kt` / `GoogleTasksNavKey.kt` for the pattern).
4. **`KoinModule.kt`.** A `val feature<Name>Module = module { ... }` registering the feature's
   ViewModels (`viewModel { ... }` / `viewModelOf`) and use cases (`factoryOf` / `factory<Interface>`).
5. **Wire into `app`.** Add `implementation(project(":feature-<name>"))` in `app/build.gradle.kts`;
   import `feature<Name>Module` and add it to the `modules(listOf(...))` call in
   [ReminderApp.kt](../app/src/main/java/com/elementary/tasks/ReminderApp.kt); import the `xyzEntries`
   function and call it inside the `entryProvider { }` block in
   [AppNavGraph.kt](../app/src/main/java/com/elementary/tasks/navigation/nav3/AppNavGraph.kt).
6. **Tests.** Unit tests under `src/test/kotlin`, JUnit4 + MockK, following the existing
   `*ViewModelTest.kt` naming next to the class under test.

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
- `feature-googletask/KoinModule.kt`, `ui-googletask/KoinModule.kt`, `feature-tags/KoinModule.kt`, `ui-tag/KoinModule.kt`, `logic-tag/KoinModule.kt`, `logic-reminder/KoinModule.kt`, `logic-schedule/KoinModule.kt`, `feature-insights/KoinModule.kt`, `localbackup/KoinModule.kt` — all added to `startKoin { modules(listOf(...)) }` like any other module (see "Runtime vs. build-time PRO gating" above for why `feature-insights`/`localbackup` aren't loaded conditionally despite being PRO features)
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
