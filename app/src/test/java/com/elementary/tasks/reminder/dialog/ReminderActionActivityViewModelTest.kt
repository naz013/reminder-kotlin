package com.elementary.tasks.reminder.dialog

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.actions.ReminderAction
import com.elementary.tasks.reminder.scheduling.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.theme.ColorProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReminderActionActivityViewModelTest : BaseTest() {
  private val reminderRepository = mockk<ReminderRepository>()
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val completeReminderUseCase = mockk<CompleteReminderUseCase>(relaxed = true)
  private val deactivateReminderUseCase = mockk<DeactivateReminderUseCase>(relaxed = true)
  private val snoozeReminderUseCase = mockk<SnoozeReminderUseCase>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)
  private val getReminderActionScreenStateUseCase = mockk<CreateReminderActionScreenStateUseCase>()
  private val notifier = mockk<Notifier>(relaxed = true)
  private val colorProvider = mockk<ColorProvider>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private val screenState =
    ReminderActionScreenState(
      id = "42",
      header = mockk(relaxed = true),
      todoList = null,
      mainAction = ReminderActionScreenActionItem(ReminderAction.Complete, "Complete", 0),
      secondaryActions = emptyList(),
    )

  private fun reminder(
    id: String = "42",
    type: Int = Reminder.BY_DATE,
    to: String = "",
    target: String = "",
    summary: String = "Buy milk",
    delay: Int = 0,
    shoppings: List<ShopItem> = emptyList(),
  ): Reminder =
    Reminder(
      uuId = id,
      type = type,
      to = to,
      target = target,
      summary = summary,
      delay = delay,
      shoppings = shoppings,
      syncState = SyncState.Synced,
    )

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getReminderActionScreenStateUseCase(any()) } returns screenState
  }

  private fun createViewModel(id: String = "42"): ReminderActionActivityViewModel =
    ReminderActionActivityViewModel(
      id = id,
      reminderRepository = reminderRepository,
      dispatcherProvider = mockDispatcherProvider(),
      saveReminderUseCase = saveReminderUseCase,
      completeReminderUseCase = completeReminderUseCase,
      deactivateReminderUseCase = deactivateReminderUseCase,
      snoozeReminderUseCase = snoozeReminderUseCase,
      prefs = prefs,
      getReminderActionScreenStateUseCase = getReminderActionScreenStateUseCase,
      notifier = notifier,
      colorProvider = colorProvider,
      textProvider = textProvider,
    )

  @Test
  fun `loads the reminder into state on creation`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()

      val viewModel = createViewModel()

      assertEquals(screenState, viewModel.state.getOrAwaitValue())
    }

  @Test
  fun `state stays unset when the reminder is not found`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns null

      val viewModel = createViewModel()

      assertEquals(null, viewModel.state.value)
    }

  @Test
  fun `onActionClick Complete completes the reminder and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Complete)

      coVerify(exactly = 1) { completeReminderUseCase(r) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick Dismiss deactivates the reminder and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Dismiss)

      coVerify(exactly = 1) { deactivateReminderUseCase(r) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SnoozeCustom shows the snooze dialog without snoozing`() =
    runTest {
      coEvery { reminderRepository.getById("42") } returns reminder()
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SnoozeCustom)

      coVerify(exactly = 0) { snoozeReminderUseCase(any(), any()) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.ShowSnoozeDialog,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick Snooze uses the reminder's own delay when non-zero`() =
    runTest {
      val r = reminder(delay = 15)
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Snooze)

      coVerify(exactly = 1) { snoozeReminderUseCase(r, 15) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick Snooze falls back to the default snooze time from prefs when delay is zero`() =
    runTest {
      every { prefs.snoozeTime } returns 30
      val r = reminder(delay = 0)
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Snooze)

      coVerify(exactly = 1) { snoozeReminderUseCase(r, 30) }
    }

  @Test
  fun `onCustomSnooze snoozes for the given number of minutes`() =
    runTest {
      val r = reminder()
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onCustomSnooze(45)

      coVerify(exactly = 1) { snoozeReminderUseCase(r, 45) }
    }

  @Test
  fun `onActionClick ShowNotification completes the reminder, notifies, and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.ShowNotification)

      coVerify(exactly = 1) { completeReminderUseCase(r) }
      coVerify(exactly = 1) { notifier.notify(any(), any()) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SendSms with a message posts SendSms`() =
    runTest {
      // Reminder.BY_DATE (10) + ReminderType.Kind.SMS (2) = SMS-flavored date reminder.
      val r = reminder(type = Reminder.BY_DATE + 2, to = "555", summary = "Call me back")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SendSms)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.SendSms("555", "Call me back"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SendSms with a blank message posts Finish instead`() =
    runTest {
      val r = reminder(type = Reminder.BY_DATE + 2, to = "555", summary = "")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SendSms)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick OpenApp for a BY_DATE_APP reminder posts OpenApp`() =
    runTest {
      val r = reminder(type = Reminder.BY_DATE_APP, target = "com.example.app")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.OpenApp)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.OpenApp("com.example.app"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick OpenUrl for a BY_DATE_LINK reminder posts OpenLink`() =
    runTest {
      val r = reminder(type = Reminder.BY_DATE_LINK, target = "https://example.com")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.OpenUrl)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.OpenLink("https://example.com"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SendEmail for a BY_DATE_EMAIL reminder posts SendEmail`() =
    runTest {
      val r =
        reminder(type = Reminder.BY_DATE_EMAIL, to = "a@b.com", summary = "Hi").apply {
          subject = "Subject"
          attachmentFile = "file.txt"
        }
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SendEmail)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.SendEmail("a@b.com", "Subject", "Hi", "file.txt"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick MakeCall posts MakeCall when the target is a phone number`() =
    runTest {
      val r = reminder(type = Reminder.BY_DATE, target = "+1 555-0100")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.MakeCall)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.MakeCall("+1 555-0100"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick MakeCall posts Finish when the target is not a phone number`() =
    runTest {
      val r = reminder(type = Reminder.BY_DATE, target = "not-a-phone")
      coEvery { reminderRepository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.MakeCall)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onTodoItemClick toggles the matching task, saves, and refreshes state`() =
    runTest {
      val task = ShopItem(uuId = "s1", summary = "Milk", isChecked = false, createTime = "")
      val r = reminder(shoppings = listOf(task))
      coEvery { reminderRepository.getById("42") } returns r
      val refreshedState = screenState.copy(id = "42-refreshed")
      coEvery { getReminderActionScreenStateUseCase(match { it.shoppings.first().isChecked }) } returns
        refreshedState
      val viewModel = createViewModel()

      viewModel.onTodoItemClick("s1")

      coVerify(exactly = 1) { saveReminderUseCase(match { it.shoppings.first().isChecked }) }
      assertEquals(refreshedState, viewModel.state.getOrAwaitValue())
    }
}
