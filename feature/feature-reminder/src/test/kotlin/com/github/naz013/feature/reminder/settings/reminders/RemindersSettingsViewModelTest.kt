package com.github.naz013.feature.reminder.settings.reminders

import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.notification.settings.VibrationPlayer
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.reminder.settings.ChoiceDialogKind
import com.github.naz013.feature.reminder.settings.DndTimeTarget
import com.github.naz013.feature.reminder.settings.RemindersSettingsDialog
import com.github.naz013.feature.reminder.settings.RemindersSettingsEvent
import com.github.naz013.feature.reminder.settings.RemindersSettingsViewModel
import com.github.naz013.feature.reminder.settings.SeekDialogKind
import com.github.naz013.platform.SystemInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalTime

class RemindersSettingsViewModelTest : BaseTest() {
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()
  private val buildInfo = mockk<BuildInfo>()
  private val vibrationPlayer = mockk<VibrationPlayer>(relaxed = true)

  private lateinit var viewModel: RemindersSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { reminderPreferences.defaultPriority } returns 2
    every { reminderPreferences.moveCompleted } returns false
    every { reminderPreferences.isWearEnabled } returns false
    every { reminderPreferences.snoozeTime } returns 10
    every { reminderPreferences.isNotificationRepeatEnabled } returns false
    every { reminderPreferences.notificationRepeatTime } returns 5
    every { reminderPreferences.isLedEnabled } returns false
    every { reminderPreferences.ledColor } returns 0
    every { reminderPreferences.isSbNotificationEnabled } returns false
    every { reminderPreferences.isSbIconEnabled } returns false
    every { reminderPreferences.isDoNotDisturbEnabled } returns false
    every { reminderPreferences.doNotDisturbFrom } returns "22:00"
    every { reminderPreferences.doNotDisturbTo } returns "07:00"
    every { reminderPreferences.doNotDisturbAction } returns 0
    every { reminderPreferences.doNotDisturbIgnore } returns 0
    every { reminderPreferences.is24HourFormat } returns true
    every { reminderPreferences.hapticsEnabled } returns true
    every { buildInfo.isPro } returns true
    every { systemInfo.hasLedIndication } returns true
    every { systemInfo.hasLocation } returns true
    every { dateTimeManager.toLocalTime(any()) } returns LocalTime.of(22, 0)
    every { dateTimeManager.getTime(any()) } returns "10:00 PM"
    every { dateTimeManager.to24HourString(any()) } returns "08:00"

