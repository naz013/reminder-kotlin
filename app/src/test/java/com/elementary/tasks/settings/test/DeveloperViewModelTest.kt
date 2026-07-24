package com.elementary.tasks.settings.test

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.note.Note
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.repository.UsedTimeRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import com.github.naz013.reviews.AppSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class DeveloperViewModelTest : BaseTest() {
  private val legalDocumentRepository = mockk<LegalDocumentRepository>(relaxed = true)
  private val prefs = mockk<Prefs>()
  private val reminderRepository = mockk<ReminderRepository>(relaxed = true)
  private val birthdayRepository = mockk<BirthdayRepository>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val calendarEventRepository = mockk<CalendarEventRepository>(relaxed = true)
  private val eventHistoryRepository = mockk<EventHistoryRepository>(relaxed = true)
  private val eventOccurrenceRepository = mockk<EventOccurrenceRepository>(relaxed = true)
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>(relaxed = true)
  private val placeRepository = mockk<PlaceRepository>(relaxed = true)
  private val recentQueryRepository = mockk<RecentQueryRepository>(relaxed = true)
  private val recurPresetRepository = mockk<RecurPresetRepository>(relaxed = true)
  private val reminderGroupRepository = mockk<ReminderGroupRepository>(relaxed = true)
  private val remoteFileMetadataRepository = mockk<RemoteFileMetadataRepository>(relaxed = true)
  private val usedTimeRepository = mockk<UsedTimeRepository>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val workflowTemplateRepository = mockk<WorkflowTemplateRepository>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>()

  private lateinit var viewModel: DeveloperViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getGmtFromDateTime(any<LocalDateTime>()) } returns "gmt"
    every { dateTimeManager.formatBirthdayDate(any()) } returns "1990-01-01"
    every { dateTimeManager.getNowGmtDateTime() } returns "now"
    coEvery { reminderGroupRepository.defaultGroup() } returns null
    every { buildInfo.isPro } returns true
    every { prefs.isUserLogged = any() } just Runs
    every { prefs.lastVersionCode = any() } just Runs

    viewModel =
      DeveloperViewModel(
        legalDocumentRepository = legalDocumentRepository,
        prefs = prefs,
        dispatcherProvider = mockDispatcherProvider(),
        reminderRepository = reminderRepository,
        birthdayRepository = birthdayRepository,
        dateTimeManager = dateTimeManager,
        calendarEventRepository = calendarEventRepository,
        eventHistoryRepository = eventHistoryRepository,
        eventOccurrenceRepository = eventOccurrenceRepository,
        googleTaskListRepository = googleTaskListRepository,
        googleTaskRepository = googleTaskRepository,
        noteRepository = noteRepository,
        placeRepository = placeRepository,
        recentQueryRepository = recentQueryRepository,
        recurPresetRepository = recurPresetRepository,
        reminderGroupRepository = reminderGroupRepository,
        remoteFileMetadataRepository = remoteFileMetadataRepository,
        usedTimeRepository = usedTimeRepository,
        reminderV2Repository = reminderV2Repository,
        groupV2Repository = groupV2Repository,
        workflowRuleRepository = workflowRuleRepository,
        workflowTemplateRepository = workflowTemplateRepository,
        buildInfo = buildInfo,
      )
  }

  @Test
  fun `initial state has no dialog and no confirmation`() {
    assertNull(viewModel.state.value.dialog)
    assertFalse(viewModel.state.value.clearAllTablesConfirmation)
  }

  @Test
  fun `onResetBannersClick resets the privacy policy banner and login flag`() {
    viewModel.onResetBannersClick()

    verify { legalDocumentRepository.resetSeen(LegalDocumentType.PRIVACY_POLICY) }
    verify { prefs.isUserLogged = false }
    verify { prefs.lastVersionCode = 0 }
    assertEquals(DeveloperEvent.BannersReset, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onReminderDialogClick shows the reminder options dialog`() {
    viewModel.onReminderDialogClick()

    val dialog = viewModel.state.value.dialog
    assertEquals(DeveloperDialogKind.REMINDER, dialog?.kind)
    assertEquals(0, dialog?.selectedIndex)
  }

  @Test
  fun `onBirthdayDialogClick shows the birthday options dialog`() {
    viewModel.onBirthdayDialogClick()

    val dialog = viewModel.state.value.dialog
    assertEquals(DeveloperDialogKind.BIRTHDAY, dialog?.kind)
  }

  @Test
  fun `onClearTableClick shows the clear table options dialog`() {
    viewModel.onClearTableClick()

    val dialog = viewModel.state.value.dialog
    assertEquals(DeveloperDialogKind.CLEAR_TABLE, dialog?.kind)
  }

  @Test
  fun `onClearAllTablesClick shows the confirmation`() {
    viewModel.onClearAllTablesClick()

    assertTrue(viewModel.state.value.clearAllTablesConfirmation)
  }

  @Test
  fun `onClearAllTablesDismiss hides the confirmation`() {
    viewModel.onClearAllTablesClick()

    viewModel.onClearAllTablesDismiss()

    assertFalse(viewModel.state.value.clearAllTablesConfirmation)
  }

  @Test
  fun `onClearAllTablesConfirm hides confirmation, clears every table and posts a message`() =
    runTest {
      viewModel.onClearAllTablesClick()

      viewModel.onClearAllTablesConfirm()

      assertFalse(viewModel.state.value.clearAllTablesConfirmation)
      coVerify { birthdayRepository.deleteAll() }
      coVerify { recentQueryRepository.deleteAll() }
      coVerify { recurPresetRepository.deleteAll() }
      coVerify { usedTimeRepository.deleteAll() }
      coVerify { calendarEventRepository.deleteAll() }
      coVerify { reminderGroupRepository.deleteAll() }
      coVerify { reminderRepository.deleteAll() }
      coVerify { placeRepository.deleteAll() }
      coVerify { noteRepository.deleteAllNotes() }
      coVerify { noteRepository.deleteAllImages() }
      coVerify { googleTaskListRepository.deleteAll() }
      coVerify { googleTaskRepository.deleteAll() }
      coVerify { remoteFileMetadataRepository.deleteAll() }
      coVerify { eventOccurrenceRepository.deleteAll() }
      coVerify { eventHistoryRepository.deleteAll() }
      val event =
        viewModel.navigationEvent.getOrAwaitValue()?.getContentIfNotHandled()
          as DeveloperEvent.ShowMessage
      assertEquals("All tables have been cleared", event.message)
    }

  @Test
  fun `onInsertDemoDataClick inserts demo reminders, birthdays and notes`() =
    runTest {
      viewModel.onInsertDemoDataClick()

      coVerify(exactly = 6) { reminderRepository.save(any()) }
      coVerify(exactly = 4) { birthdayRepository.save(any()) }
      coVerify(exactly = 5) { noteRepository.save(any<Note>()) }
      val event =
        viewModel.navigationEvent.getOrAwaitValue()?.getContentIfNotHandled()
          as DeveloperEvent.ShowMessage
      assertEquals("Demo data has been inserted", event.message)
    }

  @Test
  fun `onDialogOptionSelected updates the selected index`() {
    viewModel.onReminderDialogClick()

    viewModel.onDialogOptionSelected(3)

    assertEquals(3, viewModel.state.value.dialog?.selectedIndex)
  }

  @Test
  fun `onDialogOptionSelected is a no-op when there is no dialog`() {
    viewModel.onDialogOptionSelected(3)

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onDialogConfirm for a reminder saves it and opens the reminder action`() =
    runTest {
      viewModel.onReminderDialogClick()

      viewModel.onDialogConfirm()

      coVerify { reminderRepository.save(any()) }
      assertNull(viewModel.state.value.dialog)
      val event =
        viewModel.navigationEvent.getOrAwaitValue()?.getContentIfNotHandled()
          as DeveloperEvent.OpenReminderAction
      assertTrue(event.reminderId.isNotEmpty())
    }

  @Test
  fun `onDialogConfirm for a birthday saves it and opens the birthday action`() =
    runTest {
      viewModel.onBirthdayDialogClick()

      viewModel.onDialogConfirm()

      coVerify { birthdayRepository.save(any()) }
      assertNull(viewModel.state.value.dialog)
      val event =
        viewModel.navigationEvent.getOrAwaitValue()?.getContentIfNotHandled()
          as DeveloperEvent.OpenBirthdayAction
      assertTrue(event.birthdayId.isNotEmpty())
    }

  @Test
  fun `onDialogConfirm for clear table clears the selected table and posts a message`() =
    runTest {
      viewModel.onClearTableClick()

      viewModel.onDialogConfirm()

      coVerify { birthdayRepository.deleteAll() }
      assertNull(viewModel.state.value.dialog)
      val event =
        viewModel.navigationEvent.getOrAwaitValue()?.getContentIfNotHandled()
          as DeveloperEvent.ShowMessage
      assertEquals("Birthday table has been cleared", event.message)
    }

  @Test
  fun `onDialogConfirm is a no-op when there is no dialog`() {
    viewModel.onDialogConfirm()

    assertEquals(null, viewModel.navigationEvent.value)
  }

  @Test
  fun `onDialogDismiss clears the dialog`() {
    viewModel.onReminderDialogClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onObjectExportClick posts OpenObjectExport`() {
    viewModel.onObjectExportClick()

    assertEquals(DeveloperEvent.OpenObjectExport, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onReviewDialogClick posts OpenReviewDialog with pro app source when pro`() {
    every { buildInfo.isPro } returns true

    viewModel.onReviewDialogClick()

    val event =
      viewModel.navigationEvent.value?.peekContent() as DeveloperEvent.OpenReviewDialog
    assertEquals(AppSource.PRO, event.appSource)
  }

  @Test
  fun `onReviewDialogClick posts OpenReviewDialog with free app source when not pro`() {
    every { buildInfo.isPro } returns false

    viewModel.onReviewDialogClick()

    val event =
      viewModel.navigationEvent.value?.peekContent() as DeveloperEvent.OpenReviewDialog
    assertEquals(AppSource.FREE, event.appSource)
  }

  @Test
  fun `onProVersionClick posts OpenProVersion`() {
    viewModel.onProVersionClick()

    assertEquals(DeveloperEvent.OpenProVersion, viewModel.navigationEvent.value?.peekContent())
  }
}
