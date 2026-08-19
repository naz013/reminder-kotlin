# Reminder Kotlin Project Rules

## Architecture: Multi-module Clean Architecture
Modules are nested under seven group folders (`core/`, `data/`, `ui/`, `logic/`, `feature/`,
`extensions/`, `admin/`) plus `app` at the root — e.g. `repository-api` is `:data:repository-api`, not
`:repository-api`. See `docs/architecture.md` ("Module Groups") for the full mapping. Leaf module names
below are unqualified for readability; prepend the group to get the real Gradle path.

- **domain** (`core/`): Pure Kotlin, zero external dependencies. Foundation for all.
- **repository-api** (`data/`): Interfaces + domain. No implementation.
- **repository** (`data/`): Room implementation of `repository-api`.
- **cloud-api** (`data/`): Cloud storage interfaces.
- **cloud** (`data/`): Implementations (Google Drive/Tasks, Dropbox).
- **usecase:* modules**: Pure logic. Depend ONLY on `repository-api` (never `repository`). **Stale** —
  no `usecase:*` modules exist in `settings.gradle.kts` today; see the "Known doc gap" callout in
  `docs/architecture.md`'s Module Inventory before relying on this.
- **sync** (`data/`): Orchestration via `cloud-api` and `repository-api`.
- **app**: Entry point. Wires implementations via Koin.
- **ui-common** (`ui/`): Shared Compose components + Material 3 tokens.

## Coding Standards
- **Language**: Kotlin.
- **UI**: Jetpack Compose + Material 3.
- **Icons**: never inline `R.drawable.ic_fluent_*` (or any other drawable) in feature/screen code. Look it
  up via `ui-common`'s `DrawableCatalog` (`com.github.naz013.ui.common.icon.DrawableCatalog` - plain
  `@DrawableRes Int` constants, e.g. `DrawableCatalog.Fluent.Pin`) or `AppIcons`
  (`com.github.naz013.ui.common.compose.AppIcons` - the same catalog as `@Composable` `Painter` getters,
  e.g. `AppIcons.Fluent.Pin`). Use `AppIcons.*` where a `Painter`/`ImageVector` is expected; use
  `DrawableCatalog.*` where a raw `Int` is expected (e.g. `PopupMenuItem.iconRes`). Missing icon? Add the
  drawable XML plus a `DrawableCatalog` (and `AppIcons`, if needed) entry, then reference the constant.
- **Dependency Injection**: Koin. Use `KoinModule.kt` in each module.
- **State**: MVVM with `StateFlow` / `LiveData`.
- **Log**: Use `logging-api` (`Logger` interface) to avoid concrete coupling.
- **Visibility**: mark a class or top-level function/property `internal` if nothing outside its own
  Gradle module references it by name. Widen to `public` only once a real cross-module caller shows up
  — including a `KoinModule.kt` living in a different module than the class it binds, which this repo
  does allow.
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
  - Adding a field to a domain model that has a `*Json` counterpart (`ReminderV2Json`, `NoteV3Json`,
    `GroupV2Json`, `TagJson`, ...) is NOT covered by updating the Room entity/mapper alone - that only
    fixes local storage. You also need the new field on the `*Json` class (with a safe default, so old
    backup files without it still deserialize) and both directions of the mapping in `data:files`'s
    `DataConverterImpl.kt` (`toDomain()`/`toJson()`). This wire format is what cloud sync (Google
    Drive/Dropbox) and the PRO local encrypted backup actually read/write - `data:sync` itself is
    field-agnostic (type-dispatches on `is ReminderV2`/etc. and passes whole objects through), so
    skipping this step means the field silently never survives a backup/restore round-trip even
    though everything works locally.

## Feature Modules: `feature-*` / `ui-*` / `logic-*`
Newer features (Google Tasks, Tags, Insights) are extracted out of `app` into a per-feature module
family instead of living in `app`. Ongoing migration - not every feature has all three yet, check
before assuming. Full guide + checklist for creating one: `docs/architecture.md` ("Feature Modules").
- **`feature-<name>`**: screens, ViewModels, `<Feature>NavKey`/`<Feature>NavGraph`, own `KoinModule.kt`,
  feature-private orchestration use cases. Only `app` depends on it; it never depends on `app` or on
  another `feature-*` module.
- **`ui-<name>`** (sibling, optional): reusable Compose building blocks for the feature's domain type
  (item adapters, list items). No navigation, no ViewModels.
- **`logic-<name>`** (sibling, optional): reusable business logic/use cases for the feature that
  *other* features also need. No Compose. E.g. `feature-googletask` depends on `logic-reminder`, not
  `feature-reminder`, to complete a linked reminder.
- Add a `ui-*`/`logic-*` sibling only once a second consumer actually needs it - don't scaffold ahead
  of need.

## Dependency Rules
- Arrows: `A -> B` (A depends on B).
- `usecase` -> `repository-api` (NOT `repository`).
- `feature` -> `*-api` modules (for decoupling).
- `feature-*` -> its own `ui-*`/`logic-*` siblings and other `logic-*` modules; never -> another `feature-*` or `app`.
- `domain` is the root (no arrows outgoing).

## Build System
- Gradle Kotlin DSL (`.kts`).
- Version catalog: `libs.versions.toml`.
- Code analysis: `detekt` (all modules via `reminder.detekt` convention plugin; config at `config/detekt/detekt.yml`).
- Coverage: `kover` (all modules via `reminder.kover`; root aggregates JVM modules).
