# Time zone support (design note)

Status: design; model (2/4), zone-aware calculation (3a) and zone-change handling (3c) are implemented. Tracked as four Trello cards (1/4 is this note).

## Problem

`ReminderV2` stores no time zone. Every schedule calculation uses the device zone (`ZoneId.systemDefault()`), so
a reminder set before travel either fires at the wrong wall-clock time or at the right one, depending on what the
user meant ("9:00 wherever I am" vs "9:00 in Berlin"). The user cannot express that intent today.

## How it works today

- Storage: `ReminderSchedule.startDateTime/eventDateTime` are `LocalDateTime`s holding **UTC wall-clock**. Room
  stores them as epoch millis via `toEpochMillisUtc()` (`ReminderV2Mapper`, `sched_*` columns); backup stores them
  as strings (`ReminderV2Json.startDateTime/eventDateTime`, `DataConverterImpl`). So the stored value is a real
  instant, and range queries in `ReminderV2Dao` (`sched_eventDateTime >= :from`) compare instants.
- Conversion: `DateTimeManager.utcToLocal` / `localToUtc` (`core:date-calculations`) use `ZoneId.systemDefault()`.
  `getCurrentDateTime()` (`NowDateTimeProvider`) is `LocalDateTime.now()`, also device zone.
- Next occurrence: each `ReminderBehaviorStrategyV2` (`logic:logic-reminder`, `behavior/`) converts
  `eventDateTime` to device-local, advances it with `RecurrenceCalculator` (`findNextDayOfWeekDateTime`,
  `findNextMonthDayDateTime`, `findNextYearDayDateTime`, `findNextIntervalDateTime`; plain `LocalDateTime` math, no
  zone), and the caller (`CompleteReminderUseCase`, `SkipReminderUseCase`, `ActivateChainedRemindersUseCase`)
  converts back with `localToUtc` and saves.
- Alarm instant: `EventDateTimeCalculatorV2.calculateEventDateTime` does `utcToLocal`, subtracts `remindBefore`,
  zeroes seconds (except timers), then `toMillis(local)` using the device zone. `JobScheduler.scheduleReminder`
  (`app`) passes the millis to `AlarmManager` with request code `uniqueId`.
- RRULE: `RecurrenceRule.ICalendar(rrule)`. `ICalendarApiImpl.generate` (`data:icalendar`) builds a lib-recur
  `DateTime(TimeZone.getDefault(), startMillis)`, iterates `RuleInstances` and shifts back to the default zone. The
  result is **precomputed** into an `RDATE` list of floating `yyyyMMdd'T'HHmmss` strings (`UtcDateTime`, despite
  the name, builds the local format without `Z`) and `RecurEventManager.getNextAfterDateTime` finds the next entry
  by equality with the current event time converted to local. `COUNT` is mandatory (`createObject`). The tag
  parsers accept `;TZID=` params (see `RecurrenceRuleTagParserTest`), but nothing reads or writes them.

## Proposed model

`ReminderV2.timeZoneId: String? = null` (IANA id, e.g. `Europe/Berlin`).

- `null` = **floating**: wall-clock time follows the device zone. This is the intended meaning of every existing
  reminder, so no data migration of values is needed.
- non-null = **fixed**: wall-clock time is interpreted in that zone; the instant never depends on the device zone.
- Invalid or unknown ids (older tzdata, hand-edited backup) are treated as floating and logged.

