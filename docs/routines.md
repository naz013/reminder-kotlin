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
(+ `.maestro/routines/` for a few black-box flows), following `docs/e2e-testing.md`. **All 22 are now written and
passing**, verified on the real device (`docs/e2e-testing.md`'s established practice) - see §6.3 for the status
of each case and the handful of judgment calls/scope corrections made along the way. No `.maestro/routines/`
flows were added - everything below turned out coverable at the Tier B/Compose level, the same way this repo's
other E2E work generally favors Tier B over Maestro (`docs/e2e-testing.md` §1a) unless something specifically
needs system UI.

| # | Test Case | Scope & Expected Behavior | Tool | Pri | Status |
|---|---|---|---|---|---|
| E1 | `createRoutine_withTitleStepsAndColor` | Create a routine with a title, color, and 3 steps with duration pills; save; verify the colored card renders in the list. | Tier B | P0 | Done |
| E2 | `routineEditor_stepScheduledTimes_autoSorts` | Add steps with mixed times; save; verify preview/execution sort them chronologically. | Tier B | P0 | Done |
| E3 | `routineEditor_reorderUntimedSteps` | Reorder untimed steps via the up/down controls; verify `order` persists. | Tier B | P1 | Done |
| E4 | `routineEditor_tagAssignment` | Toggle tags in the editor; verify chips render on the card and `TagAssignment` rows exist. | Tier B | P0 | Done |
| E5 | `routineEditor_pinToggling` | Pin from the editor; verify the pin badge and top-of-list sort order. | Tier B | P0 | Done - corrected, see note below |
| E6 | `routineEditor_deleteRoutine_cleansUpData` | Delete an existing routine; verify it disappears and the repository is clean. | Tier B | P0 | Done |
| E7 | `homeTile_navigatesToRoutinesList` | Tap the home "Routines" tile; verify the list opens with a matching subtitle count. | Tier B, Maestro | P0 | Done - scope note below |
| E8 | `routinesList_searchQuery_filtersCards` | Type a search query; verify non-matching cards are filtered live. | Tier B | P0 | Done |
| E9 | `routinesList_tagFilterRow_filtersByTag` | Tap a tag filter chip; verify the list filters to that tag. | Tier B | P0 | Done |
| E10 | `routinesList_sortOrder_toggleDateAndName` | Toggle sort order; verify pinned items stay pinned at top. | Tier B | P1 | Done - redefined, see note below |
| E11 | `routinePreview_displaysColoredBannerAndSteps` | Open a routine; verify the colored banner, tags, duration badge, and sorted step checklist. | Tier B | P0 | Done |
| E12 | `routinePreview_stepCheckbox_togglesState` | Toggle a step checkbox in preview; verify it persists. | Tier B | P0 | Done |
| E13 | `routinePreview_resetSteps_unchecksAll` | Use the "Reset Steps" overflow action; verify all steps uncheck. | Tier B | P1 | Done |
| E14 | `focusRunner_countdownTimer_ticksAndPauses` | Start a routine; verify the countdown ticks and pause/resume work. | Tier B, Maestro | P0 | Done - real-time waits, see note below |
| E15 | `focusRunner_stepCompletionAndAutoAdvance` | Let a step's timer expire or tap Complete; verify haptic/tone and auto-advance. | Tier B | P0 | Done - auto-advance only, see note below |
| E16 | `focusRunner_skipStep_advancesWithoutCompletion` | Tap Skip; verify the step isn't marked completed. | Tier B | P1 | Done |
| E17 | `focusRunner_plusOneMinute_extendsTimer` | Tap +1 Min; verify the countdown extends by 60s. | Tier B | P1 | Done |
| E18 | `focusRunner_completion_recordsCompletedStepIds` | Finish with one step skipped; verify the summary and `RoutineExecutionRecord.completedStepIds`. | Tier B | P0 | Done |
| E19 | `routineRecurrence_newCycle_autoResetsSteps` | Complete a daily routine; advance a fake clock to tomorrow; verify steps reset and history is written. | Tier B | P0 | Done |
| E20 | `insights_routineStreaks_updatesConsecutiveDays` | Complete routines across days; verify Insights streak numbers. | Tier B | P0 | Done |
| E21 | `insights_stepDropoff_analyzesCompletionRatio` | Run routines with recurring skipped steps; verify drop-off ratios. | Tier B | P1 | Done |
| E22 | `cloudSyncAndLocalBackup_routineRoundTrip` | Export/restore via encrypted backup + cloud sync DTOs; verify routines/steps/execution records round-trip. | Tier B | P0 | Done - `DataConverter` round-trip, see note below |

Notes on the non-obvious Status values above:

- **E5** couldn't be implemented as literally described: `RoutineEditScreen.kt` has no pin control anywhere
  (`RoutineEditState.isPinned` exists but nothing in the composable reads or toggles it) - the only reachable
  pin control is the Preview screen's overflow menu (`RoutinePreviewViewModel.onPinToggleClick`). The
  implemented test uses that real path instead, the same kind of "corrected against the actual screen" note
  `docs/e2e-testing.md` already carries for its own A11/D9/G9 rows.
- **E7** doesn't compare the *rendered* card count against `routineRepository.getAll().size` the way the row's
  literal "matching subtitle count" wording suggests - `RoutinesListScreen` is a real `LazyColumn`, and this
  suite's shared in-memory database (one `@BeforeClass`-loaded DB for the whole 22-test class, per the
  established `ReminderRecurrenceE2ETest`/`TodoEditorE2ETest` pattern) accumulates one row per routine every
  earlier test in the class run creates, so by the time this test runs there can be more total routines than a
  `LazyColumn` keeps composed at once - comparing rendered-node count to total DB count was the actual source
  of this test's flakiness, not a navigation failure. Asserting that the just-created routine's own card is
  reachable (scrolled to) on the list this tile opened is what's actually specific to *this* test.
