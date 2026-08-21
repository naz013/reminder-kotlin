# Routines: Comprehensive Research Analysis & Technical Implementation Design

## 1. Executive Summary

This document presents the complete research analysis, architectural design, and implementation plan for introducing **Routines** into the *Reminder - TODO & Task Manager* application.

### Concept Definition
A **Routine** is a structured, sequenced habit/workflow composed of ordered, time-boxed steps (e.g., *Morning Routine*, *Workout Circuit*, *Evening Shutdown*, *Daily Planning*) that can be executed either **on-demand** or on a **recurring schedule** (daily, weekly, custom interval).

Key specifications:
1. **Strict UI Component & Design System Adherence**: Reuses established Material 3 Expressive tokens, `ui:ui-common` top bars, search bars, dialogs, `ui:ui-tag` pickers/filter rows, and `ui:ui-common`'s `ColorSlider`/`ThemeProvider.colorsForSliderThemed()` (the same solid-color-picker pair `feature-group`'s `EditGroupScreen` already uses for `GroupV2.color` — see §4.5 for why this replaced the originally-planned `NoteColorEngine.allColors()`).
2. **First-Class Domain Model**: Modeled as a dedicated `Routine` entity in `core:domain`, completely decoupled from `ReminderV2`.
3. **Step-Level Scheduled Times & Durations**: `RoutineStep` owns `scheduledTime: String?` (e.g., `"07:30"`) and `durationSeconds: Int` (e.g. `300` for 5 min). Steps are automatically sorted chronologically by `scheduledTime`.
4. **Auto-Resetting Cycles**: A recurring routine's cycle starts the moment it's saved with recurrence turned on (`Routine.lastResetAt` anchors it) - steps can be checked/unchecked freely all cycle long. At the next calendar-day rollover, whatever was completed is written to `RoutineExecutionRecord` history (even if nothing was completed) and every step is reset to `isCompleted = false`, regardless of the recurrence rule's actual cadence (a Mon/Wed/Fri routine still resets every day it's next opened, not just on its scheduled days - see `RoutineRecurrenceResetUseCase`). On-demand routines (`recurrence == null`) are never auto-reset: completing every step just leaves the routine showing as fully checked, and only a manual "Reset Steps" action (`ResetRoutineStepsUseCase`) clears it - see §4.4 for both use cases.
5. **Granular Execution Tracking**: `RoutineExecutionRecord` stores `completedStepIds: List<String>` (the specific UUIDs of completed steps) instead of a plain count, allowing precise step drop-off analytics.
6. **Solid Color Theming**: Routines support custom color selection via `ui-common`'s `ColorSlider` (the same solid, no-opacity picker `GroupV2.color` already uses), rendered as solid colored cards.
7. **Tags Integration (`TaggedItemType.ROUTINE`)**: Full support for cross-cutting tags with `TagChipPicker` in the editor and instant tag filtering in the list screen.
8. **Pinning Support**: Pinned routines appear at the top of the list, mirroring the Reminder and Note pin behavior.
9. **Home Screen Navigation Tile**: A new "Routines" header tile in `ChronologicalHomeScreen` navigating directly to the library.
10. **Todo-Like Edit Screen**: Intuitive step creation, duration selector pills, scheduled time pickers, drag-and-drop ordering, and tag picking styled similarly to `TodoEditScreen`.
11. **Dedicated Preview Screen**: Displays colored header, summary metrics, step list sorted by time, and "Start Routine" primary CTA.
12. **Guided Focus Runner (Execution Mode)**: An interactive fullscreen timer mode with countdowns, step transitions, auto-advance, and audio/haptic cues.
13. **Deep Habit Analytics (PRO Insights)**: Tracks actual focused execution time, step completion consistency, and consecutive day streaks in the PRO Insights module.
14. **Cross-Device Cloud Sync & Encrypted Local Backup**: Synchronized via Google Drive / Dropbox and included in local encrypted backups.

