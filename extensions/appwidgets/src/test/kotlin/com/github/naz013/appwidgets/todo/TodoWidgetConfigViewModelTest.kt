package com.github.naz013.appwidgets.todo

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodoWidgetConfigViewModelTest {

  private val dispatcher = UnconfinedTestDispatcher()
  private val dispatcherProvider = mockk<DispatcherProvider>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val prefsProvider = mockk<TodoWidgetPrefsProvider>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val reminderRepository = mockk<ReminderV2Repository>()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    every { dispatcherProvider.io() } returns dispatcher
    every { prefsProvider.getReminderId() } returns null
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun item(summary: String, deleted: Boolean = false) = ShopItemV2(
    summary = summary,
    isDeleted = deleted,
    createdAt = LocalDateTime.of(2026, 1, 1, 10, 0)
  )

  private fun reminder(
    id: String,
    action: ReminderAction = ReminderAction.Shopping,
    items: List<ShopItemV2> = listOf(item("Milk"))
  ) = ReminderV2(
    uuId = id,
    summary = "Reminder $id",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)),
    action = action,
    shoppingItems = items
  )

  private fun createViewModel() = TodoWidgetConfigViewModel(
    appWidgetUpdater = appWidgetUpdater,
    dispatcherProvider = dispatcherProvider,
    prefsProvider = prefsProvider,
    analyticsEventSender = analyticsEventSender,
    reminderV2Repository = reminderRepository
  )

  @Test
  fun `lists only shopping reminders that have items`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns listOf(
      reminder("todo"),
      reminder("plain", action = ReminderAction.None),
      reminder("empty", items = emptyList()),
      reminder("only-deleted", items = listOf(item("Gone", deleted = true)))
    )

    val viewModel = createViewModel()

    assertEquals(listOf("todo"), viewModel.state.value.reminders.map { it.id })
  }

  @Test
  fun `requests only active and not removed reminders`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns emptyList()

    createViewModel()

    coVerify(exactly = 1) { reminderRepository.getAll(active = true, removed = false) }
  }

  @Test
  fun `preselects stored reminder when it is still in the list`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("a"), reminder("b"))
    every { prefsProvider.getReminderId() } returns "b"

    val viewModel = createViewModel()

    assertEquals("b", viewModel.state.value.selectedReminderId)
  }

  @Test
  fun `does not preselect stored reminder when it is no longer eligible`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("a"))
    every { prefsProvider.getReminderId() } returns "gone"

    val viewModel = createViewModel()

    assertNull(viewModel.state.value.selectedReminderId)
  }

  @Test
  fun `save without selection emits not selected event and stores nothing`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns emptyList()
    val viewModel = createViewModel()

    viewModel.onSaveClick()

    verify(exactly = 0) { prefsProvider.setReminderId(any()) }
  }

  @Test
  fun `save stores selected reminder and refreshes widgets`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("a"))
    val viewModel = createViewModel()
    viewModel.onReminderSelected("a")

    viewModel.onSaveClick()

    verify(exactly = 1) { prefsProvider.setReminderId("a") }
    verify(exactly = 1) { appWidgetUpdater.updateAllWidgets() }
  }
}
