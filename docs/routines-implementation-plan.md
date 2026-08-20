# Implementation Plan: First-Class Routines Feature

Introduce a dedicated **Routines** feature into *Reminder - TODO & Task Manager*. A Routine is a structured, sequenced habit/workflow composed of ordered, time-boxed steps (e.g., *Morning Routine*, *Workout Circuit*, *Evening Shutdown*) that can be run on-demand or on a recurring schedule with auto-resetting steps, step-by-step focus execution, and deep habit tracking in PRO Insights.

## User Review Required

> [!IMPORTANT]
> **Strict Adherence to Existing UI Components & Design System**:
> - Reuses the established Material 3 Expressive design tokens (`AppTheme`, `MaterialTheme.colorScheme`, `Typography`).
> - Reuses existing building blocks from `ui:ui-common`, `ui:ui-note`, and `ui:ui-tag`:
>   - Navigation & TopBars: `TopAppBar`, `MenuIconButton`, `MenuTextButton`, `TopAppbarColor`, `SelectionTopBar`.
>   - Icons: `AppIcons` (Fluent icons and Material symbols).
>   - Color Engine: Reuses `NoteColorEngine.allColors()` for solid colored cards without opacity.
>   - Filter Chips & Pickers: `TagChipPicker`, `TagFilterRow`, `FilterChip`.
>   - Dialogs & BottomSheets: `AppModalBottomSheet`, `AppDropdownMenu`, `PopupMenuItem`.
>   - Inputs: `SearchBar`, `OutlinedTextField`, `BasicTextField`.

> [!IMPORTANT]
> **Step-Level Scheduled Times & Automatic Sorting**:
> - `RoutineStep` owns `scheduledTime: String?` (e.g. `"07:30"` in `HH:mm` format) and `durationSeconds: Int`.
> - Steps are automatically sorted chronologically by `scheduledTime` (via `RoutineStepComparator`), falling back to `order` for untimed steps.
> - `Routine` does NOT hold a top-level `scheduledTime`; it holds the `recurrence: RecurrenceRule?` (Daily, Weekdays, Monthly, etc.).

> [!IMPORTANT]
> **Granular Execution Tracking (`completedStepIds`)**:
> - `RoutineExecutionRecord` stores `completedStepIds: List<String>` (the specific UUIDs of completed steps) instead of a plain count, enabling exact step-level drop-off and habit analytics.

> [!IMPORTANT]
> **Home Screen Tile & Module Isolation**:
> - Trio module extraction: `:logic:logic-routine`, `:ui:ui-routine`, `:feature:feature-routine`.
> - Room database version bump (`Migration32To33` in `AppDb`).
> - Cloud sync (`DataType.Routines`, `DataType.RoutineExecutions`) & local encrypted backup (`BackupEnvelope`).
> - Home screen navigation tile in `ChronologicalHomeScreen` / `GetNavigationItemsUseCase`.

---

## Open Questions

> [!NOTE]
> 1. **Mixed Steps Ordering**: When a routine contains both timed steps (`scheduledTime = "07:30"`) and untimed steps (`scheduledTime = null`), should timed steps appear first in chronological order followed by untimed steps, or should untimed steps respect their manual `order` positions? (Recommended: Chronological for timed steps, then untimed steps by `order`).
> 2. **Notifications for Step Scheduled Times**: When a step has a `scheduledTime` (e.g. 07:30), should the app schedule individual reminder notifications for each timed step, or a reminder for the first step in the routine?

---

## Proposed Changes

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

### Component 1: Domain Layer (`core:domain`)

Define domain models and enums with zero Android framework dependencies.

