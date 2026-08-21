# Routines: Remaining Work Plan

Status as of this doc: `core:domain`, `data:repository(-api)`, `logic:logic-routine`, `ui:ui-routine`, and
`feature:feature-routine` (list/edit/preview screens) are implemented, wired into the home screen tile, and
gated behind `RoutineConfig.isEnabled` (a real `FeatureFlags`/`FeatureFlag.ROUTINE_ENABLED`-backed flag now,
not a hardcoded constant - see `logic/logic-routine/src/main/kotlin/com/github/naz013/logic/routine/RoutineConfigImpl.kt`).
See `docs/routines-feature-analysis-and-design.md` and `docs/routines-implementation-plan.md` for the
original design and the corrections made against it during implementation.

Since the previous version of this doc, the recurring-routine reset/history behavior was clarified and
implemented in full: at least one step is now mandatory to save a routine; a recurring routine's cycle
anchors to `lastResetAt` set at save time (not immediately eligible for a same-day reset); and
`RoutineRecurrenceResetUseCase` now writes a `RoutineExecutionRecord` (whatever was completed, even zero)
before wiping steps at the next day-boundary check. See §4.4 of the design doc for the exact rules. §1.2
below is unaffected in shape, just note that `RecordRoutineExecutionUseCase` already has one caller
(the auto-reset path, `totalTimeSpentSeconds = 0`) before the execution screen becomes its second.

**Cloud sync and local backup (previously §2) are now DONE** - see §5 below for what shipped instead of a
plan. This doc covers what's still **left**. Items are ordered by what unblocks the most value next, not by
the original design doc's component numbering.

---

## 1. Focus runner execution screen (highest priority - the feature's core loop)

Right now `RoutinePreviewScreen` has a step checklist but no "Start Routine" CTA, because there's nowhere for
it to go. This is the biggest functional gap - without it, Routines is a fancy to-do list, not a guided
habit runner.

### 1.1 Domain/nav plumbing
- Add `RoutineNavKey.Execute(id: String)` to
  `feature/feature-routine/src/main/kotlin/com/github/naz013/feature/routine/RoutineNavKey.kt`.
- Register the entry in `RoutineNavGraph.kt`'s `routineEntries(...)`.
- Wire `RoutinePreviewScreen`'s (currently absent) "Start Routine" button and
  `RoutinesListScreen`'s existing `onStartClick` (currently routes to Preview as a stand-in - see
  `RoutinesListViewModel.onStartClick`) to `RoutineNavKey.Execute(id)` instead.

### 1.2 `RoutineExecutionViewModel`
New file: `feature/feature-routine/.../execution/RoutineExecutionViewModel.kt`. Needs:
- Load the routine (`RoutineRepository.getById`), sorted steps (`Routine.sortedSteps`).
- A ticking countdown: `viewModelScope.launch { while (isActive) { delay(1000); tick() } }` gated by a
  `isPaused` flag, decrementing `remainingSeconds` for the active step.
- State machine: `currentStepIndex`, `remainingSeconds`, `isPaused`, `completedStepIds: MutableSet<String>`.
- Actions: play/pause, skip step (advance without adding to `completedStepIds`), complete step (add to
  `completedStepIds`, advance), +1 minute (`remainingSeconds += 60`), previous step.
- Auto-advance when `remainingSeconds` hits 0 **and** `Routine.autoAdvance` is true (already a domain field,
  currently unused by any use case - this is its first consumer).
- On finishing the last step (or user exits early): call `RecordRoutineExecutionUseCase` from
  `logic:logic-routine` (already implemented - `routineId`, `completedStepIds.toList()`,
  `totalTimeSpentSeconds` accumulated across the session, `totalStepsCount = steps.size`).
- Untimed steps (`durationSeconds == 0`): no countdown: show a "Complete Step" button as the only way to
  advance, no auto-advance.

