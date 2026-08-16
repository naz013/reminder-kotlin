package com.github.naz013.feature.reminder.dialog

import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.feature.reminder.actions.ReminderAction
import com.github.naz013.logic.reminder.ReminderNotifier
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeactivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.reminder.v2.ReminderAction as DomainReminderAction
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderActionActivityViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val completeReminderUseCase = mockk<CompleteReminderUseCase>(relaxed = true)
  private val deactivateReminderUseCase = mockk<DeactivateReminderUseCase>(relaxed = true)
  private val snoozeReminderUseCase = mockk<SnoozeReminderUseCase>(relaxed = true)
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val getReminderActionScreenStateUseCase = mockk<CreateReminderActionScreenStateUseCase>()
  private val reminderNotifier = mockk<ReminderNotifier>(relaxed = true)
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
    action: DomainReminderAction = DomainReminderAction.None,
    summary: String = "Buy milk",
    delayMinutes: Int? = null,
    shoppingItems: List<ShopItemV2> = emptyList(),
    attachmentFiles: List<String> = emptyList(),
  ): ReminderV2 =
    ReminderV2(
      uuId = id,
      action = action,
      summary = summary,
      notification = NotificationSettingsOverride(delayMinutes = delayMinutes),
      shoppingItems = shoppingItems,
      attachmentFiles = attachmentFiles,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getReminderActionScreenStateUseCase(any()) } returns screenState
  }

  private fun createViewModel(id: String = "42"): ReminderActionActivityViewModel =
    ReminderActionActivityViewModel(
      id = id,
      reminderV2Repository = reminderV2Repository,
      dispatcherProvider = mockDispatcherProvider(),
      saveReminderUseCase = saveReminderUseCase,
      completeReminderUseCase = completeReminderUseCase,
      deactivateReminderUseCase = deactivateReminderUseCase,
      snoozeReminderUseCase = snoozeReminderUseCase,
      reminderPreferences = reminderPreferences,
      getReminderActionScreenStateUseCase = getReminderActionScreenStateUseCase,
      reminderNotifier = reminderNotifier,
      textProvider = textProvider,
    )

  @Test
  fun `loads the reminder into state on creation`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminder()

      val viewModel = createViewModel()

      assertEquals(screenState, viewModel.state.getOrAwaitValue())
    }

  @Test
  fun `state stays unset when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns null

      val viewModel = createViewModel()

      assertEquals(null, viewModel.state.value)
    }

  @Test
  fun `onActionClick Complete completes the reminder and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Complete)

      coVerify(exactly = 1) { completeReminderUseCase(match { it.uuId == r.uuId }) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick Dismiss deactivates the reminder and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Dismiss)

      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == r.uuId }) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SnoozeCustom shows the snooze dialog without snoozing`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns reminder()
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
      val r = reminder(delayMinutes = 15)
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Snooze)

      coVerify(exactly = 1) { snoozeReminderUseCase(match { it.uuId == r.uuId }, 15) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick Snooze falls back to the default snooze time from prefs when delay is zero`() =
    runTest {
      every { reminderPreferences.snoozeTime } returns 30
      val r = reminder(delayMinutes = 0)
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.Snooze)

      coVerify(exactly = 1) { snoozeReminderUseCase(match { it.uuId == r.uuId }, 30) }
    }

  @Test
  fun `onCustomSnooze snoozes for the given number of minutes`() =
    runTest {
      val r = reminder()
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onCustomSnooze(45)

      coVerify(exactly = 1) { snoozeReminderUseCase(match { it.uuId == r.uuId }, 45) }
    }

  @Test
  fun `onActionClick ShowNotification completes the reminder, notifies, and posts Finish`() =
    runTest {
      val r = reminder()
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.ShowNotification)

      coVerify(exactly = 1) { completeReminderUseCase(match { it.uuId == r.uuId }) }
      coVerify(exactly = 1) { reminderNotifier.showFavoriteNotification(any(), any()) }
      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SendSms with a message posts SendSms`() =
    runTest {
      val r = reminder(action = DomainReminderAction.Sms(target = "555", subject = ""), summary = "Call me back")
      coEvery { reminderV2Repository.getById("42") } returns r
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
      val r = reminder(action = DomainReminderAction.Sms(target = "555", subject = ""), summary = "")
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SendSms)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.Finish,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick OpenApp posts OpenApp for an App action reminder`() =
    runTest {
      val r = reminder(action = DomainReminderAction.App(target = "com.example.app"))
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.OpenApp)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.OpenApp("com.example.app"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick OpenUrl posts OpenLink for a Link action reminder`() =
    runTest {
      val r = reminder(action = DomainReminderAction.Link(target = "https://example.com"))
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.OpenUrl)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.OpenLink("https://example.com"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick SendEmail posts SendEmail with the attachment file path`() =
    runTest {
      val r = reminder(
        action = DomainReminderAction.Email(target = "a@b.com", subject = "Subject"),
        summary = "Hi",
        attachmentFiles = listOf("file.txt"),
      )
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.SendEmail)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.SendEmail("a@b.com", "Subject", "Hi", "file.txt"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick MakeCall posts MakeCall for a Call action reminder`() =
    runTest {
      val r = reminder(action = DomainReminderAction.Call(target = "+1 555-0100"))
      coEvery { reminderV2Repository.getById("42") } returns r
      val viewModel = createViewModel()

      viewModel.onActionClick(ReminderAction.MakeCall)

      assertEquals(
        ReminderActionActivityViewModel.ViewModelEvent.MakeCall("+1 555-0100"),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onActionClick MakeCall posts Finish when the reminder has no actionable action`() =
    runTest {
      val r = reminder(action = DomainReminderAction.None)
      coEvery { reminderV2Repository.getById("42") } returns r
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
      val task = ShopItemV2(uuId = "s1", summary = "Milk", isChecked = false, createdAt = LocalDateTime.now())
      val r = reminder(shoppingItems = listOf(task))
      coEvery { reminderV2Repository.getById("42") } returns r
      val refreshedState = screenState.copy(id = "42-refreshed")
      coEvery { getReminderActionScreenStateUseCase(match { it.shoppingItems.first().isChecked }) } returns
        refreshedState
      val viewModel = createViewModel()

      viewModel.onTodoItemClick("s1")

      coVerify(exactly = 1) { saveReminderUseCase(match { it.shoppingItems.first().isChecked }) }
      assertEquals(refreshedState, viewModel.state.getOrAwaitValue())
    }
}
