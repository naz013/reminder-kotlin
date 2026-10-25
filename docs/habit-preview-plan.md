# Habit preview screen - implementation plan

Status: planned, not implemented.

## 1. How Habits work today

| Layer | Where | Notes |
|---|---|---|
| Domain | `core:domain` `habit/Habit`, `HabitCheckIn`, `HabitFrequency` | `Habit` has name, description, frequency (DAILY/WEEKLY), `targetCount` per period, `color`/`icon` palette indexes, `isPinned`. `HabitCheckIn` is one row per (habitId, local date) with a `count`. |
| Persistence | `data:repository` (`HabitEntity`, `HabitCheckInEntity`, DAOs, mappers), `data:repository-api` (`HabitRepository`, `HabitCheckInRepository`) | `HabitCheckInRepository` already offers `observeRange(habitId, from, to)` / `getRange`. |
| Backup / sync | `data:files-api` `HabitJson`, `HabitCheckInJson`; `data:files` `DataConverterImpl`; `data:sync` `HabitRepositoryCaller` | No schema change is needed for the preview. |
| Logic | `logic:logic-habit` | `ObserveHabitsWithProgressUseCase` (habit + today count + period count + streak, 400-day lookback, midnight tick), `Toggle`/`IncrementHabitCheckInUseCase`, `Save`/`Delete`/`GetHabitUseCase`, pure `HabitStreakCalculator` (ISO weeks, Monday start). Gating via `HabitConfig` in `logic-routine` (flag + PRO). |
| UI | `feature:feature-habit` | `HabitListScreen` (adaptive grid of `HabitCard`), `HabitEditorScreen` (create/edit/delete with confirm dialog). Nav3: `HabitNavKey.List`, `HabitNavKey.Editor(habitId)`, `habitEntries(backStack, adsContent)`. |
| Ads | `app` `NormalAdBanner(AdBanner.Habit)` passed into `habitEntries` as `adsContent`; today only the editor renders it. Free flavor only, no-op on pro. |

Gaps relevant to this work:

- Tapping a habit card opens the **editor** directly (`HabitListViewModel.onHabitClick` -> `OpenEditor(id)`). There is no read-only detail screen.
- The check-in decision (`shouldToggle`: toggle vs increment) is private to `HabitListViewModel`.
- Only the *current* streak exists; no history view, no longest streak, no totals.
- The repo has no heatmap / contribution-graph component to reuse.
- `docs/delete-undo.md`: Habit has no list-level undo home, so delete keeps its confirm dialog.

## 2. Goal

A `HabitNavKey.Preview(habitId)` screen opened from the habits list that shows the habit, lets the user check in,
shows a GitHub-contributions-style dot bar of recent history, and shows the ad banner (free flavor).

## 3. Design

### 3.1 Navigation

- Add `HabitNavKey.Preview(habitId: String)` (`@Serializable`).
- `HabitListViewModel.onHabitClick` emits a new `NavigationEvent.OpenPreview(id)`; the card still has its own check-in button.
- Preview top bar: back, Edit (`backStack.add(HabitNavKey.Editor(id))`), Delete (existing confirm dialog + strings).
- The Preview VM observes the habit. If it disappears after it was loaded (deleted from the editor, or by sync), emit `Back`. This avoids a double-pop after deleting from the editor.
- `habitEntries` already receives `adsContent`; register the Preview entry with it. **No change in `app`** (see 3.5).
- Two-pane / `isRenderedAsDetailPane` is out of scope: the habits list is not a two-pane screen today.

### 3.2 Logic (`logic:logic-habit`)

1. `CheckInHabitUseCase(habitId)`: moves the toggle-or-increment rule out of `HabitListViewModel`; both list and preview call it (removes duplication, single place to test).
2. `HabitHistoryBuilder` (pure, next to `HabitStreakCalculator`): `(habit, checkIns, today, weeks) -> List<HabitDay>`, where `HabitDay(date, count, level 0..4)`.
   - DAILY: level from `count / targetCount` (0 = none, 4 = target reached or exceeded).
   - WEEKLY: any check-in on a day = a filled day; the day is full level when the week's target is reached.
   - Days after `today` are not emitted (the grid pads the current week).
3. `HabitStreakCalculator.longestStreak(habit, checkIns, today)`, reusing the same period-bucketing as `streak`.
4. `ObserveHabitDetailsUseCase(habitId, weeks)`: combines `habitRepository.observeById(id)` (does not exist yet: add it to `HabitRepository`, `HabitDao` and `HabitRepositoryImpl`, modelled on `RoutineRepository.observeById`) with `habitCheckInRepository.observeRange(id, today - 400 days, today)` and the midnight tick used by `ObserveHabitsWithProgressUseCase` (extract that tick into a shared helper rather than copy it). Emits `HabitDetails(habit, todayCount, periodCount, streak, longestStreak, totalCheckIns, days)`.
5. Register the new classes in `logicHabitModule`.

