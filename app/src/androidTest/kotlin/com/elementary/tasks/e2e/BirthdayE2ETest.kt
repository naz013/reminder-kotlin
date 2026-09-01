package com.elementary.tasks.e2e

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.testfixtures.testRepositoryModule
import com.github.naz013.ui.common.R
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject
import org.threeten.bp.LocalDate

/**
 * First instrumented coverage of the Birthday feature (`EditBirthdayScreen`, `PreviewBirthdayScreen`,
 * `BirthdaysScreen`) - drives the real UI via [composeRule] against an in-memory Room database
 * ([testRepositoryModule], loaded once in [setUpClass] for the same reason documented there in
 * [ReminderRecurrenceE2ETest]) and asserts through the real [BirthdayRepository].
 *
 * There is no direct "launch straight into the editor" seam, same situation as
 * [ReminderRecurrenceE2ETest.navigateToNewReminderBuilder] - every create-flow test starts from
 * [BottomNavActivity]'s home screen and taps through the real "Add" menu. A freshly-added birthday
 * defaults its date to today (`EditBirthdayViewModel.checkArguments`'s `key.id.isNullOrEmpty() ->
 * onDateChanged(LocalDate.now())` branch), so it always shows up in Home's own "Upcoming" list
 * (`GetActiveEventsForTheDayUseCase`) right after saving - that row is this suite's only way back
 * into `PreviewBirthdayScreen` for a birthday just created, the same "tap the Home row" pattern
 * [ReminderRecurrenceE2ETest.navigateToEditReminderBuilder] already relies on for reminders.
 *
 * `BirthdaysScreen` itself (the list) is reachable only via a Home dashboard tile that's disabled
 * by default (`HeaderNavigationSection.BIRTHDAYS.isDisabledByDefault = true`) - [setUpClass] flips
 * that same on-device preference before the first [BottomNavActivity] is even created, exactly the
 * same "override before anything reads the default" reasoning already used there for the Koin
 * modules.
 */