### 1.3 `RoutineExecutionScreen`
Composable using `ui:ui-routine`'s `CircularStepTimer` (already built - takes `progress: Float` as
*remaining* fraction and a `timeLabel: String`). Layout per the original design doc's §4.5 spec: step
title, "Step N of M" counter, scheduled-time indicator, the timer ring, and Play/Pause, +1 Min, Skip,
Previous, Complete Step controls. Finish with a simple completion summary (total time, steps
completed/skipped) rather than the original doc's "celebration dialog" animation - that's pure polish and
shouldn't block the runner from working end-to-end.

### 1.4 Sound + haptics
`Routine.soundAlertsEnabled` is also an unused domain field. On step transition, if enabled: haptic tick
(`LocalHapticFeedback.current.performHapticFeedback(...)`, same pattern as `ColorSlider`/`SubTasksValueEditor`)
and optionally a short tone. Check how reminder alarms play their tone
(`ui:ui-notification-settings` or `core:platform-*`) before adding a new sound-playing mechanism - there may
already be a lightweight "play a short system sound" helper to reuse instead of pulling in `MediaPlayer`
directly.

### 1.5 Backgrounding behavior
The design doc flags "verify backgrounding and resuming the app during an active focus timer keeps timer
state intact" as a manual test. A `ViewModel`-scoped `while(isActive)` ticker survives configuration change
but **not** process death, and drifts if the process is merely backgrounded for a while (a `delay(1000)`
loop is not wall-clock-accurate). If this matters for v1, compute `remainingSeconds` from a stored
`stepStartedAtMillis` + `System.currentTimeMillis()` on each tick/resume instead of decrementing a counter -
cheap to get right now, expensive to retrofit later.

---

## 2. Full recurrence support

`RoutineEditScreen.kt:163-169` has exactly one control - a `Switch` bound to
`RoutineEditState.repeatsDaily` - and `RoutineEditViewModel.kt:218` collapses it to just two possible
outcomes: `RecurrenceRule.Daily()` or `null`. The domain model and every downstream consumer
(`RoutineRecurrenceResetUseCase`, the `*Json`/`DataConverterImpl` mapping added in §5, `RoutineMapper` at the
Room layer) already handle the full `RecurrenceRule` sealed class - `Weekly(weekdays: List<Int>)`,
`Monthly(dayOfMonth: Int)`, etc. - so this is purely a UI gap, not a data-model one.

