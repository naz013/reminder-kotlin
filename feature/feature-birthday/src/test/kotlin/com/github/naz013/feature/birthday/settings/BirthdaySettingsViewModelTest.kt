package com.github.naz013.feature.birthday.settings

import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.birthday.settings.work.CheckBirthdaysTask
import com.github.naz013.logic.birthday.BirthdayPreferences
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.platform.SystemInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalTime

class BirthdaySettingsViewModelTest : BaseTest() {
  private val birthdayPreferences = mockk<BirthdayPreferences>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val workScheduler = mockk<WorkScheduler>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()

  private lateinit var viewModel: BirthdaySettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { birthdayPreferences.isBirthdayReminderEnabled } returns false
    every { birthdayPreferences.daysToBirthday } returns 1
    every { birthdayPreferences.hapticsEnabled } returns true
    every { birthdayPreferences.birthdayPriority } returns 2
    every { birthdayPreferences.is24HourFormat } returns true
    every { birthdayPreferences.isBirthdayInWidgetEnabled } returns false
    every { birthdayPreferences.birthdayDurationInDays } returns 3
    every { birthdayPreferences.isBirthdayPermanentEnabled } returns false
    every { birthdayPreferences.isBirthdayGlobalEnabled } returns false
    every { birthdayPreferences.isBirthdayLedEnabled } returns false
    every { birthdayPreferences.birthdayLedColor } returns 0
    every { birthdayPreferences.isContactBirthdaysEnabled } returns false
    every { birthdayPreferences.isContactAutoCheckEnabled } returns false
    every { systemInfo.hasLedIndication } returns true
    every { dateTimeManager.getBirthdayLocalTime() } returns LocalTime.of(9, 0)
    every { dateTimeManager.getBirthdayVisualTime() } returns "9:00 AM"
    every { dateTimeManager.to24HourString(any()) } returns "09:00"