#### [NEW] [Routine.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/core/domain/src/main/kotlin/com/github/naz013/domain/routine/Routine.kt)
- `Routine`:
  - `id: String = UUID.randomUUID().toString()`
  - `title: String`
  - `description: String?`
  - `color: Int = 0` (solid color code from Note palette, no opacity)
  - `isPinned: Boolean = false`
  - `icon: String?`
  - `steps: List<RoutineStep> = emptyList()`
  - `autoAdvance: Boolean = true`
  - `soundAlertsEnabled: Boolean = true`
  - `recurrence: RecurrenceRule?` (Daily, Weekdays, Monthly, or null for on-demand)
  - `reminderId: String?` (linked `ReminderV2` for notification trigger)
  - `lastResetAt: LocalDateTime?` (timestamp of the last recurrence reset)
  - `createdAt: LocalDateTime`
  - `updatedAt: LocalDateTime`
  - `sync: SyncMetadata = SyncMetadata()`
- Derived Properties:
  - `val totalDurationSeconds: Int get() = steps.sumOf { it.durationSeconds }`
  - `val sortedSteps: List<RoutineStep> get() = steps.sortedWith(RoutineStepComparator)`

#### [NEW] [RoutineStep.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/core/domain/src/main/kotlin/com/github/naz013/domain/routine/RoutineStep.kt)
- `RoutineStep`:
  - `id: String = UUID.randomUUID().toString()`
  - `title: String`
  - `description: String?`
  - `durationSeconds: Int = 0` (0 = untimed)
  - `scheduledTime: String? = null` (e.g. `"07:30"` in `HH:mm` format)
  - `isCompleted: Boolean = false`
  - `order: Int = 0`

#### [NEW] [RoutineStepComparator.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/core/domain/src/main/kotlin/com/github/naz013/domain/routine/RoutineStepComparator.kt)
- Comparator for sorting steps chronologically by `scheduledTime` when present, followed by manual `order`:
  ```kotlin
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
  ```

#### [NEW] [RoutineExecutionRecord.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/core/domain/src/main/kotlin/com/github/naz013/domain/routine/RoutineExecutionRecord.kt)
- `RoutineExecutionRecord`:
  - `id: String = UUID.randomUUID().toString()`
  - `routineId: String`
  - `executedAt: LocalDateTime`
  - `totalTimeSpentSeconds: Int`
  - `completedStepIds: List<String> = emptyList()` (specific step UUIDs completed)
  - `totalStepsCount: Int`
- Derived Property: `val completedStepsCount: Int get() = completedStepIds.size`

#### [MODIFY] [TaggedItemType.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/core/domain/src/main/kotlin/com/github/naz013/domain/TaggedItemType.kt)
- Add `ROUTINE` to `TaggedItemType` enum:
  ```kotlin
  enum class TaggedItemType {
    REMINDER,
    NOTE,
    BIRTHDAY,
    GOOGLE_TASK,
    ROUTINE
  }
  ```

---

### Component 2: Persistence Layer (`data:repository-api` & `data:repository`)

Define repository interfaces, Room entities, DAOs, converters, and database migrations.

#### [NEW] [RoutineRepository.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository-api/src/main/java/com/github/naz013/repository/RoutineRepository.kt)
- Methods: `getAll(): List<Routine>`, `getById(id: String): Routine?`, `save(routine: Routine)`, `delete(id: String)`, `observeAll(): Flow<List<Routine>>`, `setPinned(id: String, isPinned: Boolean)`.

#### [NEW] [RoutineExecutionRepository.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository-api/src/main/java/com/github/naz013/repository/RoutineExecutionRepository.kt)
- Methods: `save(record: RoutineExecutionRecord)`, `getByRoutineId(id: String): List<RoutineExecutionRecord>`, `getByDateRange(from: LocalDate, to: LocalDate): List<RoutineExecutionRecord>`, `getAll(): List<RoutineExecutionRecord>`.

#### [MODIFY] [Table.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository-api/src/main/java/com/github/naz013/repository/table/Table.kt)
- Add `Table.Routine` and `Table.RoutineExecution` for reactive change notifications via `TableChangeNotifier`.