1. **Don't reuse `feature-reminder`'s builder.** `BuildReminderScreen`'s recurrence machinery
   (`build/reminder/decompose/*Decomposer.kt`, `RecurrenceRuleCalculator.kt`,
   `ReminderPredictionCalculator.kt`) is built around reminder-specific concerns - iCal decomposition,
   next-fire-time prediction, location-based triggers - that Routines doesn't need. Pulling it in would drag
   `feature-routine` into a dependency it shouldn't have and inherit UI built for a different mental model
   (a single upcoming "next fire" vs. a routine's own weekly cycle). A small dedicated picker in
   `feature-routine` is the right scope.
2. **New state shape.** Replace `RoutineEditState.repeatsDaily: Boolean` with something that can represent
   `Once` (the current `false`/no-recurrence case - keep this as the default, matching the "repeat is
   opt-in" behavior users already have), `Daily`, `Weekly(weekdays)`, and `Monthly(dayOfMonth)`. A sealed
   `RoutineRecurrenceOption` UI-state enum (mirroring how `RoutineEditState`'s other fields already separate
   UI state from domain types) keeps `RoutineEditScreen` from importing `RecurrenceRule` directly.
3. **New composable**: a segmented control or dropdown for the four options, plus a conditional row that
   only appears for `Weekly` (7-day toggle chips - check `ui-common` for an existing day-of-week selector
   before building one; `feature-reminder`'s decomposers imply one exists somewhere for the reminder builder,
   worth a quick search first even though the builder itself shouldn't be reused) or `Monthly` (a day-of-month
   number picker).
4. **`RoutineEditViewModel`**: replace the `if (stateValue.repeatsDaily) RecurrenceRule.Daily() else null`
   line with a mapping from the new UI-state enum to the corresponding `RecurrenceRule` variant. The
   mandatory-steps and `lastResetAt`-anchoring logic around it is unaffected.
5. **Reset semantics don't change.** `RoutineRecurrenceResetUseCase` already resets "at midnight regardless
   of recurrence" per the flow the user specified earlier in this feature's design - Weekly/Monthly routines
   just mean the reset check evaluates a different `RecurrenceRule` variant to decide *whether* today is a
   reset boundary, not a new code path. Confirm `RoutineRecurrenceResetUseCase`'s existing variant handling
   (it should already switch on `RecurrenceRule` broadly, not just `Daily`) before assuming this needs
   changes at all.

---

## 3. Tag chips

`RoutineCard`'s `tagsContent` slot (`ui/ui-routine/.../RoutineCard.kt:58`) exists and is already rendered in
the card layout (`RoutineCard.kt:112-114`) but every call site passes `null`/omits it - no routine anywhere
shows its tags.

1. **Per-routine tag lookup is the only real design question here.** `TagAssignmentRepository`
   (`data/repository-api/.../TagAssignmentRepository.kt`) only exposes single-item lookups
   (`getTagsForItem(itemId, itemType)`, `observeTagsForItem(...)`) - there's no batch
   `getTagsForItems(itemIds, itemType)`. Checked how Notes (the other list screen with a `TagChipStateAdapter`
   dependency) handles this: it doesn't - `NotesViewModel`'s `TagAssignmentRepository` usage is only for the
   filter row's tag list and `getItemIdsForTag` (filtering by *one selected* tag), not per-card chip
   rendering. **There's no existing precedent in this codebase for per-list-item tag chips at list scale.**
   Two honest options:
   - Call `getTagsForItem` once per visible routine (N+1, but routine lists are realistically small - tens,
     not thousands - so likely fine in practice; simplest to ship).
   - Add a batch method to `TagAssignmentRepository`/its Room DAO (`WHERE item_id IN (:ids)`) if N+1 turns
     out to matter, either for this or as a reusable improvement Notes could also adopt later.
   Default to the first option unless there's a reason to believe routine lists will be large - don't build
   the batch query preemptively.
2. **`RoutinesListViewModel`**: already has `tagAssignmentRepository` and `tagChipStateAdapter` wired
   (`RoutinesListViewModel.kt:43-44`, currently only feeding `allTags` for the filter row at line 62-63).
   Add a per-routine `List<TagChipState>` alongside each `UiRoutineListItem`, refreshed whenever the routine
   list reloads.
3. **`RoutinePreviewViewModel`**: single-item screen, so this is just one `getTagsForItem` call - no N+1
   concern there.
4. **Wire the UI**: `RoutinesListScreen`'s card rendering and `RoutinePreviewScreen`'s banner both pass
   `tagsContent = { TagChipRow(tags = ...) }` (from `ui-tag`) into their respective `RoutineCard`/preview
   layout calls.

---

## 4. PRO Insights integration

`feature:feature-insights` has no sibling routine awareness yet.
- `RoutineStreakCalculator.kt` and `RoutineStepDropoffCalculator.kt` in
  `feature/feature-insights/.../aggregator/`, consuming `RoutineExecutionRepository` (already implemented -
  `getAll()`, `getByRoutineId()`, `getByDateRange()`).
- Surface "Routine Streaks" / "Focus Time Trends" / "Step Completion Consistency" cards in
  `InsightsScreen.kt`/`InsightsViewModel.kt`.
- This depends on nothing from §1 - it can be built in parallel once real execution records exist (which
  requires §1 to actually be reachable, even behind the flag, to generate test data). `RoutineExecutionRecord`
  is **not** cloud-synced (see §5) - insights should be computed per-device from local execution history,
  same as everything else `feature-insights` already aggregates; don't assume cross-device data here.

---

## 5. Cloud sync and local backup (DONE)

`Routine` now syncs through Google Drive/Dropbox, and both `Routine` and `RoutineExecutionRecord` round-trip
through the PRO encrypted local backup. What shipped:

- **`RoutineJson`/`RoutineExecutionJson`** DTOs in `data:files-api`'s `com.github.naz013.files.model`
  package (the latter documented as backup-only, no `SyncMetadata`).
- **`DataConverterImpl.kt`** (`data:files`) - `toDomain()`/`toJson()` both directions for both types,
  including a nullable-recurrence-aware `RecurrenceRule` column mapping (`toRoutineRecurrenceRule`/
  `toRoutineColumns`) distinct from the existing non-nullable `ReminderV2` version.
- **`RoutineRepositoryCaller`** (`data:sync`) implementing `DataTypeRepositoryCaller<Routine>`, replacing the
  `NoopRepositoryCaller()` placeholder for `DataType.Routines` in `DataTypeRepositoryCallerFactory.kt`.
- **`RoutineExecutionRecord` deliberately has no cloud sync** - no `SyncMetadata`/`SyncState` field, same
  treatment as `EventHistoricalRecord`. It's still exported/imported by local backup (which doesn't need
  per-item sync state, just `DataConverter` support), so history survives a device restore even though it
  doesn't sync live between devices. Revisit only if cross-device streak tracking (§4) turns out to need it -
  that's a bigger addition (add `SyncMetadata`, a `DataType.RoutineExecutions` entry, and matching DAO/caller
  plumbing), not something to bolt on casually.