Global default setting (Settings, reminders): "New reminders use" = Floating (default, today's behaviour) or
Fixed to the zone current at creation. It only pre-fills the builder; the per-reminder value always wins.

## Supported reminder types

Types are `RecurrenceRule` variants mapped to strategies by `BehaviorStrategyResolverV2`.

| Type | Strategy | Zone support | Why |
|---|---|---|---|
| Date/time (`Once`) | `SimpleDateStrategyV2` | yes | one instant in a chosen zone |
| Repeat every X (`Daily`, millis interval) | `IntervalRepeatStrategyV2` | yes | first fire is zoned; later ones add elapsed time, so no DST adjustment |
| Weekdays (`Weekly`) | `WeekdayRepeatStrategyV2` | yes | "Mon 9:00" is wall-clock and DST sensitive |
| Month day (`Monthly`, `RelativeMonthly`) | `MonthlyRepeatStrategyV2` | yes | wall-clock calendar rule |
| Year day (`Yearly`) | `YearlyRepeatStrategyV2` | yes | wall-clock calendar rule |
| RRULE (`ICalendar`) | `RecurRepeatStrategyV2` | yes | needs TZID in DTSTART and zone-aware generation (below) |
| Countdown (`Countdown`) | `TimerRepeatStrategyV2` | excluded | elapsed duration, already an absolute instant |
| Location (`LocationEnter/Exit`, places) | `LocationBasedStrategyV2` | excluded | no time component |

The builder hides the zone control for excluded types and the save path stores `timeZoneId = null` for them.

## Where a zone must be injected

The core change is to stop calling `ZoneId.systemDefault()` implicitly for reminders. Add zone-taking overloads
and keep the old signatures delegating to the device zone, so non-reminder callers (birthdays, habits, DND) are
untouched.

1. `DateTimeManager`: `utcToLocal(dt, zone)`, `localToUtc(dt, zone)`, `toMillis(local, zone)`,
   `getCurrentDateTime(zone)`. Add a `parseFixedZone(id)` helper (valid fixed zone, else null = device zone) and `getCurrentUtcDateTime()`.
2. `EventDateTimeCalculatorV2`: use the effective zone for `utcToLocal` and `toMillis`, and "now" in that zone for
   the past check. DST rules below apply here.
3. All strategies: `utcToLocal`, `isCurrent`, `getCurrentDateTime` and the `afterOrEqualDateTime` argument use the
   effective zone; `CompleteReminderUseCase`, `SkipReminderUseCase`, `ActivateChainedRemindersUseCase` use it for
   `localToUtc`. Because the stored value stays a UTC instant, Room, DAO range queries and the backup format are
   unchanged.
4. `ICalendarApiImpl.generate`: take the zone (from the `DTSTART` TZID, falling back to the default) and use
   `DateTime(TimeZone.getTimeZone(id), millis)` / `shiftTimeZone(zone)`. lib-recur then expands in the zone, so
   wall-clock rules survive DST. The RDATE list is written as `RDATE;TZID=<id>:...` for fixed reminders.
   `RecurEventManager.getNextAfterDateTime` must compare in the same zone.
5. Floating RRULE caveat: the RDATE list is precomputed in the zone active at creation, so it goes stale when the
   device zone changes (true today as well). The zone-change handler must regenerate it; this is the main RRULE risk.

iCal import/export: there is no .ics file feature today, only the RRULE string. Emit `DTSTART;TZID=` for fixed
reminders and plain floating `DTSTART` for null. On parse, a `TZID` param sets `timeZoneId`; no TZID or a trailing
`Z` stays floating. Unknown TZIDs (Windows names, custom VTIMEZONE) fall back to floating.

## Interactions

- **Google Calendar export** (`GoogleCalendarUtils.addEvent`, `app`): sends `DTSTART` millis (correct instant) but
  `EVENT_TIMEZONE = TimeZone.getDefault().displayName`, a display name rather than an id (latent bug). Use the
  effective zone's `id`; all-day events need `EVENT_TIMEZONE = UTC`. Calendar import
  (`CalculateGoogleCalendarEventOccurrencesUseCase`, `DateTime(TimeZone.getDefault(), ...)`) is a view onto
  external events and stays device-zone.
- **Google Tasks** (`SaveReminderToGoogleTasksUseCase`): due is date-based in Google; the date comes from
  `toMillis(utcToLocal(...))` today. It must use the effective zone, since a fixed reminder near midnight can land
  on a different date than the device shows.
- **Wear** (`WearReminderSummary.eventDateTimeMillis`, `WearReminderSummaryMapper`): sends an instant; the watch
  formats with its own `ZoneId.systemDefault()` (`ReminderRow`, `NextReminderTileService`). Correct as is. For
  fixed reminders an optional nullable `timeZoneId` can be added to the payload later to show a zone label.
- **Lists, Agenda, Calendar, widgets, Today** (`GetTodayScheduleUseCase`, `UiReminderListAdapter`; about 48 files
  use `utcToLocal`/`localToUtc`): show times in the **device** zone by default (it answers "when will it ring for
  me"). Fixed reminders in a zone other than the device's additionally show a small zone label ("09:00 Berlin").
  Day bucketing (Today, calendar cells) uses the device zone.
- **Cloud backup and sync**: add `ReminderV2Json.timeZoneId: String? = null` (`@SerializedName`, safe default for
  old files) and map both directions in `DataConverterImpl` (`toDomain`/`toJson`), per CLAUDE.md. Old clients
  ignore the field and treat the reminder as floating.
- **Room**: AppDb is at version 47. New `MIGRATION_47_48`: `ALTER TABLE` on the reminder V2 table adding a
  nullable `timeZoneId TEXT` column (no default); register it in `AppDb`, bump to 48, add the field to
  `ReminderV2Entity` and `ReminderV2Mapper`. No index (never queried).

## Zone and clock changes

Today `BootReceiver` (manifest: `BOOT_COMPLETED`, `QUICKBOOT_POWERON`, `TIME_SET`, `TIMEZONE_CHANGED`) calls
`RescheduleHabitNudgesUseCase` for boot, time and zone changes, but runs `ActivateAllActiveRemindersUseCase`
**only on `BOOT_COMPLETED`**. Reminders are **not** rescheduled on a zone or clock change: their alarms keep the old
absolute instant. In practice every reminder behaves as "fixed to the zone where it was last scheduled" until its
next occurrence is computed.

Needed:

- On `TIMEZONE_CHANGED` and `TIME_SET`: re-run activation for active reminders (reuse
  `ActivateAllActiveRemindersUseCase`, today boot-only). For fixed reminders the instant is unchanged, so this is a
  cheap safety net (also corrects a manual clock change).
- Floating reminders: the stored instant was computed in the old device zone, so the wall-clock must be
  reinterpreted: `newInstant = localToUtc(utcToLocal(old, oldZone), newZone)`. This needs the previous zone: persist
  `lastKnownZoneId` in preferences, updated by the receiver after each pass; without it the old zone is
  unrecoverable. Update `schedule.eventDateTime`, `sync.version` and `syncState`, then reschedule.
- A reinterpreted time that is now in the past follows existing `ActivateReminderUseCase` behaviour (stale one-time
  is deactivated; repeating advances with `findNext*`).
- Regenerate precomputed RDATE lists for floating RRULE reminders (`CalculateReminderOccurrencesTask`).
- Run off the receiver thread (`goAsync` + `DispatcherProvider.io()`, as the habit path does).

DST rules (fixed and floating alike, in `EventDateTimeCalculatorV2`/`DateTimeManager`):

- Gap (spring forward, wall-clock does not exist): resolve with `ZonedDateTime.of(local, zone)`, which shifts the wall-clock forward by the gap length
  (02:30 becomes 03:30 in a one-hour gap). Intended: later occurrences keep the wall-clock rule (see Known limitations).
- Overlap (fall back, wall-clock occurs twice): use the earlier offset (the `ZonedDateTime` default); fire once.
- Interval (`Daily`) reminders add elapsed millis and are not DST adjusted.
- Never compute the next occurrence by adding 24 h; advance the local date, then resolve it in the zone.

Defaults assumed until the questions below are answered: existing reminders migrate as floating; display in the
device zone with a label for foreign fixed reminders; snooze is zone independent (adds millis to the current
instant, `SnoozeReminderUseCase`).

## Known limitations (3a)

- Gap drift: the stored value is only the resolved UTC instant, so a weekly 02:30 rule completed across the spring-forward gap is stored as 03:30 and the next occurrence is computed from 03:30, not 02:30. Fixing it needs the intended local time stored (schema change).
- Gap resolution is the gap-shifted time (02:30 becomes 03:30 via `ZonedDateTime.of`), not the first instant after the gap (03:00).
- RRULE reminders with a fixed zone are unsupported until card 3b: `canSkip` runs in the fixed zone while the RDATE calculation still runs in the device zone.
- Overlap: strategies return a local time, so only the earlier offset is representable; a later-offset instant cannot be expressed.
- Zone changes (3c) re-anchor floating reminders by shifting `eventDateTime` only. Precomputed RDATE lists of floating RRULE reminders are not regenerated until card 3b, so they can be stale after a zone change.

## Open questions

1. Floating semantics: should floating reminders really be reinterpreted on zone change (needs `lastKnownZoneId`)?
   Today they silently behave as fixed, so this changes behaviour for existing users.
2. Default for new reminders: floating (matches today and the proposal) or fixed to the current zone?
3. Repeating reminders while travelling: a fixed weekly "Mon 9:00 Berlin" fires at 03:00 in New York. Acceptable, or
   prompt "switch to this zone?" on zone change for fixed repeating reminders?
4. Changing a reminder's zone in the builder: keep the wall-clock time (9:00 stays 9:00 in the new zone) or keep the
   instant? Proposal: keep wall-clock.
5. Snooze and "remind before": confirm they stay instant based, not re-anchored to the zone.
6. Display: device zone plus a label for foreign fixed reminders (proposal), or the reminder's own zone? Do widgets
   and Wear show the label?
7. Old clients restoring a backup lose the zone and treat the reminder as floating. Accept, or bump a backup format
   version?
8. External iCal data with unknown or Windows zone names: fall back to floating (proposal)?
9. Google Calendar export of fixed reminders uses the reminder's zone and `UTC` for all-day events: confirm.
10. Confirm countdown and location reminders stay excluded (including location reminders with a time delay).

## Card split

- **1/4 Design note**: this document.
- **2/4 Model, Room, backup, iCal**: `ReminderV2.timeZoneId`, `ReminderV2Entity`/`Mapper`, `MIGRATION_47_48`,
  `ReminderV2Json` + `DataConverterImpl`, iCal TZID parse/emit (`RuleMap` DTSTART/RDATE). No behaviour change.
  Low risk; blocks 3/4 and 4/4.
- **3/4 Zone-aware scheduling and zone-change handling**: the largest and riskiest card; recommend **splitting**:
  - 3a: `DateTimeManager` zone overloads, `EventDateTimeCalculatorV2`, strategies, complete/skip/chained use cases,
    DST rules, Google Calendar/Tasks zone fixes, unit tests. Pure logic; can land dark because all existing data
    has `timeZoneId = null`.
  - 3b: `ICalendarApiImpl.generate` and `RecurEventManager` zone-aware RDATE generation (own risk: lib-recur and
    stored RDATE lists).
  - 3c: `BootReceiver` zone/time handling, `lastKnownZoneId`, floating reinterpretation, RDATE regeneration.
    Depends on open question 1.
- **4/4 Builder, preview UI and global default**: zone picker in the reminder builder, preview/list zone label,
  default setting, hidden for excluded types. Depends on 2/4; behaviour is only correct after 3a, so ship it after
  3a (or behind a feature flag).

Risks: the roughly 48 `utcToLocal`/`localToUtc` call sites must be audited so none keeps a device-zone assumption
for a fixed reminder; the existing "silently fixed" behaviour of floating reminders (question 1); precomputed RRULE
lists going stale.