#### [NEW] [RoutineEntity.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/entity/RoutineEntity.kt)
- Room entity for `Routine` with `@PrimaryKey val id: String`, `isPinned: Boolean`, `color: Int`, Gson `@TypeConverters` for `List<RoutineStep>` and `RecurrenceRule`.

#### [NEW] [RoutineExecutionEntity.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/entity/RoutineExecutionEntity.kt)
- Room entity for `RoutineExecutionRecord` with `@TypeConverters(ListStringTypeConverter::class)` for `completedStepIds`.

#### [NEW] [RoutineDao.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/dao/RoutineDao.kt) & [RoutineExecutionDao.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/dao/RoutineExecutionDao.kt)
- Room DAOs with Flow and suspend query operations.

#### [NEW] [Migration32To33.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/migrations/Migration32To33.kt)
- Database migration script executing `CREATE TABLE Routine (...)` and `CREATE TABLE RoutineExecution (...)`.

#### [MODIFY] [AppDb.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/repository/src/main/java/com/github/naz013/repository/AppDb.kt)
- Bump version to `33`, register new entities and `MIGRATION_32_33`.

---

### Component 3: Cloud Sync & Local Backup (`data:files-api`, `data:files`, `data:sync`, `extensions:localbackup`)

Enable cross-device cloud synchronization and passphrase-encrypted offline backup/restore for Routines.

#### [MODIFY] [DataType.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/files-api/src/main/kotlin/com/github/naz013/files/DataType.kt)
- Add `Routines(".rt1")` and `RoutineExecutions(".rte1")`.

#### [NEW] [RoutineJson.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/files-api/src/main/kotlin/com/github/naz013/files/model/RoutineJson.kt)
- DTO model for JSON file serialization.

#### [MODIFY] [DataConverterImpl.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/files/src/main/kotlin/com/github/naz013/files/DataConverterImpl.kt)
- Register `Routine` and `RoutineExecution` conversion to/from JSON.

#### [MODIFY] [DataTypeRepositoryCallerFactory.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/data/sync/src/main/kotlin/com/github/naz013/sync/local/DataTypeRepositoryCallerFactory.kt)
- Register `RoutineRepositoryCaller` and `RoutineExecutionRepositoryCaller` for cloud sync upload/download.

#### [MODIFY] [BackupEnvelope.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/extensions/localbackup/src/main/kotlin/com/github/naz013/localbackup/archive/BackupEnvelope.kt) & [LocalBackupApiImpl.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/extensions/localbackup/src/main/kotlin/com/github/naz013/localbackup/LocalBackupApiImpl.kt)
- Add `routines: List<Routine>` and `routineExecutions: List<RoutineExecutionRecord>` to local encrypted backup archive export and import.

---

### Component 4: Business Logic Layer (`logic:logic-routine`)

Create `:logic:logic-routine` module for reusable routine use cases.

#### [NEW] [RoutineDurationCalculator.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/logic/logic-routine/src/main/kotlin/com/github/naz013/logic/routine/RoutineDurationCalculator.kt)
- Utility functions for total duration, remaining duration, and formatted display strings (e.g. "25m", "1h 10m").

#### [NEW] [RoutineRecurrenceResetUseCase.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/logic/logic-routine/src/main/kotlin/com/github/naz013/logic/routine/usecase/RoutineRecurrenceResetUseCase.kt)
- Checks if the current date/time has crossed into a new recurrence cycle (e.g. today > `lastResetAt.toLocalDate()`); if so, automatically unchecks all `steps` (`isCompleted = false`) and updates `lastResetAt`.

#### [NEW] [RoutineUseCases.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/logic/logic-routine/src/main/kotlin/com/github/naz013/logic/routine/usecase/)
- `SaveRoutineUseCase`: validates steps, sorts by `scheduledTime`, updates tag assignments, and manages linked `ReminderV2` trigger if scheduled.
- `DeleteRoutineUseCase`: removes routine, associated tag assignments, linked reminder trigger, and execution logs.
- `ToggleRoutinePinUseCase`: toggles `isPinned` state.
- `RecordRoutineExecutionUseCase`: saves `RoutineExecutionRecord` with `completedStepIds` and triggers sync upload.
- `ResetRoutineStepsUseCase`: manually unchecks all steps on demand.

