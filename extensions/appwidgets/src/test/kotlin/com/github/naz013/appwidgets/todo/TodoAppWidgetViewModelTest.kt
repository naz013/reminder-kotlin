package com.github.naz013.appwidgets.todo

import com.github.naz013.appwidgets.R
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TodoAppWidgetViewModelTest {

  private val prefsProvider = mockk<TodoWidgetPrefsProvider>(relaxed = true)
  private val reminderRepository = mockk<ReminderV2Repository>()

  @Before
  fun setUp() {
    every { prefsProvider.widgetId } returns WIDGET_ID
    every { prefsProvider.getReminderId() } returns REMINDER_ID
  }

  private fun item(
    summary: String,
    checked: Boolean = false,
    deleted: Boolean = false
  ) = ShopItemV2(
    summary = summary,
    isChecked = checked,
    isDeleted = deleted,
    createdAt = LocalDateTime.of(2026, 1, 1, 10, 0)
  )

  private fun reminder(
    summary: String = "Groceries",
    isActive: Boolean = true,
    isRemoved: Boolean = false,
    items: List<ShopItemV2> = listOf(item("Milk"))
  ) = ReminderV2(
    uuId = REMINDER_ID,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)),
    action = ReminderAction.Shopping,
    shoppingItems = items,
    isActive = isActive,
    isRemoved = isRemoved
  )

  private fun createViewModel() = TodoAppWidgetViewModel(
    prefsProvider = prefsProvider,
    reminderV2Repository = reminderRepository
  )

  @Test
  fun `returns empty state when no reminder is stored for the widget`() = runTest {
    every { prefsProvider.getReminderId() } returns null

    val state = createViewModel().getState()

    assertEquals(WIDGET_ID, state.widgetId)
    assertNull(state.reminderId)
    assertEquals(emptyList<Any>(), state.items)
  }

  @Test
  fun `returns empty state when stored reminder no longer exists`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns null

    val state = createViewModel().getState()

    assertEquals(WIDGET_ID, state.widgetId)
    assertNull(state.reminderId)
    assertEquals("", state.title)
    assertEquals(emptyList<Any>(), state.items)
  }

  @Test
  fun `returns empty state when stored reminder is inactive`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns reminder(isActive = false)

    val state = createViewModel().getState()

    assertNull(state.reminderId)
    assertEquals(emptyList<Any>(), state.items)
  }

  @Test
  fun `returns empty state when stored reminder is removed`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns reminder(isRemoved = true)

    val state = createViewModel().getState()

    assertNull(state.reminderId)
    assertEquals(emptyList<Any>(), state.items)
  }

  @Test
  fun `returns reminder title and id when stored reminder is active`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns reminder(summary = "Groceries")

    val state = createViewModel().getState()

    assertEquals(REMINDER_ID, state.reminderId)
    assertEquals("Groceries", state.title)
    assertEquals(WIDGET_ID, state.widgetId)
  }

  @Test
  fun `excludes deleted items from widget items`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns reminder(
      items = listOf(item("Milk"), item("Gone", deleted = true), item("Eggs"))
    )

    val state = createViewModel().getState()

    assertEquals(listOf("Milk", "Eggs"), state.items.map { it.text })
  }

  @Test
  fun `uses checked icon for completed items and unchecked icon otherwise`() = runTest {
    coEvery { reminderRepository.getById(REMINDER_ID) } returns reminder(
      items = listOf(item("Milk", checked = true), item("Eggs", checked = false))
    )

    val state = createViewModel().getState()

    assertEquals(
      listOf(R.drawable.ic_fluent_checkbox_checked, R.drawable.ic_fluent_checkbox_unchecked),
      state.items.map { it.iconRes }
    )
  }

  companion object {
    private const val WIDGET_ID = 42
    private const val REMINDER_ID = "todo-reminder"
  }
}
