# ReminderV2 Notification Customization: the Settings → Group → Reminder Hierarchy

This documents the 3-level override architecture built for `ReminderV2`'s notification/screen/
haptics customization (color, vibration, sound, quiet hours, priority, category, DND bypass, wake
screen, lock screen visibility, etc.), so future work extending it — new fields, new UI, wiring it
into actual notification delivery — has one place to start from.

## The idea

Every customization parameter is settable at three levels, resolved with straightforward
null-coalescing, reminder wins, then group, then the global default:

```
effective = reminder.field ?: group?.field ?: settings.field
```

- **Settings** (global) is the base case: always fully populated, one value per field, app-wide.
- **Group** (`GroupV2`) is optional per-group overrides: any field can be left unset to inherit
  from Settings.
- **Reminder** (`ReminderV2`) is optional per-reminder overrides: any field can be left unset to
  inherit from its group (if it has one) or Settings.

A reminder with no group and no overrides at all just uses the Settings values everywhere — the
hierarchy degrades gracefully to "one global default" if nothing more specific is ever set.

## The two shapes

There are two versions of the same field list, living in
[`domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/ReminderV2.kt`](../domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/ReminderV2.kt)
and
[`NotificationSettingsResolver.kt`](../domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/NotificationSettingsResolver.kt)
in the same package:

- **`NotificationSettings`** — every field concrete (non-null), with a real default value. This is
  the *resolved* shape: what `ReminderSettingsRepository` always returns, and what
  `NotificationSettingsOverride.resolve(...)` produces.
- **`NotificationSettingsOverride`** — the same fields, all nullable, no meaningful defaults beyond
  `null`. This is what `GroupV2.notification` and `ReminderV2.notification` actually hold. `null`
  on any field means "inherit."

Current field list (keep both classes in sync — see the "adding a new field" checklist below):
`color`, `vibrate`, `vibrationPattern`, `repeatNotification`, `volume`, `soundUri`,
`quietHoursFrom`, `quietHoursTo`, `activeHours`, `delayMinutes`, `priority`, `category`,
`bypassDoNotDisturb`, `wakeScreen`, `lockScreenVisibility`, `remindBefore`.

