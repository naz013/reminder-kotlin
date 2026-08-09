package com.elementary.tasks.settings.reminders

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.VibrationPlayer
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.system.BuildInfo
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
  private val prefs = mockk<Prefs>(relaxed = true)
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
    every { prefs.defaultPriority } returns 2
    every { prefs.moveCompleted } returns false
    every { prefs.isWearEnabled } returns false
    every { prefs.snoozeTime } returns 10
    every { prefs.isNotificationRepeatEnabled } returns false
    every { prefs.notificationRepeatTime } returns 5
    every { prefs.isLedEnabled } returns false
    every { prefs.ledColor } returns 0
    every { prefs.isSbNotificationEnabled } returns false
    every { prefs.isSbIconEnabled } returns false
    every { prefs.isDoNotDisturbEnabled } returns false
    every { prefs.doNotDisturbFrom } returns "22:00"
    every { prefs.doNotDisturbTo } returns "07:00"
    every { prefs.doNotDisturbAction } returns 0
    every { prefs.doNotDisturbIgnore } returns 0
    every { prefs.is24HourFormat } returns true
    every { prefs.hapticsEnabled } returns true
    every { buildInfo.isPro } returns true
    every { systemInfo.hasLedIndication } returns true
    every { systemInfo.hasLocation } returns true
    every { dateTimeManager.toLocalTime(any()) } returns LocalTime.of(22, 0)
    every { dateTimeManager.getTime(any()) } returns "10:00 PM"
    every { dateTimeManager.to24HourString(any()) } returns "08:00"

    viewModel =
      RemindersSettingsViewModel(
        prefs = prefs,
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
  fun `initial state is built from prefs and system info`() {
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
        prefs,
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
        prefs,
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

    verify { prefs.moveCompleted = true }
  }

  @Test
  fun `onWearToggle flips the wear pref`() {
    viewModel.onWearToggle()

    verify { prefs.isWearEnabled = true }
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

    verify { prefs.isNotificationRepeatEnabled = true }
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
    every { prefs.hapticsEnabled } returns false
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

    verify { prefs.snoozeTime = 15 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onSeekConfirm persists the repeat interval and dismisses the dialog`() {
    viewModel.onRepeatIntervalClick()
    viewModel.onSeekValueChange(25)

    viewModel.onSeekConfirm()

    verify { prefs.notificationRepeatTime = 25 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onSeekConfirm is a no-op without an active seek dialog`() {
    viewModel.onSeekConfirm()

    verify(exactly = 0) { prefs.snoozeTime = any() }
  }

  @Test
  fun `onLedToggle flips the led pref`() {
    viewModel.onLedToggle()

    verify { prefs.isLedEnabled = true }
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

    verify { prefs.defaultPriority = 3 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onChoiceOptionSelected persists led color`() {
    viewModel.onLedColorClick()

    viewModel.onChoiceOptionSelected(4)

    verify { prefs.ledColor = 4 }
  }

  @Test
  fun `onChoiceOptionSelected persists do-not-disturb action`() {
    viewModel.onDndActionClick()

    viewModel.onChoiceOptionSelected(1)

    verify { prefs.doNotDisturbAction = 1 }
  }

  @Test
  fun `onChoiceOptionSelected persists do-not-disturb ignore level`() {
    viewModel.onDndIgnoreClick()

    viewModel.onChoiceOptionSelected(5)

    verify { prefs.doNotDisturbIgnore = 5 }
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

    verify(exactly = 0) { prefs.defaultPriority = any() }
  }

  @Test
  fun `onPermanentNotificationToggle enables the notification and shows it`() {
    every { prefs.isSbNotificationEnabled } returns false

    viewModel.onPermanentNotificationToggle()

    verify { prefs.isSbNotificationEnabled = true }
    assertEquals(RemindersSettingsEvent.ShowPermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onPermanentNotificationToggle disables the notification and hides it`() {
    every { prefs.isSbNotificationEnabled } returns true

    viewModel.onPermanentNotificationToggle()

    verify { prefs.isSbNotificationEnabled = false }
    assertEquals(RemindersSettingsEvent.HidePermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onStatusIconToggle flips the pref and always refreshes the notification`() {
    viewModel.onStatusIconToggle()

    verify { prefs.isSbIconEnabled = true }
    assertEquals(RemindersSettingsEvent.ShowPermanentNotification, viewModel.navigationEvent.value?.peekContent())
  }

  @Test
  fun `onDoNotDisturbToggle flips the do-not-disturb pref`() {
    viewModel.onDoNotDisturbToggle()

    verify { prefs.isDoNotDisturbEnabled = true }
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

    verify { prefs.doNotDisturbFrom = "08:00" }
  }

  @Test
  fun `onTimeSelected persists the do-not-disturb to-time`() {
    viewModel.onTimeSelected(DndTimeTarget.TO, LocalTime.of(6, 0))

    verify { prefs.doNotDisturbTo = "08:00" }
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