### Strategic Recommendation
**STRONG RECOMMENDATION: PROCEED WITH IMPLEMENTATION.**
Adding Routines as a standalone model solves existing recurring checklist limitations, expands the application into the high-demand habit productivity space, reinforces the app's privacy-first offline positioning, and creates compelling value for the PRO tier.

---

## 2. Research Analysis & Architectural Trade-offs

### 2.1 Current Codebase Audit & Usability Gaps

| Area | Current State in Codebase | Usability Gap / Limitation |
|---|---|---|
| **Recurring Subtasks** | `ReminderV2.shoppingItems` contains `List<ShopItemV2>`. | When a recurring reminder advances to its next occurrence in `CompleteReminderUseCase`, subtasks retain `isChecked = true`. Users must manually uncheck items every day. |
| **Subtask Semantics** | `ShopItemV2` only stores `summary`, `isChecked`, `isDeleted`, `uuId`, `createdAt`. | No ability to assign a target duration or scheduled time-of-day to individual steps. |
| **On-Demand Routines** | Tasks require fixed schedule dates/times. | Users cannot maintain a library of reusable on-demand routines (e.g. *15m Post-Workout Stretch*, *10m Clean*) to launch manually without setting an alarm. |
| **Execution Experience** | Static checklist in preview or notification popup. | No guided step-by-step timer runner. Users must switch between external timer apps and the checklist. |
| **Habit Tracking (Insights)** | `ReminderStreakCalculator` tracks when reminder alarms fire. | Does not record actual step-level completion ratios, focus duration elapsed, or step drop-off rates. |

---

### 2.2 Architectural Comparison: Dedicated Model vs. Overloading `ReminderV2`

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                          APPROACH A: Overload ReminderV2                                    │
│  (Add isRoutine flag + duration to ShopItemV2 in existing ReminderV2)                       │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│ ❌ High coupling: ReminderV2 is already massive (25+ fields, GPS, calls, SMS, RRULE, etc.)   │
│ ❌ Semantic mismatch: Subtasks are named "shoppingItems" and lack routine-specific metadata │
│ ❌ Scheduled-only: Cannot have on-demand/manual routines without setting an alarm/reminder  │
│ ❌ Messy history: Cannot track step-level execution time or skipped steps in historical logs│
└─────────────────────────────────────────────────────────────────────────────────────────────┘

                                             VS

┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                     APPROACH B: Dedicated Routine Model (SELECTED)                          │
│  (Clean first-class domain entity: Routine + RoutineStep + RoutineExecutionRecord)         │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│ ✅ Clean separation: ReminderV2 handles notifications; Routine handles sequenced execution  │
│ ✅ Step-level scheduling: RoutineStep holds scheduledTime and durationSeconds               │
│ ✅ Granular execution log: RoutineExecutionRecord tracks exact completedStepIds              │
│ ✅ Supports both Scheduled & On-Demand: Run anytime from dashboard, widget, or on schedule │
│ ✅ Rich habit tracking: Logs actual focused seconds, skipped steps, and habit streaks       │
│ ✅ Follows project standards: Sits neatly in feature-routine, ui-routine, and logic-routine │
│ ✅ Full UI parity: Colored cards, tag filtering, pin states, and Todo-like editing          │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Technical Architecture & Module Structure

Following the project's multi-module architecture guidelines (`docs/architecture.md`):

