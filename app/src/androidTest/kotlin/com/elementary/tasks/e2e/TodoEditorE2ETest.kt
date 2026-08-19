package com.elementary.tasks.e2e

import android.os.Build
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.valuedialog.editor.shopItemCheckTestTag
import com.github.naz013.feature.reminder.build.valuedialog.editor.shopItemRemoveTestTag
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.testfixtures.testRepositoryModule
import com.github.naz013.ui.common.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * First instrumented coverage of [com.github.naz013.feature.reminder.todo.TodoEditScreen] itself
 * - the general-builder tests in [ReminderRecurrenceE2ETest] (B6) exercise the same shared
 * [com.github.naz013.feature.reminder.build.valuedialog.editor.SubTasksValueEditor] editor, but
 * only through the generic builder's `ValueEditorSheet`, never through the Todo screen, which
 * embeds it directly (no bottom sheet, and a title field sits above it in the same tree - see
 * [addTwoTodoItems]'s note on node ordering).
 *
 * Drives the real UI via [composeRule] against an in-memory Room database ([testRepositoryModule],
 * loaded once in [setUpClass] for the same reason documented there in [ReminderRecurrenceE2ETest]).
 */
@RunWith(AndroidJUnit4::class)
class TodoEditorE2ETest : KoinTest {

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

  private val reminderV2Repository: ReminderV2Repository by inject()

  private fun r(resId: Int): String = composeRule.activity.getString(resId)

  private fun r(
    resId: Int,
    vararg args: Any,
  ): String = composeRule.activity.getString(resId, *args)

  private fun captureExistingReminderIds(): Set<String> =
    runBlocking { reminderV2Repository.getAll().map { it.uuId }.toSet() }

  private fun awaitNewReminder(idsBefore: Set<String>): ReminderV2 {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { reminderV2Repository.getAll().any { it.uuId !in idsBefore } }
    }
    return runBlocking { reminderV2Repository.getAll().first { it.uuId !in idsBefore } }
  }

  /** Home screen -> "Agenda" tile -> Agenda's own "Add" menu -> "Todo". Confirmed on a real
   *  device (unlike [ReminderRecurrenceE2ETest.navigateToNewReminderBuilder]'s "Reminder"/
   *  "Birthday"/"Note" entries, which live on the Home screen's own "Add" menu) - Home's "Add"
   *  menu has no Todo entry at all; only the Agenda screen's "Add" menu
   *  (`AgendaScreen.kt`'s `PopupMenuItem` list) does, dispatching
   *  `ScheduleHomeViewModel.EventType.Todo` -> `TodoEditNavKey.Main()`. */
  private fun navigateToNewTodoEditor() {
    val agendaLabel = r(R.string.agenda)
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(agendaLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(agendaLabel).performClick()
    composeRule.waitForIdle()

    val addLabel = r(R.string.acc_add)
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithContentDescription(addLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onAllNodesWithContentDescription(addLabel).onFirst().performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.todo)).performClick()
    composeRule.waitForIdle()
  }

  /** Types [first] then [second] into the checklist's first two rows. Unlike the general
   *  builder's `ValueEditorSheet` (which contains nothing but the checklist), `TodoEditScreen`
   *  also has its own title `OutlinedTextField` above the checklist in the same tree - that field
   *  is always the first (index 0) node with a set-text action, so checklist rows start at index
   *  1. The title field has no `ImeAction.Next` (only checklist rows set that), so IME-action
   *  queries don't need the same offset. */
  private fun addTwoTodoItems(
    first: String,
    second: String,
  ) {
    composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[1].performTextInput(first)
    composeRule.waitForIdle()
    composeRule.onAllNodes(hasImeAction(ImeAction.Next), useUnmergedTree = true)[0].performImeAction()
    composeRule.waitForIdle()
    composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[2].performTextInput(second)
    composeRule.waitForIdle()
  }

  private fun checkFirstVisibleItem() {
    val checkTagPrefix = shopItemCheckTestTag("")
    val checkboxMatcher =
      SemanticsMatcher("shop item checkbox") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(checkTagPrefix) == true
      }
    composeRule.onAllNodes(checkboxMatcher, useUnmergedTree = true)[0].performClick()
    composeRule.waitForIdle()
  }

  private fun tapSave() {
    composeRule.onNodeWithText(r(R.string.save)).performClick()
  }

  @Test
  fun creatingATodoWithCheckedItemPersistsCheckedState() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewTodoEditor()

    addTwoTodoItems("Buy milk", "Buy eggs")
    checkFirstVisibleItem() // checks "Buy milk", the only active item at this point

    tapSave()

    val created = awaitNewReminder(idsBefore)
    val activeItems = created.shoppingItems.filterNot { it.isDeleted }
    val milk = activeItems.single { it.summary == "Buy milk" }
    val eggs = activeItems.single { it.summary == "Buy eggs" }
    assertTrue(milk.isChecked)
    assertFalse(eggs.isChecked)
  }

  @Test
  fun checkingOffAllItemsShowsCompletedSection() {
    navigateToNewTodoEditor()
    addTwoTodoItems("Buy milk", "Buy eggs")

    // Each check sinks its item into the (collapsed) Completed section, so the remaining active
    // item is always back at checkbox index 0.
    checkFirstVisibleItem() // checks "Buy milk"
    checkFirstVisibleItem() // checks "Buy eggs" - active list is now empty

    composeRule.onNodeWithText(r(R.string.todo_completed_count, 2)).assertIsDisplayed()
    composeRule.onNodeWithText(r(R.string.todo_all_done)).assertIsDisplayed()

    composeRule.onNodeWithText(r(R.string.todo_completed_count, 2)).performClick()
    composeRule.waitForIdle()

    composeRule.onNode(hasText("Buy milk") and hasSetTextAction(), useUnmergedTree = true).assertIsDisplayed()
    composeRule.onNode(hasText("Buy eggs") and hasSetTextAction(), useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun uncheckingACompletedItemMovesItBackToActive() {
    navigateToNewTodoEditor()
    addTwoTodoItems("Buy milk", "Buy eggs")

    checkFirstVisibleItem() // checks "Buy milk"; "Buy eggs" is the sole remaining active item

    composeRule.onNodeWithText(r(R.string.todo_completed_count, 1)).performClick()
    composeRule.waitForIdle()

    // Row order at this point: active "Buy eggs" (index 0), then the expanded completed
    // "Buy milk" (index 1) - unchecking the latter moves it back to active.
    val checkTagPrefix = shopItemCheckTestTag("")
    val checkboxMatcher =
      SemanticsMatcher("shop item checkbox") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(checkTagPrefix) == true
      }
    composeRule.onAllNodes(checkboxMatcher, useUnmergedTree = true)[1].performClick()
    composeRule.waitForIdle()

    composeRule.onNode(hasText("Buy milk") and hasSetTextAction(), useUnmergedTree = true).assertIsDisplayed()
    composeRule.onNode(hasText("Buy eggs") and hasSetTextAction(), useUnmergedTree = true).assertIsDisplayed()
    composeRule.onAllNodesWithText(r(R.string.todo_completed_count, 1)).assertCountEquals(0)
  }

  /** Regression test for the deliberately-unchanged remove button (`shopItemRemoveTestTag`) - see
   *  `ReminderRecurrenceE2ETest.removingASubTaskPersistsOnlyTheRemainingItem` for the same
   *  scenario through the general builder; this is the first time it's covered through
   *  `TodoEditScreen` itself. */
  @Test
  fun removingAnItemViaXButtonStillWorks() {
    val idsBefore = captureExistingReminderIds()
    navigateToNewTodoEditor()
    addTwoTodoItems("Buy milk", "Buy eggs")

    composeRule
      .onNode(hasText("Buy milk") and hasSetTextAction(), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()

    val removeTagPrefix = shopItemRemoveTestTag("")
    val removeButtonMatcher =
      SemanticsMatcher("shop item remove button") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(removeTagPrefix) == true
      }
    composeRule.onAllNodes(removeButtonMatcher, useUnmergedTree = true).onFirst().performClick()
    composeRule.waitForIdle()

    tapSave()

    val created = awaitNewReminder(idsBefore)
    val activeItems = created.shoppingItems.filterNot { it.isDeleted }
    assertEquals(1, activeItems.size)
    assertEquals("Buy eggs", activeItems.single().summary)
  }

  /** Smoke check only - the drag-to-reorder gesture itself is unit-tested at the ViewModel level
   *  (`SubTasksViewModelTest.onReorder ...`); scripting a `performTouchInput { swipe(...) }` drag
   *  across rows for exact-position assertions here would be flaky. */
  @Test
  fun dragHandleIsPresentOnActiveRows() {
    navigateToNewTodoEditor()
    addTwoTodoItems("Buy milk", "Buy eggs")

    val handles = composeRule.onAllNodesWithContentDescription(r(R.string.todo_drag_to_reorder))
    handles.assertCountEquals(2)
  }

  companion object {
    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      val context = InstrumentationRegistry.getInstrumentation().targetContext
      loadKoinModules(testRepositoryModule(context))
    }
  }
}
