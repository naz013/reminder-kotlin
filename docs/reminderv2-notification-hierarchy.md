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

Field-by-field, across the three sites (updated after the REM-1069 pass):

| Field | Settings | Group editor | Reminder builder |
|---|---|---|---|
| color | ✅ "LED indication color" | ❌ (frozen) | ✅ `LedColorBuilderItem` |
| vibrate | ✅ "Notification defaults" | ✅ | ✅ `OtherParamsBuilderItem` |
| vibrationPattern | ✅ preset picker | ✅ preset picker | ✅ `VibrationPatternBuilderItem` |
| repeatNotification | ✅ "Notification repeating" | ✅ | ✅ `OtherParamsBuilderItem` |
| volume | ❌ (frozen, dead `Prefs.defaultVolume`) | ❌ (frozen) | ❌ (frozen) |
| soundUri | ❌ (frozen, dead `Prefs.defaultSoundUri`) | ❌ (frozen) | ❌ (frozen) |
| quietHoursFrom/To | ✅ (as global DND from/to, frozen) | ❌ (frozen) | ⚠️ Countdown-recurrence-only, frozen |
| activeHours | ❌ (frozen, hardcoded `emptyList()`) | ❌ (frozen) | ⚠️ Countdown-recurrence-only, frozen |
| delayMinutes | ⚠️ (as "default snooze time", reused pref, frozen) | ✅ | ✅ `DelayMinutesBuilderItem` |
| priority | ✅ "Reminder default priority" | ✅ | ✅ `PriorityBuilderItem` |
| category | ✅ "Notification category" | ✅ | ✅ `CategoryBuilderItem` |
| bypassDoNotDisturb | ✅ "Notification defaults" | ✅ | ✅ `BypassDndBuilderItem` |
| wakeScreen | ✅ "Notification defaults" | ✅ | ✅ `WakeScreenBuilderItem` |
| lockScreenVisibility | ✅ "Notification defaults" | ✅ | ✅ `LockScreenVisibilityBuilderItem` |
| remindBefore | ❌ (frozen, no Settings-level default beyond `0`) | ❌ (frozen) | ✅ `BeforeTimeBuilderItem` |

- **Settings** (`app/src/main/java/com/elementary/tasks/settings/reminders/`) covers all 10
  in-scope fields plus the frozen `color`/`priority`/`quietHoursFrom/To` rows. `vibrationPattern`
  is a new choice-dialog row (`ChoiceDialogKind.VIBRATION_PATTERN`) backed by
  `VibrationPresets.ALL` (`app/src/main/java/com/elementary/tasks/core/utils/VibrationPresets.kt`).
  `Prefs.defaultVibrationPattern` switched from the buggy `getLongArray`/`putLongArray`
  (`StringSet`-backed, unordered/deduplicating) to an ordered comma-joined string via
  `getString`/`putString`.
- **Reminder builder** (`app/src/main/java/com/elementary/tasks/reminder/build/`) now has all 10
  in-scope fields as builder items: `CategoryBuilderItem`, `LockScreenVisibilityBuilderItem`,
  `BypassDndBuilderItem`, `WakeScreenBuilderItem`, `VibrationPatternBuilderItem`,
  `DelayMinutesBuilderItem` (new), plus the pre-existing `remindBefore`/`priority`/`color`/
  `vibrate`/`repeatNotification` items. All six new items are `BiGroup.EXTRA`/`PARAMS` with no
  `constraints` (always offered, regardless of recurrence type) — unlike the frozen
  `quietHoursFrom`/`quietHoursTo`/`activeHours` bundle, which stays Countdown-only. The
  `OtherParamsValueEditor.kt` `notifyByVoice` dead-toggle bug (shown in UI, silently dropped on
  save) is unchanged — flagged but not fixed in this pass, since `notifyByVoice` isn't one of the
  16 hierarchy fields.
- **Group**: `EditGroupScreen` now has a "Notification overrides" section covering all 10 in-scope
  fields — one `SettingsItem` row per field, each opening a `SingleChoiceDialog` (or, for
  `delayMinutes`, an override-switch + slider dialog) with an "Inherit from Settings" option
  prepended to the field's normal choices. Selecting "Inherit" sets the field back to `null`;
  selecting anything else sets an explicit override. Row subtitles show either the overridden
  value or `"Inherited: <effective Settings value>"`, reusing the builder's `Formatter` classes
  (`CategoryFormatter`, `PriorityFormatter`, etc.) for consistent labels across Settings/Builder/
  Group. `EditGroupState.notification: NotificationSettingsOverride` now round-trips through
  `load()`/`performSave()` (previously silently dropped). `GroupV2.color` (the group's own
  list-display color, set via the pre-existing `ColorSlider`) remains a distinct field from
  `notification.color`, which stays frozen/unexposed at the Group level.