```
                                  ARCHITECTURE OVERVIEW
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                         app module                                          │
│                    ReminderApp.kt (DI), AppNavGraph.kt (Nav3 integration)                   │
└───────────────────────┬─────────────────────────────────────────────┬───────────────────────┘
                        │                                             │
         ┌──────────────▼──────────────┐               ┌──────────────▼──────────────┐
         │   feature:feature-routine   │               │    feature:feature-home     │
         │ • RoutinesListScreen        │               │ • Routines Header Tile      │
         │ • RoutineEditScreen         │               └─────────────────────────────┘
         │ • RoutinePreviewScreen      │               ┌─────────────────────────────┐
         │ • RoutineExecutionScreen    │               │  feature:feature-insights   │
         └──────────────┬──────────────┘               │ • RoutineStreakCalculator   │
                        │                              │ • RoutineStepDropoffCalc    │
         ┌──────────────▼──────────────┐               └──────────────┬──────────────┘
         │       ui:ui-routine         │                              │
         │ • Colored RoutineCard       │                              │
         │ • CircularStepTimer         │                              │
         │ • RoutineStepCard           │                              │
         └──────────────┬──────────────┘                              │
                        │                                             │
         ┌──────────────▼─────────────────────────────────────────────▼──────────────┐
         │                           logic:logic-routine                             │
         │ • RoutineStepComparator, RoutineRecurrenceResetUseCase                    │
         │ • StartRoutineUseCase, CompleteRoutineStepUseCase, ResetRoutineUseCase    │
         │ • RoutineDurationCalculator, RoutineScheduleBridge                        │
         └─────────────────────────────────────┬─────────────────────────────────────┘
                                               │
         ┌─────────────────────────────────────▼─────────────────────────────────────┐
         │                              repository-api                               │
         │ • RoutineRepository, RoutineExecutionRepository                          │
         └─────────────────────────────────────┬─────────────────────────────────────┘
                                               │
         ┌─────────────────────────────────────▼─────────────────────────────────────┐
         │                               core:domain                                 │
         │ • Routine, RoutineStep (scheduledTime), RoutineExecutionRecord             │
         │   (completedStepIds), TaggedItemType.ROUTINE                              │
         └───────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Detailed Component Design & Implementation Specifications

### 4.1 Domain Layer (`core:domain`)

```kotlin
// core/domain/src/main/kotlin/com/github/naz013/domain/routine/Routine.kt
package com.github.naz013.domain.routine

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class Routine(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val description: String? = null,
  val color: Int = 0,                     // Solid color code from Note palette, without opacity
  val isPinned: Boolean = false,          // Pin to top of list
  val icon: String? = null,
  val steps: List<RoutineStep> = emptyList(),
  val autoAdvance: Boolean = true,
  val soundAlertsEnabled: Boolean = true,
  val recurrence: RecurrenceRule? = null, // existing RecurrenceRule variants: Daily, Weekly (weekday selection), etc., or null (on-demand)
  val reminderId: String? = null,         // Linked ReminderV2 for scheduled notifications
  val lastResetAt: LocalDateTime? = null, // Recurrence reset marker
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
  val sync: SyncMetadata = SyncMetadata()
) {
  val totalDurationSeconds: Int
    get() = steps.sumOf { it.durationSeconds }

  val sortedSteps: List<RoutineStep>
    get() = steps.sortedWith(RoutineStepComparator)
}

data class RoutineStep(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val description: String? = null,
  val durationSeconds: Int = 0,      // e.g. 300 for 5 min (0 = untimed)
  val scheduledTime: String? = null, // e.g. "07:30" (HH:mm)
  val isCompleted: Boolean = false,
  val order: Int = 0
)

object RoutineStepComparator : Comparator<RoutineStep> {
  override fun compare(a: RoutineStep, b: RoutineStep): Int {
    val timeA = a.scheduledTime
    val timeB = b.scheduledTime
    return when {
      timeA != null && timeB != null -> {
        val timeComparison = timeA.compareTo(timeB)
        if (timeComparison != 0) timeComparison else a.order.compareTo(b.order)
      }
      timeA != null && timeB == null -> -1
      timeA == null && timeB != null -> 1
      else -> a.order.compareTo(b.order)
    }
  }
}

