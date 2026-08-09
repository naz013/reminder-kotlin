package com.elementary.tasks.settings.calendar

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.googlecalendar.CalendarItem
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.holidaysapi.HolidaySyncScheduler
import com.github.naz013.ui.common.theme.ThemeProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalendarSettingsViewModelTest : BaseTest() {
  private val calendarUtils = mockk<GoogleCalendarApi>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>()
  private val holidaySyncScheduler = mockk<HolidaySyncScheduler>(relaxed = true)

  private lateinit var viewModel: CalendarSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.startDay } returns 0
    every { prefs.todayColor } returns 1
    every { prefs.reminderColor } returns 2
    every { prefs.birthdayColor } returns 3
    every { prefs.googleCalendarReminderId } returns -1L
    every { prefs.addRemindersToGoogleCalendar } returns false
    every { prefs.scanGoogleCalendarEvents } returns false
    every { prefs.hapticsEnabled } returns true
    every { prefs.publicHolidaysEnabled } returns false
    every { prefs.holidayCountryCode } returns "US"
    every { themeProvider.themedColor(any()) } returns Color.Red
    every { themeProvider.colorsForSliderThemed() } returns listOf(Color.Red, Color.Blue)
    every { calendarUtils.getCalendarById(any()) } returns null

    viewModel =
      CalendarSettingsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        googleCalendarApi = calendarUtils,
        prefs = prefs,
        textProvider = textProvider,
        analyticsEventSender = analyticsEventSender,
        themeProvider = themeProvider,
        holidaySyncScheduler = holidaySyncScheduler,
      )
  }

  @Test
  fun `init sends the calendar settings screen analytics event`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR_SETTINGS)) }
  }

  @Test
  fun `init leaves no calendar selected when none is found for the stored id`() {
    assertEquals("", viewModel.state.value.selectedCalendarName)
    assertEquals(false, viewModel.state.value.isCalendarSelected)
  }

  @Test
  fun `init selects the calendar found for the stored id`() {
    every { prefs.googleCalendarReminderId } returns 7L
    every { calendarUtils.getCalendarById(7L) } returns CalendarItem("Work", 7L)

    val vm = CalendarSettingsViewModel(
      mockDispatcherProvider(), calendarUtils, prefs, textProvider, analyticsEventSender, themeProvider, holidaySyncScheduler
    )

    assertEquals("Work", vm.state.value.selectedCalendarName)
    assertEquals(true, vm.state.value.isCalendarSelected)
  }

  @Test
  fun `onFirstDayClick opens the first-day dialog seeded with the current selection`() {
    viewModel.onFirstDayClick()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.FirstDay
    assertEquals(0, dialog?.selectedIndex)
  }

  @Test
  fun `onFirstDayOptionSelected persists the choice and dismisses the dialog`() {
    viewModel.onFirstDayClick()

    viewModel.onFirstDayOptionSelected(1)

    verify { prefs.startDay = 1 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onDialogDismiss clears the active dialog`() {
    viewModel.onFirstDayClick()

    viewModel.onDialogDismiss()

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onTodayColorClick opens a color picker for the today target`() {
    viewModel.onTodayColorClick()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.ColorPicker
    assertEquals(ColorPickerTarget.TODAY, dialog?.target)
    assertEquals(1, dialog?.selectedIndex)
  }

  @Test
  fun `onReminderColorClick opens a color picker for the reminder target`() {
    viewModel.onReminderColorClick()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.ColorPicker
    assertEquals(ColorPickerTarget.REMINDER, dialog?.target)
    assertEquals(2, dialog?.selectedIndex)
  }

  @Test
  fun `onBirthdayColorClick opens a color picker for the birthday target`() {
    viewModel.onBirthdayColorClick()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.ColorPicker
    assertEquals(ColorPickerTarget.BIRTHDAY, dialog?.target)
    assertEquals(3, dialog?.selectedIndex)
  }

  @Test
  fun `onColorOptionSelected persists the today color and dismisses the dialog`() {
    viewModel.onTodayColorClick()

    viewModel.onColorOptionSelected(5)

    verify { prefs.todayColor = 5 }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onColorOptionSelected persists the reminder color`() {
    viewModel.onReminderColorClick()

    viewModel.onColorOptionSelected(6)

    verify { prefs.reminderColor = 6 }
  }

  @Test
  fun `onColorOptionSelected persists the birthday color`() {
    viewModel.onBirthdayColorClick()

    viewModel.onColorOptionSelected(7)

    verify { prefs.birthdayColor = 7 }
  }

  @Test
  fun `onColorOptionSelected is a no-op without an active color picker dialog`() {
    viewModel.onColorOptionSelected(5)

    verify(exactly = 0) { prefs.todayColor = any() }
  }

  @Test
  fun `onSelectGoogleCalendarClicked opens a dialog listing the available calendars`() {
    every { calendarUtils.getCalendarsList() } returns
      listOf(
        CalendarItem("Home", 1L),
        CalendarItem("Work", 2L),
      )

    viewModel.onSelectGoogleCalendarClicked()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.SelectGoogleCalendar
    assertEquals(2, dialog?.calendars?.size)
  }

  @Test
  fun `onSelectGoogleCalendarClicked does not open a dialog when no calendars are found`() {
    every { calendarUtils.getCalendarsList() } returns emptyList()

    viewModel.onSelectGoogleCalendarClicked()

    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onCalendarReset clears the selected calendar`() {
    every { prefs.googleCalendarReminderId } returns 7L
    every { calendarUtils.getCalendarById(7L) } returns CalendarItem("Work", 7L)
    val vm = CalendarSettingsViewModel(
      mockDispatcherProvider(), calendarUtils, prefs, textProvider, analyticsEventSender, themeProvider, holidaySyncScheduler
    )

    vm.onCalendarReset()

    verify { prefs.googleCalendarReminderId = -1L }
    assertEquals("", vm.state.value.selectedCalendarName)
    assertEquals(false, vm.state.value.isCalendarSelected)
  }

  @Test
  fun `onGoogleCalendarOptionSelected persists the chosen calendar and dismisses the dialog`() {
    every { calendarUtils.getCalendarsList() } returns
      listOf(
        CalendarItem("Home", 1L),
        CalendarItem("Work", 2L),
      )
    viewModel.onSelectGoogleCalendarClicked()

    viewModel.onGoogleCalendarOptionSelected(1)

    verify { prefs.googleCalendarReminderId = 2L }
    assertEquals("Work", viewModel.state.value.selectedCalendarName)
    assertEquals(true, viewModel.state.value.isCalendarSelected)
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onGoogleCalendarOptionSelected is a no-op for an out-of-range position`() {
    every { calendarUtils.getCalendarsList() } returns
      listOf(CalendarItem("Home", 1L))
    viewModel.onSelectGoogleCalendarClicked()

    viewModel.onGoogleCalendarOptionSelected(9)

    verify(exactly = 0) { prefs.googleCalendarReminderId = any() }
  }

  @Test
  fun `onExportToggle flips the export pref`() {
    viewModel.onExportToggle()

    verify { prefs.addRemindersToGoogleCalendar = true }
  }

  @Test
  fun `onScanToggle flips the scan pref`() {
    viewModel.onScanToggle()

    verify { prefs.scanGoogleCalendarEvents = true }
  }

  @Test
  fun `onHolidaysToggle enables the pref and schedules sync when turning on`() {
    every { prefs.publicHolidaysEnabled } returns false

    viewModel.onHolidaysToggle()

    verify { prefs.publicHolidaysEnabled = true }
    verify { holidaySyncScheduler.enable() }
  }

  @Test
  fun `onHolidaysToggle disables the pref and cancels sync when turning off`() {
    every { prefs.publicHolidaysEnabled } returns true

    viewModel.onHolidaysToggle()

    verify { prefs.publicHolidaysEnabled = false }
    verify { holidaySyncScheduler.disable() }
  }

  @Test
  fun `onHolidayCountryClick opens the country dialog seeded with the current selection`() {
    every { prefs.holidayCountryCode } returns "US"

    viewModel.onHolidayCountryClick()

    val dialog = viewModel.state.value.dialog as? CalendarSettingsDialog.SelectCountry
    assertEquals(usLabel(), dialog?.options?.get(dialog.selectedIndex))
  }

  @Test
  fun `onCountryOptionSelected persists the chosen country and dismisses the dialog`() {
    every { prefs.holidayCountryCode } returns "US"
    every { prefs.publicHolidaysEnabled } returns false
    viewModel.onHolidayCountryClick()
    val dialog = viewModel.state.value.dialog as CalendarSettingsDialog.SelectCountry
    val franceIndex = dialog.options.indexOf(frLabel())

    viewModel.onCountryOptionSelected(franceIndex)

    verify { prefs.holidayCountryCode = "FR" }
    assertNull(viewModel.state.value.dialog)
  }

  @Test
  fun `onCountryOptionSelected re-syncs when holidays are already enabled`() {
    every { prefs.holidayCountryCode } returns "US"
    every { prefs.publicHolidaysEnabled } returns true
    viewModel.onHolidayCountryClick()
    val dialog = viewModel.state.value.dialog as CalendarSettingsDialog.SelectCountry
    val franceIndex = dialog.options.indexOf(frLabel())

    viewModel.onCountryOptionSelected(franceIndex)

    verify { holidaySyncScheduler.syncNow() }
  }

  @Test
  fun `onCountryOptionSelected does not re-sync when holidays are disabled`() {
    every { prefs.holidayCountryCode } returns "US"
    every { prefs.publicHolidaysEnabled } returns false
    viewModel.onHolidayCountryClick()
    val dialog = viewModel.state.value.dialog as CalendarSettingsDialog.SelectCountry
    val franceIndex = dialog.options.indexOf(frLabel())

    viewModel.onCountryOptionSelected(franceIndex)

    verify(exactly = 0) { holidaySyncScheduler.syncNow() }
  }

  // Country display names are resolved via java.util.Locale against the JVM's default locale,
  // which varies by machine - compute expectations the same way rather than hardcoding English
  // names like "France", so this test doesn't depend on the CI/dev machine's default locale.
  private fun usLabel(): String = java.util.Locale("", "US").displayCountry
  private fun frLabel(): String = java.util.Locale("", "FR").displayCountry

  @Test
  fun `onCountryOptionSelected is a no-op for an out-of-range index`() {
    viewModel.onHolidayCountryClick()

    viewModel.onCountryOptionSelected(9999)

    verify(exactly = 0) { prefs.holidayCountryCode = any() }
  }
}
