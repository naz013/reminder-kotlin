package com.elementary.tasks.settings.birthday

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.settings.birthday.work.CheckBirthdaysTask
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.SystemInfo
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
  private val prefs = mockk<Prefs>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val jobScheduler = mockk<JobScheduler>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val workScheduler = mockk<WorkScheduler>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()

  private lateinit var viewModel: BirthdaySettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.isBirthdayReminderEnabled } returns false
    every { prefs.daysToBirthday } returns 1
    every { prefs.hapticsEnabled } returns true
    every { prefs.birthdayPriority } returns 2
    every { prefs.is24HourFormat } returns true
    every { prefs.isBirthdayInWidgetEnabled } returns false
    every { prefs.birthdayDurationInDays } returns 3
    every { prefs.isBirthdayPermanentEnabled } returns false
    every { prefs.isBirthdayGlobalEnabled } returns false
    every { prefs.isBirthdayLedEnabled } returns false
    every { prefs.birthdayLedColor } returns 0
    every { prefs.isContactBirthdaysEnabled } returns false
    every { prefs.isContactAutoCheckEnabled } returns false
    every { systemInfo.hasLedIndication } returns true
    every { dateTimeManager.getBirthdayLocalTime() } returns LocalTime.of(9, 0)
    every { dateTimeManager.getBirthdayVisualTime() } returns "9:00 AM"
    every { dateTimeManager.to24HourString(any()) } returns "09:00"

    viewModel =
      BirthdaySettingsViewModel(
        prefs = prefs,
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
    every { prefs.isBirthdayReminderEnabled } returns false

    viewModel.onReminderToggle()

    verify { prefs.isBirthdayReminderEnabled = true }
    verify { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onReminderToggle disables the reminder and cancels the daily birthday job`() {
    every { prefs.isBirthdayReminderEnabled } returns true

    viewModel.onReminderToggle()

    verify { prefs.isBirthdayReminderEnabled = false }
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

    verify { prefs.daysToBirthday = 4 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onDaysToBirthdayConfirm is a no-op without an active dialog`() {
    viewModel.onDaysToBirthdayConfirm()

    verify(exactly = 0) { prefs.daysToBirthday = any() }
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

    verify { prefs.birthdayPriority = 3 }
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
    every { prefs.isBirthdayReminderEnabled } returns true

    viewModel.onTimeSelected(LocalTime.of(10, 30))

    verify { prefs.birthdayTime = "09:00" }
    verify { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onTimeSelected persists the time without rescheduling when the reminder is disabled`() {
    every { prefs.isBirthdayReminderEnabled } returns false

    viewModel.onTimeSelected(LocalTime.of(10, 30))

    verify { prefs.birthdayTime = "09:00" }
    verify(exactly = 0) { jobScheduler.scheduleDailyBirthday() }
  }

  @Test
  fun `onWidgetToggle flips the widget pref and refreshes widgets`() {
    viewModel.onWidgetToggle()

    verify { prefs.isBirthdayInWidgetEnabled = true }
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

    verify { prefs.birthdayDurationInDays = 6 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onPermanentToggle enables permanent notification and schedules it`() {
    every { prefs.isBirthdayPermanentEnabled } returns false

    viewModel.onPermanentToggle()

    verify { prefs.isBirthdayPermanentEnabled = true }
    verify { jobScheduler.scheduleBirthdayPermanent() }
    val event =
      viewModel.navigationEvent.value?.peekContent() as? BirthdaySettingsEvent.UpdatePermanentNotificationVisibility
    assertEquals(true, event?.visible)
  }

  @Test
  fun `onPermanentToggle disables permanent notification and cancels it`() {
    every { prefs.isBirthdayPermanentEnabled } returns true

    viewModel.onPermanentToggle()

    verify { prefs.isBirthdayPermanentEnabled = false }
    verify { jobScheduler.cancelBirthdayPermanent() }
    val event =
      viewModel.navigationEvent.value?.peekContent() as? BirthdaySettingsEvent.UpdatePermanentNotificationVisibility
    assertEquals(false, event?.visible)
  }

  @Test
  fun `onGlobalToggle flips the global pref`() {
    viewModel.onGlobalToggle()

    verify { prefs.isBirthdayGlobalEnabled = true }
  }

  @Test
  fun `onLedToggle flips the led pref`() {
    viewModel.onLedToggle()

    verify { prefs.isBirthdayLedEnabled = true }
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

    verify { prefs.birthdayLedColor = 4 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onUseContactsToggle enables contact scanning and enqueues the check task`() {
    every { prefs.isContactBirthdaysEnabled } returns false

    viewModel.onUseContactsToggle()

    verify { prefs.isContactBirthdaysEnabled = true }
    verify {
      workScheduler.enqueue(
        WorkRequest(taskKey = CheckBirthdaysTask.TASK_KEY, tag = CheckBirthdaysTask.TASK_KEY),
      )
    }
  }

  @Test
  fun `onUseContactsToggle disables contact scanning and cancels the check job`() {
    every { prefs.isContactBirthdaysEnabled } returns true

    viewModel.onUseContactsToggle()

    verify { prefs.isContactBirthdaysEnabled = false }
    verify { jobScheduler.cancelBirthdaysCheck() }
  }

  @Test
  fun `onAutoScanToggle turning the pref on cancels the periodic check`() {
    every { prefs.isContactAutoCheckEnabled } returns false

    viewModel.onAutoScanToggle()

    verify { prefs.isContactAutoCheckEnabled = true }
    verify { jobScheduler.cancelBirthdaysCheck() }
  }

  @Test
  fun `onAutoScanToggle turning the pref off schedules the periodic check`() {
    every { prefs.isContactAutoCheckEnabled } returns true

    viewModel.onAutoScanToggle()

    verify { prefs.isContactAutoCheckEnabled = false }
    verify { jobScheduler.scheduleBirthdaysCheck() }
  }

  @Test
  fun `onDialogDismiss clears the active dialog`() {
    viewModel.onHomeDaysClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }
}
