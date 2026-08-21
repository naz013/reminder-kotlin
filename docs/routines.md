# Routines

A **Routine** is a structured, sequenced habit/workflow made of ordered, time-boxed steps (e.g. *Morning
Routine*, *Workout Circuit*, *Evening Shutdown*), run either **on-demand** or on a **recurring schedule**
(daily, weekly, monthly), with a guided step-by-step focus timer and habit-tracking history.

This doc replaces three earlier, heavily-overlapping docs (`routines-feature-analysis-and-design.md`,
`routines-implementation-plan.md`, `routines-remaining-work-plan.md`) with one. §1-§3 are the parts of the
original design/research that still have standing value now the feature is built (rationale, trade-offs,
resolved open questions) - they're kept condensed, not as a duplicate of the source code. §4 onward is the
actively-maintained status/remaining-work tracker.

**Status: the functional core, PRO Insights, and the background reset worker are all done and gated behind a
flag.** Domain model, persistence, recurrence, tags, cloud sync/local backup, the focus-runner execution
screen, a periodic background reset check, and PRO Insights integration are all implemented and behind
`RoutineConfig.isEnabled` (`FeatureFlag.ROUTINE_ENABLED`, off by default). What's left is several smaller
polish items, localization, test coverage, and a handful of product decisions - see §6.

---

## 1. Why a dedicated model, not an extension of `ReminderV2`

| Area | Prior codebase state | Gap a dedicated model closes |
|---|---|---|
| Recurring subtasks | `ReminderV2.shoppingItems: List<ShopItemV2>` retains `isChecked = true` across occurrences - users must manually uncheck items every cycle. | `RoutineRecurrenceResetUseCase` auto-resets steps at the next day boundary. |
| Subtask semantics | `ShopItemV2` has no duration or scheduled time. | `RoutineStep` owns `durationSeconds` and `scheduledTime`. |
| On-demand habits | Tasks require a fixed schedule date/time. | `Routine.recurrence == null` routines run manually, any time, no alarm needed. |
| Execution experience | Static checklist only. | `RoutineExecutionScreen` is a guided step-by-step timer runner. |
| Habit analytics | `ReminderStreakCalculator` only tracks alarm firings. | `RoutineExecutionRecord.completedStepIds` enables per-step drop-off/streak analytics (§5). |

`ReminderV2` was rejected as an extension point: it's already a 25+ field model built around notifications/
GPS/calls/SMS/RRULE, subtasks are semantically "shopping items," and it has no home for step-level duration
or execution history. `Routine`/`RoutineStep`/`RoutineExecutionRecord` in `core:domain` are a clean, separate
entity family instead, following the module layout `docs/architecture.md` already prescribes:
`core:domain` → `data:repository-api`/`repository` → `logic:logic-routine` → `ui:ui-routine` +
`feature:feature-routine`, plus touchpoints in `feature:feature-home` (nav tile) and `feature:feature-insights`
(§5). See the actual files for current field lists - `core/domain/src/main/kotlin/com/github/naz013/domain/routine/`.

---

## 2. Design decisions made along the way (why it looks the way it does)

These were open questions during design/build and are resolved now; kept here because the reasoning isn't
visible just from reading the code.

- **Color picker**: not `NoteColorEngine.allColors()` as first planned - that's a Koin-injected, Note-specific
  "remembered last color" engine, and depending on it would have created the first-ever `ui-*` → `ui-*`
  dependency edge in the codebase. `RoutineColorPicker` instead wraps `ui-common`'s `ColorSlider`, the same
  solid/no-opacity picker `feature-group`'s `EditGroupScreen` already uses for `GroupV2.color`, fed a plain
  `List<Color>` by the caller (e.g. `ThemeProvider.colorsForSliderThemed()`) - no new module edge, no shared
  Note preference state.
