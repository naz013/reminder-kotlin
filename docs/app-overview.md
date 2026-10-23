# Reminder – Application Overview

## Purpose

**Reminder** is an offline-first, privacy-focused task and reminder app for Android, written in Kotlin. It
lets users create richly configured reminders, capture notes, track birthdays and habits, manage Google Tasks,
and optionally back up to their own cloud storage. Data stays on the device by default; cloud features are
opt-in, and the app has no backend of its own.

Available on [Google Play](https://play.google.com/store/apps/details?id=com.cray.software.justreminder) as two
product flavors, `free` (ad-supported) and `pro`.

## Release gating

These features sit behind a `FeatureFlag` (code default `false`) and/or a PRO check. The flags are enabled for
users through Remote Config, so the remaining gate is PRO where one is listed:

| Feature | Gate |
|---|---|
| Workflow rules | `FeatureFlag.WORKFLOW_ENABLED`; running rules needs PRO |
| Routines | `FeatureFlag.ROUTINE_ENABLED`; intended PRO, but no PRO check found in code (see [routines.md](routines.md) §6.4); Insights are PRO |
| Habit tracker | `FeatureFlag.HABIT_ENABLED` and PRO |
| On-device AI digest | `FeatureFlag.AI_DIGEST`, PRO, and a device that supports on-device summarization |
| Public holidays in Calendar | `FeatureFlag.PUBLIC_HOLIDAYS` |
| Streaks & Insights, local encrypted backup, Gemini AppFunctions | PRO build |
| Pro trial | Free users can earn 24 hours of PRO by watching rewarded ads; the trial does not unlock AppFunctions |

Flags are defined in `core/feature-flags-api/.../FeatureFlag.kt` and backed by SharedPreferences and Firebase
Remote Config.

## Core features

### Reminders
- **Scheduling:** one-time, daily, by weekday, day of month, day of year, countdown, and iCalendar RRULE
  patterns, with saved RRULE presets.
- **Location reminders:** trigger on entering or leaving a saved place.
- **Actions:** call, SMS, open a URL or app, send e-mail, or show a shopping sub-task list.
- **Organization:** groups, priority, tags, a reminder icon and color shown in lists and the calendar, and
  Smart Lists on the home list (Today, Overdue, This week, No group).
- **Chained reminders:** start a reminder only after another one is completed; it waits inactive and starts
  automatically (or when the predecessor is deleted or archived). See [chained-reminders.md](chained-reminders.md).
- **Quick add:** type a phrase and the on-device parser (`logic-quickadd`) builds the reminder.
- **Notification customization:** every notification parameter (sound, vibration, priority, category, DND bypass,
  wake screen, lock-screen visibility, repeat) resolves Reminder, then Group, then global default. An opt-in
  critical mode forces an alarm-style, DND-bypassing delivery. See
  [reminderv2-notification-hierarchy.md](reminderv2-notification-hierarchy.md).
- **In-app alert banner:** a foregrounded app mirrors a firing reminder or birthday as a banner. See
  [in-app-notification-overlay.md](in-app-notification-overlay.md).
- **History, archive, do-not-disturb window,** and **multi-select** bulk actions on Notes, Groups and Tags
  (pattern in [multiselect.md](multiselect.md)).

### Notes
Rich text with images and per-note font, color, tags, archive, search and tag filtering, and iCalendar export.
Dropping text, text files and PDFs into the editor is supported ([drag-and-drop-notes.md](drag-and-drop-notes.md)).
Each note keeps a local version history that can be browsed and restored from the preview screen
([note-version-history.md](note-version-history.md)).
The editor turns typed Markdown line markers into headings and bullets ([note-markdown.md](note-markdown.md)). "Share as" sends a note as the original file, plain text, hybrid Markdown or HTML ([note-markdown.md](note-markdown.md)).

### Birthdays
Tracked independently of the system calendar, with tags and a tag filter, a dedicated action screen when the
alarm fires, and an optional import from contacts.

### Tags
One cross-entity label for reminders, notes, birthdays, Google Tasks and routines, with filtering on each list
and a tag details screen. See [tags.md](tags.md).

### Habits, Routines and Pomodoro
- **Habits:** one-tap daily check-ins with streaks.
- **Routines:** ordered, time-boxed steps run in a guided focus timer, on demand or on a daily, weekly or
  monthly schedule. A running routine keeps going in the background with a progress notification (Pause/Skip/Stop). See [routines.md](routines.md) and [timer-notifications.md](timer-notifications.md).
- **Pomodoro:** work/break timer with a foreground notification; focus time feeds Insights. Notification details: [timer-notifications.md](timer-notifications.md).

### Workflow rules
Local trigger, condition and action automation over reminders and groups (for example auto-archive, escalate on
repeated snooze, weekly completion summary, react to a Bluetooth device or Wi-Fi network). See
[workflows.md](workflows.md).

### Google Tasks
Browse and manage Google Task lists and tasks with two-way sync.

### Calendar and Agenda
A calendar of reminders, birthdays and Google Task due dates (plus public holidays when enabled), and an
Agenda of reminders and birthdays with Smart List and tag filters. On wide screens Home, Agenda and Birthdays
use a list-detail layout ([adaptive-layouts.md](adaptive-layouts.md)).

### Streaks & Insights (PRO)
Computed on-device from the event history and routine and Pomodoro records: reminder streaks with 7, 30 and
100-day milestone badges, weekly trend, busiest day, routine streaks and most-skipped step, and focus time.

### Cloud backup and sync
Reminders, notes, birthdays, groups, tags, places, recur presets, settings and routines (plus PRO workflow rules
and habits) sync through Google Drive or Dropbox. Google Tasks sync with Google itself. The `sync` module is provider-agnostic and works on per-item files with sync metadata.

### Local encrypted backup (PRO)
Export to a single passphrase-protected file saved anywhere through the system file picker, fully offline.
AES-256-GCM with a PBKDF2-derived key (600,000 iterations). Restore upserts by id and deletes nothing.

### Widgets, Wear OS, assistants
- **Widgets:** calendar, notes, birthdays, Google Tasks, single note, combined buttons and active reminders. A
  "Next Reminder" widget is built for the lock screen and the Android 14+ glanceable hub. A Quick Settings tile
  adds a reminder in one tap.
- **Wear OS:** companion watch app with a reminder list and a next-reminder tile, synced over the Wearable Data
  Layer (`extensions:wearsync`, `wear/wear-app`).
- **Gemini AppFunctions (PRO):** create, list, complete, update, delete and search reminders, notes, birthdays
  and Google Tasks from on-device assistants. See [appfunctions-testing.md](appfunctions-testing.md).

### Customization and settings
Material 3 theming and color schemes, custom sounds and vibration patterns, default reminder settings,
iCalendar import/export, biometric lock, a first-run onboarding flow, and a "What's New" screen on updates.

## Technology stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 (a few legacy View screens remain) |
| Architecture | Multi-module Clean Architecture, MVVM with StateFlow |
| Navigation | Jetpack Navigation 3 |
| Persistence | Room |
| Dependency injection | Koin |
| Concurrency | Kotlin Coroutines |
| Cloud APIs | Google Drive REST, Google Tasks, Dropbox SDK |
| Analytics and crash reporting | Firebase Analytics, Crashlytics |
| Date and time | ThreeTenABP |
| iCalendar | lib-recur |
| Build | Gradle Kotlin DSL with convention plugins in `build-logic` |

Module layout is in [architecture.md](architecture.md).