@RunWith(AndroidJUnit4::class)
class BirthdayE2ETest : KoinTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<BottomNavActivity>()

  // See ReminderRecurrenceE2ETest's identical rule for why this is gated to API 33+.
  @get:Rule
  val notificationPermissionRule: TestRule =
    if (Build.VERSION.SDK_INT >= 33) {
      GrantPermissionRule.grant("android.permission.POST_NOTIFICATIONS")
    } else {
      TestRule { base, _ -> base }
    }

  // BirthdaysNavGraph's Save wiring requests READ_CONTACTS live whenever the number field is
  // non-empty (`if (state.number.isNotEmpty()) permissionRequester.request(READ_CONTACTS, ...)`),
  // the same "checked live, not just declared" situation
  // ReminderRecurrenceE2ETest.callPhonePermissionRule documents for CALL_PHONE - confirmed live
  // (real system "Allow Reminder PRO to access your contacts?" dialog) this blocks Save entirely
  // until granted/denied, which this test can't drive without pre-granting it.
  @get:Rule
  val readContactsPermissionRule: TestRule = GrantPermissionRule.grant("android.permission.READ_CONTACTS")

  private val birthdayRepository: BirthdayRepository by inject()
  private val tagRepository: TagRepository by inject()
  private val tagAssignmentRepository: TagAssignmentRepository by inject()
  private val dateTimeManager: DateTimeManager by inject()

  private fun r(resId: Int): String = composeRule.activity.getString(resId)

  private fun captureExistingBirthdayIds(): Set<String> =
    runBlocking { birthdayRepository.getAll().map { it.uuId }.toSet() }

  private fun awaitNewBirthday(idsBefore: Set<String>): Birthday {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { birthdayRepository.getAll().any { it.uuId !in idsBefore } }
    }
    return runBlocking { birthdayRepository.getAll().first { it.uuId !in idsBefore } }
  }

  /** Seeds a birthday directly through the repository, due today (`day`/`month` match
   *  `LocalDate.now()`) so it's visible to every list/filter test below without re-proving the
   *  create flow itself (already covered by [addingABirthdayPersistsItWithTodaysDate]) - same
   *  "seed what the UI has no in-scope path to build quickly" reasoning
   *  [ReminderRecurrenceE2ETest.assigningAGroupOnCreatePersistsItsGroupId] uses for its group. */
  private fun seedBirthdayDueToday(name: String): Birthday {
    val today = LocalDate.now()
    val birthday = Birthday(
      name = name,
      date = dateTimeManager.formatBirthdayDate(today),
      day = today.dayOfMonth,
      month = today.monthValue - 1,
      dayMonth = "${today.dayOfMonth}|${today.monthValue - 1}",
      syncState = SyncState.WaitingForUpload,
    )
    runBlocking { birthdayRepository.save(birthday) }
    return birthday
  }

  /** Dismisses Home's first-run Privacy Policy consent banner if it's currently showing - see
   *  [ReminderRecurrenceE2ETest.dismissPrivacyBannerIfShown]'s kdoc for why this matters (it
   *  genuinely blocks a real touch-dispatch-ordered tap while shown, unlike the semantics-direct
   *  clicks this file's other navigation uses). */
  private fun dismissPrivacyBannerIfShown() {
    val acceptLabel = r(R.string.accept)
    if (composeRule.onAllNodesWithText(acceptLabel).fetchSemanticsNodes().isNotEmpty()) {
      composeRule.onNodeWithText(acceptLabel).performClick()
      composeRule.waitForIdle()
    }
  }

  /** Home screen -> "Add" menu -> "Birthday" - the same Reminder/Birthday/Note/Todo dropdown
   *  [ReminderRecurrenceE2ETest.navigateToNewReminderBuilder] taps into for "Reminder". */
  private fun navigateToNewBirthdayScreen() {
    val addLabel = r(R.string.acc_add)
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithContentDescription(addLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onAllNodesWithContentDescription(addLabel).onFirst().performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.birthday)).performClick()
    composeRule.waitForIdle()
  }

  /** Sets the Name field's value - always the first (index 0) editable field in
   *  `EditBirthdayScreen` (declared before the Phone number field in source), the same
   *  code-order-matches-semantics-order assumption [TodoEditorE2ETest.addTwoTodoItems] relies on
   *  for its title field. */
  private fun setNameFieldValue(value: String) {
    composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0].performTextReplacement(value)
    composeRule.waitForIdle()
  }

  /** Sets the Phone number field's value - the second (index 1) editable field, after Name. */
  private fun setPhoneFieldValue(value: String) {
    composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[1].performTextReplacement(value)
    composeRule.waitForIdle()
  }

  private fun tapSave() {
    composeRule.onNodeWithText(r(R.string.save)).performClick()
    composeRule.waitForIdle()
  }

  /** Home -> birthday details, the only path there is for a birthday that's due today (see this
   *  class's own kdoc). [name] must uniquely identify the target row - retries the tap up to 3
   *  times before giving up, mirroring [ReminderRecurrenceE2ETest.navigateToEditReminderBuilder]'s
   *  documented flakiness mitigation for the identical "tap a Home row" mechanism. */
  private fun navigateToPreviewBirthday(name: String) {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()
    }
    val editLabel = r(R.string.edit)
    var reachedPreview = false
    repeat(3) {
      if (reachedPreview) return@repeat
      dismissPrivacyBannerIfShown()
      if (composeRule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()) {
        composeRule.onAllNodesWithText(name).onFirst().performClick()
        composeRule.waitForIdle()
      }
      reachedPreview = runCatching {
        composeRule.waitUntil(timeoutMillis = 8_000) {
          composeRule.onAllNodesWithContentDescription(editLabel).fetchSemanticsNodes().isNotEmpty()
        }
      }.isSuccess
    }
    check(reachedPreview) { "Did not land on the birthday preview screen after 3 attempts" }
  }

  private fun navigateToEditFromPreview() {
    composeRule.onNodeWithContentDescription(r(R.string.edit)).performClick()
    composeRule.waitForIdle()
  }

  /** Home dashboard tile ("Birthdays", enabled class-wide in [setUpClass]) -> `BirthdaysScreen`.
   *  Waits for the screen's filter icon content-description as the "we've actually arrived"
   *  signal, the same role [ReminderRecurrenceE2ETest.navigateToEditReminderBuilder]'s
   *  "more options" wait plays for the reminder preview screen. */
  private fun navigateToBirthdaysList() {
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(r(R.string.birthdays)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(r(R.string.birthdays)).performClick()
    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithContentDescription(r(R.string.filter)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun addingABirthdayPersistsItWithTodaysDate() {
    val idsBefore = captureExistingBirthdayIds()
    val uniqueName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(uniqueName)
    tapSave()

    val created = awaitNewBirthday(idsBefore)
    assertEquals(uniqueName, created.name)
    val today = LocalDate.now()
    assertEquals(today.dayOfMonth, created.day)
    assertEquals(today.monthValue - 1, created.month)
  }

  @Test
  fun savingWithEmptyNameShowsValidationErrorAndDoesNotPersist() {
    val idsBefore = captureExistingBirthdayIds()
    navigateToNewBirthdayScreen()

    tapSave()

    composeRule.onNodeWithText(r(R.string.must_be_not_empty)).assertIsDisplayed()
    assertEquals(idsBefore, captureExistingBirthdayIds())
  }

  /** "I don't know a year" toggle persists as `Birthday.ignoreYear`. `SettingsSwitchItem`'s whole
   *  row is clickable (`onClick = { onCheckedChange(!checked) }`), so tapping its title text
   *  toggles it the same way tapping the switch itself would. */
  @Test
  fun togglingIDontKnowTheYearPersistsIgnoreYear() {
    val idsBefore = captureExistingBirthdayIds()
    val uniqueName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(uniqueName)
    composeRule.onNodeWithText(r(R.string.i_don_t_know_the_year)).performClick()
    composeRule.waitForIdle()
    tapSave()

    val created = awaitNewBirthday(idsBefore)
    assertTrue(created.ignoreYear)
  }

  /** Assigns a tag on create and confirms the assignment persists against the birthday's own id -
   *  the same `TagChipPicker` component and assertion shape as
   *  [ReminderRecurrenceE2ETest.assigningATagAndADescriptionOnCreatePersistsBoth] uses for
   *  reminders, minus that test's Description half (`EditBirthdayScreen` has no such field). */
  @Test
  fun assigningATagOnCreatePersistsTheAssignment() {
    val tag = Tag(name = "Family ${UUID.randomUUID()}", color = 0xFF00FF00.toInt())
    runBlocking { tagRepository.save(tag) }

    val idsBefore = captureExistingBirthdayIds()
    val uniqueName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(uniqueName)
    composeRule.onNodeWithText(tag.name).performClick()
    composeRule.waitForIdle()
    tapSave()

    val created = awaitNewBirthday(idsBefore)
    val assignedTags = runBlocking { tagAssignmentRepository.getTagsForItem(created.uuId, TaggedItemType.BIRTHDAY) }
    assertTrue(assignedTags.any { it.id == tag.id })
  }

  /** Attaching a phone number persists `Birthday.number`, and - since a freshly-created birthday
   *  is always due today (this class's own kdoc) - `PreviewBirthdayScreen` shows the Call/SMS
   *  buttons its `hasBirthdayToday && number != null` guard requires. */
  @Test
  fun attachingAPhoneNumberPersistsItAndShowsCallAndSmsOnPreview() {
    val idsBefore = captureExistingBirthdayIds()
    val uniqueName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(uniqueName)
    setPhoneFieldValue("+15551234567")
    tapSave()

    val created = awaitNewBirthday(idsBefore)
    assertEquals("+15551234567", created.number)

    navigateToPreviewBirthday(uniqueName)
    composeRule.onNodeWithText(r(R.string.make_call)).assertIsDisplayed()
    composeRule.onNodeWithText(r(R.string.send_sms)).assertIsDisplayed()
  }

  /** Edits an existing birthday and confirms the persisted name actually changes, under the same
   *  `uuId` (not a new row) - the birthday-equivalent of
   *  [ReminderRecurrenceE2ETest.editingAnExistingReminderChangesItsRecurrenceRule]. */
  @Test
  fun editingAnExistingBirthdayChangesItsName() {
    val idsBefore = captureExistingBirthdayIds()
    val originalName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(originalName)
    tapSave()

    val created = awaitNewBirthday(idsBefore)

    navigateToPreviewBirthday(originalName)
    navigateToEditFromPreview()

    val updatedName = "Birthday ${UUID.randomUUID()}"
    setNameFieldValue(updatedName)
    tapSave()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { birthdayRepository.getById(created.uuId)?.name == updatedName }
    }
    val updated = runBlocking { birthdayRepository.getById(created.uuId) }
    assertEquals(created.uuId, updated?.uuId)
    assertEquals(updatedName, updated?.name)
  }

  /** Deletes a birthday from `PreviewBirthdayScreen`'s own delete icon + confirm dialog. */
  @Test
  fun deletingABirthdayFromPreviewRemovesIt() {
    val idsBefore = captureExistingBirthdayIds()
    val uniqueName = "Birthday ${UUID.randomUUID()}"
    navigateToNewBirthdayScreen()

    setNameFieldValue(uniqueName)
    tapSave()

    val created = awaitNewBirthday(idsBefore)

    navigateToPreviewBirthday(uniqueName)
    composeRule.onNodeWithContentDescription(r(R.string.delete)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.are_you_sure)).assertIsDisplayed()
    composeRule.onNodeWithText(r(R.string.yes)).performClick()
    composeRule.waitForIdle()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { birthdayRepository.getById(created.uuId) == null }
    }
  }

  /** Deletes a birthday from `BirthdaysScreen`'s own row menu (Open/Edit/Delete, see
   *  `BirthdayAgendaRow.birthdayMenuItems`) + the screen's own confirm dialog
   *  (`BirthdaysScreen`'s `AlertDialog`, gated on `state.confirmDeleteId`). Seeds directly through
   *  the repository (this class's own [seedBirthdayDueToday]) rather than re-driving the create
   *  UI - already covered by [addingABirthdayPersistsItWithTodaysDate]. */
  @Test
  fun deletingABirthdayFromTheListRowMenuRemovesIt() {
    val birthday = seedBirthdayDueToday("Birthday ${UUID.randomUUID()}")

    navigateToBirthdaysList()
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(birthday.name).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onAllNodesWithContentDescription(r(R.string.more_options)).onFirst().performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.delete)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.are_you_sure)).assertIsDisplayed()
    composeRule.onNodeWithText(r(R.string.yes)).performClick()
    composeRule.waitForIdle()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { birthdayRepository.getById(birthday.uuId) == null }
    }
  }

  /** `BirthdaysScreen`'s smart-list filter chips (Today/This week, opened via the filter bottom
   *  sheet) narrow the shown list - only asserts this birthday stays visible under "Today" (a
   *  positive-presence check), not that every other birthday the shared in-memory DB has
   *  accumulated from earlier tests in this class is filtered out, the same scoping
   *  [ReminderRecurrenceE2ETest]'s own tests use throughout for a DB that's never reset mid-class. */
  @Test
  fun smartListTodayFilterKeepsABirthdayDueToday() {
    val birthday = seedBirthdayDueToday("Birthday ${UUID.randomUUID()}")

    navigateToBirthdaysList()
    composeRule.onNodeWithContentDescription(r(R.string.filter)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.smart_list_today)).performClick()
    composeRule.waitForIdle()
    Espresso.pressBack()
    composeRule.waitForIdle()

    composeRule.onNodeWithText(birthday.name).assertIsDisplayed()
  }

  /** `BirthdaysScreen`'s tag filter (also in the filter bottom sheet, `TagFilterRow`) narrows the
   *  shown list to birthdays carrying the selected tag. */
  @Test
  fun tagFilterKeepsABirthdayCarryingThatTag() {
    val tag = Tag(name = "Family ${UUID.randomUUID()}", color = 0xFF00FF00.toInt())
    runBlocking { tagRepository.save(tag) }
    val birthday = seedBirthdayDueToday("Birthday ${UUID.randomUUID()}")
    runBlocking { tagAssignmentRepository.attach(birthday.uuId, TaggedItemType.BIRTHDAY, tag.id) }

    navigateToBirthdaysList()
    composeRule.onNodeWithContentDescription(r(R.string.filter)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(tag.name).performClick()
    composeRule.waitForIdle()
    Espresso.pressBack()
    composeRule.waitForIdle()

    composeRule.onNodeWithText(birthday.name).assertIsDisplayed()
  }

  /** `BirthdaysScreen`'s own `SearchBar` (only rendered `if (state.hasAnyItems)`) filters the
   *  shown list by name. */
  @Test
  fun searchBarFindsABirthdayByName() {
    val birthday = seedBirthdayDueToday("Birthday ${UUID.randomUUID()}")

    navigateToBirthdaysList()
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextInput(birthday.name)
    composeRule.waitForIdle()

    // The search field's own EditableText now also contains `birthday.name` (it's what was just
    // typed), so excluding editable nodes is needed to land on the list row uniquely - the same
    // ambiguity ReminderRecurrenceE2ETest.addBuilderItem's kdoc documents for its search field.
    composeRule
      .onNode(hasText(birthday.name) and !hasSetTextAction(), useUnmergedTree = true)
      .assertIsDisplayed()
  }

  companion object : KoinComponent {
    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      val context = InstrumentationRegistry.getInstrumentation().targetContext
      loadKoinModules(testRepositoryModule(context))

      // BirthdaysScreen is only reachable via this Home dashboard tile, which is disabled by
      // default (HeaderNavigationSection.BIRTHDAYS.isDisabledByDefault) - flipped here, before the
      // very first BottomNavActivity/Home composition, the same "override before anything reads
      // the default" reasoning ReminderRecurrenceE2ETest.setUpClass already uses for its Koin
      // modules. This mutates a real on-device preference (not part of the in-memory DB
      // testRepositoryModule swaps in), so it's a one-way, permanent side effect on whichever
      // device/emulator runs this suite - the same class of side effect already accepted for
      // clearPackageData not resetting mid-run (see dismissPrivacyBannerIfShown's kdoc).
      val homePreferences = get<HomePreferences>()
      homePreferences.disabledHeaderNavigationSections =
        homePreferences.disabledHeaderNavigationSections - HeaderNavigationSection.BIRTHDAYS
    }
  }
}
