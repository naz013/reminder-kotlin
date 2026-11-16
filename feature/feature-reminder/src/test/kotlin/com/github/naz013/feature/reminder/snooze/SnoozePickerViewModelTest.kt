package com.github.naz013.feature.reminder.snooze

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.logic.reminder.usecase.SnoozeReminderToPresetUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SnoozePickerViewModelTest : BaseTest() {
  private val repository = mockk<ReminderV2Repository>()
  private val snoozeToPreset = mockk<SnoozeReminderToPresetUseCase>(relaxed = true)
  private val snooze = mockk<SnoozeReminderUseCase>(relaxed = true)
  private val preferences = mockk<ReminderPreferences>(relaxed = true)

  private val reminder =
    ReminderV2(
      uuId = "42",
      summary = "Buy milk",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  private fun createViewModel(
    enabled: List<SnoozePresetType> = SnoozePresetType.DEFAULT_ENABLED,
  ): SnoozePickerViewModel {
    every { preferences.enabledSnoozePresets } returns enabled
    return SnoozePickerViewModel(
      reminderId = "42",
      reminderV2Repository = repository,
      snoozeReminderToPresetUseCase = snoozeToPreset,
      snoozeReminderUseCase = snooze,
      reminderPreferences = preferences,
      dispatcherProvider = mockDispatcherProvider(),
    )
  }

  @Test
  fun `lists only the enabled presets in preference order`() {
    val enabled = listOf(SnoozePresetType.TOMORROW_MORNING, SnoozePresetType.HOUR_1)

    val viewModel = createViewModel(enabled)

    assertEquals(enabled, viewModel.state.value.presets)
    assertEquals(SnoozePickerStep.Presets, viewModel.state.value.step)
  }

  @Test
  fun `snoozes to the chosen preset and reports snoozed`() =
    runTest {
      coEvery { repository.getById("42") } returns reminder
      val viewModel = createViewModel()

      viewModel.onPresetClick(SnoozePresetType.HOUR_1)

      coVerify(exactly = 1) { snoozeToPreset(reminder, SnoozePresetType.HOUR_1) }
      assertEquals(SnoozePickerEvent.Snoozed, viewModel.events.first())
    }

  @Test
  fun `pick time switches to the custom time step without snoozing`() {
    val viewModel = createViewModel()

    viewModel.onPickTimeClick()

    assertEquals(SnoozePickerStep.CustomTime, viewModel.state.value.step)
    coVerify(exactly = 0) { snooze(any(), any()) }
    coVerify(exactly = 0) { snoozeToPreset(any(), any()) }
  }

  @Test
  fun `back returns from custom time to the presets`() {
    val viewModel = createViewModel()
    viewModel.onPickTimeClick()

    viewModel.onBackClick()

    assertEquals(SnoozePickerStep.Presets, viewModel.state.value.step)
  }

  @Test
  fun `custom option snoozes for the given minutes and reports snoozed`() =
    runTest {
      coEvery { repository.getById("42") } returns reminder
      val viewModel = createViewModel()

      viewModel.onCustomMinutesClick(30)

      coVerify(exactly = 1) { snooze(reminder, 30) }
      assertEquals(SnoozePickerEvent.Snoozed, viewModel.events.first())
    }

  @Test
  fun `dismiss finishes without snoozing so the notification stays`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onDismiss()

      assertEquals(SnoozePickerEvent.Finish, viewModel.events.first())
      coVerify(exactly = 0) { snooze(any(), any()) }
      coVerify(exactly = 0) { snoozeToPreset(any(), any()) }
    }

  @Test
  fun `finishes without snoozing when the reminder no longer exists`() =
    runTest {
      coEvery { repository.getById("42") } returns null
      val viewModel = createViewModel()

      viewModel.onPresetClick(SnoozePresetType.MINUTES_15)

      assertEquals(SnoozePickerEvent.Finish, viewModel.events.first())
      coVerify(exactly = 0) { snoozeToPreset(any(), any()) }
    }
}
