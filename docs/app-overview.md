# Reminder – Application Overview

## Purpose

**Reminder** is a comprehensive task management and reminder application for Android, written in Kotlin. It helps users stay organised by letting them create richly configured reminders, capture notes, track birthdays, manage Google Tasks, and back up all data to the cloud.

The app is available on [Google Play](https://play.google.com/store/apps/details?id=com.cray.software.justreminder).

---

## Core Features

### Reminders & Tasks

| Capability | Description |
|---|---|
| **Flexible scheduling** | One-time, daily, by day-of-week, by day-of-month, by day-of-year, countdown timer, and iCalendar (RRULE) patterns |
| **Location-based reminders** | Triggers when the user enters or leaves a saved geographic location |
| **Actions** | A reminder can trigger a phone call, send an SMS, open a URL, launch an application, send an e-mail, or show a shopping sub-task list |
| **Priority & groups** | Reminders belong to user-defined groups and can be assigned a priority level |
| **Sub-tasks** | Shopping-list reminders support nested sub-task items |
| **Do-Not-Disturb** | Configurable quiet-hours window that suppresses reminder notifications |
| **Event history** | A historical log of every reminder event that has fired |
| **Recur presets** | Saved RRULE presets for quickly re-applying complex repeat patterns |
| **Smart lists** | Pinned quick filters (Today, Overdue, This week, No group) on the home reminder list, computed entirely on-device from existing reminder fields |
| **Tags** | Cross-cutting labels (name + color) that can be attached to both reminders and notes, independent of Groups; managed from their own screen and embeddable as a chip picker in edit screens |

### Streaks & Insights (PRO)

- A dashboard showing per-reminder streaks (consecutive days fired), a weekly activity trend, and the busiest day of the week — computed entirely on-device from the existing event-history log, with no data leaving the device
- Reachable from Settings; hidden on the free flavor

### Notes

- **Quick note-taking** with rich-text support and image attachments
- **Search and filter** across all notes by text content
- **iCalendar export** so notes can be shared via standard calendar formats

### Birthdays

- Track contact birthdays independently of the system calendar
- Look up birthdays by day and month for "upcoming today" widgets

### Google Tasks integration

- Browse and manage Google Task lists and individual tasks
- Full two-way sync with the Google Tasks API

### Gemini AppFunctions integration (PRO)

- Exposes reminder, note, birthday, and Google Task actions (create, list, complete, search, delete) to Gemini and other on-device assistants via Android's `androidx.appfunctions` platform API — no network round trip, everything runs on-device
- PRO-only; a "Gemini functions" screen under Settings → Other explains what's supported

### Calendar View

- A combined calendar screen that surfaces reminders, birthdays, and Google Task due dates in a single timeline view

### Cloud Backup & Sync

The app supports backing up and restoring all data (reminders, notes, birthdays, Google Tasks, places) to and from:

- **Google Drive**
- **Dropbox**

Sync is handled by a dedicated `sync` module that negotiates differences between local storage and remote file metadata so data survives device changes.

### Local Encrypted Backup (PRO)

- Exports all local data (reminders, notes excluded, groups, birthdays, places, recur presets) to a single passphrase-encrypted file the user saves anywhere via the system file picker (Storage Access Framework) — entirely offline, no network involved
- AES-256-GCM encryption with a PBKDF2-derived key (600,000 iterations); a wrong passphrase or corrupted file is detected and rejected cleanly
- Restoring upserts items back by ID without deleting anything already on-device
- Reachable from Settings ("Export backup" / "Restore backup"); hidden on the free flavor

### Widgets

Home-screen widgets give at-a-glance access to upcoming reminders, notes, birthdays, and Google Tasks without opening the app. A Quick Settings tile ("Add reminder") offers the same one-tap reminder creation from the notification shade, reusing the existing app-shortcut deep link.

### Customisation & Settings

- Material 3 theming with user-selectable colour schemes
- Custom notification sounds and vibration patterns
- Configurable default reminder settings
- Import / export using iCalendar (`.ics`) format
- Biometric lock for the app

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Minimum / Target SDK | See `app/build.gradle.kts` |
| UI toolkit | Jetpack Compose + Material 3 (views used in legacy screens) |
| Architecture | MVVM with ViewModels and StateFlow / LiveData |
| Navigation | Jetpack Navigation Component |
| Local persistence | Room (SQLite) |
| Dependency injection | Koin |
| Concurrency | Kotlin Coroutines |
| Cloud APIs | Google Drive REST API, Google Tasks API, Dropbox SDK |
| Analytics & crash reporting | Firebase Analytics, Firebase Crashlytics |
| Date / time | ThreeTenABP (JSR-310 back-port) |
| iCalendar | lib-recur (RRULE parsing) |
| Build system | Gradle with Kotlin DSL |

---

## User-Facing Screens

| Screen | Description |
|---|---|
| Home / Dashboard | Main entry point; shows upcoming reminders and quick-add actions |
| Reminder list | Filterable list of all reminders |
| Reminder create/edit | Multi-step wizard for creating or editing a reminder with all scheduling options |
| Notes list | Grid/list of all notes |
| Note create/edit | Editor for creating or editing a note with image support |
| Birthdays list | List of tracked birthdays |
| Google Tasks | List-based view of Google Task lists and tasks |
| Calendar | Monthly/weekly calendar view with all events |
| Places | Map view for saved geographic locations used in GPS reminders |
| Settings | Full settings hierarchy |
| Widgets | Multiple widget configurations |
| Global search | Search across reminders, notes, birthdays, and tasks |
