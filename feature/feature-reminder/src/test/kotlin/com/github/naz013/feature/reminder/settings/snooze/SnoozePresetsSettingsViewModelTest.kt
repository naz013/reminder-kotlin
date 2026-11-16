package com.github.naz013.feature.reminder.settings.snooze

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalTime

class SnoozePresetsSettingsViewModelTest : BaseTest() {
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()

  private lateinit var viewModel: SnoozePresetsSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { reminderPreferences.enabledSnoozePresets } returns listOf(SnoozePresetType.HOUR_1)
    every { reminderPreferences.snoozeEveningTime } returns "18:00"
    every { reminderPreferences.snoozeMorningTime } returns "08:00"
    every { reminderPreferences.is24HourFormat } returns true
    every { dateTimeManager.toLocalTime(any()) } returns LocalTime.of(22, 0)
    every { dateTimeManager.getTime(any()) } returns "10:00 PM"
    every { dateTimeManager.to24HourString(any()) } returns "07:30"
    viewModel = SnoozePresetsSettingsViewModel(reminderPreferences, textProvider, dateTimeManager)
  }

  @Test
  fun `state exposes enabled presets and formatted times`() {
    val state = viewModel.state.value

    assertEquals(setOf(SnoozePresetType.HOUR_1), state.enabledPresets)
    assertEquals("10:00 PM", state.eveningTimeText)
    assertEquals("10:00 PM", state.morningTimeText)
  }

  @Test
  fun `onPresetToggle enables a disabled preset keeping display order`() {
    every { reminderPreferences.enabledSnoozePresets } returns
      listOf(SnoozePresetType.HOURS_3, SnoozePresetType.THIS_EVENING)

    viewModel.onPresetToggle(SnoozePresetType.MINUTES_15)

    verify {
      reminderPreferences.enabledSnoozePresets =
        listOf(SnoozePresetType.MINUTES_15, SnoozePresetType.HOURS_3, SnoozePresetType.THIS_EVENING)
    }
  }

  @Test
  fun `onPresetToggle disables an enabled preset`() {
    viewModel.onPresetToggle(SnoozePresetType.HOUR_1)

    verify { reminderPreferences.enabledSnoozePresets = emptyList() }
  }

  @Test
  fun `onEveningTimeClick shows a time picker for the evening time`() {
    viewModel.onEveningTimeClick()

    val event = viewModel.events.value?.peekContent() as? SnoozePresetsSettingsEvent.ShowTimePicker
    assertEquals(SnoozeTimeTarget.EVENING, event?.target)
    assertEquals(LocalTime.of(22, 0), event?.time)
    assertEquals(true, event?.is24Hour)
  }

  @Test
  fun `onMorningTimeClick shows a time picker for the morning time`() {
    viewModel.onMorningTimeClick()

    val event = viewModel.events.value?.peekContent() as? SnoozePresetsSettingsEvent.ShowTimePicker
    assertEquals(SnoozeTimeTarget.MORNING, event?.target)
  }

  @Test
  fun `onTimeSelected persists the evening time`() {
    viewModel.onTimeSelected(SnoozeTimeTarget.EVENING, LocalTime.of(7, 30))

    verify { reminderPreferences.snoozeEveningTime = "07:30" }
  }

  @Test
  fun `onTimeSelected persists the morning time`() {
    viewModel.onTimeSelected(SnoozeTimeTarget.MORNING, LocalTime.of(7, 30))

    verify { reminderPreferences.snoozeMorningTime = "07:30" }
  }
}