There used to be a `useGlobalSettings: Boolean` blanket flag (mirroring V1's `Reminder.useGlobal`)
— it was removed because per-field nullability already expresses "use the global value," and
having both a blanket flag *and* granular nulls was a source of confusion, not a feature.

## Resolution

The merge itself is one pure function,
[`NotificationSettingsOverride.resolve(group, defaults)`](../domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/NotificationSettingsResolver.kt):

```kotlin
fun NotificationSettingsOverride.resolve(
  group: NotificationSettingsOverride?,
  defaults: NotificationSettings
): NotificationSettings = NotificationSettings(
  color = color ?: group?.color ?: defaults.color,
  vibrate = vibrate ?: group?.vibrate ?: defaults.vibrate,
  // ... one line per field
)
```

It's pure domain logic (no repository dependency), so it's usable anywhere — including a future
"effective settings" preview in the builder UI.

The use case that actually wires it up for a given reminder is
[`ResolveReminderV2NotificationSettingsUseCase`](../usecase/reminders/src/main/kotlin/com/github/naz013/usecase/reminders/ResolveReminderV2NotificationSettingsUseCase.kt)
(`usecase:reminders`):

```kotlin
class ResolveReminderV2NotificationSettingsUseCase(
  private val groupV2Repository: GroupV2Repository,
  private val reminderSettingsRepository: ReminderSettingsRepository,
) {
  suspend operator fun invoke(reminder: ReminderV2): NotificationSettings {
    val group = reminder.groupId?.let { groupV2Repository.getById(it) }
    return reminder.notification.resolve(
      group = group?.notification,
      defaults = reminderSettingsRepository.getNotificationDefaults()
    )
  }
}
```

**This use case is not called from anywhere in the app yet.** It exists and is tested (see
`ResolveReminderV2NotificationSettingsUseCaseTest`), ready for whoever wires `ReminderV2` into
actual notification delivery to call at the point a notification/alarm is built.

## The three levels, concretely

| Level | Type | Where it lives | Always populated? |
|---|---|---|---|
| Settings | `NotificationSettings` | `ReminderSettingsRepository` (repo-api interface, `Prefs`-backed impl in `app`) | Yes |
| Group | `GroupV2.notification: NotificationSettingsOverride` | `GroupV2` table (Room) | No — any/all fields can be null |
| Reminder | `ReminderV2.notification: NotificationSettingsOverride` | `ReminderV2` table (Room) | No — any/all fields can be null |

### Settings (global defaults)

[`ReminderSettingsRepository`](../repository-api/src/main/java/com/github/naz013/repository/ReminderSettingsRepository.kt)
(`repository-api`) is the abstraction:

```kotlin
interface ReminderSettingsRepository {
  fun getNotificationDefaults(): NotificationSettings
  fun setNotificationDefaults(settings: NotificationSettings)
}
```

It exists as an interface (rather than reading `Prefs` directly from `usecase:reminders`) because
`Prefs` is an Android/`Context`-coupled `SharedPreferences` wrapper that lives in `app`, and lower
modules can't depend on `app` per the project's Clean Architecture rule. The implementation,
[`ReminderSettingsRepositoryImpl`](../app/src/main/java/com/elementary/tasks/core/data/repository/ReminderSettingsRepositoryImpl.kt),
just maps each `NotificationSettings` field to/from a `Prefs` property — reusing existing prefs
where a clear V1 analog already existed (`color` ← `ledColor`, `priority` ← `defaultPriority`,
`quietHoursFrom/To` ← `doNotDisturbFrom/To`, etc.) and adding eight new ones
(`isDefaultVibrateEnabled`, `defaultVibrationPattern`, `defaultVolume`, `defaultSoundUri`,
`defaultNotificationCategory`, `isDefaultBypassDoNotDisturbEnabled`, `isDefaultWakeScreenEnabled`,
`defaultLockScreenVisibility`) in
[`Prefs.kt`](../app/src/main/java/com/elementary/tasks/core/utils/params/Prefs.kt) /
[`PrefsConstants.kt`](../app/src/main/java/com/elementary/tasks/core/utils/params/PrefsConstants.kt).
No DB schema involved — `Prefs` is a flat SharedPreferences key-value store, so new global defaults
are just a new key + property pair.

### Group

`GroupV2` (`domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/GroupV2.kt`) replaces the
V1 `ReminderGroup` concept for anything UI-facing going forward — it's just called "Group" now,
not "Reminder Group." Full stack: `GroupV2Repository` (repository-api) →
`GroupV2Entity`/`GroupV2Dao`/`GroupV2RepositoryImpl` (repository) → `GroupsViewModel` /
`EditGroupViewModel` (app, `groups/` package). The old V1 `ReminderGroup` table/repository/screens
are untouched — `GroupV2` is a genuinely new, separate table (see Storage below), backfilled from
`ReminderGroup` by id so existing `ReminderV2.groupId` references keep resolving.

### Reminder

`ReminderV2.notification: NotificationSettingsOverride` (was `NotificationSettings` before this
change — see Migration history below for why the type change needed a schema migration).

## Storage

`NotificationSettingsOverride` is stored via Room `@Embedded` (real, individually-typed columns,
prefix `notif_`) on both `ReminderV2Entity` and `GroupV2Entity` — **not** the opaque
JSON-payload-column trick used for `RecurrenceRule`/`ReminderAction`. This matters for anyone
adding a new field: **every new `NotificationSettingsOverride` field needs an actual DB migration**
(a new `ALTER TABLE ... ADD COLUMN`, nullable, no default), unlike `RecurrenceRule`, which absorbs
new fields into its JSON payload column for free.

```kotlin
// repository/src/main/java/com/github/naz013/repository/entity/ReminderV2Entity.kt
data class NotificationSettingsOverrideColumns(
  val color: Int? = null,
  val vibrate: Boolean? = null,
  val vibrationPattern: List<Long>? = null,   // via ReminderV2VibrationPatternConverter
  // ...
  val activeHours: List<Int>? = null,          // via ReminderV2NullableIntListConverter
  // ...
)
```

Both `ReminderV2Entity.notification` and `GroupV2Entity.notification` reuse this exact same
`NotificationSettingsOverrideColumns` class and the same `NotificationSettingsOverride.toColumns()`
/ `.toDomain()` mapper functions
([`ReminderV2Mapper.kt`](../repository/src/main/java/com/github/naz013/repository/entity/ReminderV2Mapper.kt),
made `internal` rather than `private` specifically so `GroupV2Mapper.kt` can call them too) — one
mapping to maintain, not two.

### Migration history (DB versions)

| Version | Migration file | What changed |
|---|---|---|
| 22 → 23 | `Migration22To23.kt` | Added `vibrationPattern`, `soundUri`, `category`, `bypassDoNotDisturb`, `wakeScreen`, `lockScreenVisibility` to `ReminderV2` (still non-null at this point — this was before the 3-level hierarchy existed, when `ReminderV2.notification` was a single concrete `NotificationSettings`). |
| 23 → 24 | `Migration23To24.kt` | Recreate-table migration: SQLite can't relax `NOT NULL` via `ALTER TABLE`, so this rebuilds `ReminderV2` with all `notif_*` columns nullable and drops the now-dead `notif_useGlobalSettings` column, preserving existing rows' concrete values as their new explicit per-reminder override (no data loss). |
| 24 → 25 | `Migration24To25.kt` | Creates the new `GroupV2` table (same `notif_*` nullable-column shape, embedded). |

If a schema change is ever needed on `NotificationSettingsOverrideColumns` again, prefer additive
`ALTER TABLE ADD COLUMN` (nullable, no default) — only recreate the table if an existing column's
nullability/type must change, following the `Migration23To24.kt` pattern as a reference.

### Backfill (V1 → V2)

Two one-time migrations populate the V2 tables from V1 data, both invoked once from
`SplashViewModel.checkDb()` at app startup, gated by `Prefs` one-shot flags
(`groupV2BackfillDone` / `reminderV2BackfillDone`):

1. [`GroupV2BackfillUseCase`](../repository/src/main/java/com/github/naz013/repository/migration/GroupV2BackfillUseCase.kt) —
   maps every V1 `ReminderGroup` → `GroupV2`, **preserving the id** (`groupUuId` → `uuId`) so
   `ReminderV2.groupId` (copied from V1 `Reminder.groupUuId` during its own backfill) keeps
   resolving to the same group.
2. [`ReminderV2BackfillUseCase`](../repository/src/main/java/com/github/naz013/repository/migration/ReminderV2BackfillUseCase.kt) —
   maps every V1 `Reminder` → `ReminderV2`, including its notification fields into
   `NotificationSettingsOverride` (dropping the dead `useGlobalSettings = useGlobal` mapping since
   there's no destination field for it anymore).

`GroupsUtil.initDefaultIfEmpty()` also seeds three default groups into `GroupV2` directly (same
fixed ids as the V1 seeding, `default_group_1/2/3`) for **fresh installs** that have no V1 data to
backfill from at all.

## UI surface today

- **Settings**: a new "Notification defaults" section on the existing Reminders settings screen
  (`app/src/main/java/com/elementary/tasks/settings/reminders/`) — `RemindersSettingsScreen` /
  `RemindersSettingsViewModel` / `RemindersSettingsState`. Covers `vibrate` (toggle),
  `bypassDoNotDisturb` (toggle), `wakeScreen` (toggle), `category` (choice dialog),
  `lockScreenVisibility` (choice dialog).
- **Group**: `EditGroupScreen` currently has **no** notification-override UI yet — `GroupV2`'s
  `notification` field exists and round-trips correctly, but nothing in the edit-group screen lets
  a user actually set a per-group override yet. This is the most obvious next step for whoever
  picks this up.
- **Not yet exposed anywhere**: `volume`, `soundUri`, `vibrationPattern`. These need dedicated UI
  (a slider, a system ringtone picker `Intent`, a pattern editor) that didn't exist as a reusable
  component anywhere in the codebase, so building it was scoped out rather than done quickly.

## Explicit non-goals (as of this writing)

- **Nothing reads the resolved `NotificationSettings` at notification-fire time.** Actual delivery
  (`Notifier` channel creation, `ReminderHandlerQ`/`ReminderHandlerSilent`, `DoNotDisturbManager`)
  still operates entirely on V1 `Reminder` and is unaffected by any of this. Wiring it up means
  solving Android 8+'s per-channel sound/vibration constraint (a channel's sound/vibration is fixed
  at creation and can't be changed later — a "channel per configuration" strategy is likely needed)
  and belongs with the eventual reminder-builder cutover to `ReminderV2`.
- V1 `Reminder`/`ReminderGroup` and their tables/migrations/usecases are fully untouched.
- The V1 reminder-builder's group picker (`GroupDecomposer`, `BiFactory`) and home/schedule
  group-based filtering (`home/eventsview`, `home/scheduleview`) still read the V1 `ReminderGroup`
  table — only the standalone Group list/edit screens moved to `GroupV2`.
- No cloud-sync wiring for `GroupV2Json`/`ReminderV2Json`'s notification fields — the JSON DTOs
  exist and are kept in sync with the domain shapes, but aren't registered in `DataType`/
  `GetClassByDataTypeUseCase` yet.

## Checklist: adding a new customization field

Whenever a new per-reminder/per-group/global customization parameter is added (e.g. "notification
badge count", "custom LED blink rate"), touch all of these:

1. **`domain`**: add the field to both `NotificationSettings` (concrete, real default) and
   `NotificationSettingsOverride` (nullable, default `null`) in `ReminderV2.kt` /
   `NotificationSettingsResolver.kt`. Add the `?: group?.x ?: defaults.x` line to `resolve()`.
2. **`domain/sync`**: add the matching nullable field to `NotificationSettingsOverrideJson`
   (`ReminderV2Json.kt`) for cloud-backup completeness (even though it isn't wired into the sync
   flow yet).
3. **`repository` entity**: add the nullable column to `NotificationSettingsOverrideColumns`
   (`ReminderV2Entity.kt`). If it's a non-primitive type, add/reuse a nullable-aware
   `@TypeConverter` (see `ReminderV2VibrationPatternConverter` / `ReminderV2NullableIntListConverter`
   in `ReminderV2ListConverters.kt` for the pattern — don't reuse the *non-nullable* list converters
   used elsewhere in the codebase).
4. **`repository` mapper**: add the field to both `NotificationSettingsOverride.toColumns()` and
   `NotificationSettingsOverrideColumns.toDomain()` in `ReminderV2Mapper.kt`.
5. **Migration**: new `MigrationNTo(N+1).kt`, `ALTER TABLE ReminderV2 ADD COLUMN notif_x ...` **and**
   `ALTER TABLE GroupV2 ADD COLUMN notif_x ...` (nullable, no default) — both tables share the same
   embedded shape, so both need the column. Bump `AppDb.version` and register the migration.
6. **Backfill mapper**: if V1 `Reminder` has an equivalent field, map it into the new override field
   in `ReminderToReminderV2Mapper.kt`; otherwise leave it unset (defaults to `null`/inherit).
7. **Global default**: add a `Prefs`/`PrefsConstants` entry and wire it in both directions in
   `ReminderSettingsRepositoryImpl.getNotificationDefaults()` / `setNotificationDefaults()`.
8. **Settings UI**: add a row to the "Notification defaults" section in `RemindersSettingsScreen`/
   `RemindersSettingsViewModel`/`RemindersSettingsState`, following the existing toggle
   (`SettingsSwitchItem`) or choice-dialog (`SettingsItem` + `ChoiceDialogKind`) patterns.
9. **Group UI** (not yet built for *any* field — see above): once it exists, add the same row there,
   defaulting to "inherit" (unset).
10. **Tests**: extend `ReminderV2MapperTest`/`GroupV2MapperTest` round-trips, and add a resolution
    case to `ResolveReminderV2NotificationSettingsUseCaseTest` if the new field has interesting
    resolution semantics.

## Key files

- `domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/ReminderV2.kt` — `NotificationSettings`, `ReminderV2`
- `domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/NotificationSettingsResolver.kt` — `NotificationSettingsOverride`, `resolve()`
- `domain/src/main/kotlin/com/github/naz013/domain/reminder/v2/GroupV2.kt`
- `domain/src/main/kotlin/com/github/naz013/domain/sync/ReminderV2Json.kt`, `GroupV2Json.kt`
- `repository-api/src/main/java/com/github/naz013/repository/ReminderSettingsRepository.kt`, `GroupV2Repository.kt`
- `repository/src/main/java/com/github/naz013/repository/entity/ReminderV2Entity.kt`, `GroupV2Entity.kt`, `ReminderV2Mapper.kt`, `GroupV2Mapper.kt`
- `repository/src/main/java/com/github/naz013/repository/migrations/Migration23To24.kt`, `Migration24To25.kt`
- `repository/src/main/java/com/github/naz013/repository/migration/GroupV2BackfillUseCase.kt`, `ReminderGroupToGroupV2Mapper.kt`, `ReminderV2BackfillUseCase.kt`, `ReminderToReminderV2Mapper.kt`
- `usecase/reminders/src/main/kotlin/com/github/naz013/usecase/reminders/ResolveReminderV2NotificationSettingsUseCase.kt`
- `app/src/main/java/com/elementary/tasks/core/data/repository/ReminderSettingsRepositoryImpl.kt`
- `app/src/main/java/com/elementary/tasks/core/utils/params/Prefs.kt`, `PrefsConstants.kt`
- `app/src/main/java/com/elementary/tasks/settings/reminders/` — Settings UI section
- `app/src/main/java/com/elementary/tasks/groups/` — Group CRUD screens (list, create/edit, use cases)
- `app/src/main/java/com/elementary/tasks/splash/SplashViewModel.kt` — backfill trigger points
