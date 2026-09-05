package com.github.naz013.feature.reminder.lists.removed

import com.github.naz013.common.TextProvider
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.usecase.DeleteAllReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RemindersArchiveViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val deleteAllReminderUseCase = mockk<DeleteAllReminderUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: RemindersArchiveViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { reminderV2Repository.observeByRemovedStatus(removed = true) } returns flowOf(emptyList())
    coEvery { groupV2Repository.getAll() } returns emptyList()
    every { uiReminderListAdapter.createV2(any(), any()) } answers { uiReminderList(firstArg<ReminderV2>().uuId) }

    viewModel = createViewModel()
  }

  private fun createViewModel(): RemindersArchiveViewModel =
    RemindersArchiveViewModel(
      reminderV2Repository = reminderV2Repository,
      groupV2Repository = groupV2Repository,
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      uiReminderListAdapter = uiReminderListAdapter,
      deleteReminderUseCase = deleteReminderUseCase,
      deleteAllReminderUseCase = deleteAllReminderUseCase,
    )

  private fun reminderV2(
    id: String,
    summary: String = "",
  ) = ReminderV2(
    uuId = id,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  private fun uiReminderList(id: String) =
    UiReminderList(
      id = id,
      noteId = null,
      dueDateTime = null,
      mainText = mockk(relaxed = true),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )

  @Test
  fun `loads archived reminders into state on first collection`() =
    runTest {
      val reminders = listOf(reminderV2("1"), reminderV2("2"))
      every { reminderV2Repository.observeByRemovedStatus(removed = true) } returns flowOf(reminders)
      val vm = createViewModel()

      val state = vm.state.first()

      assertEquals(reminders, state.allReminders)
      assertEquals(reminders, state.filteredReminders)
      assertTrue(state.listState is ListState.Ready)
      assertEquals(2, (state.listState as ListState.Ready).items.size)
    }

  @Test
  fun `state is Empty when there are no archived reminders`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `onSearchQueryChange updates the search query in state immediately`() =
    runTest {
      // Filtering itself runs behind SEARCH_DEBOUNCE_MS (skipped only for an empty query, see
      // RemindersArchiveViewModel.init) - real time, not the test's virtual scheduler, since
      // mockDispatcherProvider() maps default() to the real Dispatchers.Unconfined. Matches
      // BirthdaysViewModelTest's identically-scoped test for the same reason.
      viewModel.onSearchQueryChange("milk")

      assertEquals("milk", viewModel.state.first().searchQuery)
    }

  @Test
  fun `onSearchQueryChange with an empty query re-filters reminders without debounce`() =
    runTest {
      val matching = reminderV2("1", summary = "Buy milk")
      val nonMatching = reminderV2("2", summary = "Call mom")
      every { reminderV2Repository.observeByRemovedStatus(removed = true) } returns flowOf(listOf(matching, nonMatching))
      val vm = createViewModel()

      vm.onSearchQueryChange("")
      val state = vm.state.first()

      assertEquals("", state.searchQuery)
      assertEquals(listOf(matching, nonMatching), state.filteredReminders)
    }

  @Test
  fun `onItemClick emits OpenEdit navigation event`() {
    viewModel.onItemClick(uiReminderList("7"))

    val event = viewModel.event.value?.peekContent()
    assertEquals(RemindersArchiveViewModel.NavigationEvent.OpenEdit("7"), event)
  }

  @Test
  fun `onMenuAction EDIT emits OpenEdit navigation event`() {
    viewModel.onMenuAction(uiReminderList("7"), ArchiveReminderMenuAction.EDIT)

    val event = viewModel.event.value?.peekContent()
    assertEquals(RemindersArchiveViewModel.NavigationEvent.OpenEdit("7"), event)
  }

  @Test
  fun `onMenuAction DELETE emits ConfirmDeleteReminder navigation event`() {
    viewModel.onMenuAction(uiReminderList("7"), ArchiveReminderMenuAction.DELETE)

    val event = viewModel.event.value?.peekContent()
    assertEquals(RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteReminder("7"), event)
  }

  @Test
  fun `onDeleteAllClick emits ConfirmDeleteAll navigation event`() {
    viewModel.onDeleteAllClick()

    val event = viewModel.event.value?.peekContent()
    assertEquals(RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteAll, event)
  }

  @Test
  fun `deleteReminder hides the reminder immediately and posts ShowUndoDelete without deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.reminder_deleted) } returns "Reminder deleted"
      val target = reminderV2("1")
      coEvery { reminderV2Repository.getById("1") } returns target

      viewModel.deleteReminder("1")

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      assertEquals(
        RemindersArchiveViewModel.NavigationEvent.ShowUndoDelete(batchKey = "1", message = "Reminder deleted"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `commitDelete deletes the found reminder after the undo window`() =
    runTest {
      val target = reminderV2("1")
      coEvery { reminderV2Repository.getById("1") } returns target
      viewModel.deleteReminder("1")

      viewModel.commitDelete("1")

      coVerify(exactly = 1) { deleteReminderUseCase(target) }
    }

  @Test
  fun `commitDelete does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null
      viewModel.deleteReminder("missing")

      viewModel.commitDelete("missing")

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    }

  @Test
  fun `undoDelete cancels a pending single delete`() =
    runTest {
      val target = reminderV2("1")
      coEvery { reminderV2Repository.getById("1") } returns target
      viewModel.deleteReminder("1")

      viewModel.undoDelete("1")
      viewModel.commitDelete("1")

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    }

  @Test
  fun `deleteAll hides the reminders immediately and posts ShowUndoDelete without deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.reminders_deleted_count, 2) } returns "2 reminders deleted"
      val remindersV2 = listOf(reminderV2("1"), reminderV2("2"))
      every { reminderV2Repository.observeByRemovedStatus(removed = true) } returns flowOf(remindersV2)
      val vm = createViewModel()

      vm.deleteAll()

      coVerify(exactly = 0) { deleteAllReminderUseCase(any()) }
      val event = vm.event.value?.peekContent() as RemindersArchiveViewModel.NavigationEvent.ShowUndoDelete
      assertEquals("2 reminders deleted", event.message)
    }

  @Test
  fun `commitDelete after deleteAll re-fetches the reminders by id, deletes them and emits ArchiveEmptied`() =
    runTest {
      val remindersV2 = listOf(reminderV2("1"), reminderV2("2"))
      every { reminderV2Repository.observeByRemovedStatus(removed = true) } returns flowOf(remindersV2)
      coEvery { reminderV2Repository.getById("1") } returns remindersV2[0]
      coEvery { reminderV2Repository.getById("2") } returns remindersV2[1]
      val vm = createViewModel()
      vm.deleteAll()
      val batchKey = (vm.event.value?.peekContent() as RemindersArchiveViewModel.NavigationEvent.ShowUndoDelete).batchKey

      vm.commitDelete(batchKey)

      coVerify(exactly = 1) { deleteAllReminderUseCase(remindersV2) }
      val event = vm.event.value?.peekContent()
      assertEquals(RemindersArchiveViewModel.NavigationEvent.ArchiveEmptied, event)
    }

  @Test
  fun `deleteAll does nothing when there are no filtered reminders`() =
    runTest {
      viewModel.deleteAll()

      coVerify(exactly = 0) { deleteAllReminderUseCase(any()) }
      assertEquals(null, viewModel.event.value)
    }
}
