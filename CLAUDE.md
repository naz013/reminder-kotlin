# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Reminder (`com.cray.software.justreminder`) — a Kotlin Android task/reminder manager with notes, birthday
tracking, Google Tasks integration, calendar view, and cloud backup (Google Drive, Dropbox). Ships as two
product flavors: `free` and `pro`.

For deeper background, read (don't duplicate into this file):
- `docs/app-overview.md` — full feature list and user-facing screens
- `docs/architecture.md` — module inventory and dependency graph
- `rules and agents.md` — condensed architecture rules referenced by the prompt files in `.github/prompts/`

## Build & Test Commands

JDK 17 required. Use `./gradlew` (or `gradlew.bat` on plain cmd.exe).

Module `build.gradle.kts` files are intentionally thin: shared Android/Kotlin config (compileSdk, minSdk,
compileOptions, kotlin compiler opt-ins, buildTypes defaults, Compose setup) lives in convention plugins
under `build-logic/convention` (`reminder.kotlin.jvm`, `reminder.android.library[.compose]`,
`reminder.android.application[.compose]`), included via `pluginManagement.includeBuild("build-logic")` in
`settings.gradle.kts`. A module's own `build.gradle.kts` should only need `plugins { id("reminder.*") }`,
`android { namespace = "..." }`, and its `dependencies {}` block — put any genuinely module-specific Android
config there, not in the convention plugin.

```bash
# Build a flavor (debug)
./gradlew assembleFreeDebug
./gradlew assembleProDebug

# Run Kotlin-only module unit tests (domain, usecase:*, sync, cloud-api, etc.)
./gradlew test

# Run Android module unit tests for a specific flavor
./gradlew testProDebugUnitTest
./gradlew testFreeDebugUnitTest

# Run every test in one module
./gradlew :usecase:reminders:test
./gradlew :app:testProDebugUnitTest

# Run a single test class
./gradlew :app:testProDebugUnitTest --tests "com.elementary.tasks.SomeClassTest"
```

CI (`.github/workflows/build_and_test.yml`) runs `testProDebugUnitTest` for Android modules and `test` for
pure-Kotlin modules, excluding `:cloudtestadmin`, `:reviewsadmin`, and `:reviews` (admin/debug-only modules).
`app/src/free/google-services.json` and `app/src/pro/google-services.json` are required for the `app` module
to build and already exist in this checkout — don't remove them.

Release signing is controlled by an optional `keystore.properties` file at the repo root (`signApk=true` plus
key paths/passwords). Without it, builds are unsigned and that's expected locally.

Use only the `pro` flavor's debug variant for on-device verification and unit tests — `assembleProDebug` /
`testProDebugUnitTest` (never `free`). Don't run `assembleFreeDebug` or `testFreeDebugUnitTest` unless the
task specifically concerns free-flavor-only code.

## Architecture

Multi-module Clean Architecture. The dependency rule that matters most: **lower layers never depend on
upper layers, and `*-api` modules contain interfaces only — implementations live in the sibling non-`-api`
module.**

```
domain            pure Kotlin models/enums, zero dependencies (the foundation)
repository-api    repository interfaces, depends on domain
repository        Room implementation of repository-api (DAOs, entities, entity<->domain mappers)
cloud-api         interfaces for Google Drive/Tasks + Dropbox access
cloud             concrete cloud-api implementations
usecase:reminders, usecase:notes, usecase:birthdays, usecase:googletasks
                  pure read-logic per feature; depend ONLY on repository-api, never repository
sync              cloud-provider-agnostic sync orchestration; depends on cloud-api + repository-api
icalendar         .ics serialization + RRULE evaluation (lib-recur)
logging-api / logging   Logger interface, with Logback+SLF4J+Crashlytics implementation
analytics         Firebase Analytics wrapper
navigation-api    DeepLinkDestination contracts shared across app/widgets
feature-common    DispatcherProvider, SingleLiveEvent, Flow extensions (Kotlin/Android utils)
platform-common   biometric auth, permissions, Google sign-in helpers
ui-common         shared Compose components + Material 3 tokens
appwidgets        home-screen widgets; aggregates the usecase:* modules
reviews / reviewsadmin / cloudtestadmin   in-app review flow + debug-only admin tooling
app               the only module that wires concrete Koin bindings; hosts all UI (Activities/Fragments/
                  Compose), ViewModels, and the nav graph
```

Per-feature vertical slice: `app` ViewModel/UI uses `usecase:*` for reads and calls `repository-api` directly
for writes → `usecase:*` calls `repository-api` → `repository` implements it against Room.

Each module that exposes injectables declares its own `KoinModule.kt` with a `module { }` block; `app`
collects all of them into `startKoin {}`. Never call `GlobalContext.get()` outside DI wiring code.

## Conventions

- Kotlin idioms (data/sealed classes, extension functions); prefer `val`; coroutines (`suspend`/`Flow`/
  `StateFlow`) everywhere — no raw threads or `AsyncTask`. Use `DispatcherProvider` from `feature-common`
  instead of hardcoding `Dispatchers.IO`.
- New screens: Jetpack Compose + Material 3, stateless composables driven by a ViewModel exposing
  `StateFlow`/`LiveData`, connected via `collectAsStateWithLifecycle()`. Legacy XML/View screens may remain
  but shouldn't be extended. Reuse `ui-common` before adding new shared components.
- No logic in Fragments, Activities, or Compose screens — they only render state and forward user actions
  to the ViewModel (`onSomeClick = { viewModel.onSomeClick() }`). Anything beyond that (business logic,
  branching, calls into use cases/repositories/DI singletons, formatting decisions) belongs in the
  ViewModel. UI classes should stay thin enough to read at a glance.
- Logging only through the `Logger` interface (`logging-api`) — never `println`/`android.util.Log`.
- Room Entity <-> domain-model mapping is mandatory at the `repository` boundary; domain models must stay
  free of Room/Gson annotations.
- Tests: JUnit 4 + MockK (not Mockito, despite it still being on the `app` test classpath), AAA structure,
  plain-English test names ("returns empty list when reminder is expired"). Unit tests in `src/test/`,
  instrumented tests in `src/androidTest/`.
- Conventional commit messages (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`); one logical change
  per PR.
