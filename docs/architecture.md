# Architecture & Module Dependency

## Overview

The project follows a **multi-module Clean Architecture** approach. Concerns are separated into distinct Gradle modules so that:

- The `domain` and `*-api` modules contain no Android framework dependencies and can be unit-tested without an emulator.
- The `app` module only wires everything together; all business logic lives in lower-layer modules.
- Feature modules depend on *interfaces* (`-api` modules), not on concrete implementations, which keeps them decoupled and swappable.

---

## Module Groups

Physical module directories are nested under seven top-level group folders (plus `app` at the repo
root), so `settings.gradle.kts` includes e.g. `:core:domain` or `:feature:feature-note` instead of a
flat `:domain` / `:feature-note`. Grouping is purely organizational — a folder like `core/` or `data/`
is not itself a buildable module (Gradle treats it as an empty container project); dependency rules
between the actual leaf modules are unchanged from before the reorg.

| Group | Contains | Why together |
|---|---|---|
| `core/` | `domain`, `logging`, `logging-api`, `feature-common`, `feature-flags-api`, `navigation-api`, `analytics`, `date-calculations`, `platform-common`, `platform-api`, `preferences`, `crypto`, `testing` | Foundation every other layer depends on |
| `data/` | `repository(-api)`, `cloud(-api)`, `sync`, `files(-api)`, `work(-api)`, `icalendar(-api)`, `holidays(-api)`, `legal(-api)`, `digest(-api)`, `demo-photo(-api)`, `googlecalendar-api`, `location-api`, `notification-api`, `scheduler-api` | Persistence, cloud, sync, and the thin platform-contract `-api` modules `app` implements directly |
| `ui/` | `ui-common`, `ui-map`, `ui-note`, `ui-reminder`, `ui-tag`, `ui-googletask`, `ui-group`, `ui-agenda`, `ui-birthday`, `ui-routine`, `ui-notification-settings`, `ui-icons-solar` | Shared Compose building blocks, no navigation/ViewModels |
| `logic/` | `logic-reminder`, `logic-note`, `logic-tag`, `logic-googletask`, `logic-group`, `logic-birthday`, `logic-schedule`, `logic-workflow`, `logic-habit`, `logic-routine`, `logic-quickadd`, `logic-notification-action`, `logic-demo-data` | Cross-feature business logic |
| `feature/` | `feature-home`, `feature-agenda`, `feature-calendar`, `feature-reminder`, `feature-note`, `feature-birthday`, `feature-googletask`, `feature-places`, `feature-group`, `feature-tags`, `feature-insights`, `feature-workflow`, `feature-habit`, `feature-routine`, `feature-pomodoro`, `feature-onboarding`, `feature-settings` | Vertical feature slices; only `app` depends on these |
| `extensions/` | `appwidgets(-api)`, `appfunctions`, `localbackup`, `wearsync(-api)` | Cross-feature, flavor/runtime-gated additions `app` pulls in as a unit |
| `admin/` | `cloudtestadmin`, `reviews`, `reviewsadmin` | Debug/internal-only tooling, excluded from release builds and most CI test runs |

The leaf module's own directory/Gradle name is unchanged by the move (e.g. `repository-api` is still
named `repository-api`, just included as `:data:repository-api`) — only the group prefix is new. The
rest of this document refers to modules by their leaf name for
readability; prepend the group from the table above to get the full Gradle path (e.g. `sync` →
`:data:sync`).

---

## Module Inventory