data class RoutineExecutionRecord(
  val id: String = UUID.randomUUID().toString(),
  val routineId: String,
  val executedAt: LocalDateTime,
  val totalTimeSpentSeconds: Int,
  val completedStepIds: List<String> = emptyList(),
  val totalStepsCount: Int
) {
  val completedStepsCount: Int
    get() = completedStepIds.size
}
```

```kotlin
// core/domain/src/main/kotlin/com/github/naz013/domain/TaggedItemType.kt
enum class TaggedItemType {
  REMINDER,
  NOTE,
  BIRTHDAY,
  GOOGLE_TASK,
  ROUTINE
}
```

---

### 4.2 Persistence Layer (`data:repository-api` & `data:repository`)

1. **Repository Interfaces**:
   - `RoutineRepository`: `getAll()`, `getById(id)`, `save(routine)`, `delete(id)`, `setPinned(id, isPinned)`, `observeAll(): Flow<List<Routine>>`.
   - `RoutineExecutionRepository`: `save(record)`, `getByRoutineId(id)`, `getByDateRange(from, to)`, `getAll()`.
2. **Room Database Entities & DAOs**:
   - `RoutineEntity`: Primary key `id`, `isPinned`, `color`, embedded schedule columns, Gson `@TypeConverters` for `List<RoutineStep>`. `RecurrenceRule` is **not** stored via a bare `@TypeConverters` Gson converter — it is a sealed class, and `RecurrenceRule.kt`'s own doc comment warns that R8 can strip/rename unprotected fields during Gson round-tripping (this already caused a production crash for `ReminderV2`). Instead, follow the precedent in `ReminderV2Mapper.kt`: split into a `recurrenceType` discriminator column plus a `recurrencePayload` JSON blob column, with an explicit `RoutineMapper.toColumns()`/`toRecurrenceRule()` pair and a documented fallback (e.g. to `null`/on-demand) on unparseable payloads.
   - `RoutineExecutionEntity`: Primary key `id`, index on `routineId` and `executedAt`, `@TypeConverters(ListStringTypeConverter::class)` for `completedStepIds`.
   - `RoutineMapper.kt`: dedicated entity↔domain mapper (mirrors `ReminderV2Mapper.kt`/`GroupV2Mapper.kt`/`TagMapper.kt`), required because `Routine` carries polymorphic (`RecurrenceRule`) and list (`List<RoutineStep>`) fields that need explicit column mapping, not just a generated Room converter.
3. **Database Migration (`Migration32To33`)**:
   - Adds tables `Routine` and `RoutineExecution` to `AppDb`.
   - Updates `AppDb.version` from `32` to `33`.
4. **Table Event Bus (`Table.kt`)**:
   - Register `Table.Routine` and `Table.RoutineExecution` so UI flows update reactively via `TableChangeListener`.

---

### 4.3 Cloud Sync & Encrypted Local Backup

1. **Cloud Sync (Google Drive & Dropbox)**:
   - `DataType`: add `Routines(".rt1")` and `RoutineExecutions(".rte1")`.
   - `RoutineJson` / `RoutineExecutionJson` DTOs in `data:files-api`.
   - `DataConverterImpl`: Register serializers/deserializers for cloud backup files.
   - `DataTypeRepositoryCallerFactory`: Wire `RoutineRepositoryCaller` and `RoutineExecutionRepositoryCaller`.
2. **Offline Local Encrypted Backup (PRO)**:
   - Extend `BackupEnvelope` in `extensions:localbackup`:
     ```kotlin
     data class BackupEnvelope(
       // ... existing entities ...
       val routines: List<Routine> = emptyList(),
       val routineExecutions: List<RoutineExecutionRecord> = emptyList()
     )
     ```
   - Update `LocalBackupApiImpl` export and import routines with full passphrase-based AES-256-GCM encryption.

---

### 4.4 Business Logic Layer (`logic:logic-routine`) — IMPLEMENTED (deviates from the original plan below in a few places, noted inline)

1. **`RoutineRecurrenceResetUseCase`**:
   - No-ops for on-demand routines (`recurrence == null`) - they're never auto-reset.
   - Otherwise: if `lastResetAt`'s date is before today (or `null`), writes the cycle's result to history via `RecordRoutineExecutionUseCase` (`completedStepIds` as of *before* the reset, `totalTimeSpentSeconds = 0` since this is a lazy day-boundary check, not a focus-runner session), **then** unchecks all `steps` and bumps `lastResetAt` to now.
   - Runs lazily on next list/preview screen load, not on a background schedule - see the kdoc on the class for the resulting limitation (skipped days between app opens don't each get their own history entry).
   - `Routine.lastResetAt` is also "when did the current cycle start" - callers of `SaveRoutineUseCase` (i.e. `RoutineEditViewModel`) are responsible for setting it to the save time the moment recurrence goes from off to on, so the very first cycle starts at save time instead of being immediately eligible for a same-day reset. Resaving an already-recurring routine (e.g. editing its description) must leave `lastResetAt` untouched, and turning recurrence off clears it (on-demand routines don't need an anchor).
2. **`RoutineDurationCalculator`**: `calculateTotalDuration`/`calculateRemainingDuration(steps): Int`, `formatDuration(seconds): String` (e.g. `"25m"`, `"1h 10m"`, `"45s"`).
3. **`SaveRoutineUseCase`**: bumps `sync.version`/`syncState` and `updatedAt`, saves, schedules a cloud upload. **Does not** validate steps/title or manage tags itself (unlike the original plan) - matches the rest of this codebase's convention (`SaveReminderUseCase` doesn't validate or touch tags either): validation (title non-blank, **at least one step is mandatory**) and the `lastResetAt` anchoring logic live in `RoutineEditViewModel`, and tag assignment is a separate immediate `ToggleTagAssignmentUseCase` call per toggle, not bundled into save. `RoutineScheduleBridge` (linked `ReminderV2` trigger) doesn't exist yet - deferred, see the open questions.
4. **`DeleteRoutineUseCase`**: deletes the routine, its execution history, and tag assignments; schedules a cloud delete. Doesn't yet clear a linked `ReminderV2` trigger (same `RoutineScheduleBridge` dependency).
5. **`ToggleRoutinePinUseCase`** / **`ResetRoutineStepsUseCase`**: both delegate to `SaveRoutineUseCase` after toggling `isPinned` / clearing every step's `isCompleted`. `ResetRoutineStepsUseCase` is the **only** reset path for on-demand routines (manual, user-triggered from the preview screen's overflow menu) and does **not** write execution history - only the automatic recurring-cycle reset does.
6. **`RecordRoutineExecutionUseCase`**: saves a `RoutineExecutionRecord`. Two callers: `RoutineRecurrenceResetUseCase` (automatic, `totalTimeSpentSeconds = 0`) and, once built, the focus-runner execution screen (real elapsed time).

---

### 4.5 UI & Presentation Layer (`ui:ui-routine` & `feature:feature-routine`)

#### 1. Reusable Building Blocks (`ui:ui-routine`) — IMPLEMENTED
- **`RoutineCard`**: Material 3 card rendered with a caller-supplied solid `backgroundColor`/`contentColor` pair (via `UiRoutineListItem`) — shows title, description, step-count/duration/schedule badges, an optional caller-supplied `tagsContent` slot, pin icon, and a "Start" CTA. Takes no `Routine`/`RoutineStep`/tag domain types directly and depends only on `ui-common` (`docs/architecture.md`'s `ui-*` dependency rule forbids a sibling `ui-*` → `ui-*` edge), so `feature-routine` renders `ui-tag`'s `TagChipRow` through the slot instead of `ui-routine` depending on `ui-tag`.
- **`CircularStepTimer`**: M3 determinate `CircularProgressIndicator` (remaining-fraction ring, drains as the step's time elapses) with the remaining time centered inside it.
- **`RoutineColorPicker`**: **Superseded the `NoteColorEngine.allColors()` plan.** `NoteColorEngine` is a Koin-injected class (needs `ThemeProvider`+`NotePreferences`) whose "remember last color" state is Note-specific, and depending on it would have created the first-ever `ui-*` → `ui-*` dependency edge in the codebase (`ui-routine` → `ui-note`) — none currently exists (`docs/architecture.md`'s `ui-<feature>` allowed-dependency list is `domain`/`logging-api`/`ui-common`/`platform-*` only). `feature-group`'s `EditGroupScreen` already solves the identical problem for `GroupV2.color` (also solid, no opacity) by wrapping `ui-common`'s existing `ColorSlider` — `RoutineColorPicker` is that same "surfaceContainer card + label + `ColorSlider`" shell, extracted into `ui-routine` and fed whatever `List<Color>` the caller supplies (e.g. `ThemeProvider.colorsForSliderThemed()`), with no new module dependency.

#### 2. Feature Screens & Navigation (`feature:feature-routine`)
- **`RoutinesListScreen`**:
  - Reuses `com.github.naz013.ui.common.compose.foundation.component.SearchBar`.
  - Reuses `com.github.naz013.ui.tag.TagFilterRow` for instant tag filtering.
  - Reuses `MenuIconButton` and `AppDropdownMenu` with `PopupMenuItem` for sort order toggling.
  - Sorting: Pinned items at top, followed by sort order toggle: By Creation Date or By Name.
  - Colored `RoutineCard`s in a 2-column staggered grid or list.
  - Floating Action Button to create a new routine.
- **`RoutineEditScreen`** (Todo-like layout):
  - `TopAppBar` with `TopAppbarColor`: Back button, Title, Pin toggle icon, Delete icon (if editing), Save action.
  - Title `OutlinedTextField`.
  - Solid Color Palette Selector (`RoutineColorPicker`).
  - Steps Editor:
    - Step title input.
    - Duration selector pills: `None`, `5m`, `10m`, `15m`, `Custom`.
    - Scheduled time picker button (`⏱ 07:30` / `No time`) per step.
    - Drag handles for reordering untimed steps.
  - Recurrence Section (Recurrence rule selector over existing `RecurrenceRule` variants: Daily, Weekly (with weekday selection), Monthly, etc.).
  - Tags Section: `TagChipPicker` with `onTagToggle` and `onManageTagsClick`.
  - Auto-advance & sound chimes toggle switches.
- **`RoutinePreviewScreen`**:
  - Colored top banner/card with title, description, and tag chips.
  - Info metrics: Total Duration (`⏱ 35 min`), Step Count (`5 steps`), Recurrence status.
  - Step-by-step checklist sorted chronologically by `scheduledTime`, showing time badges (e.g., `07:30`), duration badges (`15 min`), and interactive checkboxes.
  - TopAppBar actions: Edit, Pin/Unpin, Reset Steps, Share, Delete.
  - Primary **"Start Routine"** (Focus Runner) CTA button at bottom.
- **`RoutineExecutionScreen` (Focus Mode Runner)**:
  - Distraction-free, full-screen runner.
  - Active step title, scheduled time indicator (`07:30`), and step counter (*Step 2 of 5*).
  - `CircularStepTimer` with remaining countdown.
  - Controls: Play / Pause, +1 Min, Skip Step, Previous Step, Complete Step.
  - Sound tone & haptic vibration on step transitions.
  - Final celebration summary card displaying total focus minutes logged and completed steps summary.

---

### 4.6 Home Screen Tile Integration (`feature:feature-home`)

- In `GetNavigationItemsUseCase.kt`, add a `Routines` header tile (`HeaderNavigationItem`):
  - Icon: `R.drawable.ic_fluent_timer`
  - Color: `Color(0xFF86E3CE)`
  - Subtitle: Total routine count from `RoutineRepository`
  - Navigation event: `OpenRoutines` → navigates to `RoutineNavKey.List`.

---

### 4.7 Streaks & Habit Insights (`feature:feature-insights`)

1. **`RoutineStreakCalculator`**:
   - Computes consecutive day execution streaks from `RoutineExecutionRecord` history.
   - Handles multi-execution days and calculates all-time longest streaks.
2. **`RoutineStepDropoffCalculator`**:
   - Analyzes `completedStepIds` across runs to determine which steps are most consistently completed vs frequently skipped.
3. **`InsightsScreen`**:
   - Surfaces **Routine Habit Streaks**, **Focus Time Trends**, and **Step Completion Consistency** cards on the PRO Insights dashboard.

---

## 4.8 Open Questions & Follow-ups

1. ~~**`NoteColorEngine` reuse**~~ — RESOLVED: see §4.5. `RoutineColorPicker` uses `ui-common`'s `ColorSlider` (the same building block `GroupV2.color` already uses), fed by a caller-supplied `List<Color>` (e.g. `ThemeProvider.colorsForSliderThemed()`). No `NoteColorEngine` dependency, no new `ui-*` → `ui-*` edge, and no accidental sharing of Note's "remembered last color" preference state.
2. **Free vs. PRO gating of base Routines**: Insights integration is explicitly PRO-gated, but whether `feature-routine` itself (create/edit/run) is free, PRO-only, or free-with-limits is undecided and should be settled before implementation of the editor/list screens.
3. **Notifications for step scheduled times**: should each timed step schedule its own notification, or only the routine's first step / linked `ReminderV2` trigger?
4. **Duration/color-contrast resolution**: `RoutineCard`'s `UiRoutineListItem` takes pre-formatted `durationLabel`/`stepCountLabel`/`scheduleRangeLabel` strings and a pre-resolved `contentColor` — `ui-routine` can't depend on `logic-routine` (for `RoutineDurationCalculator`) or compute color contrast itself without its own palette engine, so `feature-routine`'s ViewModel/mapper (Component 6) owns both, using `RoutineDurationCalculator` and (for contrast) Compose's built-in `Color.luminance()`.

---

## 5. Comprehensive Verification & E2E Testing Plan

### 5.1 Automated Unit & Database Tests
- `RoutineStepComparatorTest`: Validates sorting steps chronologically by `scheduledTime`, handling null times and equal timestamps.
- `RoutineDurationCalculatorTest`: Validates step duration summation and time formatting.
- `RoutineRecurrenceResetUseCaseTest`: Auto-reset of steps upon crossing into new recurrence cycle.
- `RoutineStreakCalculatorTest`: Streak calculations, edge cases (gap days, same-day multiple executions).
- `RoutineStepDropoffCalculatorTest`: Verification of step completion rates using `completedStepIds`.
- `RoutinesListViewModelTest`: Search query filtering, tag chip filtering, pinned item ordering.
- `RoutineEditViewModelTest` & `RoutineExecutionViewModelTest`: State flows, timer countdown ticks, pause/resume, and auto-advance.
- `Migration32To33Test`: Verify SQLite table creation with `completedStepIds` list converter, column types, and data preservation.
- `DataConverterRoutineTest` & `LocalBackupRoutineTest`: JSON & encrypted archive export/import round-trips.

---

### 5.2 End-to-End (E2E) UI Instrumentation Test Suite (`RoutinesE2ETest.kt`)

Following `docs/e2e-testing.md`, the following 22 test cases will be implemented in `app/src/androidTest/kotlin/com/elementary/tasks/e2e/RoutinesE2ETest.kt` (Tier B Compose tests) and `.maestro/routines/`:

| # | Test Case | Scope & Expected Behavior | Tool | Pri |
|---|---|---|---|---|
| **E1** | `createRoutine_withTitleStepsAndColor` | Create a routine with a title, select a solid color from Note palette, add 3 steps with duration pills (`5m`, `10m`), save, and verify the colored card renders in `RoutinesListScreen`. | Tier B | P0 |
| **E2** | `routineEditor_stepScheduledTimes_autoSorts` | In `RoutineEditScreen`, add steps with mixed times (`09:00`, `07:15`, `08:30`); save; verify preview and execution screens sort them chronologically (`07:15` → `08:30` → `09:00`). | Tier B | P0 |
| **E3** | `routineEditor_reorderUntimedSteps` | Add untimed steps, drag to reorder; verify manual `order` is saved and preserved. | Tier B | P1 |
| **E4** | `routineEditor_tagAssignment` | Toggle tags in `TagChipPicker` in editor; verify tag chips render on the routine card and `TagAssignment` rows exist. | Tier B | P0 |
| **E5** | `routineEditor_pinToggling` | Tap pin icon in editor top bar; save; verify routine displays pin badge and sorts to top of `RoutinesListScreen`. | Tier B | P0 |
| **E6** | `routineEditor_deleteRoutine_cleansUpData` | Open existing routine, tap delete, confirm; verify routine card disappears from list and repository is clean. | Tier B | P0 |
| **E7** | `homeTile_navigatesToRoutinesList` | From `ChronologicalHomeScreen`, tap the "Routines" header tile; verify `RoutinesListScreen` opens with matching subtitle count. | Tier B, Maestro | P0 |
| **E8** | `routinesList_searchQuery_filtersCards` | Type query into `SearchBar`; verify non-matching routine cards are filtered out in real time. | Tier B | P0 |
| **E9** | `routinesList_tagFilterRow_filtersByTag` | Tap a tag chip in `TagFilterRow`; verify list instantly filters to show only routines carrying that tag. | Tier B | P0 |
| **E10** | `routinesList_sortOrder_toggleDateAndName` | Switch sort order between "By Date" and "By Name"; verify list order updates while pinned items stay pinned at the top. | Tier B | P1 |
| **E11** | `routinePreview_displaysColoredBannerAndSteps` | Tap routine card from list; verify `RoutinePreviewScreen` displays solid colored header, tag chips, total duration badge, and sorted step checklist. | Tier B | P0 |
| **E12** | `routinePreview_stepCheckbox_togglesState` | In `RoutinePreviewScreen`, toggle a step checkbox; verify state updates and persists. | Tier B | P0 |
| **E13** | `routinePreview_resetSteps_unchecksAll` | Tap "Reset Steps" menu action in preview; verify all steps revert to unchecked. | Tier B | P1 |
| **E14** | `focusRunner_countdownTimer_ticksAndPauses` | Tap "Start Routine"; verify `RoutineExecutionScreen` opens, circular countdown timer ticks down, and pause/resume buttons work accurately. | Tier B, Maestro | P0 |
| **E15** | `focusRunner_stepCompletionAndAutoAdvance` | Let step timer expire or tap "Complete Step"; verify haptic/audio chime triggers and runner automatically transitions to the next step. | Tier B | P0 |
| **E16** | `focusRunner_skipStep_advancesWithoutCompletion` | Tap "Skip Step"; verify runner moves to next step without marking the skipped step as completed. | Tier B | P1 |
| **E17** | `focusRunner_plusOneMinute_extendsTimer` | Tap "+1 Min" button during active timer; verify countdown timer extends by 60 seconds. | Tier B | P1 |
| **E18** | `focusRunner_completion_recordsCompletedStepIds` | Complete routine with 1 step skipped; verify celebration dialog appears and `RoutineExecutionRecord` contains only the 2 completed step UUIDs. | Tier B | P0 |
| **E19** | `routineRecurrence_newCycle_autoResetsSteps` | Complete a daily routine today; advance simulated clock (`FakeNowDateTimeProvider`) to tomorrow; verify all steps are fresh and unchecked while execution logs remain intact. | Tier B | P0 |
| **E20** | `insights_routineStreaks_updatesConsecutiveDays` | Complete routines across multiple days; open PRO Insights; verify `UiStreak` shows incremented current streak and longest streak. | Tier B | P0 |
| **E21** | `insights_stepDropoff_analyzesCompletionRatio` | Run routines with recurring skipped steps; verify `RoutineStepDropoffCalculator` reports accurate consistency ratios. | Tier B | P1 |
| **E22** | `cloudSyncAndLocalBackup_routineRoundTrip` | Export routines to encrypted backup archive and cloud sync DTOs; wipe local database; restore; verify all routines, steps, and execution records restore intact. | Tier B | P0 |

---

### 5.3 Manual Verification Checklist
- **UI Design Guidelines Compliance**:
  - Verify typography, surface colors, top bars, chips, and dialogs match existing Material 3 Expressive screens (`NotesScreen`, `TodoEditScreen`, `PreviewReminderScreen`).
- **Physical Device Experience**:
  - Verify haptic feedback and audio chime play cleanly during step transitions in Focus Mode without lag.
  - Verify backgrounding and resuming the app during an active focus timer keeps timer state intact.