### 3.3 Dot bar component

`HabitActivityBar` in `feature-habit` (`internal`; promote to `ui-common` only if a second consumer appears).

- Canvas/`Row` of columns = weeks, 7 rows = weekdays, Monday at top (matches `HabitStreakCalculator`).
- Number of weeks fits the available width (`BoxWithConstraints`: `cell + gap` math, capped at ~26 weeks / the 400-day lookback), newest week on the right; extra history is not scrolled in v1.
- Colour: the habit's own palette colour at 5 alpha steps (level 0 uses `surfaceVariant`), content-contrast not needed because cells hold no text.
- Rounded-square cells, ~12dp with 3dp gaps; month labels above and a small "Less ... More" legend below.
- Accessibility: one `semantics { contentDescription }` summary ("N check-ins in the last M weeks") instead of per-cell nodes.
- Stateless: takes `List<HabitDay>`, `Color`, and renders; all level math is in `HabitHistoryBuilder`.

### 3.4 Screen and ViewModel (`feature-habit/preview/`)

- `HabitPreviewViewModel(habitId, dispatcherProvider, observeHabitDetailsUseCase, checkInHabitUseCase, deleteHabitUseCase, textProvider)`: builds `HabitPreviewState` (Loading / Ready), formats labels (frequency + target, "today", streak, best streak, total), exposes `navigationEvent` (Back, OpenEditor). No logic in the composable.
- `HabitPreviewScreen` (`modifier` first param): header card in the habit colour (icon, name, description), primary check-in button reusing the list-card styling, stat tiles row (current streak, best streak, total check-ins, this week for weekly habits), `HabitActivityBar`, then `adsContent()` at the bottom.
- Scrollable column with `widthIn(max = 600.dp)` like the editor, so it behaves on tablets.
- Koin: `viewModel { (id: String) -> HabitPreviewViewModel(id, get(), ...) }` in `habitModule`.

### 3.5 Ads

Reuse `AdBanner.Habit` (already wired into `habitEntries`). The editor and preview are never visible together, so one unit is sufficient and no AdMob id or `AdsProvider` edit is needed. If a dedicated unit is wanted, add `AdBanner.HabitPreview` plus an id in `app/src/free/.../AdsProvider.kt`; that needs a new AdMob unit id from the account owner.

### 3.6 Strings and docs

- Append to the end of `ui-common` `strings.xml` and every `values-*/strings.xml`: `habit_best_streak`, `habit_total_check_ins`, `habit_activity_title`, `habit_activity_description` (plural-style summary), `habit_legend_less`, `habit_legend_more`. Reuse `edit`, `delete`, `delete_habit_*`, `habit_check_in`.
- Update `docs/architecture.md` (`feature:feature-habit`, `logic:logic-habit` rows) and `docs/delete-undo.md` only if delete behaviour changes (it should not).

## 4. Tests (JUnit 4 + MockK)

- `HabitHistoryBuilderTest`: levels for daily partial/full/over-target, weekly week-reached, future days excluded, week padding.
- `HabitStreakCalculatorTest`: `longestStreak` for daily and weekly, gaps, empty history.
- `CheckInHabitUseCaseTest`: toggle vs increment cases moved from the list VM.
- `HabitPreviewViewModelTest`: Ready state mapping, check-in click, delete confirm, `Back` when the habit disappears, edit navigation.
- Update `HabitListViewModelTest`: `onHabitClick` emits `OpenPreview`; check-in delegates to the new use case.

## 5. Suggested Trello cards (one commit each)

1. `refactor:` extract `CheckInHabitUseCase` from the list VM (+ tests).
2. `feat:` `HabitHistoryBuilder`, `longestStreak`, `ObserveHabitDetailsUseCase` (+ tests, `observeById` if missing).
3. `feat:` `HabitActivityBar` dot component.
4. `feat:` `HabitPreviewScreen`/VM, nav key, list -> preview navigation, ad banner, strings in all locales, docs.

## 6. Decisions taken by default (change if you disagree)

- Dot bar shows the most recent weeks that fit the width, not a scrollable full year.
- "Best streak" is computed over the existing 400-day lookback and may be capped there.
- The preview reuses `AdBanner.Habit` rather than a new ad unit.
- Delete stays confirm-dialog based (no undo), consistent with `docs/delete-undo.md`.
