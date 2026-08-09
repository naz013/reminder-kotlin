package com.elementary.tasks.settings.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.holidaysapi.HolidaySyncScheduler
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CalendarSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val googleCalendarApi: GoogleCalendarApi,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  analyticsEventSender: AnalyticsEventSender,
  private val themeProvider: ThemeProvider,
  private val holidaySyncScheduler: HolidaySyncScheduler,
) : ViewModel() {
  val state: StateFlow<CalendarSettingsState> field = MutableStateFlow(buildState())

  private var selectedCalendarId: Long = -1L
  private var selectedCalendarName: String? = null
  private var calendars: List<GoogleCalendar> = emptyList()
  private var countryOptions: List<CountryOption> = emptyList()

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

  fun onColorOptionSelected(index: Int) {
    val dialog = state.value.dialog as? CalendarSettingsDialog.ColorPicker ?: return
    when (dialog.target) {
      ColorPickerTarget.TODAY -> prefs.todayColor = index
      ColorPickerTarget.REMINDER -> prefs.reminderColor = index
      ColorPickerTarget.BIRTHDAY -> prefs.birthdayColor = index
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
      val selectedPosition = calendars.indexOfFirst { it.id == selectedCalendarId }
      withContext(dispatcherProvider.main()) {
        state.update {
          it.copy(
            dialog = CalendarSettingsDialog.SelectGoogleCalendar(
              calendars = calendars,
              selectedPosition = selectedPosition
            ),
          )
        }
      }
    }
  }

  fun onCalendarReset() {
    selectedCalendarId = -1L
    selectedCalendarName = null
    prefs.googleCalendarReminderId = selectedCalendarId
    refreshState()
  }

  fun onGoogleCalendarOptionSelected(position: Int) {
    val calendar = calendars.getOrNull(position) ?: return
    selectedCalendarId = calendar.id
    selectedCalendarName = calendar.name
    prefs.googleCalendarReminderId = selectedCalendarId
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

  fun onHolidayCountryClick() {
    val options = buildCountryOptions()
    countryOptions = options
    val selectedIndex = options.indexOfFirst { it.code == prefs.holidayCountryCode }.coerceAtLeast(0)
    state.update {
      it.copy(
        dialog = CalendarSettingsDialog.SelectCountry(
          options = options.map { option -> option.label },
          selectedIndex = selectedIndex,
        )
      )
    }
  }

  fun onCountryOptionSelected(index: Int) {
    val country = countryOptions.getOrNull(index) ?: return
    prefs.holidayCountryCode = country.code
    if (prefs.publicHolidaysEnabled) {
      holidaySyncScheduler.syncNow()
    }
    dismissDialog()
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
      selectedCalendarId = prefs.googleCalendarReminderId
      val calendar = googleCalendarApi.getCalendarById(selectedCalendarId)
      selectedCalendarName = calendar?.name
      if (calendar == null) {
        Logger.e(TAG, "Selected calendar not found for id: $selectedCalendarId")
        selectedCalendarId = -1L
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
    val isCalendarSelected = selectedCalendarId != -1L
    return CalendarSettingsState(
      firstDayName = firstDayOptions()[prefs.startDay.coerceIn(0, 1)],
      todayColor = themeProvider.themedColor(prefs.todayColor),
      reminderColor = themeProvider.themedColor(prefs.reminderColor),
      birthdayColor = themeProvider.themedColor(prefs.birthdayColor),
      selectedCalendarName = selectedCalendarName ?: "",
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

  private fun buildCountryOptions(): List<CountryOption> =
    Locale.getISOCountries()
      .map { code -> CountryOption(code, countryLabel(code)) }
      .sortedBy { it.label }

  companion object {
    private const val TAG = "CalendarSettingsViewModel"
  }
}
