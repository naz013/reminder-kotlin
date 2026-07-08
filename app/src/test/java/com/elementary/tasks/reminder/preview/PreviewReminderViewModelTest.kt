package com.elementary.tasks.reminder.preview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.UiReminderPlaceAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class PreviewReminderViewModelTest : BaseTest() {
  private val reminderRepository = mockk<ReminderRepository>()
  private val googleCalendarUtils = mockk<GoogleCalendarUtils>(relaxed = true)
  private val uiReminderPlaceAdapter = mockk<UiReminderPlaceAdapter>()
  private val uiReminderCommonAdapter = mockk<UiReminderCommonAdapter>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val uiNoteListAdapter = mockk<UiNoteListAdapter>(relaxed = true)
  private val uiGoogleTaskListAdapter = mockk<UiGoogleTaskListAdapter>(relaxed = true)
  private val uriToAttachmentFileAdapter = mockk<UriToAttachmentFileAdapter>(relaxed = true)
  private val backupTool = mockk<BackupTool>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxed = true)
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>(relaxed = true)
  private val calendarEventRepository = mockk<CalendarEventRepository>(relaxed = true)
  private val reminderGroupRepository = mockk<ReminderGroupRepository>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>()

  @Before
  override fun setUp() {
    super.setUp()
    every { uiReminderCommonAdapter.getReminderStatus(any(), any()) } returns
      UiReminderStatus(title = "", active = true, removed = false)
    every { uiReminderCommonAdapter.getDue(any(), any()) } returns
      UiReminderDueData(
        dateTime = "1 Jan 2026, 10:00",
        repeat = "Once",
        before = null,
        remaining = null,
        formattedTime = "10:00",
        formattedDateTime = "1 Jan 2026, 10:00",
      )
    every { uiReminderCommonAdapter.getTarget(any(), any()) } returns null
    every { uiReminderCommonAdapter.getPriorityTitle(any()) } returns "Normal"
    every { uiGroupListAdapter.convert(any(), any(), any()) } returns null
    every { dateTimeManager.fromGmtToLocal(any<String>()) } returns null
    every { dateTimeManager.getGmtFromDateTime(any<LocalDateTime>()) } returns ""
    coEvery { reminderGroupRepository.getById(any()) } returns null
    coEvery { noteRepository.getById(any()) } returns null
    coEvery { googleTaskRepository.getByReminderId(any()) } returns null
  }

  private fun reminder(
    id: String = "42",
    type: Int = Reminder.BY_DATE,
    isRemoved: Boolean = false,
    shoppings: List<ShopItem> = emptyList(),
  ): Reminder =
    Reminder(
      uuId = id,
      summary = "Buy milk",
      type = type,
      isActive = true,
      isRemoved = isRemoved,
      shoppings = shoppings,
      syncState = SyncState.Synced,
    )

  private fun createViewModel(id: String = "42"): PreviewReminderViewModel =
    PreviewReminderViewModel(
      id = id,
      reminderRepository = reminderRepository,
      googleCalendarUtils = googleCalendarUtils,
      dispatcherProvider = mockDispatcherProvider(),
      uiReminderPlaceAdapter = uiReminderPlaceAdapter,
      uiReminderCommonAdapter = uiReminderCommonAdapter,
      uiGroupListAdapter = uiGroupListAdapter,
      uiNoteListAdapter = uiNoteListAdapter,
      uiGoogleTaskListAdapter = uiGoogleTaskListAdapter,
      uriToAttachmentFileAdapter = uriToAttachmentFileAdapter,
      backupTool = backupTool,
      noteRepository = noteRepository,
      googleTaskRepository = googleTaskRepository,
      googleTaskListRepository = googleTaskListRepository,
      calendarEventRepository = calendarEventRepository,
      reminderGroupRepository = reminderGroupRepository,
      dateTimeManager = dateTimeManager,
      textProvider = textProvider,
      deleteReminderUseCase = deleteReminderUseCase,
      moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
      activateReminderUseCase = activateReminderUseCase,
      toggleReminderStateUseCase = toggleReminderStateUseCase,
    )

  @Test
  fun `onResume loads status, details, and target info into state`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()

      viewModel.onResume(mockk(relaxed = true))
      val state = viewModel.state.value

      assertEquals("Buy milk", state.summary)
      assertEquals(true, state.status?.active)
      assertEquals("Once", state.repeat)
      assertEquals("Normal", state.priorityTitle)
      assertFalse(state.isLoading)
    }

  @Test
  fun `canCopy is true for a date-type reminder`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder(type = Reminder.BY_DATE)
      val viewModel = createViewModel()

      viewModel.onResume(mockk(relaxed = true))

      assertTrue(viewModel.state.value.canCopy)
    }

  @Test
  fun `canCopy is false for a non-date-type reminder`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder(type = Reminder.BY_TIME)
      val viewModel = createViewModel()

      viewModel.onResume(mockk(relaxed = true))

      assertFalse(viewModel.state.value.canCopy)
    }

  @Test
  fun `canDelete mirrors reminder isRemoved`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder(isRemoved = true)
      val viewModel = createViewModel()

      viewModel.onResume(mockk(relaxed = true))

      assertTrue(viewModel.state.value.canDelete)
    }

  @Test
  fun `onToggleClick posts SAVED when the toggle succeeds`() =
    runTest {
      val activeReminder = reminder()
      coEvery { reminderRepository.getById("42") } returns activeReminder
      coEvery { toggleReminderStateUseCase(activeReminder) } returns (true to activeReminder)
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.SAVED, event?.getContentIfNotHandled())
    }

  @Test
  fun `onToggleClick posts OUTDATED when the toggle fails`() =
    runTest {
      val activeReminder = reminder()
      coEvery { reminderRepository.getById("42") } returns activeReminder
      coEvery { toggleReminderStateUseCase(activeReminder) } returns (false to activeReminder)
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.OUTDATED, event?.getContentIfNotHandled())
    }

  @Test
  fun `onSubTaskChecked toggles the checked state and saves`() =
    runTest {
      val subTask = ShopItem(uuId = "s1", summary = "Milk", isChecked = false, createTime = "")
      coEvery { reminderRepository.getById("42") } returns reminder(shoppings = listOf(subTask))
      coEvery { reminderRepository.save(any()) } returns Unit
      val viewModel = createViewModel()

      viewModel.onSubTaskChecked("s1")

      coVerify {
        reminderRepository.save(
          match { it.shoppings.first { s -> s.uuId == "s1" }.isChecked },
        )
      }
    }

  @Test
  fun `onSubTaskRemoved removes the subtask and saves`() =
    runTest {
      val subTask = ShopItem(uuId = "s1", summary = "Milk", isChecked = false, createTime = "")
      coEvery { reminderRepository.getById("42") } returns reminder(shoppings = listOf(subTask))
      coEvery { reminderRepository.save(any()) } returns Unit
      val viewModel = createViewModel()

      viewModel.onSubTaskRemoved("s1")

      coVerify { reminderRepository.save(match { it.shoppings.isEmpty() }) }
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()

      viewModel.onDeleteClick()

      assertTrue(viewModel.state.value.showDeleteConfirm)
    }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()
      viewModel.onDeleteClick()

      viewModel.onDeleteDismiss()

      assertFalse(viewModel.state.value.showDeleteConfirm)
    }

  @Test
  fun `onDeleteConfirmed hard-deletes when canDelete is true`() =
    runTest {
      val removedReminder = reminder(isRemoved = true)
      coEvery { reminderRepository.getById("42") } returns removedReminder
      val viewModel = createViewModel()
      viewModel.onResume(mockk(relaxed = true))

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteReminderUseCase(removedReminder) }
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.DELETED, event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteConfirmed moves to archive when canDelete is false`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder(isRemoved = false)
      val viewModel = createViewModel()
      viewModel.onResume(mockk(relaxed = true))

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { moveReminderToArchiveUseCase("42") }
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.DELETED, event?.getContentIfNotHandled())
    }

  @Test
  fun `copyReminder activates a new reminder and posts SAVED`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()

      viewModel.copyReminder(LocalTime.of(9, 0))

      coVerify(exactly = 1) { activateReminderUseCase(any()) }
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.SAVED, event?.getContentIfNotHandled())
    }

  @Test
  fun `shareReminder posts a UiShareData event`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      every { backupTool.reminderToFile(any()) } returns null
      val viewModel = createViewModel()

      viewModel.shareReminder()

      val shared = viewModel.sharedFile.getOrAwaitValue()
      assertEquals("Buy milk", shared?.name)
    }

  @Test
  fun `deleteEvent removes the calendar event locally and remotely`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()
      val event =
        UiCalendarEventList(
          title = "Event",
          description = "",
          calendarName = null,
          dateStartFormatted = null,
          dateEndFormatted = null,
          id = 7L,
          localId = "local-7",
        )

      viewModel.deleteEvent(event)

      coVerify(exactly = 1) { calendarEventRepository.delete("local-7") }
      coVerify(exactly = 1) { googleCalendarUtils.deleteEvent(7L) }
    }
}