- **E10** is scoped down to "pinned stays pinned at top regardless of sort," seeded via a repository-inserted
  pinned routine (deterministic; the editor's own pin flow is already covered by E5) whose title/date both sort
  *last* under either raw mode - so it only lands at the top because pinning overrides sort order, not by
  coincidence. An earlier version instead searched for two specific *unpinned* routines by name across the
  whole list, which went flaky once enough other tests in the class run had accumulated enough rows that a
  plain `performScrollTo()` (which only searches whatever the `LazyColumn` already has composed) couldn't find
  both at once.
- **E14/E15** lean on real (short, 3-8s) step durations and generous `waitUntil` timeouts instead of any
  virtual-time control, per this task's own guidance and `docs/routines.md` §6.3's flag on
  `RoutineExecutionViewModel`'s ticker: this is a real Android instrumented test running the production
  `DispatcherProvider`, so `dispatcherProvider.main()` resolves to the real `Dispatchers.Main` and `delay(1000)`
  really does complete after one real second - not the JVM-unit-test-with-`Dispatchers.Unconfined` hang risk
  §6.3 originally flagged, which only applies to a future `RoutineExecutionViewModelTest`. E15 covers
  auto-advance-on-expiry specifically (not the "tap Complete" half of its own row's description, which E18
  already exercises via the bottom bar's Complete button).
- **E22** exercises `DataConverter` (`data:files`) directly, in-process - the exact conversion both cloud sync
  (`RoutineRepositoryCaller`, `data:sync`) and the PRO local encrypted backup depend on - rather than driving a
  real Google Drive/Dropbox OAuth flow or the system file picker a full local-backup-file round trip would
  need (same exclusion this suite's `ReminderRecurrenceE2ETest` already applies to Google Task/Calendar
  linkage). `DataConverter` uses `android.util.Base64`, so this only runs on a real Android runtime - the exact
  reason §4.5 above flags no equivalent test existing at the `data:files` JVM unit-test level. The full
  encrypted-backup-file round trip already has its own dedicated coverage
  (`BackupArchiveReaderWriterTest`/`LocalBackupApiImplTest`, §4.5); this test's scope is specifically the
  `Routine`/`RoutineJson`/`RoutineExecutionRecord`/`RoutineExecutionJson` conversion functions both of those
  higher-level paths share.
- No case was skipped or deferred - all 22 got a real, passing implementation, though several (E5, E7, E10,
  E15, E22 above) needed their literal scope corrected against what the shipped screens actually do once read
  directly, rather than what the original planning table guessed.

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

### 4.7 Icon picker
`Routine.icon` (previously dead - see the now-removed §6.1 note) is now a nullable `Int` index into
`RoutineIconSet.ALL` (`ui-routine`) - 32 icons hand-picked from `DrawableCatalog.Fluent`, in a fixed order
that's only ever appended to (existing saved routines depend on index stability). Default is `null` (no icon)
for new routines.

- **Index, not a drawable resource id**, mirroring `Routine.color`'s existing convention - keeps the domain
  model free of any UI/resource dependency; only `RoutineIconSet`'s fixed order gives the index meaning.
- **`RoutineIconPicker`** (`ui-routine`): a circular "bubble" trigger next to the title field showing the
  selected icon (or a generic add-icon placeholder) that opens a `DropdownMenu`-hosted grid of all 32 icons
  plus a "None" tile to clear the selection - reuses `DropdownMenu` for anchored positioning rather than
  hand-rolling `Popup` offset math.
- **Fixed: crash on open.** The grid was originally a `LazyVerticalGrid`, which crashed every time
  (`IllegalStateException: Asking for intrinsic measurements of SubcomposeLayout layouts is not supported`),
  confirmed via an on-device crash log. Material3's `DropdownMenu` measures its content `Column` with
  `IntrinsicSize.Max` to shrink-wrap to the widest item, and any `SubcomposeLayout`-backed lazy layout
  (`LazyColumn`/`LazyRow`/`LazyVerticalGrid`) can't answer an intrinsic-width query - a known Compose
  Material3 pitfall. Fixed by replacing it with a plain non-lazy `Column` of `Row`s (33 options total, so
  laziness bought nothing) wrapped in `Modifier.heightIn(max = 220.dp).verticalScroll(...)`.
- **Shown in three places**: the edit screen (same row as the title field, per the request), the preview
  banner, and `RoutineCard` (list screen) - all resolved from the stored index to a drawable id once, in the
  respective ViewModel (`RoutinesListViewModel`/`RoutinePreviewViewModel`), matching how `color`/duration
  labels are already pre-resolved before reaching `ui-routine`.
- **Room migration**: `Migration35To36` changes the `Routine.icon` column from TEXT (a leftover from the
  original, never-implemented string-based plan) to INTEGER, recreating the table like `Migration34To35` -
  no data worth carrying over since the column was always unpopulated.
- **Title capped at 100 characters**, enforced in `RoutineEditScreen`'s `onValueChange` (a client-side
  `MAX_TITLE_LENGTH` filter, not a domain-level constraint - consistent with how validation elsewhere in this
  screen lives in the UI/ViewModel layer, not `core:domain`).

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
- **E2E suite**: **done** - all 22 of §3's cases are written and passing
  (`app/src/androidTest/kotlin/com/elementary/tasks/e2e/RoutinesE2ETest.kt`), verified on a real device (Pixel
  2 XL, API 30) via `adb shell am instrument`, not just compiled - see §3's own Status column/notes for what
  each case actually covers and the handful that needed their scope corrected against the shipped screens.
  `RoutineCard` already carried the `routine_card_${id}`/`routine_start_button_${id}` test tags the plan
  called for; `RoutineEditScreen`'s step rows gained their own `routineStepCardTestTag(stepId)` (mirroring
  `SubTasksValueEditor.kt`'s `shopItemCheckTestTag(itemId)` pattern - a stable prefix, since a step's id isn't
  known to the test until after it's added) so individual rows' title/duration/time fields can be targeted
  unambiguously, and `RoutineColorPicker`'s `ColorSlider` gained a `routineColorSliderTestTag` (that
  composable is a bare `Canvas` with a raw `pointerInput` gesture and no semantics `OnClick` action at all, so
  a test can't `performClick()` it even with a tag - the test drives it via `performTouchInput` at a position
  computed from the tagged node's own reported width instead). `testRepositoryModule`
  (`data:repository`'s `testFixtures`) gained `RoutineRepository`/`RoutineExecutionRepository` entries against
  the in-memory `AppDb`, the same pattern its existing `GroupV2Repository`/`NoteRepository`/etc. entries
  already follow. The feature-flag gate (`RoutineConfig.isEnabled`, off by default) needed its own new pattern
  - no prior E2E test had to deal with a flagged screen - solved with a `FeatureFlags` Koin test override
  (`RoutinesE2ETest.TestFeatureFlags`, loaded in `@BeforeClass` the same way `testRepositoryModule` is) that
  forces `ROUTINE_ENABLED` on without touching the real production default in `FeatureFlag.kt`.
  `.maestro/routines/` wasn't needed - every case turned out coverable at Tier B.
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