| Module | Type | Purpose |
|---|---|---|
| `app` | Android Application | Entry point. Hosts all UI screens (Activities, Fragments, Compose screens), ViewModels, navigation graph, and Koin DI wiring. |
| `core:domain` | Kotlin library | Pure data models and enums (`Reminder`, `Note`, `Birthday`, `GoogleTask`, `Place`, `ReminderType`, …). Zero Android/framework dependencies. |
| `data:repository-api` | Android library | Repository interfaces (`ReminderRepository`, `NoteRepository`, `BirthdayRepository`, etc.) and `TableChangeListener` observers. Depends on `domain`. |
| `data:repository` | Android library | Room-based implementation of `repository-api`. Contains the Room database, DAOs, and entity-to-domain mappers. |
| `data:cloud-api` | Kotlin library | Interfaces for cloud file access (`CloudFileApi`, `GoogleDriveApi`, `DropboxApi`, `GoogleTasksApi`) and auth managers. |
| `data:cloud` | Android library | Concrete implementations of `cloud-api` using the Google Drive REST SDK, Google Tasks SDK, and Dropbox Android SDK. |
| `data:sync` | Kotlin library | Cloud-sync orchestration use-cases: upload, download, delete, and diff-check between local and remote file metadata. Depends on `cloud-api` and `repository-api`. |
| `data:icalendar` | Android library | iCalendar (`.ics`) serialisation / deserialisation and RRULE evaluation using the lib-recur library. |
| `core:analytics` | Android library | Firebase Analytics wrapper; provides a single `AnalyticsManager` abstraction over Firebase events. |
| `core:logging-api` | Kotlin library | `Logger` interface with no implementation. All other modules depend on this so that log calls compile without pulling in a concrete logging library. |
| `core:logging` | Android library | Logback + SLF4J + Firebase Crashlytics implementation of `logging-api`. |
| `core:navigation-api` | Android library | `DeepLinkDestination` and screen-navigation contracts shared between `app` and feature/widget modules. |
| `core:feature-flags-api` | Kotlin library | `FeatureFlags` interface + `FeatureFlag` enum. Answers "is this feature enabled" without exposing where flags come from (SharedPreferences + Firebase Remote Config); implemented by `FeatureManager` in `app`. Any Kotlin-only module can depend on this without depending on `app`. |
| `core:feature-common` | Android library | Shared Kotlin/Android utilities: coroutine `DispatcherProvider`, `SingleLiveEvent`, Flow extensions, `SystemServiceProvider`, etc. |
| `core:platform-common` | Android library | Android-platform-level helpers: biometric auth, permission helpers, Google sign-in wrappers, camera/gallery pickers (`rememberCameraPicker`/`rememberGalleryPicker`), and speech-to-text (`SpeechEngine`). |
| `ui:ui-common` | Android library | Shared Compose components, Material 3 theme tokens, reusable composables, and color-picker utilities. |
| `ui:ui-map` | Android library (Compose) | `SimpleMapView` — the stateful Google Maps widget (tap-to-set marker + radius circle, layers/marker-style/radius/recent-places picker chrome) extracted from `app` so `feature-reminder` and `places` (still in `app`) can both depend on it directly instead of routing map screens through callback threading. Also owns `GeocoderTask`, the recent-places `UiPlaceList`/`UiPlaceListAdapter`, and `DefaultRadiusFormatter`. Exposes a `MapPreferences` seam interface (`app`'s `MapPreferencesImpl` delegates to `Prefs`) and takes `BuildInfo` for the one PRO-gated behavior (marker-style picker visibility), rather than depending on `app`-only `Prefs`/`BuildParams` directly. Unlike other `ui-<feature>` modules it does own a ViewModel (`SimpleMapViewViewModel`) and a Koin module — a deliberate exception since it's a reusable stateful widget, not a full screen with its own nav graph. |
| `extensions:appwidgets` | Android library | All home-screen widget implementations (reminders, notes, birthdays, Google Tasks), plus the compact "Next Reminder" widget purpose-built for lock-screen/keyguard placement and the Android 14+ Glanceable Hub (`widgetCategory="home_screen\|keyguard"` + `widgetFeatures="configuration_optional"` on every provider). Reads through `repository-api` and the `logic-*` modules. |
| `extensions:appfunctions` | Android library | Exposes reminders/notes/birthdays/Google Tasks capabilities to Gemini and other on-device assistants via the Android `androidx.appfunctions` platform API. **PRO-only**: wired into `app` via `"proImplementation"(project(":extensions:appfunctions"))`, never a plain `implementation` — see "Flavor-gated modules" below. For on-device/`adb` testing, see [appfunctions-testing.md](appfunctions-testing.md). |
| `reviews` | Android library | In-app review flow using the Play Core / Play Review API. |
| `reviewsadmin` | Android library | Internal admin tooling for the review flow (debug/test only). |
| `cloudtestadmin` | Android library | Internal admin tooling for testing cloud integrations (debug/test only). — these three live under `admin/`, see "Module Groups" above. |
| `feature:feature-googletask` | Android library (Compose) | Fully extracted Google Tasks feature: screens, ViewModels, `GoogleTasksNavKey`/`GoogleTasksNavGraph`, feature-private sync/upload/download orchestration use cases, own `KoinModule`. See "Feature Modules" below. |
| `ui:ui-googletask` | Android library (Compose) | Small reusable Compose pieces for Google Tasks: item-state adapter, Google Sign-In Compose helper (`rememberGoogleTasksLogin`). No navigation, no ViewModels. |
| `logic:logic-googletask` | Android library | Cross-feature Google Tasks logic shared by `feature-googletask` and other consumers. |
| `feature:feature-tags` | Android library (Compose) | Fully extracted Tags feature: screens, ViewModels, `TagsNavKey`/`TagsNavGraph`, own `KoinModule`. Tag data now lives in the shared `AppDb` via `repository-api`'s `TagRepository`/`TagAssignmentRepository` — there is no separate `tags_db` any more. Both flavors. |
| `ui:ui-tag` | Android library (Compose) | Reusable Tags Compose building blocks (chip picker, item styling) usable outside `feature-tags`. |
| `ui:ui-icons-solar` | Android library (resources only) | The "Solar Linear" icon pack (480 Design, 1246 icons) converted to VectorDrawables (`ic_solar_*`) plus the `SolarIcons` catalog of `@DrawableRes` ids grouped by upstream category. No dependencies. Used by `ui-routine`'s `RoutineIconSet` for routine/habit icons; see the module `README.md` for source and licensing. |
| `logic:logic-habit` | Android library | Habit Tracker business logic: `ObserveHabitsWithProgressUseCase` (habits + today's count + streak), `ObserveHabitDetailsUseCase` (one habit + best streak, totals and the per-day grid from `HabitHistoryBuilder`), `CheckInHabitUseCase` (toggle-or-increment rule), `Toggle`/`IncrementHabitCheckInUseCase`, `Save`/`Delete`/`GetHabitUseCase`, and the pure `HabitStreakCalculator`. Reads through `HabitRepository`/`HabitCheckInRepository`. Gating (`HabitConfig`) lives in `logic-routine`. |
| `logic:logic-tag` | Kotlin library | Cross-feature tag logic, e.g. `ToggleTagAssignmentUseCase`, usable by any `feature-*` module that assigns tags. |
| `feature:feature-insights` | Android library (Compose) | Fully extracted **PRO-only** Streaks & Insights dashboard: screens, ViewModel, `InsightsNavKey`/`InsightsNavGraph`, aggregator classes (`ReminderStreakCalculator`, `CompletionStatsCalculator`), own `KoinModule`. Purely computed from `repository-api` — no schema of its own. Gated at runtime via `BuildInfo.isPro`, not a build-time flavor split. No sibling `ui-*`/`logic-*` module yet (nothing shared elsewhere). |
| `feature:feature-habit` | Android library (Compose) | Habit Tracker list screen (adaptive card grid with one-tap check-in, streak badge, empty state), the read-only preview screen (`HabitPreviewScreen`: check-in, streak/best/total stats, GitHub-style `HabitActivityBar` of the last 26 weeks, ad banner via `adsContent`; a card tap opens it, Edit opens the editor), `HabitNavKey`/`habitEntries`, own `KoinModule` (`habitModule`). Reads through `logic-habit`; reuses `ui-routine`'s color/icon palettes. Reached from the Home grid and nav rail via the `HABITS` header section, shown only when `HabitConfig.isEnabled` (flag and PRO). `habitEntries` registers the list, the preview (`HabitNavKey.Preview`) and the create/edit screen (`HabitNavKey.Editor`). |
| `feature:feature-workflow` | Android library (Compose) | Fully extracted Workflow automation-rules feature: gallery/group/builder screens and ViewModels, `WorkflowNavKey`/`WorkflowNavGraph`, background-polling `BackgroundTask`s (`RunWorkflowRulesTask`, `RunWorkflowUnacknowledgedRulesTask`), own `KoinModule`. Reads through `logic-workflow` (`WorkflowEngine` and friends), depends on `logic-reminder` for `ActivateReminderUseCase`/`CompleteReminderUseCase` to finish the actions the pure-JVM engine can't apply itself. No sibling `ui-*` module yet (nothing Compose-shared elsewhere). |
| `logic:logic-workflow` | Kotlin library | Cross-feature-shaped workflow engine and rule/template use cases: `WorkflowEngine` (evaluates enabled `WorkflowRule`s against reminder/group state and runs their action, returning `PendingWorkflowAction` for the two actions — `CompleteReminder`/`ActivateReminder` — it can't finish itself), `ApplyWorkflowTemplateUseCase`, `CreateWorkflowRuleUseCase`, `SaveWorkflowRuleAsTemplateUseCase`, `GetWorkflowTemplatesUseCase`, and `WorkflowTemplate.isExecutable()` — scope lookups (global/group/reminder) go through `WorkflowRuleRepository.observeByScope`/`getByScope` directly, not a dedicated use case. `feature-workflow` is currently its only consumer. |
| `feature:feature-note` | Android library (Compose) | Fully extracted Notes feature: list/archive, edit, preview, and image-viewer screens, `NotesNavKey`/`NotesNavGraph`, own `KoinModule`, and feature-private use cases (`SaveNoteUseCase`, `DeleteNoteUseCase`, `ChangeNoteArchiveStateUseCase`, `CreateSharedNoteFileUseCase`). Threads `applicationId`, `adsContent`, `onOpenNoteSettings`, and `onEditReminder` in from `AppNavGraph.kt` since it can't reference `app`'s `BuildConfig`, ad banners, or `SettingsNavKey`/`BuildReminderNavKey` directly. |
| `ui:ui-note` | Android library (Compose) | Canonical Notes UI model and Compose building blocks: `UiNoteListItem`/`UiNoteImage` + their adapters, `NoteColorEngine`, and one shared `NoteCard`/`CheckableNoteCard` — replaces three note-card implementations that had drifted apart (app's old `NoteCard`, feature-note's leaner `NoteListItemCard`, and the Single Note widget config screen's bespoke rendering). Exposes `NotePreferences`/`NoteFontProvider`/`NoteNotifier` seam interfaces implemented by `app` (`core/notes/AppNote*`) and bound via Koin there, since `ui-note` can't depend on `app`'s `Prefs`/`AssetsUtil`/`Notifier`. |
| `logic:logic-note` | Kotlin library | Cross-feature Notes business logic used by `feature-note` and other features. |
| `feature:feature-reminder` | Android library (Compose) | Fully extracted Reminders feature: build/edit, action popup, preview, archive, and settings screens, `BuildReminderNavKey`/`ReminderPreviewNavKey`/other nav keys and their `*NavGraph.kt`s, own `KoinModule`, and feature-private orchestration (builders, decomposers, validators, use cases). Owns its map screens directly (`MapEditorScreen`/`MapValueEditor` for the location builder, `EmbeddedMap`/`FullscreenMapEntry` for the preview screen) via a direct dependency on `ui-map` — the `mapEditorContent`/`embeddedMapContent`/`fullscreenMapEntryContent` callback threading these used to need was removed once `ui-map` existed for both `feature-reminder` and `places` to depend on. Still threads genuinely app-only infrastructure it can't depend on directly — ads, OS pickers, file/intent sending, and cross-feature navigation into Notes/Google Tasks — as composable/callback params supplied by `AppNavGraph.kt`. Depends on `ui-map`, `ui-reminder`, `logic-reminder`, and (for cross-feature rendering) `ui-note`/`ui-tag`/`ui-group`/`ui-googletask`/`feature-tags`. |
| `ui:ui-reminder` | Android library (Compose) | Reminder domain-type Compose building blocks and adapters (`UiReminderListAdapter`, `UiReminderCommonAdapter`, `UiReminderPlaceAdapter`, due-date/status formatting). No navigation, no ViewModels. |
| `logic:logic-reminder` | Kotlin library | Reminder behavior strategies, occurrence calculators (`*OccurrenceCalculatorV2`), save/delete/activate/pause/complete/snooze use cases, `AddReminderToHistoryUseCase`, and the `ReminderPreferences`/`ReminderNotifier` seam interfaces implemented by `app`. Consumed cross-feature by `feature-googletask` (`CompleteRelatedGoogleTaskUseCase`), `feature-workflow` (`ActivateReminderUseCase`/`CompleteReminderUseCase`), and `feature-reminder` itself. |
| `logic:logic-schedule` | Kotlin library | Background-work scheduling use cases (`ScheduleBackgroundWorkUseCase`, sync/upload/delete `BackgroundTask` impls) shared by multiple features via `work-api`. |
| `extensions:localbackup` | Android library (Compose) | **PRO-only** local encrypted backup/restore. Owns the crypto (PBKDF2 + AES-GCM), the archive framing (reusing `files-api`'s `DataConverter`), and the passphrase Compose UI. Gated at runtime via `BuildInfo.isPro`. |

### Other modules

Modules added after the table above was written. Each follows the `feature` / `ui` / `logic` roles described under "Feature Modules"; read the module's own source for detail.

| Module | Purpose |
|---|---|
| `feature:feature-home` | Home screen: chronological list, day-overview pane (list-detail on wide screens), header navigation grid. |
| `feature:feature-agenda` (+ `ui:ui-agenda`) | Reminders and birthdays agenda with Smart List and tag filters. |
| `feature:feature-calendar` | Month/week calendar with reminders, birthdays, Google Tasks and (flagged) public holidays. |
| `feature:feature-birthday` (+ `ui:ui-birthday`, `logic:logic-birthday`) | Birthday list, editor, preview and settings. |
| `feature:feature-places` | Saved places map screens. |
| `feature:feature-group` (+ `ui:ui-group`, `logic:logic-group`) | Reminder group list and editor. |
| `feature:feature-routine` (+ `ui:ui-routine`, `logic:logic-routine`) | Routines: sequenced, timed steps with a focus runner. See [routines.md](routines.md). |
| `feature:feature-pomodoro` | Pomodoro timer with a foreground notifier; feeds Insights. |
| `feature:feature-onboarding` | First-run flow. |
| `feature:feature-settings` | The settings hierarchy (largest feature module). |
| `ui:ui-notification-settings` | Shared notification-customization Compose pieces; see [reminderv2-notification-hierarchy.md](reminderv2-notification-hierarchy.md). |
| `logic:logic-notification-action` | Reminder and birthday alarm / notification-action pipeline (`ReminderActionProcessor`, `BirthdayActionProcessor`, in-app alert bus). |
| `logic:logic-quickadd` | On-device natural-language parser for quick-add text, with per-language vocabularies. |
| `logic:logic-demo-data`, `data:demo-photo(-api)` | Demo content generation. |
| `data:digest(-api)` | On-device daily digest summarization behind `FeatureFlag.AI_DIGEST` and a device-capability check. |
| `data:holidays(-api)` | Public holiday sync, behind `FeatureFlag.PUBLIC_HOLIDAYS`. |
| `data:legal(-api)` | Legal documents access. |
| `core:preferences`, `core:crypto` | Preference storage engine and shared crypto helpers. |
| `extensions:wearsync(-api)` + `wear/wear-app` | Phone-side Wear OS data sync and the watch app (reminder list, next-reminder tile). |

---

## Dependency Graph

Arrows read "depends on". Per-module dependency lists are not maintained by hand any more (they drifted). To
see the exact edges of one module, read its `build.gradle.kts` or run
`./gradlew :<group>:<module>:dependencies`. The allowed directions are:

```
app
 ├── feature-*   (screens, ViewModels, NavKeys, own Koin module; only app may depend on these)
 │     ├── ui-*      (reusable Compose pieces; no navigation, no ViewModels)
 │     ├── logic-*   (cross-feature use cases; no Compose)
 │     └── ui-common, feature-common, platform-common, analytics, date-calculations
 ├── extensions:*    (appwidgets, appfunctions [PRO], localbackup, wearsync)
 ├── data:repository ──implements──> data:repository-api
 ├── data:cloud      ──implements──> data:cloud-api
 └── data:*-api / core:*-api interfaces implemented directly in app (scheduler, location, notification, ...)

logic-* / ui-* / feature-* / data:sync
 └── repository-api, cloud-api, work-api, files-api, ...   (interfaces only)
       └── core:domain                                      (no dependencies)
```

### Key architectural rules

1. **`domain` has zero external dependencies.** It is the foundation everything else builds on.
2. **`*-api` modules contain only interfaces and data types.** Implementations live in the corresponding non-`-api` module.
3. **Nothing except `app` depends on `repository` (the Room implementation).** `logic-*`, `ui-*` and `feature-*` modules go through `repository-api`.
4. **`sync` is cloud-provider agnostic.** It operates through `cloud-api` interfaces and is never aware of whether storage is Google Drive or Dropbox.
5. **`app` is the only module that wires concrete implementations** to their interfaces via Koin `KoinModule` objects.
6. **Flavor-gated modules** (currently `appfunctions`) are added in `app/build.gradle.kts` via `"proImplementation"(project(":x"))` / `"freeImplementation"(project(":x"))`, never a plain `implementation` — see "Flavor-gated modules" below for how `main`-sourceset code still references them.
7. **Newer features are extracted into a `feature-*` (+ optional `ui-*` / `logic-*`) module family instead of living in `app`.** Only `app` may depend on a `feature-*` module; `feature-*` modules never depend on `app` or on each other directly — a feature that needs another feature's logic depends on that feature's `logic-*` module instead (e.g. `feature-googletask` → `logic-reminder`, not → `feature-reminder`). See "Feature Modules" below.

---

### Flavor-gated modules

A module needed by only one product flavor (e.g. `appfunctions`, PRO-only) is added with `"proImplementation"(project(":extensions:appfunctions"))` in `app/build.gradle.kts` instead of a plain `implementation`, so the `free` APK never contains its code, its manifest entries, or its transitive dependencies.

Because `main`-sourceset code (e.g. `ReminderApp.kt`) can't reference types from a dependency that only one flavor sees, each flavor provides its own same-named shim class — e.g. `app/src/pro/.../AppFunctionsInitializer.kt` (real implementation, calls `loadKoinModules(appFunctionsModule)`) and `app/src/free/.../AppFunctionsInitializer.kt` (empty no-op) — mirroring the older `AdsProvider` free/pro split. `main` code calls the shim unconditionally; Gradle compiles whichever flavor's version is on the classpath for that variant. The flavor-gated module's own `KoinModule.kt` is therefore loaded at runtime via `loadKoinModules(...)` from the pro-flavor shim, not included in `ReminderApp.kt`'s static `startKoin { modules(listOf(...)) }` call like every other module's.

### Runtime vs. build-time PRO gating

`feature-insights` and `localbackup` are **PRO features gated at runtime**, not build-time flavor splits like `appfunctions`: both modules are plain `implementation(project(":x"))` dependencies present in every build, their Koin modules are registered unconditionally in `ReminderApp.kt`, and their Nav3 entries are always reachable in the graph. The only PRO check is a `buildInfo.isPro` read in `SettingsHubViewModel` that hides their Settings entry points on the free flavor (`SettingsHubState.isInsightsVisible` / `isLocalBackupVisible`). This was a deliberate simplicity trade-off — these two features have no free-flavor-only code paths or extra manifest entries worth stripping from the free APK, unlike `appfunctions`, so the Gradle-flavor-split machinery wasn't worth the extra complexity.

---

## Layered Architecture (per feature)

Each major feature follows the same vertical slice:

```
┌──────────────────────────────────────────────────┐
│  feature-*  (Compose screen -> ViewModel)         │
│     reads/writes through repository-api directly, │
│     or through logic-* use cases when the logic   │
│     is shared across features                     │
└────────────────────┬─────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │   logic-* (optional) │
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │   repository-api     │
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │   repository        │
          │   (Room + DAOs)      │
          └─────────────────────┘
```

An older `usecase:*` read-only layer used to sit between the UI and `repository-api`. It no longer exists.

---

## Feature Modules: `feature-*` / `ui-*` / `logic-*`

Most features (Google Tasks, Tags, Insights, Reminders, Notes, ...) have been extracted out of `app` into their own module
family instead of living entirely inside `app`. Not every extracted feature has all three module kinds. Check
what actually exists before assuming a feature follows the full pattern (see "Current extraction
status" below).

### The three module kinds

| Module kind | Example | Contains | Depends on | Consumed by |
|---|---|---|---|---|
| `logic-<feature>` | `logic-reminder`, `logic-tag`, `logic-schedule`, `logic-workflow` | Pure/Android-library business logic and use cases for the feature that **other features also need**. No Compose, no ViewModels, no navigation. | `domain`, `repository-api`, `logging-api`, whichever `*-api` modules it needs (`files-api`, `work-api`, `icalendar-api`, …), sometimes another `logic-*` module (`logic-tag` → `logic-schedule`) | Its own `feature-<feature>` module **and** any other `feature-*` module that needs that logic — e.g. `feature-googletask` depends on `logic-reminder` for `CompleteRelatedGoogleTaskUseCase`, not on `feature-reminder` |
| `ui-<feature>` | `ui-googletask`, `ui-tag`, `ui-reminder` | Small, reusable Compose building blocks for the feature's domain type — item-state adapters, list-item composables, auth/login Compose helpers. Deliberately free of navigation and ViewModels. | `domain`, `logging-api`, `ui-common`, `platform-common`/`platform-api`, occasionally `cloud-api` (e.g. `ui-googletask`'s Google Sign-In helper) | Its own `feature-<feature>` module **and** any other module that wants to render that feature's items |
| `feature-<feature>` | `feature-googletask`, `feature-tags`, `feature-insights` | The screens: ViewModels, Compose screens, a sealed `<Feature>NavKey : NavKey` per destination, a `<Feature>NavGraph.kt` exposing `fun EntryProviderScope<NavKey>.xyzEntries(backStack, ...)`, the feature's own `KoinModule.kt`, and any use cases that are genuinely feature-private orchestration (not reused elsewhere). | `domain`, `repository-api`, its own `ui-<feature>`/`logic-<feature>`, other `logic-*` modules it needs, `feature-common`, `ui-common`, `platform-common`/`platform-api`, `analytics`, `date-calculations` as needed | `app` **only** — wired into `ReminderApp.kt`'s `startKoin { modules(...) }` and into `AppNavGraph.kt`'s `entryProvider { }` via its `xyzEntries(backStack)` call |

`feature-*` modules never depend on `app` or on each other — see rule 7 above.

`feature-googletask` shows the full shape: it owns `GoogleTasksNavKey`/`GoogleTasksNavGraph` (five
destinations — list, task-list, task preview, task edit, list edit), five ViewModels, its own
`KoinModule.kt` (`featureGoogleTaskModule`), and a `usecase/` package of feature-private orchestration
(`SyncGoogleTaskList`, `UploadGoogleTask`, `AddNewTaskList`, `DownloadGoogleTasks`, …) that wrap
`cloud-api`/`repository-api` calls — these aren't shared with any other feature, so they live inside
`feature-googletask` rather than in `logic-googletask`. It pulls in `ui-googletask` for the Google
Sign-In Compose helper (`rememberGoogleTasksLogin`) and item-state adapter, and — notably —
`logic-reminder` for completing a reminder linked to a task.

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

- **Fully extracted** — screens, ViewModels, nav graph and Koin module all live in the `feature-*` module,
  wired via `xyzEntries()` in `AppNavGraph.kt`: every module listed under `feature/` in "Module Groups".
- **Still in `app`** — seam implementations (`*PreferencesImpl`, notifiers, `*Gate` impls), DI wiring, the
  nav graph, and the remaining legacy Activities/receivers/services.

`feature-reminder` is the largest extraction. App-only pieces it cannot own stay behind as seam interfaces
implemented in `app` (`ReminderPreferences`, `ReminderNotifier`), and genuinely app-only infrastructure (ads,
OS pickers, cross-feature navigation) is passed in as composable/callback parameters from `AppNavGraph.kt`.
Its map screens live in `feature-reminder` and depend on `ui-map` directly.

Don't assume every `feature-*` module owns a full vertical slice — check whether it has its own
`*NavGraph.kt`/`*NavKey.kt` before assuming its screens are wired outside `app`.

### Creating a new feature module

1. **Module skeleton.** Add `feature-<name>` (and `ui-<name>`/`logic-<name>` only once something is
   actually shared — see "Why the split" above) as `include(":feature:feature-<name>")` (and
   `include(":ui:ui-<name>")` / `include(":logic:logic-<name>")`) in `settings.gradle.kts`, with the
   directory nested under `feature/feature-<name>` / `ui/ui-<name>` / `logic/logic-<name>` to match — see
   "Module Groups" above. Give it a `build.gradle.kts` using `id("reminder.android.library.compose")`
   (add `alias(libs.plugins.kotlin.serialization)` if it defines `@Serializable` `NavKey`s) and
   `android { namespace = "com.github.naz013.feature.<name>" }`.
2. **Dependencies.** Only what the feature needs directly: `domain`, `repository-api`, `logging-api`, `feature-common`, `ui-common`,
   `platform-common`/`platform-api`, plus its own `ui-<name>`/`logic-<name>` and any cross-feature
   `logic-*` modules it calls into. Never depend on `repository` (the Room implementation) directly — go
   through `repository-api`.
3. **NavKey + NavGraph.** A `sealed interface <Feature>NavKey : NavKey` with one `@Serializable`
   destination per screen, and a `<Feature>NavGraph.kt` exposing
   `fun EntryProviderScope<NavKey>.xyzEntries(backStack: MutableList<NavKey>, ...)` that registers one
   `entry<...>` per destination (see `GoogleTasksNavGraph.kt` / `GoogleTasksNavKey.kt` for the pattern).
4. **`KoinModule.kt`.** A `val feature<Name>Module = module { ... }` registering the feature's
   ViewModels (`viewModel { ... }` / `viewModelOf`) and use cases (`factoryOf` / `factory<Interface>`).
5. **Wire into `app`.** Add `implementation(project(":feature:feature-<name>"))` in `app/build.gradle.kts`;
   import `feature<Name>Module` and add it to the `modules(listOf(...))` call in
   [ReminderApp.kt](../app/src/main/java/com/elementary/tasks/ReminderApp.kt); import the `xyzEntries`
   function and call it inside the `entryProvider { }` block in
   [AppNavGraph.kt](../app/src/main/java/com/elementary/tasks/navigation/nav3/AppNavGraph.kt).
6. **Tests.** Unit tests under `src/test/kotlin`, JUnit4 + MockK, following the existing
   `*ViewModelTest.kt` naming next to the class under test.

### Chained reminders

`ReminderV2.startAfterReminderId` makes a reminder wait for another one. The shared logic lives in
`logic:logic-reminder` (`ActivateChainedRemindersUseCase`, `ResolveChainedStartUseCase`,
`ReminderChainValidator`) and is called by every complete, delete and archive path, including
`extensions:appfunctions`. See [chained-reminders.md](chained-reminders.md).

---

## Dependency Injection (Koin)

Each module that exposes objects declares a `KoinModule.kt` file containing a Koin `module { }` block. The `app` module collects all `KoinModule` lists and passes them to `startKoin { }` in the Application class.

Modules that provide DI configuration:

- `data/cloud/KoinModule.kt`
- `data/repository/KoinModule.kt` (inferred from Room setup)
- `core:analytics/KoinModule.kt` (inferred)
- `core:navigation-api/KoinModule.kt`
- `core:feature-common/KoinModule.kt`
- `app/core/utils/KoinModule.kt` (top-level wiring)
- `feature/feature-googletask/KoinModule.kt`, `ui/ui-googletask/KoinModule.kt`, `feature/feature-tags/KoinModule.kt`, `ui/ui-tag/KoinModule.kt`, `logic/logic-tag/KoinModule.kt`, `logic/logic-reminder/KoinModule.kt`, `logic/logic-schedule/KoinModule.kt`, `feature/feature-insights/KoinModule.kt`, `feature/feature-workflow/KoinModule.kt`, `logic/logic-workflow/KoinModule.kt`, `extensions/localbackup/KoinModule.kt`, `feature/feature-note/KoinModule.kt`, `ui/ui-note/KoinModule.kt`, `ui/ui-map/KoinModule.kt` — all added to `startKoin { modules(listOf(...)) }` like any other module (see "Runtime vs. build-time PRO gating" above for why `feature-insights`/`localbackup` aren't loaded conditionally despite being PRO features)
- `extensions/appfunctions/KoinModule.kt` — the exception: loaded at runtime via `loadKoinModules(...)` from `app/src/pro`'s `AppFunctionsInitializer`, not added to `startKoin { modules(listOf(...)) }` (see "Flavor-gated modules" above)

---

## Build System

The project uses **Gradle with Kotlin DSL** (`build.gradle.kts`).

- A shared `libs.versions.toml` version catalog centralises all dependency versions.
- **Detekt** (`reminder.detekt` convention plugin) is applied to all modules for static analysis and formatting (wraps ktlint rules via `detekt-formatting`; config in `config/detekt/detekt.yml`).
- Firebase is integrated via the `google-services` and `crashlytics-gradle` plugins.
- Jetpack Compose compiler metrics/reports are generated automatically by the `compose.compiler` plugin.
- CI runs on GitHub Actions (`.github/workflows/build_and_test.yml`) and a release pipeline (`.github/workflows/build_and_publish_release.yml`).