    viewModel =
      RemindersSettingsViewModel(
        reminderPreferences = reminderPreferences,
        textProvider = textProvider,
        dateTimeManager = dateTimeManager,
        analyticsEventSender = analyticsEventSender,
        systemInfo = systemInfo,
        buildInfo = buildInfo,
        vibrationPlayer = vibrationPlayer,
      )
  }

  @Test
  fun `init sends the reminders settings screen analytics event`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_SETTINGS)) }
  }

  @Test
  fun `initial state is built from reminderPreferences and system info`() {
    val state = viewModel.state.value

    assertEquals(false, state.isCompletedChecked)
    assertEquals(false, state.isWearChecked)
    assertEquals(false, state.isRepeatChecked)
    assertEquals(false, state.isRepeatIntervalRowEnabled)
    assertEquals(true, state.isLedVisible)
    assertEquals(false, state.isLedChecked)
    assertEquals(false, state.isLedColorRowEnabled)
    assertEquals(false, state.isPermanentNotificationChecked)
    assertEquals(false, state.isStatusIconRowEnabled)
    assertEquals(false, state.isDoNotDisturbChecked)
    assertEquals(true, state.hasLocation)
    assertNull(state.dialog)
  }

  @Test
  fun `isLedVisible is false when the build is not pro`() {
    every { buildInfo.isPro } returns false
    val vm =
      RemindersSettingsViewModel(
        reminderPreferences,
        textProvider,
        dateTimeManager,
        analyticsEventSender,
        systemInfo,
        buildInfo,
        vibrationPlayer,
      )

    assertEquals(false, vm.state.value.isLedVisible)
  }

  @Test
  fun `isInsightsLocked is false when the build is pro`() {
    assertEquals(false, viewModel.state.value.isInsightsLocked)
  }

  @Test
  fun `isInsightsLocked is true when the build is not pro`() {
    every { buildInfo.isPro } returns false
    val vm =
      RemindersSettingsViewModel(
        reminderPreferences,
        textProvider,
        dateTimeManager,
        analyticsEventSender,
        systemInfo,
        buildInfo,
        vibrationPlayer,
      )

    assertEquals(true, vm.state.value.isInsightsLocked)
  }

  @Test
  fun `onInsightsLockedClick sends a feature gate tapped analytics event`() {
    viewModel.onInsightsLockedClick()

    verify { analyticsEventSender.send(FeatureGateTappedEvent(Feature.INSIGHTS)) }
  }

  @Test
  fun `onPresetsClick opens the presets screen`() {
    viewModel.onPresetsClick()

    assertEquals(RemindersSettingsEvent.OpenPresets, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onLocationClick opens location settings`() {
    viewModel.onLocationClick()

    assertEquals(RemindersSettingsEvent.OpenLocationSettings, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onPriorityClick opens a choice dialog seeded with the current priority`() {
    viewModel.onPriorityClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Choice
    assertEquals(ChoiceDialogKind.PRIORITY, dialog?.kind)
    assertEquals(2, dialog?.selectedIndex)
  }

  @Test
  fun `onCompletedToggle flips the move-completed pref`() {
    viewModel.onCompletedToggle()

    verify { reminderPreferences.moveCompleted = true }
  }

  @Test
  fun `onWearToggle flips the wear pref`() {
    viewModel.onWearToggle()

    verify { reminderPreferences.isWearEnabled = true }
  }

  @Test
  fun `onSnoozeClick opens a seek dialog seeded with the current snooze time`() {
    viewModel.onSnoozeClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Seek
    assertEquals(SeekDialogKind.SNOOZE, dialog?.kind)
    assertEquals(10, dialog?.previewValue)
  }

  @Test
  fun `onRepeatToggle flips the repeat pref`() {
    viewModel.onRepeatToggle()

    verify { reminderPreferences.isNotificationRepeatEnabled = true }
  }

  @Test
  fun `onRepeatIntervalClick opens a seek dialog seeded with the repeat interval`() {
    viewModel.onRepeatIntervalClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Seek
    assertEquals(SeekDialogKind.REPEAT_INTERVAL, dialog?.kind)
    assertEquals(5, dialog?.previewValue)
  }

  @Test
  fun `onSeekValueChange updates the preview value and fires haptic feedback on change`() {
    viewModel.onSnoozeClick()

    viewModel.onSeekValueChange(20)

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Seek
    assertEquals(20, dialog?.previewValue)
    assertEquals(RemindersSettingsEvent.HapticFeedback, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onSeekValueChange does not fire haptic feedback when the value is unchanged`() {
    viewModel.onSnoozeClick()

    viewModel.onSeekValueChange(10)

    assertNull(viewModel.navigationEvent.value)
  }

  @Test
  fun `onSeekValueChange does not fire haptic feedback when haptics are disabled`() {
    every { reminderPreferences.hapticsEnabled } returns false
    viewModel.onSnoozeClick()

    viewModel.onSeekValueChange(30)

    assertNull(viewModel.navigationEvent.value)
  }

  @Test
  fun `onSeekValueChange is a no-op without an active seek dialog`() {
    viewModel.onSeekValueChange(30)

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onSeekConfirm persists the snooze time and dismisses the dialog`() {
    viewModel.onSnoozeClick()
    viewModel.onSeekValueChange(15)

    viewModel.onSeekConfirm()

    verify { reminderPreferences.snoozeTime = 15 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onSeekConfirm persists the repeat interval and dismisses the dialog`() {
    viewModel.onRepeatIntervalClick()
    viewModel.onSeekValueChange(25)

    viewModel.onSeekConfirm()

    verify { reminderPreferences.notificationRepeatTime = 25 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onSeekConfirm is a no-op without an active seek dialog`() {
    viewModel.onSeekConfirm()

    verify(exactly = 0) { reminderPreferences.snoozeTime = any() }
  }

  @Test
  fun `onLedToggle flips the led pref`() {
    viewModel.onLedToggle()

    verify { reminderPreferences.isLedEnabled = true }
  }

  @Test
  fun `onLedColorClick opens a choice dialog seeded with the current led color`() {
    viewModel.onLedColorClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Choice
    assertEquals(ChoiceDialogKind.LED_COLOR, dialog?.kind)
  }

  @Test
  fun `onChoiceOptionSelected persists priority and dismisses the dialog`() {
    viewModel.onPriorityClick()

    viewModel.onChoiceOptionSelected(3)

    verify { reminderPreferences.defaultPriority = 3 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onChoiceOptionSelected persists led color`() {
    viewModel.onLedColorClick()

    viewModel.onChoiceOptionSelected(4)

    verify { reminderPreferences.ledColor = 4 }
  }

  @Test
  fun `onChoiceOptionSelected persists do-not-disturb action`() {
    viewModel.onDndActionClick()

    viewModel.onChoiceOptionSelected(1)

    verify { reminderPreferences.doNotDisturbAction = 1 }
  }

  @Test
  fun `onChoiceOptionSelected persists do-not-disturb ignore level`() {
    viewModel.onDndIgnoreClick()

    viewModel.onChoiceOptionSelected(5)

    verify { reminderPreferences.doNotDisturbIgnore = 5 }
  }

  @Test
  fun `onChoiceOptionSelected plays the vibration pattern preset when persisting it`() {
    viewModel.onDefaultVibrationPatternClick()

    viewModel.onChoiceOptionSelected(0)

    verify { vibrationPlayer.play(any()) }
  }

  @Test
  fun `onChoiceOptionSelected is a no-op without an active choice dialog`() {
    viewModel.onChoiceOptionSelected(1)

    verify(exactly = 0) { reminderPreferences.defaultPriority = any() }
  }

  @Test
  fun `onPermanentNotificationToggle enables the notification and shows it`() {
    every { reminderPreferences.isSbNotificationEnabled } returns false

    viewModel.onPermanentNotificationToggle()

    verify { reminderPreferences.isSbNotificationEnabled = true }
    assertEquals(RemindersSettingsEvent.ShowPermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onPermanentNotificationToggle disables the notification and hides it`() {
    every { reminderPreferences.isSbNotificationEnabled } returns true

    viewModel.onPermanentNotificationToggle()

    verify { reminderPreferences.isSbNotificationEnabled = false }
    assertEquals(RemindersSettingsEvent.HidePermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onStatusIconToggle flips the pref and always refreshes the notification`() {
    viewModel.onStatusIconToggle()

    verify { reminderPreferences.isSbIconEnabled = true }
    assertEquals(RemindersSettingsEvent.ShowPermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onDoNotDisturbToggle flips the do-not-disturb pref`() {
    viewModel.onDoNotDisturbToggle()

    verify { reminderPreferences.isDoNotDisturbEnabled = true }
  }

  @Test
  fun `onDndFromClick shows a time picker for the from-time`() {
    viewModel.onDndFromClick()

    val event = viewModel.navigationEvent.value?.peekContent() as? RemindersSettingsEvent.ShowTimePicker
    assertEquals(DndTimeTarget.FROM, event?.target)
  }

  @Test
  fun `onDndToClick shows a time picker for the to-time`() {
    viewModel.onDndToClick()

    val event = viewModel.navigationEvent.value?.peekContent() as? RemindersSettingsEvent.ShowTimePicker
    assertEquals(DndTimeTarget.TO, event?.target)
  }

  @Test
  fun `onTimeSelected persists the do-not-disturb from-time`() {
    viewModel.onTimeSelected(DndTimeTarget.FROM, LocalTime.of(23, 0))

    verify { reminderPreferences.doNotDisturbFrom = "08:00" }
  }

  @Test
  fun `onTimeSelected persists the do-not-disturb to-time`() {
    viewModel.onTimeSelected(DndTimeTarget.TO, LocalTime.of(6, 0))

    verify { reminderPreferences.doNotDisturbTo = "08:00" }
  }

  @Test
  fun `onDndActionClick opens a choice dialog for the dnd action`() {
    viewModel.onDndActionClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Choice
    assertEquals(ChoiceDialogKind.DND_ACTION, dialog?.kind)
  }

  @Test
  fun `onDndIgnoreClick opens a choice dialog for the dnd ignore level`() {
    viewModel.onDndIgnoreClick()

    val dialog = viewModel.state.value.dialog as? RemindersSettingsDialog.Choice
    assertEquals(ChoiceDialogKind.DND_IGNORE, dialog?.kind)
  }

  @Test
  fun `onDialogDismiss clears the active dialog`() {
    viewModel.onPriorityClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }
}