- **`BackupEnvelope`/`BackupArchiveWriter`/`BackupArchiveReader`** (`extensions:localbackup`) gained
  `routines`/`routineExecutions` lists; `LocalBackupApiImpl.export()`/`import()` read/write both repositories
  (upsert-by-`save()` on import, same pattern as reminders/groups - not `replaceAll`, which only
  `tagAssignments` uses); `ImportSummary` and the backup UI's total-imported count include both.
- **Tests**: `BackupArchiveReaderWriterTest` and `LocalBackupApiImplTest` both cover round-tripping routines
  and execution records. No `data:files`-level unit test was added for the new `DataConverterImpl` conversion
  functions - they're Kotlin file-private (unreachable even from same-package test files), and the public
  API around them uses `android.util.Base64`, which needs Robolectric (not configured for `data:files`,
  which has no tests at all currently). The logic mirrors the already-tested `RoutineMapperTest` (Room layer)
  implementation, so this is a documented, low-risk gap rather than an oversight.
- Verified: `:app:compileProDebugKotlin`, `:data:sync:test`, `:data:files:test`, and
  `:extensions:localbackup:testDebugUnitTest` all pass.

---

## 6. Gaps left in the already-shipped screens

Smaller items, each independent and low-risk to pick off individually:

- **Step reordering is up/down buttons, not drag gesture.** `feature-reminder`'s
  `SubTasksValueEditor.kt` has a working `detectDragGesturesAfterLongPress`-based implementation that could
  be adapted for `RoutineStepUiState` rows in `RoutineEditScreen.kt` if the up/down buttons prove too
  clunky in practice - not worth doing preemptively.
- **`Routine.reminderId` / linked notification trigger.** Nothing sets or reads it yet -
  `SaveRoutineUseCase`/`DeleteRoutineUseCase` both have comments noting this is deferred until a
  `RoutineScheduleBridge` (or equivalent) exists to own the relationship between a scheduled `Routine` and a
  `ReminderV2` notification trigger. Needs its own design pass - see open question below.
- **`Routine.icon`** is an unused `String?` domain field with no UI to set or display it - either wire it up
  (e.g. an icon picker in the editor, shown on `RoutineCard` instead of/alongside the color swatch) or drop
  it from the domain model if it's not actually wanted; leaving it dead is the one state to avoid.

---

## 7. Localization

