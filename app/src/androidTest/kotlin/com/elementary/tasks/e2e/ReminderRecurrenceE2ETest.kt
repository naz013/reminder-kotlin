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
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.testfixtures.testRepositoryModule
import com.github.naz013.ui.common.R
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject
import org.threeten.bp.LocalDate
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

  companion object {
    /** Runs before the very first [BottomNavActivity] is created for this class (JUnit applies
     *  `@Rule`s - including the compose rule's activity launch - only around `@Before`/`@Test`, not
     *  `@BeforeClass`), so this override is already in place before `ReminderApp`'s production
     *  `startKoin {}` bindings could be read by anything the app creates on launch (e.g.
     *  `BottomNavInitViewModel`'s `GroupV2Repository` usage). `loadKoinModules`'s single-`Module`
     *  overload defaults to allowing overrides, so no explicit override flag is needed. */
    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      val context = InstrumentationRegistry.getInstrumentation().targetContext
      loadKoinModules(testRepositoryModule(context))
    }
  }
}
