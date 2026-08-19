# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Reminder (`com.cray.software.justreminder`) — a Kotlin Android task/reminder manager with notes, birthday
tracking, Google Tasks integration, calendar view, and cloud backup (Google Drive, Dropbox). Ships as two
product flavors: `free` and `pro`.

For deeper background, read (don't duplicate into this file):
- `docs/app-overview.md` — full feature list and user-facing screens
- `docs/architecture.md` — module inventory and dependency graph
- `docs/adaptive-layouts.md` — tablet/desktop/foldable layout conventions (nav rail, two-pane, breakpoints)
- `docs/multiselect.md` — long-press bulk-selection pattern (`Selectable`, `SelectionTopBar`, `SelectionOverlay`)
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

Modules are nested under group folders (`core/`, `data/`, `ui/`, `logic/`, `feature/`, `extensions/`,
`admin/`) plus `app` at the repo root — e.g. `repository-api` is Gradle path `:data:repository-api`, not
`:repository-api`. See `docs/architecture.md` ("Module Groups") for the full group → module mapping.

```bash
# Build a flavor (debug)
./gradlew assembleFreeDebug
./gradlew assembleProDebug

# Run Kotlin-only module unit tests (core:domain, data:sync, data:cloud-api, etc.)
./gradlew test

# Run Android module unit tests for a specific flavor
./gradlew testProDebugUnitTest
./gradlew testFreeDebugUnitTest

# Run every test in one module
./gradlew :logic:logic-reminder:test
./gradlew :app:testProDebugUnitTest

# Run a single test class
./gradlew :app:testProDebugUnitTest --tests "com.elementary.tasks.SomeClassTest"
```

CI (`.github/workflows/build_and_test.yml`) runs `testProDebugUnitTest` for Android modules and `test` for
pure-Kotlin modules, excluding `:admin:cloudtestadmin`, `:admin:reviewsadmin`, and `:admin:reviews`
(admin/debug-only modules).
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
core:domain            pure Kotlin models/enums, zero dependencies (the foundation)
data:repository-api    repository interfaces, depends on domain
data:repository        Room implementation of repository-api (DAOs, entities, entity<->domain mappers)
data:cloud-api         interfaces for Google Drive/Tasks + Dropbox access
data:cloud             concrete cloud-api implementations
data:sync              cloud-provider-agnostic sync orchestration; depends on cloud-api + repository-api
data:icalendar         .ics serialization + RRULE evaluation (lib-recur)
core:logging-api / core:logging   Logger interface, with Logback+SLF4J+Crashlytics implementation
core:analytics         Firebase Analytics wrapper
core:navigation-api    DeepLinkDestination contracts shared across app/widgets
core:feature-common    DispatcherProvider, SingleLiveEvent, Flow extensions (Kotlin/Android utils)
core:platform-common   biometric auth, permissions, Google sign-in helpers
ui:ui-common           shared Compose components + Material 3 tokens
extensions:appwidgets  home-screen widgets, aggregating feature/logic modules per data type
admin:reviews / admin:reviewsadmin / admin:cloudtestadmin   in-app review flow + debug-only admin tooling
app               the only module that wires concrete Koin bindings; hosts all UI (Activities/Fragments/
                  Compose), ViewModels, and the nav graph
```

See `docs/architecture.md` ("Module Groups") for the full group → module mapping — `feature:*`, `ui:*`,
and `logic:*` module families aren't repeated here.

Per-feature vertical slice: `app` ViewModel/UI reads and writes through `repository-api` directly, or
through a `logic:*` module's use cases where the logic is shared across features → `repository-api` →
`repository` implements it against Room. (An older `usecase:*` read-only layer described elsewhere in
this repo's docs no longer exists as a module family — see the "Known doc gap" note in
`docs/architecture.md`'s Module Inventory; don't assume it's still there.)

Each module that exposes injectables declares its own `KoinModule.kt` with a `module { }` block; `app`
collects all of them into `startKoin {}`. Never call `GlobalContext.get()` outside DI wiring code.

## Conventions

- Kotlin idioms (data/sealed classes, extension functions); prefer `val`; coroutines (`suspend`/`Flow`/
  `StateFlow`) everywhere — no raw threads or `AsyncTask`. Use `DispatcherProvider` from `feature-common`
  instead of hardcoding `Dispatchers.IO`.
- A class or top-level function/property used only within its own Gradle module should be marked
  `internal`, not left implicitly `public`. Only widen it back to `public` once another module's source
  actually references it by name — including a `KoinModule.kt` that registers it from a different module
  than the one the class lives in, which does happen here since a class's Koin binding doesn't have to be
  declared in the same module as the class itself.
- New screens: Jetpack Compose + Material 3, stateless composables driven by a ViewModel exposing
  `StateFlow`/`LiveData`, connected via `collectAsStateWithLifecycle()`. Legacy XML/View screens may remain
  but shouldn't be extended. Reuse `ui-common` before adding new shared components.
- Composable functions take `modifier: Modifier = Modifier` as their first parameter, before every other
  parameter (required ones included) — matches the official Compose API guidelines and keeps call sites
  consistent across the codebase.
- No logic in Fragments, Activities, or Compose screens — they only render state and forward user actions
  to the ViewModel (`onSomeClick = { viewModel.onSomeClick() }`). Anything beyond that (business logic,
  branching, calls into use cases/repositories/DI singletons, formatting decisions) belongs in the
  ViewModel. UI classes should stay thin enough to read at a glance.
- New `<string>`/`<plurals>` resources are appended at the end of `strings.xml` (before `</resources>`),
  not inserted next to whatever existing entry looks topically related — keeps diffs additive-only and
  merge conflicts rare. Translate into every `values-*/strings.xml` the app ships, appended the same way.
- Icons are never referenced as a bare `R.drawable.ic_fluent_*` (or any other drawable) from feature/screen
  code. `ui-common`'s `com.github.naz013.ui.common.icon.DrawableCatalog` (plain `@DrawableRes Int`
  constants, grouped by family — `DrawableCatalog.Fluent.X`, `DrawableCatalog.Builder.X`) and
  `com.github.naz013.ui.common.compose.AppIcons` (the same catalog wrapped as `@Composable` `Painter`
  getters — `AppIcons.Fluent.X`) are the only two places a drawable resource ID is allowed to be looked up
  from. Use `AppIcons.*` wherever a `Painter`/`ImageVector` is expected (`Icon(...)`, `MenuIconButton(icon =
  ...)`); use `DrawableCatalog.*` wherever a raw `@DrawableRes Int` is expected (e.g. `PopupMenuItem.iconRes`).
  If the icon you need isn't cataloged yet, add it to `DrawableCatalog` (and `AppIcons` if a `Painter` form
  is also needed) alongside the new drawable XML, then reference the catalog constant — don't inline
  `R.drawable.*` as a shortcut.
- Logging only through the `Logger` interface (`logging-api`) — never `println`/`android.util.Log`.
- Room Entity <-> domain-model mapping is mandatory at the `repository` boundary; domain models must stay
  free of Room/Gson annotations.
- Adding/removing a field on a domain model that has cloud/local backup support (`ReminderV2`, `Note`,
  `GroupV2`, `Tag`, etc. — check for a matching `*Json` class in `data:files-api`'s
  `com.github.naz013.files.model` package) means updating **three** places, not just the Room entity/mapper:
  the `*Json` DTO in `data:files-api` (new field needs a safe default so old backup files without it still
  deserialize), and both mapping directions (`toDomain()`/`toJson()`) in `data:files`'s
  `DataConverterImpl.kt`. This wire format is what Google Drive/Dropbox sync and the PRO local encrypted
  backup actually serialize — `data:sync` itself has no field-level knowledge of any domain type (it only
  type-dispatches on `is ReminderV2`/etc. and passes whole objects through), so the `*Json`/`DataConverterImpl`
  pair is the only place a schema change needs to be threaded through for backup/restore to round-trip it.
  A field added only to the Room entity+domain model survives local use but silently drops out of every
  backup and cloud-synced copy.
- Tests: JUnit 4 + MockK (not Mockito, despite it still being on the `app` test classpath), AAA structure,
  plain-English test names ("returns empty list when reminder is expired"). Unit tests in `src/test/`,
  instrumented tests in `src/androidTest/`.
- Conventional commit messages (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`); one logical change
  per PR.
