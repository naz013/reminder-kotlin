# Reusing the Core for a Second App: Gaps and Implementation Plans

This document answers a specific question: **if we wanted to ship a second reminder app with a
different UI but the same underlying core, what's actually missing from today's module graph, and
how would we close each gap?** It's a companion to [architecture.md](architecture.md) — read that
first for the module inventory and dependency rules; this doc only covers what blocks reuse across
two applications.

The short version: the incremental `feature-*`/`ui-*`/`logic-*` extraction already did most of the
hard work. `app` is down to 132 Kotlin files, and most of what remains is seam implementations and
DI wiring, not business logic. Four real gaps remain, ranked by blast radius.

## Summary

| # | Gap | Where it lives today | Priority | Effort |
|---|---|---|---|---|
| 1 | Alarm/notification-action pipeline never extracted | Done — `logic:logic-notification-action` | ~~High — blocks reuse entirely~~ | ~~Large~~ |
| 2 | Preferences storage is a monolithic app-only engine | `app/.../core/utils/params/Prefs.kt` + `app/.../module/*Impl.kt` (~20 classes) | Medium — duplicated, not blocked | Medium |
| 3 | Notification-building mechanics entangled with branding | `app/.../core/utils/Notifier.kt` | Medium | Small |
| 4 | Cloud provider credentials are app-specific | `app/.../core/cloud/*Impl.kt` | Not a gap — a checklist | N/A |

## Already reusable — no work needed

Verified by direct inspection, not assumption:

- **`core:domain`, `data:repository(-api)`, `data:cloud(-api)`, `data:sync`, `data:icalendar`** have
  zero Android/brand coupling.
- **`app` has zero `strings.xml` files.** Every UI string already lives in `ui-common`/`ui-*`/
  `feature-*` modules, already localized into every shipped locale. A second UI gets all existing
  feature text for free.
- **[`logic-reminder`](../logic/logic-reminder) has zero `R.string`/`R.drawable` references** — pure
  business logic, safe to depend on unmodified from a second app.
- **Crashlytics/Firebase Analytics are behind interfaces** (`core:logging-api`, `core:analytics`'s
  `AnalyticsEventSenderImpl`) — a second app can swap or drop either without touching any logic
  module.
- **`feature-reminder`, `feature-note`, `feature-tags`, `feature-googletask`, `feature-insights`,
  `feature-workflow`** already depend only on `-api`/`logic-*`/`ui-*` modules, never on `app` (rule 7
  in architecture.md). Structurally, these could be pulled into a second application module today —
  the cost is implementing their seam interfaces (see Appendix).
- **Room (`AppDb`, database name `"app_db"`)** makes no cross-app assumptions. A second app is a
  separate process with its own database automatically.

---

## Gap 1: Alarm/notification-action pipeline

### Problem

`app/src/main/java/com/elementary/tasks/core/services/` contains the code that actually fires when a
reminder or birthday alarm goes off: `AlarmReceiver`, `BootReceiver`, `JobScheduler`,
`PermanentReminderReceiver`, `PermanentBirthdayReceiver`, and the whole `services/action/` package —
`ReminderActionProcessor`, `ReminderHandlerFactory`, `ReminderRepeatProcessor`,
`NotificationAlertActionHandler`, `WearNotification`, `BirthdayActionProcessor`,
`BirthdayHandlerFactory`, `ActionHandler`, `CancelNotificationDecorator` — roughly 40 files.

This is the single most "core" piece of runtime behavior a reminder app has, and it has **no `-api`
seam at all**. It isn't in `logic-reminder`, doesn't implement an interface a second app could
re-target, and isn't reachable from any `feature-*` module. A second app cannot reuse it — the entire
alarm-firing → action-processing → notification-building chain would have to be copy-pasted and would
immediately start drifting from the original.

Everything else in this document assumes a seam interface already exists somewhere and just needs an
implementation. This gap has no seam to implement — it needs one designed.

### Status: done

