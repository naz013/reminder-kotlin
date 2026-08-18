package com.elementary.tasks.e2e

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.ImeAction
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.testfixtures.testRepositoryModule
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.builderItemRemoveTestTag
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * First batch of Tier-B instrumented tests: drives the real reminder builder UI (FAB -> item
 * picker -> value editor -> Save) end to end and asserts the persisted [RecurrenceRule] through
 * the real [ReminderV2Repository], bound in [setUpClass] against an in-memory Room database (see
 * [testRepositoryModule]) instead of the on-device one.
 *
 * There is no direct "launch straight into the builder" seam (`BuildReminderNavKey.Main` isn't
 * reachable from an Intent extra), so every test starts from [BottomNavActivity]'s home screen and
 * taps through the real "Add" menu, exactly as a user would.
 *
 * All 7 tests share one [composeRule]/one in-memory database for the whole class (loaded once in
 * [setUpClass], which - being `@BeforeClass` - runs before the very first [BottomNavActivity]
 * instance is created, so the override is in place before any production Koin binding could be
 * used). Because the database isn't reset between tests, each test snapshots the existing reminder
 * ids before driving the UI and diffs against that snapshot afterward to find the row it just
 * created, rather than assuming the database is empty.
 */
@RunWith(AndroidJUnit4::class)
class ReminderRecurrenceE2ETest : KoinTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<BottomNavActivity>()

  // Pre-grants POST_NOTIFICATIONS so Save's askNotificationPermissionIfNeeded() (see
  // BuildReminderNavGraph) never blocks on a system permission dialog the test can't drive.
  // POST_NOTIFICATIONS doesn't exist as a platform permission below API 33 - confirmed live on a
  // real API 30 device, GrantPermissionRule.grant() throws SecurityException("Unknown
  // permission") there rather than no-op'ing, so this only applies the rule on API 33+.
  @get:Rule
  val notificationPermissionRule: TestRule =
    if (Build.VERSION.SDK_INT >= 33) {
      GrantPermissionRule.grant("android.permission.POST_NOTIFICATIONS")
    } else {
      TestRule { base, _ -> base }
    }

  private val reminderV2Repository: ReminderV2Repository by inject()
  private val dateTimeManager: DateTimeManager by inject()

  /** Resets before every test so tests that don't touch it (all but the day-of-month/day-of-year
   *  edge cases below) see the real device date, same as before [FakeNowDateTimeProvider] existed. */
  @Before
  fun resetFakeClock() {
    fakeNowDateTimeProvider.setDate(LocalDate.now())
  }

  private fun r(resId: Int): String = composeRule.activity.getString(resId)

  private fun captureExistingReminderIds(): Set<String> =
    runBlocking { reminderV2Repository.getAll().map { it.uuId }.toSet() }

  private fun awaitNewReminder(idsBefore: Set<String>): ReminderV2 {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { reminderV2Repository.getAll().any { it.uuId !in idsBefore } }
    }
    return runBlocking { reminderV2Repository.getAll().first { it.uuId !in idsBefore } }
  }

  /** Home screen -> "Add" menu -> "Reminder" - the same path a user takes to create a new
   *  reminder (`ScheduleHomeViewModel.EventType.Reminder` -> `onOpenCreateReminder` ->
   *  `BuildReminderNavKey.Main()`), since there is no way to launch straight into the builder. */
  private fun navigateToNewReminderBuilder() {
    val addLabel = r(R.string.acc_add)
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithContentDescription(addLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onAllNodesWithContentDescription(addLabel).onFirst().performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.reminder)).performClick()
    composeRule.waitForIdle()
  }

  /** Taps the builder's FAB (same "Add" content description as the home screen's), searches for
   *  [title] in the item picker (`BuilderSelectorSheet`'s `SearchBar`, located by its ImeAction -
   *  it has no test tag) and selects it, which opens its `ValueEditorSheet` automatically
   *  (`BuildReminderViewModel.addItem` -> `onItemEditedClicked`). */
  private fun addBuilderItem(title: String) {
    val addLabel = r(R.string.acc_add)
    composeRule.onAllNodesWithContentDescription(addLabel).onFirst().performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasImeAction(ImeAction.Search), useUnmergedTree = true)
      .performTextInput(title)
    composeRule.waitForIdle()
    // Once typed, the search field's own EditableText also matches `title` (confirmed live: it
    // has a `SetText` semantics action, unlike the actual result row), so exclude editable nodes
    // to land on the result row rather than the field itself.
    composeRule
      .onNode(hasText(title) and !hasSetTextAction(), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
  }

  /** Removes the already-added builder item row titled [title] (main builder screen, not the
   *  item picker) via `BuilderListItemCard`'s remove button, located by its `testTag`
   *  (`builderItemRemoveTestTag`) since the button itself carries no text/content-description. */
  private fun removeBuilderItem(title: String) {
    composeRule.onNodeWithTag(builderItemRemoveTestTag(title)).performClick()
    composeRule.waitForIdle()
  }

  /** Closes the current `ValueEditorSheet`. Its own close button has a null contentDescription
   *  (see `ValueEditorSheet.kt`), so back press - which `ModalBottomSheet` handles itself - is the
   *  only locator-free way to dismiss it; every editor commits live on each interaction (see
   *  `ValueEditorSheet`'s kdoc), so nothing is lost by not tapping an explicit "done" action. */
  private fun closeValueEditor() {
    Espresso.pressBack()
    composeRule.waitForIdle()
  }

  private fun clickText(text: String) {
    composeRule.onNodeWithText(text, useUnmergedTree = true).performClick()
    composeRule.waitForIdle()
  }

  /** Scrolls the current sheet's lazy list/grid (`DayOfMonthValueEditor`'s `WheelPicker` or
   *  `DayOfYearValueEditor`'s `SelectableChipGrid`, both backed by a Lazy* layout) so that
   *  [index] is composed and clickable - needed for any target beyond what's composed by default,
   *  unlike e.g. day 1 or day 10 which [addBuilderItem]'s callers rely on already being visible.
   *
   *  Confirmed live (real device, API 30): the main builder screen's own `BuilderListItemCard`
   *  list stays in the semantics tree - and matches `hasScrollToIndexAction()` too - even while a
   *  `ValueEditorSheet` is open on top of it, so `onNode(hasScrollToIndexAction())` throws
   *  "found 2 nodes" instead of finding the sheet's list uniquely. `onAllNodes(...).onLast()`
   *  reliably picks the sheet's list instead: confirmed live across both call sites (the
   *  day-of-month wheel and the day-of-year grid) that it's always the higher-id / later-composed
   *  node, i.e. whatever was composed most recently - the sheet, since it opens after the
   *  underlying screen. */
  private fun scrollLazyListToIndex(index: Int) {
    composeRule
      .onAllNodes(hasScrollToIndexAction(), useUnmergedTree = true)
      .onLast()
      .performScrollToIndex(index)
    composeRule.waitForIdle()
  }

  /** Selects today's date on the Material 3 `DatePicker` (`DateValueEditor`) - the exact date
   *  doesn't matter for these tests, only that DATE+TIME resolve to a non-null schedule.
   *
   *  Confirmed live (via `printToLog`, since the day cells render no bare digit `Text`/
   *  `ContentDescription` that `uiautomator dump`'s merged accessibility tree exposes - only
   *  Compose's own semantics tree shows them): every day of the current month is already
   *  composed without scrolling, and each cell's `Text` is the full formatted string Material3
   *  uses for its content description, e.g. `"Thursday, August 20, 2026"` - except *today's* own
   *  cell, which gets an extra `"Today, "` prefix (`"Today, Monday, August 17, 2026"`), so this
   *  matches as a substring rather than requiring an exact match. */
  private fun setDateToday() {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
    val todayText = today.format(formatter)
    composeRule
      .onNode(hasText(todayText, substring = true), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
  }

  /** Picks an hour then a minute on the Material 3 clock-face `TimePicker` (`TimeValueEditor`).
   *
   *  Confirmed live (via `printToLog`): dial positions expose only a `ContentDescription` (e.g.
   *  `"10 o'clock"`, `"30 minutes"`) with no `Text` - `onNodeWithText` never matches them at all.
   *  The always-visible hour/minute *display* digits (e.g. `"10"`) do carry a `Text`, and happen
   *  to share the exact same `ContentDescription` string as their matching dial position whenever
   *  the picker's current value equals that number - so matching on content description alone
   *  isn't unique either. `!hasText(...)` excludes the display digits (they alone carry `Text`),
   *  landing on the actual dial position. Also confirmed live: tapping an hour position does
   *  *not* auto-advance to minute-select mode (unlike this app's own live-commit editors) -
   *  `"Select minutes"` has to be tapped explicitly first. Values are Material3's own English
   *  accessibility strings, not this app's string resources. */
  private fun setTime() {
    composeRule
      .onNode(hasContentDescription("10 o'clock") and !hasText("10"), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasContentDescription("Select minutes"), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasContentDescription("30 minutes") and !hasText("30"), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
  }

  /** Punches [digits] into `CountdownTimeValueEditor`'s numeric keypad. Each press shifts a 6-digit
   *  HHMMSS window left (see that editor's kdoc), so pressing 1,0,0 from empty produces "000100" =
   *  1 minute = 60_000ms. */
  private fun pressCountdownDigits(vararg digits: Int) {
    digits.forEach { clickText(it.toString()) }
  }

  /** Sets `RepeatTimeValueEditor`'s `NumberStepperField` value directly via its editable text
   *  field (found by `hasSetTextAction()` - it's the only editable text field in this sheet)
   *  instead of tapping +/- repeatedly. Deliberately leaves the unit `WheelPicker` untouched at its
   *  default (SECOND, index 0 - see `RepeatTimeValueEditor`'s `decomposeDuration(null)`), so the
   *  resulting `repeatInterval` is exactly [seconds] * 1000ms. */
  private fun setRepeatTimeSeconds(seconds: Int) {
    composeRule
      .onNode(hasSetTextAction(), useUnmergedTree = true)
      .performTextReplacement(seconds.toString())
    composeRule.waitForIdle()
  }

  /** Sets `RepeatIntervalValueEditor`'s `NumberStepperField` value the same way
   *  [setRepeatTimeSeconds] does for `RepeatTimeValueEditor` - it's the only editable text field
   *  in this sheet. Unlike [setRepeatTimeSeconds], this value is a plain unit-less repeat count
   *  (e.g. "every 3 months"/"every 2 years"), not a millisecond duration. */
  private fun setRepeatIntervalValue(value: Int) {
    composeRule
      .onNode(hasSetTextAction(), useUnmergedTree = true)
      .performTextReplacement(value.toString())
    composeRule.waitForIdle()
  }

  /** Drives `RepeatLimitValueEditor`'s Material 3 `Slider` via its `SetProgress` semantics action
   *  rather than a drag gesture, matching the standard way Compose UI tests set slider values. */
  private fun setRepeatLimit(value: Float) {
    composeRule
      .onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress), useUnmergedTree = true)
      .performSemanticsAction(SemanticsActions.SetProgress) { it(value) }
    composeRule.waitForIdle()
  }

  private fun tapSave() {
    composeRule.onNodeWithText(r(R.string.save)).performClick()
  }

  /** Dismisses Home's first-run Privacy Policy consent banner (`ScheduleHomeViewModel`'s
   *  `BannerState.Privacy`, gated on `LegalDocumentRepository`'s on-device SharedPreferences flag
   *  - not reset by [testRepositoryModule] or between test methods sharing one app install) if
   *  it's currently showing; a no-op otherwise.
   *
   *  Confirmed live (real device, API 30, running this class's tests in isolation): unlike this
   *  file's other interactions - which fire a target's semantics `OnClick` action directly,
   *  bypassing normal touch-dispatch ordering, which is why e.g. [navigateToNewReminderBuilder]'s
   *  FAB tap works even with a dialog visually on top - this banner genuinely blocks navigation
   *  while shown: a run landed on this exact failure, [navigateToEditReminderBuilder] stuck on
   *  Home (confirmed via `composeRule.onRoot().printToLog(...)`) instead of on
   *  PreviewReminderScreen after tapping the target row. */
  private fun dismissPrivacyBannerIfShown() {
    val acceptLabel = r(R.string.accept)
    if (composeRule.onAllNodesWithText(acceptLabel).fetchSemanticsNodes().isNotEmpty()) {
      composeRule.onNodeWithText(acceptLabel).performClick()
      composeRule.waitForIdle()
    }
  }

  /** Home -> reminder details -> builder in edit mode, the only path there is (see
   *  [navigateToNewReminderBuilder]'s kdoc for the equivalent "no direct seam" situation for
   *  create). [summary] must uniquely identify the target row - the home list only ever shows
   *  today's active reminders (`GetActiveEventsForTheDayUseCase`), and its row text is exactly the
   *  reminder's `Summary` builder item value, falling back to a shared non-unique placeholder when
   *  absent - so every test using this helper adds a `Summary` item with a fresh [UUID] to make its
   *  own row unambiguous, since the database isn't reset between tests.
   *
   *  Confirmed live (real device, API 30): the Home -> `PreviewReminderScreen` navigation this tap
   *  triggers is a genuine intermittent flake, not a locator bug - across several back-to-back
   *  runs (both in isolation and as part of the full class, with no code changes in between) a
   *  single tap + 20s wait passed some runs and missed "More options" entirely on others, with one
   *  run's `printToLog` dump confirming the tap never even left Home that time (stuck behind
   *  [dismissPrivacyBannerIfShown]'s banner) while a different failing run showed no banner at
   *  all - i.e. more than one distinct cause can produce the same symptom here. Retrying the tap
   *  (rather than just a single longer wait) is the general-purpose mitigation: cheap when the
   *  first attempt already worked, and self-corrects when it doesn't, regardless of which
   *  underlying cause was at fault that time.
   *
   *  Confirmed live the unsafe way *not* to recover between attempts: `Espresso.pressBack()` when
   *  the tap never actually left Home throws (`BottomNavActivity` is the root of its own back
   *  stack, so there's nothing for Back to pop there - matches the identical warning already
   *  documented for the Maestro flows' cleanup step). So each retry only re-taps if the summary
   *  row is still visible (i.e. we're confirmed still on Home, safe to retry) and never presses
   *  Back at all - if a tap left Home for some third, unrecognized state, this just keeps waiting
   *  rather than risk closing the Activity. */
  private fun navigateToEditReminderBuilder(summary: String) {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(summary).fetchSemanticsNodes().isNotEmpty()
    }
    val moreOptionsLabel = r(R.string.more_options)
    var reachedPreview = false
    repeat(3) {
      if (reachedPreview) return@repeat
      dismissPrivacyBannerIfShown()
      if (composeRule.onAllNodesWithText(summary).fetchSemanticsNodes().isNotEmpty()) {
        composeRule.onNodeWithText(summary).performClick()
        composeRule.waitForIdle()
      }
      reachedPreview = runCatching {
        composeRule.waitUntil(timeoutMillis = 8_000) {
          composeRule.onAllNodesWithContentDescription(moreOptionsLabel).fetchSemanticsNodes().isNotEmpty()
        }
      }.isSuccess
    }
    check(reachedPreview) { "Did not land on the reminder details screen after 3 attempts" }
    composeRule.onNodeWithContentDescription(moreOptionsLabel).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.edit)).performClick()
    composeRule.waitForIdle()
  }

  @Test
  fun savesAOneTimeReminderWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.builder_date))
    setDateToday()
    closeValueEditor()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Once, created.recurrence)
  }

  @Test
  fun savesADailyRepeatingReminderWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.builder_date))
    setDateToday()
    closeValueEditor()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.repeat))
    setRepeatTimeSeconds(30)
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Daily(repeatInterval = 30_000L, repeatLimit = -1), created.recurrence)
  }

  @Test
  fun savesACountdownReminderWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.builder_countdown))
    pressCountdownDigits(1, 0, 0)
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(
      RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 0, repeatLimit = -1),
      created.recurrence,
    )
  }

  @Test
  fun savesARepeatingCountdownReminderWithARepeatLimitAndTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.builder_countdown))
    pressCountdownDigits(1, 0, 0)
    closeValueEditor()

    addBuilderItem(r(R.string.repeat))
    setRepeatTimeSeconds(15)
    closeValueEditor()

    addBuilderItem(r(R.string.repeat_limit))
    setRepeatLimit(5f)
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(
      RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 15_000L, repeatLimit = 5),
      created.recurrence,
    )
  }

  @Test
  fun savesAWeeklyReminderOnASingleWeekdayWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_days_of_week))
    clickText(r(R.string.mon))
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    // DaysOfWeekValueEditor stores flags Sunday-first ([sun, mon, tue, wed, thu, fri, sat]);
    // selecting only Monday sets index 1.
    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 0, 0, 0, 0)), created.recurrence)
  }

  @Test
  fun savesAWeeklyReminderOnMultipleWeekdaysWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_days_of_week))
    clickText(r(R.string.mon))
    clickText(r(R.string.wed))
    clickText(r(R.string.fri))
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    // sun, mon, tue, wed, thu, fri, sat - Mon/Wed/Fri set indices 1, 3, 5.
    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 1, 0, 1, 0)), created.recurrence)
  }

  @Test
  fun savesAMonthlyReminderOnASingleDayOfMonthWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.day_of_month))
    // Day 1 sits at the DayOfMonthValueEditor wheel's default (unscrolled) position, so it's
    // reachable without driving the WheelPicker's LazyColumn scroll.
    clickText("1")
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1), created.recurrence)
  }

  @Test
  fun savesAMonthlyReminderWithARepeatIntervalAndLimitWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.day_of_month))
    clickText("1")
    closeValueEditor()

    addBuilderItem(r(R.string.builder_repeat_interval))
    setRepeatIntervalValue(3)
    closeValueEditor()

    addBuilderItem(r(R.string.repeat_limit))
    setRepeatLimit(5f)
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(
      RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 3, repeatLimit = 5),
      created.recurrence,
    )
  }

  /** Edge case: `DayOfMonthValueEditor`'s wheel only offers days 1-28 plus a "Last day" sentinel
   *  (stored as `dayOfMonth = 0`, see that editor's kdoc) - there is no way to pick a literal 29-31
   *  through the real UI, so it can never land on a day that doesn't exist in some months. "Last
   *  day" is the one UI-reachable case where the *resolved* day genuinely varies by target month
   *  (28 in a non-leap February, 31 in January, etc.), so this pins `now` via
   *  [fakeNowDateTimeProvider] to a date whose very next month is a non-leap February, and asserts
   *  the persisted `eventDateTime` actually lands on the 28th - not that it silently overflowed
   *  into March or crashed. `getNextMonthDayDateTime`'s `dayOfMonth <= 0 -> lastDayOfTargetMonth`
   *  branch (`RecurrenceCalculatorImpl.kt`) is what's under test here. */
  @Test
  fun savesAMonthlyReminderOnTheLastDayResolvingToTheTargetMonthsActualLastDay() {
    fakeNowDateTimeProvider.setDate(LocalDate.of(2027, 1, 15)) // 2027 is not a leap year.
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.day_of_month))
    // "Last day" is the wheel's final entry (index 28: days 1-28 at indices 0-27), well past
    // what's composed by default - has to be scrolled into view first.
    scrollLazyListToIndex(28)
    clickText(r(R.string.last_day))
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 0), created.recurrence)
    assertEquals(
      LocalDateTime.of(2027, 2, 28, 10, 30),
      dateTimeManager.utcToLocal(created.schedule.eventDateTime!!),
    )
  }

  @Test
  fun savesAYearlyReminderOnASingleDayOfYearWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_day_of_year))
    // Day-of-year 10 is always January 10th regardless of the current year/leap-year status,
    // keeping the expected (dayOfMonth, monthOfYear) deterministic across real run dates.
    clickText("10")
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Yearly(dayOfMonth = 10, monthOfYear = 0), created.recurrence)
  }

  @Test
  fun savesAYearlyReminderWithARepeatIntervalAndLimitWithTheCorrectRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_day_of_year))
    clickText("10")
    closeValueEditor()

    addBuilderItem(r(R.string.builder_repeat_interval))
    setRepeatIntervalValue(2)
    closeValueEditor()

    addBuilderItem(r(R.string.repeat_limit))
    setRepeatLimit(3f)
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(
      RecurrenceRule.Yearly(dayOfMonth = 10, monthOfYear = 0, repeatInterval = 2, repeatLimit = 3),
      created.recurrence,
    )
  }

  /** Edge case: day-of-year 60 is Feb 29 in a leap year (`fakeNowDateTimeProvider` pins `now` to
   *  2024, a leap year) but the *next* occurrence a year later (2025) isn't a leap year, so
   *  `getNextYearDayDateTime`'s `dayOfMonth > lastDayOfTargetMonth -> lastDayOfTargetMonth` branch
   *  (`RecurrenceCalculatorImpl.kt`) must fall back to Feb 28 rather than overflowing into March or
   *  crashing - this is the scenario `docs/e2e-testing.md`'s A14 row calls out. The *stored*
   *  `RecurrenceRule.Yearly(dayOfMonth, monthOfYear)` is fixed at 29/February regardless (that's
   *  the recurrence rule itself, re-evaluated every year) - it's only the computed `eventDateTime`
   *  that needs the fallback, so that's what this test asserts on. */
  @Test
  fun savesAYearlyReminderOnFeb29FallingBackToFeb28InANonLeapYear() {
    fakeNowDateTimeProvider.setDate(LocalDate.of(2024, 1, 15)) // 2024 is a leap year.
    val idsBefore = captureExistingReminderIds()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_day_of_year))
    // Day 60 sits at grid index 59 (days 1-365 at indices 0-364) - scroll it into view first,
    // unlike day 10 above which the grid's default viewport already covers.
    scrollLazyListToIndex(59)
    clickText("60")
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Yearly(dayOfMonth = 29, monthOfYear = 1), created.recurrence)
    assertEquals(
      LocalDateTime.of(2025, 2, 28, 10, 30),
      dateTimeManager.utcToLocal(created.schedule.eventDateTime!!),
    )
  }

  /** Edits an existing reminder and confirms the persisted `RecurrenceRule` actually changes,
   *  under the same `uuId` (not a new row): removes the Date item (via `BuilderListItemCard`'s
   *  `testTag`, see `builderItemRemoveTestTag`) and adds DaysOfWeek instead, switching Once ->
   *  Weekly. Date and DaysOfWeek block each other (`BuilderItem.kt`'s `constraints {}`), so
   *  removing Date first is required here, not just a nicety - Time is left in place from the
   *  original save since `fromWeekdays` needs it too. */
  @Test
  fun editingAnExistingReminderChangesItsRecurrenceRule() {
    val idsBefore = captureExistingReminderIds()
    val uniqueSummary = UUID.randomUUID().toString()
    navigateToNewReminderBuilder()

    addBuilderItem(r(R.string.builder_summary))
    composeRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextReplacement(uniqueSummary)
    composeRule.waitForIdle()
    closeValueEditor()

    addBuilderItem(r(R.string.builder_date))
    setDateToday()
    closeValueEditor()

    addBuilderItem(r(R.string.time))
    setTime()
    closeValueEditor()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    assertEquals(RecurrenceRule.Once, created.recurrence)

    navigateToEditReminderBuilder(uniqueSummary)

    removeBuilderItem(r(R.string.builder_date))

    addBuilderItem(r(R.string.builder_days_of_week))
    clickText(r(R.string.mon))
    closeValueEditor()

    tapSave()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { reminderV2Repository.getById(created.uuId)?.recurrence != RecurrenceRule.Once }
    }
    val updated = runBlocking { reminderV2Repository.getById(created.uuId) }
    // sun, mon, tue, wed, thu, fri, sat - Monday sets index 1, same convention as the create-flow
    // weekly tests above.
    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 0, 0, 0, 0)), updated?.recurrence)
  }

  companion object {
    /** Runs before the very first [BottomNavActivity] is created for this class (JUnit applies
     *  `@Rule`s - including the compose rule's activity launch - only around `@Before`/`@Test`, not
     *  `@BeforeClass`), so this override is already in place before `ReminderApp`'s production
     *  `startKoin {}` bindings could be read by anything the app creates on launch (e.g.
     *  `BottomNavInitViewModel`'s `GroupV2Repository` usage). `loadKoinModules`'s single-`Module`
     *  overload defaults to allowing overrides, so no explicit override flag is needed. */
    /** Shared across the whole class (see [resetFakeClock]) so date-relative recurrence edge
     *  cases (day-of-month/day-of-year rollover) can pin "today" instead of depending on the
     *  real device date - see [FakeNowDateTimeProvider]'s kdoc. */
    private val fakeNowDateTimeProvider = FakeNowDateTimeProvider()

    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      val context = InstrumentationRegistry.getInstrumentation().targetContext
      loadKoinModules(testRepositoryModule(context))
      loadKoinModules(testDateTimeModule(fakeNowDateTimeProvider))
    }
  }
}
