package com.elementary.tasks.reminder.lists.removed

import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.github.naz013.logic.reminder.usecase.DeleteAllReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.usecase.reminders.GetRemindersV2ByRemovedStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RemindersArchiveViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val getRemindersV2ByRemovedStatusUseCase = mockk<GetRemindersV2ByRemovedStatusUseCase>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val deleteAllReminderUseCase = mockk<DeleteAllReminderUseCase>(relaxed = true)

  private lateinit var viewModel: RemindersArchiveViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // RemindersArchiveViewModel.state runs loadReminders() in onStart on every collection - every
    // test collects state at least once, so a default stub avoids an unstubbed-call failure.
    coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns emptyList()
    coEvery { groupV2Repository.getAll() } returns emptyList()
    every { uiReminderListAdapter.createV2(any(), any()) } answers { uiReminderList(firstArg<ReminderV2>().uuId) }

    viewModel =
      RemindersArchiveViewModel(
        reminderV2Repository = reminderV2Repository,
        getRemindersV2ByRemovedStatusUseCase = getRemindersV2ByRemovedStatusUseCase,
        groupV2Repository = groupV2Repository,
        dispatcherProvider = mockDispatcherProvider(),
        uiReminderListAdapter = uiReminderListAdapter,
        deleteReminderUseCase = deleteReminderUseCase,
        deleteAllReminderUseCase = deleteAllReminderUseCase,
      )
  }

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
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns reminders

      val state = viewModel.state.first()

      assertEquals(reminders, state.allReminders)
      assertEquals(reminders, state.filteredReminders)
      assertTrue(state.listState is ListState.Ready)
      assertEquals(2, (state.listState as ListState.Ready).items.size)
    }

  @Test
  fun `state is Empty when there are no archived reminders`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns emptyList()

      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `onSearchQueryChange updates search query and filters reminders on next load`() =
    runTest {
      val matching = reminderV2("1", summary = "Buy milk")
      val nonMatching = reminderV2("2", summary = "Call mom")
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns listOf(matching, nonMatching)

      viewModel.onSearchQueryChange("milk")
      val state = viewModel.state.first()

      assertEquals("milk", state.searchQuery)
      assertEquals(listOf(matching), state.filteredReminders)
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
  fun `deleteReminder deletes the found reminder and reloads the archive`() =
    runTest {
      val target = reminderV2("1")
      coEvery { reminderV2Repository.getById("1") } returns target
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns listOf(reminderV2("1"))

      viewModel.deleteReminder("1")

      coVerify(exactly = 1) { deleteReminderUseCase(target) }
      coVerify(exactly = 1) { getRemindersV2ByRemovedStatusUseCase(removed = true) }
    }

  @Test
  fun `deleteReminder does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null

      viewModel.deleteReminder("missing")

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      coVerify(exactly = 0) { getRemindersV2ByRemovedStatusUseCase(removed = true) }
    }

  @Test
  fun `deleteAll re-fetches the reminders by id, deletes them, reloads and emits ArchiveEmptied`() =
    runTest {
      val remindersV2 = listOf(reminderV2("1"), reminderV2("2"))
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns remindersV2
      coEvery { reminderV2Repository.getById("1") } returns remindersV2[0]
      coEvery { reminderV2Repository.getById("2") } returns remindersV2[1]
      viewModel.state.first()

      viewModel.deleteAll()

      coVerify(exactly = 1) { deleteAllReminderUseCase(remindersV2) }
      coVerify(exactly = 2) { getRemindersV2ByRemovedStatusUseCase(removed = true) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(RemindersArchiveViewModel.NavigationEvent.ArchiveEmptied, event)
    }

  @Test
  fun `deleteAll does nothing when there are no filtered reminders`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = true) } returns emptyList()
      viewModel.state.first()

      viewModel.deleteAll()

      coVerify(exactly = 0) { deleteAllReminderUseCase(any()) }
      assertEquals(null, viewModel.event.value)
    }
}