---

### Component 5: Reusable UI Components (`ui:ui-routine`)

Create `:ui:ui-routine` Compose building blocks adhering to Material 3 Expressive guidelines.

#### [NEW] [RoutineCard.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/ui/ui-routine/src/main/kotlin/com/github/naz013/ui/routine/RoutineCard.kt)
- Reuses `NoteColorEngine.allColors()` for solid colored surfaces (mirroring `NoteCard`).
- Includes semantics test tags: `testTag = "routine_card_${routine.id}"`, `testTag = "routine_start_button_${routine.id}"`.
- Shows title, description, step count, total duration badge (`[⏱ 25 min]`), tag chips, step schedule pill (`07:30 - 08:45`), pin icon, and a "Start" CTA.

#### [NEW] [CircularStepTimer.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/ui/ui-routine/src/main/kotlin/com/github/naz013/ui/routine/CircularStepTimer.kt)
- Animated circular progress indicator with countdown time display, pulse animations, and pause states.

#### [NEW] [RoutineColorPicker.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/ui/ui-routine/src/main/kotlin/com/github/naz013/ui/routine/RoutineColorPicker.kt)
- Horizontal row of solid color circles reusing `NoteColorEngine.allColors()`, without opacity slider.

---

### Component 6: Feature Screens & Navigation (`feature:feature-routine`)

Create `:feature:feature-routine` module with 4 primary destinations adhering strictly to `ui-common` components and layout patterns:

#### [NEW] [RoutineNavKey.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/RoutineNavKey.kt) & [RoutineNavGraph.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/RoutineNavGraph.kt)
- Destinations:
  - `RoutineNavKey.List`: Library of routines.
  - `RoutineNavKey.Edit(id: String?)`: Create / edit routine.
  - `RoutineNavKey.Preview(id: String)`: Preview routine with sorted steps and start button.
  - `RoutineNavKey.Execute(id: String)`: Fullscreen focus execution runner.

#### [NEW] [RoutinesListScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/list/RoutinesListScreen.kt) & [RoutinesListViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/list/RoutinesListViewModel.kt)
- Reuses `com.github.naz013.ui.common.compose.foundation.component.SearchBar`.
- Reuses `com.github.naz013.ui.tag.TagFilterRow` for instant tag filtering.
- Reuses `MenuIconButton` and `AppDropdownMenu` with `PopupMenuItem` for sort order toggling (Creation Date vs Name).
- Sorting: Pinned items at top, followed by chosen sort order.
- Colored `RoutineCard`s in a 2-column staggered grid (`LazyVerticalStaggeredGrid`) or list.
- Material 3 `FloatingActionButton` to create a new routine.

#### [NEW] [RoutineEditScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/edit/RoutineEditScreen.kt) & [RoutineEditViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/edit/RoutineEditViewModel.kt)
- Layout styled identically to `TodoEditScreen.kt`:
  - `TopAppBar` with `TopAppbarColor`: Back button (`MenuIconButton`), Title (`Text`), Pin toggle icon, Delete icon (if editing), Save action (`MenuTextButton`).
  - Title `OutlinedTextField`.
  - Solid Color Palette Selector (`RoutineColorPicker`).
  - Steps Editor:
    - Step title input.
    - Duration selector pills: `None`, `5m`, `10m`, `15m`, `Custom`.
    - Scheduled time picker button (`⏱ 07:30` / `No time`) per step.
    - Drag handles (`AppIcons.Fluent.ReOrderDots`) for reordering untimed steps.
  - Recurrence Section (Recurrence rule selector: Daily, Weekdays, Monthly, etc.).
  - Tags Section: `TagChipPicker` with `onTagToggle` and `onManageTagsClick`.
  - Auto-advance & sound chimes toggle switches.

