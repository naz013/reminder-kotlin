package com.github.naz013.feature.reminder.preview

import android.net.Uri
import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.reminder.UiReminderCommonAdapter
import com.github.naz013.feature.reminder.UiReminderPlaceAdapter
import com.github.naz013.feature.reminder.UiRepeatLimitInfo
import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.feature.reminder.note.UiNoteListAdapter
import com.github.naz013.ui.reminder.UiReminderDueData
import com.github.naz013.ui.reminder.UiReminderStatus
import com.github.naz013.files.BackupTool
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.github.naz013.feature.reminder.preview.data.UiCalendarEventList
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.logic.reminder.usecase.SyncReminderToCloudUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import com.github.naz013.logic.reminder.usecase.TogglePinnedReminderUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.observer.TableChangeListener
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.File

class PreviewReminderViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val googleCalendarUtils = mockk<GoogleCalendarApi>(relaxed = true)
  private val uiReminderPlaceAdapter = mockk<UiReminderPlaceAdapter>()
  private val uiReminderCommonAdapter = mockk<UiReminderCommonAdapter>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val uiNoteListAdapter = mockk<UiNoteListAdapter>(relaxed = true)
  private val googleTaskItemStateAdapter = mockk<GoogleTaskItemStateAdapter>(relaxed = true)
  private val uriToAttachmentFileAdapter = mockk<UriToAttachmentFileAdapter>(relaxed = true)
  private val backupTool = mockk<BackupTool>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxed = true)
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>(relaxed = true)
  private val calendarEventRepository = mockk<CalendarEventRepository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>()
  private val togglePinnedReminderUseCase = mockk<TogglePinnedReminderUseCase>(relaxed = true)
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val tableChangeListenerFactory = mockk<TableChangeListenerFactory>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()
  private val syncReminderToCloudUseCase = mockk<SyncReminderToCloudUseCase>(relaxed = true)
  private val googleDriveAuthManager = mockk<GoogleDriveAuthManager>()
  private val dropboxAuthManager = mockk<DropboxAuthManager>()

  @Before
  override fun setUp() {
    super.setUp()
    every { googleDriveAuthManager.isAuthorized() } returns false
    every { dropboxAuthManager.isAuthorized() } returns false
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    every { uiReminderCommonAdapter.getReminderStatus(any(), any()) } returns
      UiReminderStatus(title = "", active = true, removed = false)
    every { uiReminderCommonAdapter.getDueV2(any()) } returns
      UiReminderDueData(
        dateTime = "1 Jan 2026, 10:00",
        repeat = "Once",
        before = null,
        remaining = null,
        formattedTime = "10:00",
        formattedDateTime = "1 Jan 2026, 10:00",
      )
    every { uiReminderCommonAdapter.getTargetV2(any()) } returns null
    every { uiReminderCommonAdapter.getPriorityTitle(any()) } returns "Normal"
    every { uiReminderCommonAdapter.getRepeatLimitInfoV2(any()) } returns null
    every { uiReminderCommonAdapter.getRepeatUntilV2(any()) } returns null
    every { uiReminderCommonAdapter.getTriggeredCountTextV2(any()) } returns null
    every { uiReminderCommonAdapter.getSnoozedCountTextV2(any()) } returns null
    every { dateTimeManager.fromGmtToLocal(any<String>()) } returns null
    every { dateTimeManager.getGmtFromDateTime(any<LocalDateTime>()) } returns ""
    coEvery { reminderV2Repository.getById(any()) } returns reminderV2()
    coEvery { groupV2Repository.getById(any()) } returns null
    coEvery { noteRepository.getById(any()) } returns null
    coEvery { googleTaskRepository.getByReminderId(any()) } returns null
  }

  private fun reminderV2(
    id: String = "42",
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    isRemoved: Boolean = false,
    isActive: Boolean = true,
    isPinned: Boolean = false,
    offlineOnly: Boolean = false,
  ): ReminderV2 =
    ReminderV2(
      uuId = id,
      summary = "Buy milk",
      recurrence = recurrence,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      isActive = isActive,
      isRemoved = isRemoved,
      isPinned = isPinned,
      offlineOnly = offlineOnly,
    )

  private fun createViewModel(id: String = "42"): PreviewReminderViewModel =
    PreviewReminderViewModel(
      id = id,
      reminderV2Repository = reminderV2Repository,
      googleCalendarApi = googleCalendarUtils,
      dispatcherProvider = mockDispatcherProvider(),
      uiReminderPlaceAdapter = uiReminderPlaceAdapter,
      uiReminderCommonAdapter = uiReminderCommonAdapter,
      uiGroupListAdapter = uiGroupListAdapter,
      uiNoteListAdapter = uiNoteListAdapter,
      googleTaskItemStateAdapter = googleTaskItemStateAdapter,
      uriToAttachmentFileAdapter = uriToAttachmentFileAdapter,
      backupTool = backupTool,
      noteRepository = noteRepository,
      googleTaskRepository = googleTaskRepository,
      googleTaskListRepository = googleTaskListRepository,
      calendarEventRepository = calendarEventRepository,
      groupV2Repository = groupV2Repository,
      dateTimeManager = dateTimeManager,
      textProvider = textProvider,
      deleteReminderUseCase = deleteReminderUseCase,
      moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
      activateReminderUseCase = activateReminderUseCase,
      toggleReminderStateUseCase = toggleReminderStateUseCase,
      togglePinnedReminderUseCase = togglePinnedReminderUseCase,
      saveReminderUseCase = saveReminderUseCase,
      tableChangeListenerFactory = tableChangeListenerFactory,
      tagAssignmentRepository = tagAssignmentRepository,
      tagChipStateAdapter = tagChipStateAdapter,
      syncReminderToCloudUseCase = syncReminderToCloudUseCase,
      googleDriveAuthManager = googleDriveAuthManager,
      dropboxAuthManager = dropboxAuthManager,
    )

  @Test
  fun `loads status, details, and target info into state`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals("Buy milk", state.summary)
      assertEquals(true, state.status?.active)
      assertEquals("Once", state.repeat)
      assertEquals("Normal", state.priorityTitle)
      assertFalse(state.isLoading)
    }

  @Test
  fun `re-collecting state after a config change reloads it from the repository`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()
      viewModel.state.first()
      coEvery { reminderV2Repository.getById("42") } returns reminderV2().copy(summary = "Buy bread")

      val state = viewModel.state.first()

      assertEquals("Buy bread", state.summary)
    }

  @Test
  fun `refresh reloads state from the repository`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()
      viewModel.state.first()
      coEvery { reminderV2Repository.getById("42") } returns reminderV2().copy(summary = "Buy bread")

      viewModel.refresh()
      val state = viewModel.state.first()

      assertEquals("Buy bread", state.summary)
    }

  @Test
  fun `reloads state when the ReminderV2 table changes externally`() =
    runTest {
      val listener = mockk<TableChangeListener>(relaxed = true)
      val onChanged = slot<() -> Unit>()
      every { tableChangeListenerFactory.create(Table.ReminderV2, capture(onChanged)) } returns listener
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()
      viewModel.state.first()
      coEvery { reminderV2Repository.getById("42") } returns reminderV2().copy(summary = "Buy bread")

      onChanged.captured.invoke()
      val state = viewModel.state.first()

      assertEquals("Buy bread", state.summary)
      verify { listener.register() }
    }

  @Test
  fun `canCopy is true for a date-type reminder`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(recurrence = RecurrenceRule.Once)
      val viewModel = createViewModel()

      assertTrue(viewModel.state.first().canCopy)
    }

  @Test
  fun `canCopy is false for a non-date-type reminder`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns
        reminderV2(recurrence = RecurrenceRule.Weekly(weekdays = listOf(1, 0, 0, 0, 0, 0, 0)))
      val viewModel = createViewModel()

      assertFalse(viewModel.state.first().canCopy)
    }

  @Test
  fun `canDelete mirrors reminder isRemoved`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isRemoved = true)
      val viewModel = createViewModel()

      assertTrue(viewModel.state.first().canDelete)
    }

  @Test
  fun `onToggleClick reloads state after a successful toggle`() =
    runTest {
      val activeReminder = reminderV2(isActive = true)
      coEvery { reminderV2Repository.getById("42") } returns activeReminder
      coEvery { toggleReminderStateUseCase(activeReminder) } returns ToggleReminderStateUseCase.Result(true , activeReminder.copy(isActive = false))
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      coVerify(exactly = 1) { toggleReminderStateUseCase(activeReminder) }
      assertEquals(null, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onToggleClick shows an error when the toggle fails`() =
    runTest {
      every { textProvider.getString(any()) } returns "Reminder is outdated"
      val activeReminder = reminderV2()
      coEvery { reminderV2Repository.getById("42") } returns activeReminder
      coEvery { toggleReminderStateUseCase(activeReminder) } returns ToggleReminderStateUseCase.Result(false, activeReminder)
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewReminderViewModel.ViewModelEvent.ShowError("Reminder is outdated"), event)
    }

  @Test
  fun `onToggleClick does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns null
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      coVerify(exactly = 0) { toggleReminderStateUseCase(any()) }
      assertEquals(PreviewReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onToggleClick does nothing when the reminder is already removed`() =
    runTest {
      every { textProvider.getString(any()) } returns "Error"
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isRemoved = true)
      val viewModel = createViewModel()

      viewModel.onToggleClick()

      coVerify(exactly = 0) { toggleReminderStateUseCase(any()) }

      val event = viewModel.event.value?.peekContent()
      assertTrue(event is PreviewReminderViewModel.ViewModelEvent.ShowError)
      assertEquals("Error", (event as? PreviewReminderViewModel.ViewModelEvent.ShowError)?.message)
    }

  @Test
  fun `state exposes isPinned from the reminder`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isPinned = true)
      val viewModel = createViewModel()

      assertTrue(viewModel.state.first().isPinned)
    }

  @Test
  fun `state exposes repeat limit info from the adapter`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      every { uiReminderCommonAdapter.getRepeatLimitInfoV2(any()) } returns
        UiRepeatLimitInfo(text = "3 of 10 times · 7 left", isLimitReached = false)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals("3 of 10 times · 7 left", state.repeatLimitText)
      assertFalse(state.isRepeatLimitReached)
    }

  @Test
  fun `state flags the repeat limit as reached when the adapter reports it`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      every { uiReminderCommonAdapter.getRepeatLimitInfoV2(any()) } returns
        UiRepeatLimitInfo(text = "Repeat limit reached · 10 of 10 times", isLimitReached = true)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertTrue(state.isRepeatLimitReached)
    }

  @Test
  fun `state exposes the repeat-until, triggered-count, and snoozed-count text from the adapter`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      every { uiReminderCommonAdapter.getRepeatUntilV2(any()) } returns "Repeats until 31 Dec 2026"
      every { uiReminderCommonAdapter.getTriggeredCountTextV2(any()) } returns "Triggered 5 times"
      every { uiReminderCommonAdapter.getSnoozedCountTextV2(any()) } returns "Snoozed 2 times"
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertEquals("Repeats until 31 Dec 2026", state.repeatUntilText)
      assertEquals("Triggered 5 times", state.triggeredCountText)
      assertEquals("Snoozed 2 times", state.snoozedCountText)
    }

  @Test
  fun `state exposes isOfflineOnly from the reminder`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(offlineOnly = true)
      val viewModel = createViewModel()

      assertTrue(viewModel.state.first().isOfflineOnly)
    }

  @Test
  fun `onPinToggleClick toggles pinned state and reloads`() =
    runTest {
      val reminder = reminderV2(isPinned = false)
      coEvery { reminderV2Repository.getById("42") } returns reminder
      val viewModel = createViewModel()

      viewModel.onPinToggleClick()

      coVerify(exactly = 1) { togglePinnedReminderUseCase(reminder) }
    }

  @Test
  fun `onPinToggleClick does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns null
      val viewModel = createViewModel()

      viewModel.onPinToggleClick()

      coVerify(exactly = 0) { togglePinnedReminderUseCase(any()) }
    }

  @Test
  fun `showSyncToCloud is false for a normal reminder even when a cloud provider is logged in`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(offlineOnly = false)
      every { googleDriveAuthManager.isAuthorized() } returns true
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertFalse(state.showSyncToCloud)
    }

  @Test
  fun `showSyncToCloud is false for an offline-only reminder when no cloud provider is logged in`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(offlineOnly = true)
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertFalse(state.showSyncToCloud)
    }

  @Test
  fun `showSyncToCloud is true for an offline-only reminder when Google Drive is logged in`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(offlineOnly = true)
      every { googleDriveAuthManager.isAuthorized() } returns true
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertTrue(state.showSyncToCloud)
    }

  @Test
  fun `showSyncToCloud is true for an offline-only reminder when Dropbox is logged in`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(offlineOnly = true)
      every { dropboxAuthManager.isAuthorized() } returns true
      val viewModel = createViewModel()

      val state = viewModel.state.first()

      assertTrue(state.showSyncToCloud)
    }

  @Test
  fun `onSyncToCloudClick syncs the reminder to cloud and reloads`() =
    runTest {
      val reminder = reminderV2(offlineOnly = true)
      coEvery { reminderV2Repository.getById("42") } returns reminder
      val viewModel = createViewModel()

      viewModel.onSyncToCloudClick()

      coVerify(exactly = 1) { syncReminderToCloudUseCase(reminder) }
    }

  @Test
  fun `onSyncToCloudClick does nothing when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns null
      val viewModel = createViewModel()

      viewModel.onSyncToCloudClick()

      coVerify(exactly = 0) { syncReminderToCloudUseCase(any()) }
    }

  @Test
  fun `onSubTaskChecked toggles the checked state and saves`() =
    runTest {
      val subTask = ShopItemV2(uuId = "s1", summary = "Milk", isChecked = false, createdAt = LocalDateTime.now())
      coEvery { reminderV2Repository.getById("42") } returns reminderV2().copy(shoppingItems = listOf(subTask))

      val viewModel = createViewModel()

      viewModel.onSubTaskChecked("s1")

      coVerify {
        saveReminderUseCase(
          match { it.shoppingItems.first { s -> s.uuId == "s1" }.isChecked },
        )
      }
    }

  @Test
  fun `onSubTaskRemoved removes the subtask and saves`() =
    runTest {
      val subTask = ShopItemV2(uuId = "s1", summary = "Milk", isChecked = false, createdAt = LocalDateTime.now())
      coEvery { reminderV2Repository.getById("42") } returns reminderV2().copy(shoppingItems = listOf(subTask))

      val viewModel = createViewModel()

      viewModel.onSubTaskRemoved("s1")

      coVerify { saveReminderUseCase(match { it.shoppingItems.isEmpty() }) }
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onDeleteClick()

      assertTrue(viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() =
    runTest {
      val viewModel = createViewModel()
      viewModel.onDeleteClick()

      viewModel.onDeleteDismiss()

      assertFalse(viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteConfirmed hard-deletes when canDelete is true`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isRemoved = true)
      val removedReminder = reminderV2(isRemoved = true)
      coEvery { reminderV2Repository.getById("42") } returns removedReminder
      val viewModel = createViewModel()
      viewModel.state.first()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteReminderUseCase(removedReminder) }
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewReminderViewModel.ViewModelEvent.MoveBack, event)
    }

  @Test
  fun `onDeleteConfirmed moves to archive when canDelete is false`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isRemoved = false)
      coEvery { reminderV2Repository.getById("42") } returns reminderV2(isRemoved = false)
      val viewModel = createViewModel()
      viewModel.state.first()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { moveReminderToArchiveUseCase("42") }
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewReminderViewModel.ViewModelEvent.MoveBack, event)
    }

  @Test
  fun `copyReminder activates a new reminder with a fresh id`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()

      viewModel.copyReminder(LocalTime.of(9, 0))

      coVerify(exactly = 1) { activateReminderUseCase(match { it.uuId != "42" }) }
    }

  @Test
  fun `copyReminder preserves a null group id instead of backfilling a default`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val viewModel = createViewModel()

      viewModel.copyReminder(LocalTime.of(9, 0))

      coVerify(exactly = 1) { activateReminderUseCase(match { it.groupId == null }) }
      coVerify(exactly = 0) { groupV2Repository.defaultGroup() }
    }

  @Test
  fun `shareReminder posts a ShareData event when the backup file is created`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      val file = File("reminder.ics")
      coEvery { backupTool.reminderToFile(any()) } returns file
      val viewModel = createViewModel()

      viewModel.shareReminder()

      val event = viewModel.event.value?.peekContent()
      assertEquals(PreviewReminderViewModel.ViewModelEvent.ShareData(file, "Buy milk"), event)
    }

  @Test
  fun `shareReminder emits nothing when the backup file cannot be created`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminderV2()
      coEvery { backupTool.reminderToFile(any()) } returns null
      val viewModel = createViewModel()

      viewModel.shareReminder()

      assertEquals(null, viewModel.event.value?.peekContent())
    }

  @Test
  fun `deleteEvent removes the calendar event locally and remotely`() =
    runTest {
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

  @Test
  fun `onOpenCalendarClicked posts OpenCalendar for a valid event id`() =
    runTest {
      // toUri() delegates to the real Uri.parse(), which isn't mockable-by-default under the
      // JVM unit test android.jar stub - mock the underlying static call it wraps instead.
      mockkStatic(Uri::class)
      every { Uri.parse(any()) } returns mockk(relaxed = true)
      every { textProvider.getString(any()) } returns "Calendar"
      val viewModel = createViewModel()

      viewModel.onOpenCalendarClicked(7L)

      val event = viewModel.event.getOrAwaitValue()?.getContentIfNotHandled()
      assertTrue(event is PreviewReminderViewModel.ViewModelEvent.OpenCalendar)
      assertEquals("Calendar", (event as PreviewReminderViewModel.ViewModelEvent.OpenCalendar).title)
    }

  @Test
  fun `onOpenCalendarClicked does nothing for a non-positive event id`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onOpenCalendarClicked(0L)

      assertEquals(null, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onCopyClicked posts a ShowCopyTimeDialog event with half-hour slots`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onCopyClicked()

      val event = viewModel.event.value?.peekContent()
      assertTrue(event is PreviewReminderViewModel.ViewModelEvent.ShowCopyTimeDialog)
      // Slots run from 00:00 up to (but excluding) 23:30 in 30-minute steps: 47 entries, last one 23:00.
      assertEquals(47, (event as PreviewReminderViewModel.ViewModelEvent.ShowCopyTimeDialog).times.size)
      assertEquals(LocalTime.of(0, 0), event.times.first())
      assertEquals(LocalTime.of(23, 0), event.times.last())
    }
}