Both halves shipped as [`logic:logic-notification-action`](../logic/logic-notification-action)
(`id("reminder.android.library")` — not pure-JVM like `logic-reminder`/`logic-workflow`, since it
builds `NotificationCompat.Builder`/`PendingIntent` directly, same rationale as `ui-map`'s exception
in architecture.md). It owns `ReminderActionProcessor`/`ReminderRepeatProcessor`/
`ReminderCompleteSnoozeFactory` (reminder), `BirthdayActionProcessor`/`BirthdayCancelActionFactory`
(birthday), `DoNotDisturbManager`, and the shared mechanics both domains use: `ActionHandler`,
`NotificationAlertActionHandler`, `CancelNotificationDecorator`, `WearNotification`,
`NotificationStyle`. Four seam interfaces carry the shared/reminder half: `NotificationGateway`
(build/notify/cancel, wraps `Notifier`), `DoNotDisturbPreferences`, `WearPreferences`,
`PhoneCallStateProvider` (replaces `SuperUtil.isPhoneCallActive`) — plus `ReminderAlertHandlerFactory`
and `BirthdayAlertHandlerFactory`, each implemented only in `app` (`AppReminderAlertHandlerFactory`/
`AppBirthdayAlertHandlerFactory`) because the concrete alert handlers still have to target `app`-only
classes (`ReminderActionReceiver`/`ReminderActionActivity`, `BirthdayActionReceiver`/
`BirthdayActionActivity`). The birthday half needed **zero new preference seams** — `BirthdayPreferences`
(`logic-birthday`) already exposed every field `BirthdayActionProcessor`/`BirthdayNotificationHandler`
needed (`daysToBirthday`, `birthdayPriority`, etc.), so it slotted straight in.
`AlarmReceiver`/`ReminderActionReceiver`/`BirthdayActionReceiver`/`BootReceiver`/`GeolocationService`
stay in `app` as thin manifest-bound adapters, unchanged in shape, now delegating to the new module.
`JobScheduler.kt` (the `JobSchedulerApi` implementation) correctly stayed in `app` too — it's already
just an implementation of an existing `-api` seam.

Design points that came up only once real files were read (worth knowing before touching this
again): **`ReminderDataProvider`/`BirthdayDataProvider` stay in `app`**, not in the new module. Both
resolve LED colors via `com.github.naz013.feature.reminder.util.LED`, which lives in `feature-reminder`
— a module *above* `logic-*` in the dependency graph. Moving either down would have meant
`logic-notification-action` depending on a `feature-*` module, inverting the layering this whole
effort exists to fix. Since their only consumers, `ReminderNotificationHandler`/
`BirthdayNotificationHandler`, also have to stay in `app` (they reference the app-only
receiver/activity classes directly), leaving the data providers in place was the correct call, not a
shortcut.

---

## Gap 2: Preferences storage is a monolithic app-only engine

### Problem

`app/src/main/java/com/elementary/tasks/core/utils/params/Prefs.kt` (plus `SharedPrefs.kt`,
`RemotePrefs.kt`) is a single `SharedPreferences`-backed object that every one of the ~20
`*PreferencesImpl` adapter classes in `app/src/main/java/com/elementary/tasks/module/` delegates to —
`ReminderPreferencesImpl`, `ThemePreferencesImpl`, `NoteSettingsPreferencesImpl`,
`GoogleTasksPreferencesImpl`, `LocationSettingsPreferencesImpl`, `SchedulePreferencesImpl`,
`MapPreferencesImpl`, `AuthPreferencesImpl`, `LocalePreferencesImpl`, `FontApiImpl`, and more.

The *pattern* is correct — every seam interface (`ReminderPreferences`, `NotePreferences`, etc.) is
already declared in the module that owns the logic, exactly per architecture.md's rules. The problem
is narrower: the storage engine itself (`Prefs`) is a God object with no reusable shape, so a second
app must write an equivalent from scratch and re-author all ~20 impl classes — even though most of
that work is boilerplate delegation to typed key-value storage, and several of these preferences
(theme, locale, auth, font) likely don't need to differ between two apps from the same team at all.

### Implementation plan

1. **Extract a generic typed-preferences engine into a new `core:preferences` module** — a small
   `TypedPrefs` wrapper (string/bool/int/enum delegates over `SharedPreferences` or
   `androidx.datastore`) with no knowledge of reminder-specific keys. `Prefs.kt`'s actual key
   constants (`PrefsConstants.kt`) and defaults stay app-specific; the delegate mechanism moves out.