New strings were added **only** to `ui/ui-common/src/main/res/values/strings.xml` (English) - the other 26
`values-*/strings.xml` files are untranslated. `CLAUDE.md` requires translating every new string into every
shipped locale; this was deliberately deferred rather than bulk-machine-translating ~19 strings × 26
languages without review. Search for these keys to find what needs translating:
`routines`, `new_routine`, `no_routines`, `routine_description_hint`, `routine_steps`, `add_step`,
`step_title_hint`, `duration_none`, `repeat_daily`, `start_routine`, `reset_steps`,
`reset_steps_confirmation`, `routine_step_count`, `delete_routine_confirmation`, `move_step_up`,
`move_step_down`, `sort`, `sort_by_date`, `sort_by_name`, `routine_on_demand`.
(`reset_steps_confirmation` isn't wired to any confirmation dialog yet either - `onResetStepsClick` in
`RoutinePreviewViewModel` fires immediately with no "are you sure"; either wire the string up or drop it.)

---

## 8. Test coverage

- **E2E suite**: `docs/routines-feature-analysis-and-design.md` §5.2 has a 22-case table
  (`app/src/androidTest/kotlin/com/elementary/tasks/e2e/RoutinesE2ETest.kt`, Tier B) - none written yet.
  `RoutineCard` already carries the `routine_card_${id}`/`routine_start_button_${id}` test tags the plan
  calls for; `RoutineEditScreen`'s step rows don't yet have per-step test tags (add them alongside whichever
  E-case needs to target a specific step, following `SubTasksValueEditor.kt`'s
  `shopItemCheckTestTag(itemId)`-style helper functions rather than inline tag strings).
- **ViewModel tests**: none of `RoutinesListViewModelTest`/`RoutineEditViewModelTest`/
  `RoutinePreviewViewModelTest` exist yet (only `logic-routine`'s use-case-level tests do). Worth adding
  before the execution screen lands, since that ViewModel's countdown/pause/auto-advance state machine is
  the highest-risk piece to get right and will be much easier to test with the surrounding ViewModels
  already covered as a pattern reference.
- **Migration test**: `Migration32To33Test` was scoped out because no Room migration test exists anywhere
  in this repo yet (not a Routines-specific gap - flagged in case a project-wide migration-testing pass ever
  happens, at which point Routines' migration should be included).

---

## 9. Open product decisions (need a person, not just an implementer)

These block cleanly finishing the items above rather than being implementation work themselves:

1. **Free vs. PRO gating.** Is `feature-routine` itself free, PRO-only, or free-with-limits (e.g. N routines
   on free)? Affects `RoutineEditScreen`'s save flow and possibly the home tile's visibility logic
   alongside `RoutineConfig.isEnabled`. Currently nothing gates it by `BuildInfo.isPro`.
2. **Step-level notifications.** Should a step's `scheduledTime` fire its own notification, or only the
   routine's linked `ReminderV2` (once §6's `reminderId` wiring exists)? Blocks designing
   `RoutineScheduleBridge`.
3. **When to flip `RoutineConfig.isEnabled` to `true`.** Realistically after §1 (execution screen) at
   minimum - shipping a Routines tile that can create/preview but never *run* a routine would be a strange
   half-feature to expose, even behind a flag meant mainly for internal QA builds.

---

## Suggested sequencing

1. §1 (execution screen) - makes the feature actually usable end-to-end, generates real execution data for
   §4 (Insights) to consume, and is the one piece a QA pass can't route around.
2. §3 (tag chips) - cheap, self-contained, improves the two screens already shipped.
3. §2 (full recurrence support) - unblocks flipping `RoutineConfig.isEnabled` on for real users; cloud sync
   (§5) is already done, so this and §1 are what's left before the flag can seriously go live.
4. §4 (Insights) - depends on §1 for meaningful data, otherwise independent.
5. §7 (localization) - can happen any time, ideally batched once the string set stabilizes (still adding
   strings for §1's execution screen and §2's recurrence picker) rather than repeated per-PR.
6. §8 (tests) - ideally alongside each item above, not batched at the end.