#### [NEW] [RoutinePreviewScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/preview/RoutinePreviewScreen.kt) & [RoutinePreviewViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/preview/RoutinePreviewViewModel.kt)
- Colored top banner/card with title, description, and tag chips.
- Info metrics: Total Duration (`⏱ 35 min`), Step Count (`5 steps`), Recurrence status.
- Step-by-step checklist sorted chronologically by `scheduledTime`, showing time badges (e.g., `07:30`), duration badges (`15 min`), and interactive checkboxes.
- TopAppBar actions: Edit, Pin/Unpin, Reset Steps, Share, Delete.
- Primary **"Start Routine"** (Focus Runner) CTA button at bottom.

#### [NEW] [RoutineExecutionScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/execution/RoutineExecutionScreen.kt) & [RoutineExecutionViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/execution/RoutineExecutionViewModel.kt)
- Distraction-free, fullscreen Focus Mode runner:
  - Active step title, scheduled time indicator (`07:30`), and step counter (*Step 2 of 5*).
  - `CircularStepTimer` with remaining countdown.
  - Controls: Play / Pause, +1 Min, Skip Step, Previous Step, Complete Step.
  - Sound tone & haptic feedback on step transitions.
  - Auto-advance to next step upon timer expiry.
  - Celebration dialog on completion logging `RoutineExecutionRecord` with `completedStepIds`.

---

### Component 7: Home Screen Tile Integration (`feature:feature-home`)

#### [MODIFY] [GetNavigationItemsUseCase.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/scheduleview/GetNavigationItemsUseCase.kt)
- Inject `RoutineRepository` and add a `Routines` navigation tile (`HeaderNavigationItem`):
  ```kotlin
  add(getRoutineItem(scope = scope))
  
  private suspend fun getRoutineItem(scope: CoroutineScope): HeaderNavigationItem =
    scope.async(dispatcherProvider.io()) {
      HeaderNavigationItem(
        titleRes = R.string.routines,
        iconRes = R.drawable.ic_fluent_timer,
        color = Color(0xFF86E3CE),
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenRoutines,
        subtitle = "${routineRepository.getAll().size}",
      )
    }.await()
  ```

#### [MODIFY] [ScheduleHomeViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/scheduleview/ScheduleHomeViewModel.kt) & [HomeScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-home/src/main/kotlin/com/github/naz013/feature/home/HomeScreen.kt)
- Add `ViewModelEvent.OpenRoutines` and navigate to `RoutineNavKey.List`.

---

### Component 8: PRO Insights Integration (`feature:feature-insights`)

#### [NEW] [RoutineStreakCalculator.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-insights/src/main/kotlin/com/github/naz013/insights/aggregator/RoutineStreakCalculator.kt)
- Computes consecutive day execution streaks from `RoutineExecutionRecord` history.

#### [NEW] [RoutineStepDropoffCalculator.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-insights/src/main/kotlin/com/github/naz013/insights/aggregator/RoutineStepDropoffCalculator.kt)
- Analyzes `completedStepIds` across runs to determine which steps are most consistently completed vs frequently skipped.

#### [MODIFY] [InsightsScreen.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-insights/src/main/kotlin/com/github/naz013/insights/compose/InsightsScreen.kt) & [InsightsViewModel.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/feature/feature-insights/src/main/kotlin/com/github/naz013/insights/compose/InsightsViewModel.kt)
- Surface "Routine Streaks", "Total Focus Time", and "Step Completion Consistency" cards on the PRO Insights dashboard.

---

### Component 9: Application Wiring (`app` & `settings.gradle.kts`)

#### [MODIFY] [settings.gradle.kts](file:///c:/Users/nsuho/Code/reminder-kotlin/settings.gradle.kts)
- Include `:logic:logic-routine`, `:ui:ui-routine`, and `:feature:feature-routine`.