2. **Identify which `*PreferencesImpl` classes are actually brand-specific vs. shareable.**
   `ThemePreferencesImpl`, `LocalePreferencesImpl`, `AuthPreferencesImpl`, `FontApiImpl` (all in
   `app/.../module/uicommon/`) look like they'd be identical for a second app built by the same team —
   candidates to become default implementations shipped from `ui-common` itself (or a new small
   module) rather than re-authored per app. Preferences that encode actual product behavior
   (`ReminderPreferencesImpl`, `GoogleTasksPreferencesImpl`) stay app-authored since a second app may
   want different defaults/toggles.
3. **Don't move `RemotePrefs.kt`** — it's tied to Firebase Remote Config message plumbing
   (`InternalMessageV1`, `SaleMessageV2`, `UpdateMessageV2`), which is inherently app/campaign-specific
   and correctly stays in `app`.

---

## Gap 3: Notification-building mechanics entangled with branding

### Problem

`app/src/main/java/com/elementary/tasks/core/utils/Notifier.kt` mixes two concerns: generic Android
notification-channel mechanics (channel creation, importance mapping, building an expandable
notification) and app-specific resource lookups (icons, channel names, default sounds). It's consumed
by `AppReminderNotifier`, `AppBirthdayNotifier`, `AppNoteNotifier` — the seam implementations for
`ReminderNotifier`/etc. that already live correctly in `app`.

### Implementation plan

1. **Introduce a small `NotificationBrandingProvider` interface** (small icon res id, channel display
   names, default sound URI) — the only pieces of `Notifier.kt` that are genuinely app-specific.
2. **Move the channel-creation/notification-building mechanics into `core:platform-common`** (or the
   new `logic-notification-action` module from Gap 1, if that lands first — the two overlap and should
   land together rather than as two separate migrations) parameterized by
   `NotificationBrandingProvider`.
3. **`app`'s implementation of `NotificationBrandingProvider` becomes the only piece a second app must
   author** to get equivalent notification behavior with its own icons/text.

This is the smallest of the three real gaps and is a natural side-effect of doing Gap 1 properly —
consider folding it into that migration rather than sequencing it separately.

---

## Gap 4: Cloud provider credentials (not a gap — a checklist)

`app/src/main/java/com/elementary/tasks/core/cloud/*Impl.kt` (`CloudApiProviderImpl`,
`CloudKeysStorageImpl`, `DataPostProcessorImpl`, `FileCacheProviderImpl`, `SyncSettingsImpl`,
`TagSyncTriggerImpl`) implement `cloud-api`/`sync`'s seam interfaces using OAuth client IDs and cache
paths sourced from `Prefs`/`BuildParams`. This is *supposed* to differ per app — a second app needs
its own Google Drive/Dropbox app registration regardless of module structure — so it's not something
to fix. It's listed here only so a second app's setup checklist includes: register cloud app
credentials, then re-implement these six classes (thin — they're mostly credential plumbing, not
logic) against the existing `cloud-api`/`sync` interfaces unchanged.

---

## Appendix: seam-interface checklist for a second app

Reusing a `feature-*` module means implementing every seam interface it depends on. From what's
already extracted, the minimum checklist today is:

- `ReminderPreferences`, `ReminderNotifier` (`logic-reminder`)
- `NotePreferences`, `NoteNotifier`, `NoteFontProvider` (`ui-note`)
- `GoogleTasksPreferences` (`feature-googletask`)
- `ThemePreferences`, `LocalePreferences`, `AuthPreferences`, `FontApi` (`ui-common`)
- `MapPreferences` (`ui-map`)
- `SchedulePreferences` (`logic-schedule`)
- `BuildInfo` (`platform-api`)
- `FeatureFlags` (`feature-flags-api`)

Per Gap 2 above, `ThemePreferences`/`LocalePreferences`/`AuthPreferences`/`FontApi` are the best
candidates to turn into shared default implementations so this checklist gets shorter over time
instead of growing with every new feature extraction.

## Recommended sequencing

1. ~~**Gap 1** (alarm/action pipeline)~~ — done, both reminder- and birthday-side
   (`logic:logic-notification-action`).
2. **Gap 2** (preferences engine + shared defaults for theme/locale/auth/font) — removes most of the
   ~20-class rewrite cost for a second app.
3. **Stand up a second `app`-like Gradle module** depending on the same `feature-*`/`logic-*`/`data-*`
   set, supplying its own `BuildInfo`, cloud credentials, and resources. This is the real validation
   step — it will surface any seam gaps this document missed.
