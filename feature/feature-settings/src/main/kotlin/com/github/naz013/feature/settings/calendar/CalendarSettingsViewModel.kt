package com.github.naz013.feature.settings.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.holidaysapi.HolidaySyncScheduler
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

internal class CalendarSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val googleCalendarApi: GoogleCalendarApi,
  private val prefs: CalendarSettingsPreferences,
  private val textProvider: TextProvider,
  analyticsEventSender: AnalyticsEventSender,
  private val themeProvider: ThemeProvider,
  private val holidaySyncScheduler: HolidaySyncScheduler,
) : ViewModel() {
  // Declared before `state` - its eager `buildState()` call reads these, and Kotlin initializes
  // properties in declaration order, so they must already hold their default values by then.
  private var selectedCalendarIds: Set<Long> = emptySet()
  private var selectedCalendarNames: List<String> = emptyList()
  private var pendingSelectedPositions: Set<Int> = emptySet()
  private var calendars: List<GoogleCalendar> = emptyList()

  val state: StateFlow<CalendarSettingsState> field = MutableStateFlow(buildState())

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR_SETTINGS))
    loadSelectedCalendar()
  }

  fun onFirstDayClick() {
    val options = firstDayOptions()
    state.update {
      it.copy(
        dialog = CalendarSettingsDialog.FirstDay(
          options = options,
          selectedIndex = prefs.startDay
        )
      )
    }
  }

  fun onFirstDayOptionSelected(index: Int) {
    prefs.startDay = index
    dismissDialog()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  fun onTodayColorClick() {
    showColorPicker(
      target = ColorPickerTarget.TODAY,
      currentColorIndex = prefs.todayColor,
      title = textProvider.getString(R.string.today_color),
    )
  }

  fun onReminderColorClick() {
    showColorPicker(
      target = ColorPickerTarget.REMINDER,
      currentColorIndex = prefs.reminderColor,
      title = textProvider.getString(R.string.reminders_color),
    )
  }

  fun onBirthdayColorClick() {
    showColorPicker(
      target = ColorPickerTarget.BIRTHDAY,
      currentColorIndex = prefs.birthdayColor,
      title = textProvider.getString(R.string.birthdays_color),
    )
  }

  fun onCalendarEventColorClick() {
    showColorPicker(
      target = ColorPickerTarget.CALENDAR_EVENT,
      currentColorIndex = prefs.calendarEventColor,
      title = textProvider.getString(R.string.google_calendar_events_color),
    )
  }

  fun onColorOptionSelected(index: Int) {
    val dialog = state.value.dialog as? CalendarSettingsDialog.ColorPicker ?: return
    when (dialog.target) {
      ColorPickerTarget.TODAY -> prefs.todayColor = index
      ColorPickerTarget.REMINDER -> prefs.reminderColor = index
      ColorPickerTarget.BIRTHDAY -> prefs.birthdayColor = index
      ColorPickerTarget.CALENDAR_EVENT -> prefs.calendarEventColor = index
    }
    dismissDialog()
  }

  fun onSelectGoogleCalendarClicked() {
    viewModelScope.launch(dispatcherProvider.default()) {
      calendars =
        googleCalendarApi.getCalendarsList().map { GoogleCalendar(id = it.id, name = it.name) }
      if (calendars.isEmpty()) {
        Logger.e(TAG, "No Google Calendars found.")
        return@launch
      }
      pendingSelectedPositions =
        calendars.withIndex().filter { (_, calendar) -> calendar.id in selectedCalendarIds }
          .mapTo(mutableSetOf()) { it.index }
      withContext(dispatcherProvider.main()) {
        state.update {
          it.copy(
            dialog = CalendarSettingsDialog.SelectGoogleCalendar(
              calendars = calendars,
              selectedPositions = pendingSelectedPositions,
            ),
          )
        }
      }
    }
  }

  fun onCalendarReset() {
    selectedCalendarIds = emptySet()
    selectedCalendarNames = emptyList()
    prefs.selectedGoogleCalendarIds = selectedCalendarIds
    refreshState()
  }

  fun onGoogleCalendarOptionToggled(position: Int) {
    pendingSelectedPositions =
      if (position in pendingSelectedPositions) {
        pendingSelectedPositions - position
      } else {
        pendingSelectedPositions + position
      }
    val dialog = state.value.dialog as? CalendarSettingsDialog.SelectGoogleCalendar ?: return
    state.update { it.copy(dialog = dialog.copy(selectedPositions = pendingSelectedPositions)) }
  }

  fun onGoogleCalendarSelectionConfirmed() {
    val selected = pendingSelectedPositions.mapNotNull { calendars.getOrNull(it) }
    selectedCalendarIds = selected.mapTo(mutableSetOf()) { it.id }
    selectedCalendarNames = selected.mapNotNull { it.name }
    prefs.selectedGoogleCalendarIds = selectedCalendarIds
    dismissDialog()
  }

  fun onExportToggle() {
    prefs.addRemindersToGoogleCalendar = !prefs.addRemindersToGoogleCalendar
    refreshState()
  }

  fun onScanToggle() {
    prefs.scanGoogleCalendarEvents = !prefs.scanGoogleCalendarEvents
    refreshState()
  }

  fun onHolidaysToggle() {
    val newValue = !prefs.publicHolidaysEnabled
    prefs.publicHolidaysEnabled = newValue
    if (newValue) {
      holidaySyncScheduler.enable()
    } else {
      holidaySyncScheduler.disable()
    }
    refreshState()
  }

  /** Called once the user picks a country on the separate holiday-country-picker screen and it
   *  pops back - see `HolidayCountryPickerResultHolder`. */
  fun onHolidayCountryPicked(code: String) {
    prefs.holidayCountryCode = code
    if (prefs.publicHolidaysEnabled) {
      holidaySyncScheduler.syncNow()
    }
    refreshState()
  }

  private fun showColorPicker(
    target: ColorPickerTarget,
    currentColorIndex: Int,
    title: String,
  ) {
    state.update {
      it.copy(
        dialog = CalendarSettingsDialog.ColorPicker(
          target = target,
          title = title,
          selectedIndex = currentColorIndex,
          colors = themeProvider.colorsForSliderThemed(),
          hapticFeedback = prefs.hapticsEnabled,
        )
      )
    }
  }

  private fun loadSelectedCalendar() {
    viewModelScope.launch(dispatcherProvider.default()) {
      selectedCalendarIds = prefs.selectedGoogleCalendarIds
      val resolved = selectedCalendarIds.mapNotNull { googleCalendarApi.getCalendarById(it) }
      selectedCalendarNames = resolved.mapNotNull { it.name }
      val missing = selectedCalendarIds - resolved.mapTo(mutableSetOf()) { it.id }
      if (missing.isNotEmpty()) {
        Logger.e(TAG, "Selected calendars not found for ids: $missing")
        selectedCalendarIds = selectedCalendarIds - missing
        prefs.selectedGoogleCalendarIds = selectedCalendarIds
      }
      withContext(dispatcherProvider.main()) { refreshState() }
    }
  }

  private fun dismissDialog() {
    state.update { buildState().copy(dialog = null) }
  }

  private fun refreshState() {
    state.update { buildState().copy(dialog = it.dialog) }
  }

  private fun buildState(): CalendarSettingsState {
    val isCalendarSelected = selectedCalendarIds.isNotEmpty()
    return CalendarSettingsState(
      firstDayName = firstDayOptions()[prefs.startDay.coerceIn(0, 1)],
      todayColor = themeProvider.themedColor(prefs.todayColor),
      reminderColor = themeProvider.themedColor(prefs.reminderColor),
      birthdayColor = themeProvider.themedColor(prefs.birthdayColor),
      calendarEventColor = themeProvider.themedColor(prefs.calendarEventColor),
      selectedCalendarsLabel = selectedCalendarNames.joinToString(", "),
      isCalendarSelected = isCalendarSelected,
      isExportChecked = prefs.addRemindersToGoogleCalendar,
      isScanChecked = prefs.scanGoogleCalendarEvents,
      isHolidaysEnabled = prefs.publicHolidaysEnabled,
      holidayCountryLabel = countryLabel(prefs.holidayCountryCode),
    )
  }

  private fun firstDayOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.sunday),
      textProvider.getString(R.string.monday),
    )

  private fun countryLabel(code: String): String = Locale("", code).displayCountry.ifBlank { code }

  companion object {
    private const val TAG = "CalendarSettingsViewModel"
  }
}