    viewModel =
      BirthdaySettingsViewModel(
        birthdayPreferences = birthdayPreferences,
        textProvider = textProvider,
        jobScheduler = jobScheduler,
        appWidgetUpdater = appWidgetUpdater,
        dateTimeManager = dateTimeManager,
        analyticsEventSender = analyticsEventSender,
        workScheduler = workScheduler,
        systemInfo = systemInfo,
      )
  }

  @Test
  fun `init sends the birthday settings screen analytics event`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.BIRTHDAY_SETTINGS)) }
  }

  @Test
  fun `initial state is built from prefs and system info`() {
    val state = viewModel.state.value

    assertEquals(false, state.isReminderChecked)
    assertEquals(1, state.daysToBirthday)
    assertEquals(false, state.isWidgetChecked)
    assertEquals(false, state.isPermanentChecked)
    assertEquals(false, state.isGlobalChecked)
    assertEquals(false, state.isLedChecked)
    assertEquals(false, state.isUseContactsChecked)
    assertEquals(false, state.isAutoScanChecked)
    assertEquals(false, state.isAutoScanRowEnabled)
    assertNull(state.dialog)
  }

  @Test
  fun `onReminderToggle enables the reminder and schedules the daily birthday job`() {
    every { birthdayPreferences.isBirthdayReminderEnabled } returns false

    viewModel.onReminderToggle()

    verify { birthdayPreferences.isBirthdayReminderEnabled = true }
    verify { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onReminderToggle disables the reminder and cancels the daily birthday job`() {
    every { birthdayPreferences.isBirthdayReminderEnabled } returns true

    viewModel.onReminderToggle()

    verify { birthdayPreferences.isBirthdayReminderEnabled = false }
    verify { jobScheduler.cancelDailyBirthday() }
  }

  @Test
  fun `onDaysToBirthdayClick opens the days-to-birthday dialog seeded with current values`() {
    viewModel.onDaysToBirthdayClick()

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.DaysToBirthday
    assertEquals(1, dialog?.previewValue)
    assertEquals(true, dialog?.hapticFeedbackEnabled)
  }

  @Test
  fun `onDaysToBirthdayPreviewChange updates the preview value`() {
    viewModel.onDaysToBirthdayClick()

    viewModel.onDaysToBirthdayPreviewChange(5)

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.DaysToBirthday
    assertEquals(5, dialog?.previewValue)
  }

  @Test
  fun `onDaysToBirthdayPreviewChange is a no-op for a mismatched dialog type`() {
    viewModel.onHomeDaysClick()

    viewModel.onDaysToBirthdayPreviewChange(5)

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.HomeDays
    assertEquals(3, dialog?.previewValue)
  }

  @Test
  fun `onDaysToBirthdayConfirm persists the preview value and dismisses the dialog`() {
    viewModel.onDaysToBirthdayClick()
    viewModel.onDaysToBirthdayPreviewChange(4)

    viewModel.onDaysToBirthdayConfirm()

    verify { birthdayPreferences.daysToBirthday = 4 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onDaysToBirthdayConfirm is a no-op without an active dialog`() {
    viewModel.onDaysToBirthdayConfirm()

    verify(exactly = 0) { birthdayPreferences.daysToBirthday = any() }
  }

  @Test
  fun `onPriorityClick opens the priority dialog seeded with the current priority`() {
    viewModel.onPriorityClick()

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.Priority
    assertEquals(2, dialog?.selectedIndex)
  }

  @Test
  fun `onPriorityOptionSelected persists the priority and dismisses the dialog`() {
    viewModel.onPriorityClick()

    viewModel.onPriorityOptionSelected(3)

    verify { birthdayPreferences.birthdayPriority = 3 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onReminderTimeClick shows a time picker seeded with the birthday time`() {
    viewModel.onReminderTimeClick()

    val event = viewModel.navigationEvent.value?.peekContent() as? BirthdaySettingsEvent.ShowTimePicker
    assertEquals(LocalTime.of(9, 0), event?.time)
    assertEquals(true, event?.is24Hour)
  }

  @Test
  fun `onTimeSelected persists the time and reschedules when the reminder is enabled`() {
    every { birthdayPreferences.isBirthdayReminderEnabled } returns true

    viewModel.onTimeSelected(LocalTime.of(10, 30))

    verify { birthdayPreferences.birthdayTime = "09:00" }
    verify { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onTimeSelected persists the time without rescheduling when the reminder is disabled`() {
    every { birthdayPreferences.isBirthdayReminderEnabled } returns false

    viewModel.onTimeSelected(LocalTime.of(10, 30))

    verify { birthdayPreferences.birthdayTime = "09:00" }
    verify(exactly = 0) { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onWidgetToggle flips the widget pref and refreshes widgets`() {
    viewModel.onWidgetToggle()

    verify { birthdayPreferences.isBirthdayInWidgetEnabled = true }
    verify { appWidgetUpdater.updateCalendarWidget() }
    verify { appWidgetUpdater.updateAllWidgets() }
  }

  @Test
  fun `onHomeDaysClick opens the home-days dialog seeded with current values`() {
    viewModel.onHomeDaysClick()

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.HomeDays
    assertEquals(3, dialog?.previewValue)
    assertEquals(true, dialog?.hapticFeedbackEnabled)
  }

  @Test
  fun `onHomeDaysPreviewChange updates the preview value`() {
    viewModel.onHomeDaysClick()

    viewModel.onHomeDaysPreviewChange(7)

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.HomeDays
    assertEquals(7, dialog?.previewValue)
  }

  @Test
  fun `onHomeDaysConfirm persists the preview value and dismisses the dialog`() {
    viewModel.onHomeDaysClick()
    viewModel.onHomeDaysPreviewChange(6)

    viewModel.onHomeDaysConfirm()

    verify { birthdayPreferences.birthdayDurationInDays = 6 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onPermanentToggle enables permanent notification and schedules it`() {
    every { birthdayPreferences.isBirthdayPermanentEnabled } returns false

    viewModel.onPermanentToggle()

    verify { birthdayPreferences.isBirthdayPermanentEnabled = true }
    verify { jobScheduler.scheduleBirthdayPermanent() }
    val event =
      viewModel.navigationEvent.value?.peekContent() as? BirthdaySettingsEvent.UpdatePermanentNotificationVisibility
    assertEquals(true, event?.visible)
  }

  @Test
  fun `onPermanentToggle disables permanent notification and cancels it`() {
    every { birthdayPreferences.isBirthdayPermanentEnabled } returns true

    viewModel.onPermanentToggle()

    verify { birthdayPreferences.isBirthdayPermanentEnabled = false }
    verify { jobScheduler.cancelBirthdayPermanent() }
    val event =
      viewModel.navigationEvent.value?.peekContent() as? BirthdaySettingsEvent.UpdatePermanentNotificationVisibility
    assertEquals(false, event?.visible)
  }

  @Test
  fun `onGlobalToggle flips the global pref`() {
    viewModel.onGlobalToggle()

    verify { birthdayPreferences.isBirthdayGlobalEnabled = true }
  }

  @Test
  fun `onLedToggle flips the led pref`() {
    viewModel.onLedToggle()

    verify { birthdayPreferences.isBirthdayLedEnabled = true }
  }

  @Test
  fun `onLedColorClick opens the led-color dialog seeded with the current color`() {
    viewModel.onLedColorClick()

    val dialog = viewModel.state.value.dialog as? BirthdayDialog.LedColor
    assertEquals(0, dialog?.selectedIndex)
  }

  @Test
  fun `onLedColorOptionSelected persists the led color and dismisses the dialog`() {
    viewModel.onLedColorClick()

    viewModel.onLedColorOptionSelected(4)

    verify { birthdayPreferences.birthdayLedColor = 4 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onUseContactsToggle enables contact scanning and enqueues the check task`() {
    every { birthdayPreferences.isContactBirthdaysEnabled } returns false

    viewModel.onUseContactsToggle()

    verify { birthdayPreferences.isContactBirthdaysEnabled = true }
    verify {
      workScheduler.enqueue(
        WorkRequest(taskKey = CheckBirthdaysTask.TASK_KEY, tag = CheckBirthdaysTask.TASK_KEY),
      )
    }
  }

  @Test
  fun `onUseContactsToggle disables contact scanning and cancels the check job`() {
    every { birthdayPreferences.isContactBirthdaysEnabled } returns true

    viewModel.onUseContactsToggle()

    verify { birthdayPreferences.isContactBirthdaysEnabled = false }
    verify { jobScheduler.cancelBirthdaysCheck() }
  }

  @Test
  fun `onAutoScanToggle turning the pref on cancels the periodic check`() {
    every { birthdayPreferences.isContactAutoCheckEnabled } returns false

    viewModel.onAutoScanToggle()

    verify { birthdayPreferences.isContactAutoCheckEnabled = true }
    verify { jobScheduler.cancelBirthdaysCheck() }
  }

  @Test
  fun `onAutoScanToggle turning the pref off schedules the periodic check`() {
    every { birthdayPreferences.isContactAutoCheckEnabled } returns true

    viewModel.onAutoScanToggle()

    verify { birthdayPreferences.isContactAutoCheckEnabled = false }
    verify { jobScheduler.scheduleBirthdaysCheck() }
  }

  @Test
  fun `onDialogDismiss clears the active dialog`() {
    viewModel.onHomeDaysClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }
}