#### [MODIFY] [app/build.gradle.kts](file:///c:/Users/nsuho/Code/reminder-kotlin/app/build.gradle.kts)
- Add dependencies for the new feature and logic modules.

#### [MODIFY] [ReminderApp.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/app/src/main/java/com/elementary/tasks/ReminderApp.kt)
- Register `featureRoutineModule`, `uiRoutineModule`, and `logicRoutineModule` in Koin.

#### [MODIFY] [AppNavGraph.kt](file:///c:/Users/nsuho/Code/reminder-kotlin/app/src/main/java/com/elementary/tasks/navigation/nav3/AppNavGraph.kt)
- Register `routineEntries(backStack)` in `entryProvider`.

---

## Verification Plan

### 1. Automated Unit & Architecture Tests
- **Unit Tests**:
  - `RoutineStepComparatorTest`: Validates sorting steps chronologically by `scheduledTime`, handling null times and equal timestamps.
  - `RoutineDurationCalculatorTest`: Validates step duration summation and time formatting.
  - `RoutineRecurrenceResetUseCaseTest`: Auto-reset of steps upon crossing into new recurrence cycle.
  - `RoutineStreakCalculatorTest`: Streak calculations, edge cases (gap days, same-day multiple executions).
  - `RoutineStepDropoffCalculatorTest`: Verification of step completion rates using `completedStepIds`.
  - `RoutinesListViewModelTest`: Search query filtering, tag chip filtering, pinned item ordering.
  - `RoutineEditViewModelTest` & `RoutineExecutionViewModelTest`: State flows, timer countdown ticks, pause/resume, and auto-advance.
- **Database Migration Tests**:
  - `Migration32To33Test`: Verify SQLite table creation with `completedStepIds` list converter, column types, and data preservation.
- **Sync & Backup Tests**:
  - `DataConverterRoutineTest`: JSON serialization / deserialization round-trip with step scheduled times and execution records.
  - `LocalBackupRoutineTest`: Verify routines and execution records survive export → import.

---

### 2. Comprehensive End-to-End (E2E) & UI Instrumentation Test Suite

Following the testing patterns in [`docs/e2e-testing.md`](file:///c:/Users/nsuho/Code/reminder-kotlin/docs/e2e-testing.md), the following 22 test cases will be added in `app/src/androidTest/kotlin/com/elementary/tasks/e2e/RoutinesE2ETest.kt` (Tier B Compose tests) and `.maestro/routines/` (black-box Maestro flows):

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
| **E19** | `routineRecurrence_newCycle_autoResetsSteps` | Complete a daily routine today; advance simulated clock (`FakeDateTimeManager`) to tomorrow; verify all steps are fresh and unchecked while execution logs remain intact. | Tier B | P0 |
| **E20** | `insights_routineStreaks_updatesConsecutiveDays` | Complete routines across multiple days; open PRO Insights; verify `UiStreak` shows incremented current streak and longest streak. | Tier B | P0 |
| **E21** | `insights_stepDropoff_analyzesCompletionRatio` | Run routines with recurring skipped steps; verify `RoutineStepDropoffCalculator` reports accurate consistency ratios. | Tier B | P1 |
| **E22** | `cloudSyncAndLocalBackup_routineRoundTrip` | Export routines to encrypted backup archive and cloud sync DTOs; wipe local database; restore; verify all routines, steps, and execution records restore intact. | Tier B | P0 |

---

### 3. Manual Verification Checklist
- **UI Design Guidelines Compliance**:
  - Verify that typography, surface colors, top bars, and dialogs strictly match existing Material 3 Expressive screens (`NotesScreen`, `TodoEditScreen`, `PreviewReminderScreen`).
- **Physical Device Experience**:
  - Verify haptic feedback and audio chime play cleanly during step transitions in Focus Mode without lag.
  - Verify backgrounding and resuming the app during an active focus timer keeps timer state intact.
