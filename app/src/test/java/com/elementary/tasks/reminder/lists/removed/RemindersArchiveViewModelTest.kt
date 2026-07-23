package com.elementary.tasks.reminder.lists.removed

import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.elementary.tasks.reminder.usecase.DeleteAllReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderRepository
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

class RemindersArchiveViewModelTest : BaseTest() {
  private val reminderRepository = mockk<ReminderRepository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val deleteAllReminderUseCase = mockk<DeleteAllReminderUseCase>(relaxed = true)

  private lateinit var viewModel: RemindersArchiveViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // RemindersArchiveViewModel.state runs loadReminders() in onStart on every collection - every
    // test collects state at least once, so a default stub avoids an unstubbed-call failure.
    coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns emptyList()
    every { uiReminderListAdapter.create(any()) } answers { uiReminderList(firstArg<Reminder>().uuId) }

    viewModel =
      RemindersArchiveViewModel(
        reminderRepository = reminderRepository,
        dispatcherProvider = mockDispatcherProvider(),
        uiReminderListAdapter = uiReminderListAdapter,
        deleteReminderUseCase = deleteReminderUseCase,
        deleteAllReminderUseCase = deleteAllReminderUseCase,
      )
  }

  private fun reminder(
    id: String,
    summary: String = "",
  ) = Reminder(uuId = id, summary = summary, syncState = SyncState.Synced)

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
      val reminders = listOf(reminder("1"), reminder("2"))
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns reminders

      val state = viewModel.state.first()

      assertEquals(reminders, state.allReminders)
      assertEquals(reminders, state.filteredReminders)
      assertTrue(state.listState is ListState.Ready)
      assertEquals(2, (state.listState as ListState.Ready).items.size)
    }

  @Test
  fun `state is Empty when there are no archived reminders`() =
    runTest {
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns emptyList()

      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `onSearchQueryChange updates search query and filters reminders on next load`() =
    runTest {
      val matching = reminder("1", summary = "Buy milk")
      val nonMatching = reminder("2", summary = "Call mom")
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns listOf(matching, nonMatching)

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
      val target = reminder("1")
      coEvery { reminderRepository.getById("1") } returns target
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns listOf(target)

      viewModel.deleteReminder("1")

      coVerify(exactly = 1) { deleteReminderUseCase(target) }
      coVerify(exactly = 1) { reminderRepository.getByRemovedStatus(removed = true) }
    }

  @Test
  fun `deleteReminder does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderRepository.getById("missing") } returns null

      viewModel.deleteReminder("missing")

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      coVerify(exactly = 0) { reminderRepository.getByRemovedStatus(removed = true) }
    }

  @Test
  fun `deleteAll deletes filtered reminders, reloads and emits ArchiveEmptied`() =
    runTest {
      val reminders = listOf(reminder("1"), reminder("2"))
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns reminders
      viewModel.state.first()

      viewModel.deleteAll()

      coVerify(exactly = 1) { deleteAllReminderUseCase(reminders) }
      coVerify(exactly = 2) { reminderRepository.getByRemovedStatus(removed = true) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(RemindersArchiveViewModel.NavigationEvent.ArchiveEmptied, event)
    }

  @Test
  fun `deleteAll does nothing when there are no filtered reminders`() =
    runTest {
      coEvery { reminderRepository.getByRemovedStatus(removed = true) } returns emptyList()
      viewModel.state.first()

      viewModel.deleteAll()

      coVerify(exactly = 0) { deleteAllReminderUseCase(any()) }
      assertEquals(null, viewModel.event.value)
    }
}
