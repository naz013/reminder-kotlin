package com.github.naz013.feature.habit.editor

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.GetHabitUseCase
import com.github.naz013.logic.habit.usecase.SaveHabitUseCase
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.preferences.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class HabitEditorViewModelTest : BaseTest() {
  private val now = LocalDateTime.of(2026, 10, 18, 9, 0)
  private val getHabit = mockk<GetHabitUseCase>()
  private val saveHabit = mockk<SaveHabitUseCase>(relaxed = true)
  private val deleteHabit = mockk<DeleteHabitUseCase>(relaxed = true)
  private val nowProvider = mockk<NowDateTimeProvider> {
    every { nowDateTime() } returns now
  }
  private val textProvider = mockk<TextProvider> {
    every { getString(any(), *anyVararg()) } answers {
      "res-${firstArg<Int>()}-" + (args[1] as Array<*>).joinToString("-")
    }
    every { getString(any<Int>()) } answers { "res-${firstArg<Int>()}" }
  }
  private val appPreferences = mockk<AppPreferences> {
    every { hapticsEnabled } returns true
  }

  private fun createViewModel(id: String? = null) = HabitEditorViewModel(
    id = id,
    dispatcherProvider = mockDispatcherProvider(),
    getHabitUseCase = getHabit,
    saveHabitUseCase = saveHabit,
    deleteHabitUseCase = deleteHabit,
    nowDateTimeProvider = nowProvider,
    textProvider = textProvider,
    appPreferences = appPreferences,
  )

  private fun savedHabit(): Habit {
    val slot = slot<Habit>()
    coVerify { saveHabit(capture(slot)) }
    return slot.captured
  }

  @Test
  fun `new habit starts empty, not savable and not deletable`() {
    val state = createViewModel().state.value

    assertEquals("", state.name)
    assertFalse(state.canSave)
    assertFalse(state.canDelete)
    assertFalse(state.canDecreaseTarget)
  }

  @Test
  fun `save with a blank name shows an inline error and does not save`() {
    val viewModel = createViewModel()
    viewModel.onNameChange("   ")

    viewModel.onSaveClick()

    assertNotNull(viewModel.state.value.nameError)
    coVerify(exactly = 0) { saveHabit(any()) }
  }

  @Test
  fun `typing a name clears the error and enables save`() {
    val viewModel = createViewModel()
    viewModel.onSaveClick()

    viewModel.onNameChange("Run")

    assertNull(viewModel.state.value.nameError)
    assertTrue(viewModel.state.value.canSave)
  }

  @Test
  fun `create saves a trimmed habit with the chosen fields`() {
    val viewModel = createViewModel()
    viewModel.onNameChange("  Run  ")
    viewModel.onDescriptionChange("  Morning  ")
    viewModel.onFrequencyChange(HabitFrequency.WEEKLY)
    viewModel.onTargetIncrease()
    viewModel.onTargetIncrease()
    viewModel.onColorSelected(4)
    viewModel.onIconSelected(2)

    viewModel.onSaveClick()

    val habit = savedHabit()
    assertEquals("Run", habit.name)
    assertEquals("Morning", habit.description)
    assertEquals(HabitFrequency.WEEKLY, habit.frequency)
    assertEquals(3, habit.targetCount)
    assertEquals(4, habit.color)
    assertEquals(2, habit.icon)
    assertEquals(now, habit.createdAt)
  }

  @Test
  fun `name longer than the limit is truncated`() {
    val viewModel = createViewModel()

    viewModel.onNameChange("a".repeat(150))

    assertEquals(100, viewModel.state.value.name.length)
  }

  @Test
  fun `blank description is saved as null`() {
    val viewModel = createViewModel()
    viewModel.onNameChange("Run")
    viewModel.onDescriptionChange("   ")

    viewModel.onSaveClick()

    assertNull(savedHabit().description)
  }

  @Test
  fun `target never drops below one or rises above the maximum`() {
    val viewModel = createViewModel()

    viewModel.onTargetDecrease()
    assertFalse(viewModel.state.value.canDecreaseTarget)

    repeat(150) { viewModel.onTargetIncrease() }
    assertFalse(viewModel.state.value.canIncreaseTarget)
    viewModel.onNameChange("Run")
    viewModel.onSaveClick()
    assertEquals(99, savedHabit().targetCount)
  }

  @Test
  fun `edit loads the existing habit into state`() {
    coEvery { getHabit("h-1") } returns Habit(
      id = "h-1",
      name = "Read",
      description = "Evenings",
      frequency = HabitFrequency.WEEKLY,
      targetCount = 4,
      color = 3,
      icon = 5,
      createdAt = now.minusDays(5),
      updatedAt = now.minusDays(5)
    )

    val state = createViewModel("h-1").state.value

    assertEquals("Read", state.name)
    assertEquals("Evenings", state.description)
    assertEquals(HabitFrequency.WEEKLY, state.frequency)
    assertEquals(3, state.colorPosition)
    assertEquals(5, state.iconIndex)
    assertTrue(state.canSave)
    assertTrue(state.canDelete)
    assertTrue(state.canDecreaseTarget)
  }

  @Test
  fun `edit keeps the original id and creation time when saving`() {
    val created = now.minusDays(5)
    coEvery { getHabit("h-1") } returns Habit(id = "h-1", name = "Read", createdAt = created, updatedAt = created)
    val viewModel = createViewModel("h-1")
    viewModel.onNameChange("Read more")

    viewModel.onSaveClick()

    val habit = savedHabit()
    assertEquals("h-1", habit.id)
    assertEquals("Read more", habit.name)
    assertEquals(created, habit.createdAt)
  }

  @Test
  fun `delete asks for confirmation first and only deletes once confirmed`() {
    coEvery { getHabit("h-1") } returns Habit(id = "h-1", name = "Read", createdAt = now, updatedAt = now)
    val viewModel = createViewModel("h-1")

    viewModel.onDeleteClick()

    assertTrue(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 0) { deleteHabit(any()) }

    viewModel.onDeleteConfirmed()

    assertFalse(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 1) { deleteHabit("h-1") }
  }

  @Test
  fun `dismissing the delete dialog keeps the habit`() {
    coEvery { getHabit("h-1") } returns Habit(id = "h-1", name = "Read", createdAt = now, updatedAt = now)
    val viewModel = createViewModel("h-1")
    viewModel.onDeleteClick()

    viewModel.onDeleteDismissed()

    assertFalse(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 0) { deleteHabit(any()) }
  }

  @Test
  fun `delete is ignored for a new habit`() {
    val viewModel = createViewModel()

    viewModel.onDeleteClick()
    viewModel.onDeleteConfirmed()

    assertFalse(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 0) { deleteHabit(any()) }
  }

  @Test
  fun `turning the reminder on asks for notification permission without enabling it yet`() {
    val viewModel = createViewModel()

    viewModel.onReminderToggle(true)

    assertFalse(viewModel.state.value.isReminderEnabled)
    assertEquals(
      HabitEditorViewModel.NavigationEvent.RequestNotificationPermission,
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `granted notification permission enables the reminder`() {
    val viewModel = createViewModel()

    viewModel.onNotificationPermissionGranted()

    assertTrue(viewModel.state.value.isReminderEnabled)
  }

  @Test
  fun `turning the reminder off disables it`() {
    val viewModel = createViewModel()
    viewModel.onNotificationPermissionGranted()

    viewModel.onReminderToggle(false)

    assertFalse(viewModel.state.value.isReminderEnabled)
  }

  @Test
  fun `changing the reminder time updates state`() {
    val viewModel = createViewModel()

    viewModel.onReminderTimeChange(LocalTime.of(7, 30))

    assertEquals(LocalTime.of(7, 30), viewModel.state.value.reminderTime)
  }

  @Test
  fun `only if not done switch updates state`() {
    val viewModel = createViewModel()

    viewModel.onRemindOnlyIfNotDoneChange(false)

    assertFalse(viewModel.state.value.remindOnlyIfNotDone)
  }

  @Test
  fun `save maps enabled reminder time and only if not done to the habit`() {
    val viewModel = createViewModel()
    viewModel.onNameChange("Run")
    viewModel.onNotificationPermissionGranted()
    viewModel.onReminderTimeChange(LocalTime.of(7, 30))
    viewModel.onRemindOnlyIfNotDoneChange(false)

    viewModel.onSaveClick()

    val habit = savedHabit()
    assertEquals(LocalTime.of(7, 30), habit.reminderTime)
    assertFalse(habit.remindOnlyIfNotDone)
  }

  @Test
  fun `save leaves reminder time null when the reminder is off`() {
    val viewModel = createViewModel()
    viewModel.onNameChange("Run")
    viewModel.onReminderTimeChange(LocalTime.of(7, 30))

    viewModel.onSaveClick()

    assertNull(savedHabit().reminderTime)
  }

  @Test
  fun `loading a habit with a reminder enables it and restores the time`() {
    coEvery { getHabit("h-1") } returns Habit(
      id = "h-1",
      name = "Read",
      reminderTime = LocalTime.of(21, 15),
      remindOnlyIfNotDone = false,
      createdAt = now,
      updatedAt = now,
    )

    val state = createViewModel("h-1").state.value

    assertTrue(state.isReminderEnabled)
    assertEquals(LocalTime.of(21, 15), state.reminderTime)
    assertFalse(state.remindOnlyIfNotDone)
  }
}