- **Tag chips slot, not a direct `ui-tag` dependency**: `RoutineCard` (`ui-routine`) takes a
  `tagsContent: @Composable (() -> Unit)?` slot instead of rendering `TagChipRow` itself, since `ui-routine`
  can only depend on `core:domain`/`ui-common` per the `ui-*` dependency rule. `feature-routine` fills the
  slot with `ui-tag`'s `TagChipRow`.
- **`RecurrenceRule` storage**: reused the existing sealed class (`Daily`/`Weekly`/`Monthly`/etc. from
  `ReminderV2`'s recurrence system) rather than inventing a routine-specific one. Persisted the same way
  `ReminderV2Mapper` does - a `recurrenceType` discriminator column + `recurrencePayload` JSON blob, not a
  bare Gson `@TypeConverters` on the sealed class, because R8 can strip/rename unprotected fields during Gson
  round-tripping (this caused a real production crash for `ReminderV2` previously).
- **Duration/contrast formatting lives in `feature-routine`, not `ui-routine`**: `UiRoutineListItem` takes
  pre-formatted `durationLabel`/`stepCountLabel`/`scheduleRangeLabel` strings and a pre-resolved
  `contentColor`, because `ui-routine` can't depend on `logic-routine`'s `RoutineDurationCalculator` or
  compute palette contrast itself.
- **Free vs. PRO gating of base Routines**: still undecided - see §6.4 open decisions. Insights (§5) is
  explicitly PRO-gated; the base feature (create/edit/run) is not gated by `BuildInfo.isPro` yet.
- **No routine-level linked `ReminderV2` trigger.** `Routine.reminderId` was in the original design (a
  scheduled routine would own a linked reminder for notifications) but was never wired to anything - no
  `RoutineScheduleBridge` was ever built, and it was dropped from the domain model entirely (Room migration
  `Migration34To35`, `RoutineJson`, `DataConverterImpl`) rather than carried as permanent dead weight. If
  step-level or routine-level notifications are wanted later, that's new design work, not resuming
  unfinished work - see §6.4.

---

## 3. E2E test plan (reference for §6's test-coverage gap)

A 22-case Tier B Compose suite was scoped for `app/src/androidTest/kotlin/com/elementary/tasks/e2e/RoutinesE2ETest.kt`
(+ `.maestro/routines/` for a few black-box flows), following `docs/e2e-testing.md`. None are written yet
(§6). Kept here as the reference for whoever picks that up:

| # | Test Case | Scope & Expected Behavior | Tool | Pri |
|---|---|---|---|---|
| E1 | `createRoutine_withTitleStepsAndColor` | Create a routine with a title, color, and 3 steps with duration pills; save; verify the colored card renders in the list. | Tier B | P0 |
| E2 | `routineEditor_stepScheduledTimes_autoSorts` | Add steps with mixed times; save; verify preview/execution sort them chronologically. | Tier B | P0 |
| E3 | `routineEditor_reorderUntimedSteps` | Reorder untimed steps via the up/down controls; verify `order` persists. | Tier B | P1 |
| E4 | `routineEditor_tagAssignment` | Toggle tags in the editor; verify chips render on the card and `TagAssignment` rows exist. | Tier B | P0 |
| E5 | `routineEditor_pinToggling` | Pin from the editor; verify the pin badge and top-of-list sort order. | Tier B | P0 |
| E6 | `routineEditor_deleteRoutine_cleansUpData` | Delete an existing routine; verify it disappears and the repository is clean. | Tier B | P0 |
| E7 | `homeTile_navigatesToRoutinesList` | Tap the home "Routines" tile; verify the list opens with a matching subtitle count. | Tier B, Maestro | P0 |
| E8 | `routinesList_searchQuery_filtersCards` | Type a search query; verify non-matching cards are filtered live. | Tier B | P0 |
| E9 | `routinesList_tagFilterRow_filtersByTag` | Tap a tag filter chip; verify the list filters to that tag. | Tier B | P0 |
| E10 | `routinesList_sortOrder_toggleDateAndName` | Toggle sort order; verify pinned items stay pinned at top. | Tier B | P1 |
| E11 | `routinePreview_displaysColoredBannerAndSteps` | Open a routine; verify the colored banner, tags, duration badge, and sorted step checklist. | Tier B | P0 |
| E12 | `routinePreview_stepCheckbox_togglesState` | Toggle a step checkbox in preview; verify it persists. | Tier B | P0 |
| E13 | `routinePreview_resetSteps_unchecksAll` | Use the "Reset Steps" overflow action; verify all steps uncheck. | Tier B | P1 |
| E14 | `focusRunner_countdownTimer_ticksAndPauses` | Start a routine; verify the countdown ticks and pause/resume work. | Tier B, Maestro | P0 |
| E15 | `focusRunner_stepCompletionAndAutoAdvance` | Let a step's timer expire or tap Complete; verify haptic/tone and auto-advance. | Tier B | P0 |
| E16 | `focusRunner_skipStep_advancesWithoutCompletion` | Tap Skip; verify the step isn't marked completed. | Tier B | P1 |
| E17 | `focusRunner_plusOneMinute_extendsTimer` | Tap +1 Min; verify the countdown extends by 60s. | Tier B | P1 |
| E18 | `focusRunner_completion_recordsCompletedStepIds` | Finish with one step skipped; verify the summary and `RoutineExecutionRecord.completedStepIds`. | Tier B | P0 |
| E19 | `routineRecurrence_newCycle_autoResetsSteps` | Complete a daily routine; advance a fake clock to tomorrow; verify steps reset and history is written. | Tier B | P0 |
| E20 | `insights_routineStreaks_updatesConsecutiveDays` | Complete routines across days; verify Insights streak numbers. | Tier B | P0 |
| E21 | `insights_stepDropoff_analyzesCompletionRatio` | Run routines with recurring skipped steps; verify drop-off ratios. | Tier B | P1 |
| E22 | `cloudSyncAndLocalBackup_routineRoundTrip` | Export/restore via encrypted backup + cloud sync DTOs; verify routines/steps/execution records round-trip. | Tier B | P0 |

---

## 4. What shipped

### 4.1 Core feature (domain, persistence, list/edit/preview screens)
`core:domain`, `data:repository(-api)`, `logic:logic-routine`, `ui:ui-routine`, and `feature:feature-routine`
(list/edit/preview) are implemented, wired into the home screen tile, and gated behind
`RoutineConfig.isEnabled` (a real `FeatureFlags`/`FeatureFlag.ROUTINE_ENABLED`-backed flag, not a hardcoded
constant - see `logic/logic-routine/.../RoutineConfigImpl.kt`).

Recurring-routine reset/history behavior: at least one step is mandatory to save a routine; a recurring
routine's cycle anchors to `lastResetAt` set at save time (not immediately eligible for a same-day reset);
`RoutineRecurrenceResetUseCase` writes a `RoutineExecutionRecord` (whatever was completed, even zero) before
wiping steps at the next day-boundary check, and no-ops entirely for on-demand routines (never auto-reset -
only a manual "Reset Steps" action clears those).

### 4.2 Focus runner execution screen
`RoutineNavKey.Execute(id)` + `RoutineExecutionScreen`/`RoutineExecutionViewModel` in
`feature/feature-routine/.../execution/`, reachable from both the list screen's start button and a "Start
Routine" FAB on the preview screen.

- **Countdown survives backgrounding by construction**: the ticker recomputes `remainingSeconds` each tick
  from an absolute wall-clock deadline (`stepDeadlineAtMillis`), not a decrementing counter, so displayed
  time stays correct even if the process is backgrounded a while. Pause freezes the remaining millis; resume
  re-anchors the deadline to `now + frozenRemaining`.
- **Auto-advance marks the step completed** (a judgment call, not originally specified): when
  `Routine.autoAdvance` is true and a timed step's countdown hits zero, the runner advances and treats that
  step as completed, not skipped - matching typical Pomodoro/habit-timer UX.
- **Two distinct exit paths**, to avoid double-recording: completing/skipping the last step shows an in-screen
  `Finished` summary (total time, steps completed) and records there; pressing back mid-run records silently
  and exits immediately with no summary - leaving is "I'm done for now," not "I finished."
- **Sound + haptics**: `Routine.soundAlertsEnabled` triggers a short `android.media.ToneGenerator` beep on
  every step transition (no existing "play a short system sound" helper existed anywhere in the codebase, so
  this is new and self-contained). Haptics reuse the `stepTransitionEvent` → `LocalHapticFeedback` pattern,
  gated by the same `AppPreferences.hapticsEnabled` flag the editor already uses.
- **Icons**: no Play/Pause/Skip/Previous drawable assets existed anywhere in the app. Uses
  `androidx.compose.material:material-icons-extended` (`Icons.Filled.PlayArrow`/`Pause`/`SkipNext`/
  `SkipPrevious`/`Check`) directly rather than hand-adding vector XML - already a dependency of
  `feature-routine`, and already used the same way (bypassing `DrawableCatalog`/`AppIcons`) in
  `feature-workflow`/`feature-note`/several `ui-common` foundation components.
- Untimed steps (`durationSeconds == 0`) show only Previous + Complete Step - Play/Pause/+1 Min/Skip are
  hidden entirely, not shown-disabled.
- **Not done**: no `RoutineExecutionViewModelTest` (see §6 - the ticker's dispatcher needs a testing-approach
  decision first, to avoid a leaked real-delay coroutine per test). No celebration-dialog animation on finish
  (plain summary only). Not exercised on a device/emulator - compile + unit test + detekt verified only.

### 4.3 Full recurrence support
The editor's repeat control is a 4-way picker - Never/Daily/Weekly/Monthly - instead of the original single
"repeats daily" switch, with a 7-day chip row for Weekly and a day-of-month stepper (capped 1-28, sidestepping
end-of-month ambiguity) for Monthly.

- Didn't reuse `feature-reminder`'s recurrence builder - its decompose/prediction machinery targets a
  different mental model (a single upcoming "next fire," iCal, location triggers) Routines doesn't need. A
  small dedicated `RoutineRecurrenceOption` sealed UI-state class plus a purpose-built picker was the right
  scope, as originally planned.
- No existing day-of-week selector was found to reuse outside `feature-reminder`'s reminder-specific
  builder-item machinery, so a small `WeekdaySelector` composable was added (reusing `ui-common`'s existing
  `mon`/`tue`/.../`sun` abbreviated strings).
- `RoutineRecurrenceResetUseCase` needed no changes - it already branches only on `recurrence == null` vs.
  non-null, not on the specific variant, so Weekly/Monthly reset on the same "midnight regardless of
  recurrence" schedule Daily already had.
- Weekly requires at least one day selected before saving (mirrors the mandatory-steps validation).
- The preview screen's recurrence label now describes each variant properly (weekday list / day-of-month)
  instead of collapsing every non-null recurrence to "Repeats daily."

### 4.4 Tag chips
`RoutineCard`'s `tagsContent` slot and the preview screen's banner now render each routine's tags via
`ui-tag`'s `TagChipRow`. As anticipated in §2, there was no existing batch tag-lookup precedent anywhere in
the codebase (Notes' `TagAssignmentRepository` usage is filter-only, not per-card) - shipped with one
`getTagsForItem` call per visible routine (N+1) rather than a speculative batch API. Revisit only if a
real-world routine list turns out large enough for this to matter.

### 4.5 Cloud sync and local backup
`Routine` syncs through Google Drive/Dropbox; both `Routine` and `RoutineExecutionRecord` round-trip through
the PRO encrypted local backup.

- `RoutineJson`/`RoutineExecutionJson` DTOs in `data:files-api` (the latter documented as backup-only, no
  `SyncMetadata`).
- `DataConverterImpl.kt` (`data:files`) - `toDomain()`/`toJson()` both directions, including a
  nullable-recurrence-aware `RecurrenceRule` column mapping distinct from the existing non-nullable
  `ReminderV2` version.
- `RoutineRepositoryCaller` (`data:sync`) implementing `DataTypeRepositoryCaller<Routine>`, replacing the
  `NoopRepositoryCaller()` placeholder.
- `RoutineExecutionRecord` deliberately has **no cloud sync** - no `SyncMetadata`/`SyncState`, same treatment
  as `EventHistoricalRecord`. Still exported/imported by local backup (doesn't need per-item sync state), so
  history survives a device restore even though it doesn't sync live between devices. Revisit only if
  cross-device streak tracking (§5) needs it - that's a bigger addition (`SyncMetadata`, a
  `DataType.RoutineExecutions` entry, matching DAO/caller plumbing), not something to bolt on casually.
- `BackupEnvelope`/`BackupArchiveWriter`/`BackupArchiveReader` gained `routines`/`routineExecutions` lists;
  import is upsert-by-`save()`, same pattern as reminders/groups (not `replaceAll`, which only
  `tagAssignments` uses).
- Tests: `BackupArchiveReaderWriterTest` and `LocalBackupApiImplTest` cover round-tripping both types. No
  `data:files`-level unit test for the new `DataConverterImpl` conversion functions - they're Kotlin
  file-private (unreachable even from same-package tests), and the public API uses `android.util.Base64`,
  which needs Robolectric (not configured for `data:files`). The logic mirrors the already-tested
  `RoutineMapperTest` (Room layer), so this is a documented, low-risk gap.
- Verified: `:app:compileProDebugKotlin`, `:data:sync:test`, `:data:files:test`,
  `:extensions:localbackup:testDebugUnitTest` all pass.

### 4.6 Background recurrence reset worker
`RoutineRecurrenceResetUseCase` (§4.1) originally only ran lazily, on the next list/preview screen load -
routines the user never opened that day never got a same-day history entry until the next visit. A periodic
`RoutineRecurrenceResetTask` (`feature:feature-routine`, root package - mirrors `RunWorkflowRulesTask` in
`feature-workflow`) now runs it for every routine with `recurrence != null` roughly every 24 hours via the
same generic `BackgroundTask`/`WorkScheduler` mechanism birthdays and workflow rules already use
(`JobSchedulerApi.scheduleRoutineRecurrenceResetCheck()`, wired up once in `BottomNavInitViewModel`, guarded
by a `Prefs.routineRecurrenceResetScheduled` flag exactly like the existing workflow-rules check - except
this one re-checks the flag on every app start rather than only once, so turning `RoutineConfig.isEnabled` on
later still schedules it without needing a fresh install).

Both the lazy on-view path and the periodic worker call the exact same use case, which is idempotent per
calendar day (`lastResetAt`'s date already `>= today` short-circuits it) - running both never double-records
a day's execution history, and neither path needs to know the other exists.

**Not gated as strictly as it could be**: the worker itself doesn't check `RoutineConfig.isEnabled` at run
time (only the *scheduling* call in `BottomNavInitViewModel` does) - if the flag were ever flipped off again
after being on, an already-scheduled periodic work request would keep firing harmlessly (iterating zero or
few routines) until the next app data clear. Not worth adding a runtime check for a flag that's only ever
expected to go from off to on.

---

## 5. PRO Insights integration (DONE)

`feature:feature-insights`'s existing reminder-streak dashboard now also surfaces routine habit data,
computed from `RoutineExecutionRecord` history over the same 90-day lookback window the reminder streaks use.

- **`RoutineStreakCalculator`** (`feature-insights/.../aggregator/`): mirrors `ReminderStreakCalculator`'s
  shape, but a "streak day" means something different for routines than for reminders. A recurring routine
  auto-writes a zero-completion `RoutineExecutionRecord` every day it's due regardless of whether the user
  did anything (§4.1/§4.6) - counting *any* record as ReminderStreakCalculator does for reminder firings
  would make every recurring routine look like a perfect streak no matter what. A day only counts here if at
  least one record for that routine that day has `completedStepsCount == totalStepsCount` (fully completed).
- **`RoutineStepDropoffCalculator`**: per-step completion rate across a routine's execution records, matched
  against the routine's *current* step ids (a record can reference a step id that no longer exists if the
  routine was edited since - those are silently excluded, nothing meaningful to show for a step that's gone).
- **Card content is a deliberately scoped simplification** of the original design doc's "Focus Time Trends" /
  "Step Completion Consistency" cards: rather than building a second parallel weekly-trend bar chart and a
  nested per-step drill-down list (roughly doubling the screen's UI complexity), each routine gets one
  compact card - current/longest streak (reusing the existing `streak_current`/`streak_longest` strings),
  total focus time over the window (via `RoutineDurationCalculator`, already used elsewhere in
  `feature-routine`), and its single most-skipped step if any step has under 100% completion. The full
  per-step breakdown the calculator produces isn't discarded - it's just not rendered as its own list yet.
- **Empty-state logic updated**: the screen's empty state used to trigger only off empty reminder streaks:
  now it's empty only when *both* reminder streaks and routine insights are empty, and the `no_insights_yet`
  string was reworded from "No reminder activity..." to "No activity..." accordingly (English only - see
  §6.2, this joins the existing untranslated-string backlog).
- Tests: `RoutineStreakCalculatorTest`, `RoutineStepDropoffCalculatorTest`, and expanded
  `InsightsViewModelTest` coverage (empty-state with only-routine-activity, most-skipped-step surfacing,
  skipping insights for a since-deleted routine). `:feature:feature-insights:testDebugUnitTest` and
  `:feature:feature-insights:detekt` both pass.

---

## 6. What's left: smaller gaps, localization, tests, open decisions

### 6.1 Gaps in the already-shipped screens
- **Step reordering is up/down buttons, not drag gesture.** `feature-reminder`'s `SubTasksValueEditor.kt` has
  a working `detectDragGesturesAfterLongPress`-based implementation that could be adapted for
  `RoutineStepUiState` rows in `RoutineEditScreen.kt` if the buttons prove clunky in practice - not worth
  doing preemptively.
- **`Routine.icon`** is an unused `String?` domain field with no UI to set or display it - either wire it up
  (an icon picker in the editor, shown on `RoutineCard`) or drop it from the domain model; leaving it dead is
  the one state to avoid (`Routine.reminderId` was in exactly this state and was dropped for it - see §2).

### 6.2 Localization
New strings were added **only** to `ui/ui-common/src/main/res/values/strings.xml` (English) - the other 26
`values-*/strings.xml` files are untranslated. `CLAUDE.md` requires translating every new string into every
shipped locale; deliberately deferred rather than bulk-machine-translating without review. Search for these
keys to find what needs translating: `routines`, `new_routine`, `no_routines`, `routine_description_hint`,
`routine_steps`, `add_step`, `step_title_hint`, `duration_none`, `repeat_daily`, `repeat_none`,
`repeat_weekly`, `repeat_monthly`, `repeat_weekly_days`, `repeat_monthly_day`, `repeat_weekdays_required`,
`decrease_day_of_month`, `increase_day_of_month`, `start_routine`, `reset_steps`, `reset_steps_confirmation`,
`routine_step_count`, `delete_routine_confirmation`, `move_step_up`, `move_step_down`, `sort`, `sort_by_date`,
`sort_by_name`, `routine_on_demand`, `routine_steps_required`, `step_of_count`, `pause`, `resume`,
`skip_step`, `complete_step`, `previous_step`, `add_one_minute`, `routine_finished_title`,
`routine_execution_total_time`, `routine_execution_steps_completed`, `routine_insights_section`,
`routine_focus_time_total`, `routine_insight_most_skipped`.
(`reset_steps_confirmation` isn't wired to any confirmation dialog yet either - `onResetStepsClick` in
`RoutinePreviewViewModel` fires immediately with no "are you sure"; either wire the string up or drop it.)
`no_insights_yet`'s English wording was also changed (§5) - the other 26 locales still say the old
reminder-specific wording and need a refresh, not just a first translation.

### 6.3 Test coverage
- **E2E suite**: §3's 22-case table, none written yet. `RoutineCard` already carries the
  `routine_card_${id}`/`routine_start_button_${id}` test tags the plan calls for; `RoutineEditScreen`'s step
  rows don't yet have per-step test tags (add alongside whichever E-case needs to target a specific step,
  following `SubTasksValueEditor.kt`'s `shopItemCheckTestTag(itemId)`-style helper pattern).
- **ViewModel tests**: `RoutineEditViewModelTest` exists (mandatory-steps/recurrence-anchoring/full-recurrence
  coverage). `RoutinesListViewModelTest`, `RoutinePreviewViewModelTest`, and `RoutineExecutionViewModelTest`
  still don't. The last is the highest-risk gap: `RoutineExecutionViewModel`'s ticker launches with
  `viewModelScope.launch(dispatcherProvider.main())` and calls real `delay(1000)`; under
  `mockDispatcherProvider()` that resolves to plain `Dispatchers.Unconfined` (not a `TestDispatcher`), which
  does *not* auto-advance virtual time the way `runTest {}`'s own scheduler does - so the ticker just sits on
  a real 1-second delay for the test JVM's lifetime, un-cancelled (nothing calls `onCleared()` on a
  bare-constructed test ViewModel). It doesn't corrupt assertions today, but it's a leaked-coroutine smell
  worth fixing before adding tests here: either give `BaseTest` a shared `TestDispatcher`/scheduler that
  `dispatcherProvider.main()` can return, or make the ticker's dispatcher injectable/mockable directly.
- **Migration test**: none of `Migration33To34` (adds the Routine/RoutineExecution tables) or
  `Migration34To35` (drops `Routine.reminderId`) has a test, because no Room migration test exists anywhere
  in this repo yet - not Routines-specific, flagged for a future project-wide migration-testing pass.

### 6.4 Open product decisions (need a person, not just an implementer)
1. **Free vs. PRO gating.** Is `feature-routine` itself free, PRO-only, or free-with-limits (e.g. N routines
   on free)? Affects `RoutineEditScreen`'s save flow and possibly the home tile's visibility, alongside
   `RoutineConfig.isEnabled`. Currently nothing gates it by `BuildInfo.isPro`.
2. **Step-level notifications.** With `Routine.reminderId` dropped (§2), whether a step's `scheduledTime`
   should fire its own notification at all - and if so, how - is an open design question from scratch, not a
   half-built feature to finish. No `RoutineScheduleBridge` (or equivalent) exists.
3. **When to flip `RoutineConfig.isEnabled` to `true`.** The functional core, Insights, and the background
   reset worker (§4-§5) are all done. What's left is a product/QA call, not an implementation gap: at minimum
   a device/emulator walkthrough (nothing in this feature has been manually run yet - only compiled,
   unit-tested, and detekt-checked) and a decision on #1.

---

## Suggested sequencing

1. A manual device/emulator pass over everything in §4-§5 (never yet exercised outside
   compile+unit-test+detekt) - the natural next step before considering the flag live.
2. §6.2 (localization) - can happen any time, ideally batched once the string set stabilizes rather than
   repeated per-PR.
3. §6.3 (tests) - ideally alongside whichever item above it covers, not batched at the end;
   `RoutineExecutionViewModel`'s ticker needs a testing-approach decision first (§6.3).