- **User-facing help**: a new "How does this work?" row at the top of both the Settings
  "Notification defaults" section and the Group editor "Notification overrides" section opens
  `NotificationCustomizationHelpScreen`
  (`app/src/main/java/com/elementary/tasks/settings/reminders/help/NotificationCustomizationHelpScreen.kt`,
  registered as `SettingsNavKey.NotificationCustomizationHelp` so both features' nav graphs can
  push it on the shared backstack) — a WebView-hosted HTML guide
  (`app/src/main/assets/files/notification_customization.html`) explaining the 3-level hierarchy,
  what each field does, and that some options (vibration patterns, custom sounds, bypassing Do Not
  Disturb, notification-channel behavior) vary by device/manufacturer/Android version. Follows the
  same pattern as `ReminderHelpScreen`/`how_to_create_a_reminder.html`.
- `ResolveReminderV2NotificationSettingsUseCase` is still unused outside its own DI registration
  and test — nothing resolves the hierarchy at notification-fire time yet (see Explicit non-goals).
  This pass only adds override UI; wiring resolution into actual notification delivery remains a
  separate follow-up.
- `volume`/`soundUri` still have no reusable picker component anywhere in `ui-common` and remain
  frozen/out of scope. `vibrationPattern` shipped in this pass as a small fixed preset list
  (`VibrationPresets.ALL`: Short/Long/Double buzz/Default) rather than a free-form
  millisecond-array editor — there was nothing to reuse, so this is a net-new, deliberately
  simple picker. Selecting a preset in any of the three sites immediately plays it via
  `VibrationPlayer` (`app/src/main/java/com/elementary/tasks/core/utils/VibrationPlayer.kt`,
  a thin `Vibrator`/`VibrationEffect.createWaveform` wrapper) so the user can feel the difference,
  not just read the name.

### Scope for REM-1069 ("Finish notification override") — completed

`color`, `volume`, `soundUri`, `quietHoursFrom`, `quietHoursTo`, `activeHours`, and `remindBefore`
were kept **frozen** — left exactly as they were, no new UI (either already fine, or blocked on
picker components still out of scope). The remaining 9 fields (`vibrate`, `vibrationPattern`,
`repeatNotification`, `delayMinutes`, `priority`, `category`, `bypassDoNotDisturb`, `wakeScreen`,
`lockScreenVisibility`) now have full 3-level UI coverage across Settings, Group editor, and
Reminder builder.

In scope — bring these to full 3-level coverage (Settings + Group + Builder) where a site is
currently missing them: `vibrate`, `vibrationPattern`, `repeatNotification`, `delayMinutes`,
`priority`, `category`, `bypassDoNotDisturb`, `wakeScreen`, `lockScreenVisibility`.

## Explicit non-goals (as of this writing)

- **Nothing reads the resolved `NotificationSettings` at notification-fire time.** Actual delivery
  (`Notifier` channel creation, `ReminderHandlerQ`/`ReminderHandlerSilent`, `DoNotDisturbManager`)
  still operates entirely on V1 `Reminder` and is unaffected by any of this. Wiring it up means
  solving Android 8+'s per-channel sound/vibration constraint (a channel's sound/vibration is fixed
  at creation and can't be changed later — a "channel per configuration" strategy is likely needed)
  and belongs with the eventual reminder-builder cutover to `ReminderV2`.
- V1 `Reminder`/`ReminderGroup` and their tables/migrations/usecases are fully untouched.
- The V1 reminder-builder's group picker (`GroupDecomposer`, `BiFactory`) and home/schedule
  group-based filtering (`home/agenda`, `home/scheduleview`) still read the V1 `ReminderGroup`
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
9. **Group UI**: add a row to the "Notification overrides" section in `EditGroupScreen`/
   `EditGroupViewModel`/`EditGroupState` (`app/src/main/java/com/elementary/tasks/groups/create/`),
   following the established `SettingsItem` + `SingleChoiceDialog` pattern with an "Inherit from
   Settings" option prepended to the field's choices (see `GroupNotificationDialogKind`,
   `onNotificationChoiceSelected`, `refreshNotificationSubtitles()`). `delayMinutes` instead uses
   the dedicated `EditGroupDialog.DelayMinutes` override-switch + slider dialog, since it's a
   continuous value rather than a fixed choice list.
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
- `app/src/main/java/com/elementary/tasks/groups/` — Group CRUD screens (list, create/edit, use cases), notification-overrides section in `create/EditGroupScreen.kt`/`EditGroupViewModel.kt`/`EditGroupState.kt`
- `app/src/main/java/com/elementary/tasks/reminder/build/BuilderItem.kt` — all per-reminder builder items, including `CategoryBuilderItem`/`LockScreenVisibilityBuilderItem`/`BypassDndBuilderItem`/`WakeScreenBuilderItem`/`VibrationPatternBuilderItem`/`DelayMinutesBuilderItem`
- `app/src/main/java/com/elementary/tasks/reminder/build/formatter/` — `Formatter` implementations shared across the builder and (via direct reuse) the Group editor's subtitle text
- `app/src/main/java/com/elementary/tasks/core/utils/VibrationPresets.kt` — the fixed vibration-pattern preset list shared by Settings/Builder/Group
- `app/src/main/java/com/elementary/tasks/splash/SplashViewModel.kt` — backfill trigger points
