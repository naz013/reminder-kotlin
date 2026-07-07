package com.elementary.tasks.settings.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val calendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {
  val state: StateFlow<CalendarSettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<CalendarSettingsEvent>> field = mutableLiveEventOf()

  private val _showSelectGoogleCalendarDialog = mutableLiveEventOf<ShowSelectGoogleCalendarDialog>()
  val showSelectGoogleCalendarDialog = _showSelectGoogleCalendarDialog.toLiveData()

  private var selectedCalendarId: Long = -1L
  private var selectedCalendarName: String? = null
  private var calendars: List<GoogleCalendar> = emptyList()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR_SETTINGS))
    loadSelectedCalendar()
  }

  fun onFirstDayClick() {
    val options = firstDayOptions()
    state.update {
      it.copy(dialog = CalendarSettingsDialog.FirstDay(options = options, selectedIndex = prefs.startDay))
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
    navigationEvent.value =
      Event(
        CalendarSettingsEvent.ShowColorPicker(
          target = ColorPickerTarget.TODAY,
          currentColorIndex = prefs.todayColor,
          title = textProvider.getString(R.string.today_color),
        ),
      )
  }

  fun onReminderColorClick() {
    navigationEvent.value =
      Event(
        CalendarSettingsEvent.ShowColorPicker(
          target = ColorPickerTarget.REMINDER,
          currentColorIndex = prefs.reminderColor,
          title = textProvider.getString(R.string.reminders_color),
        ),
      )
  }

  fun onBirthdayColorClick() {
    navigationEvent.value =
      Event(
        CalendarSettingsEvent.ShowColorPicker(
          target = ColorPickerTarget.BIRTHDAY,
          currentColorIndex = prefs.birthdayColor,
          title = textProvider.getString(R.string.birthdays_color),
        ),
      )
  }

  fun onColorSelected(
    target: ColorPickerTarget,
    colorIndex: Int,
  ) {
    when (target) {
      ColorPickerTarget.TODAY -> prefs.todayColor = colorIndex
      ColorPickerTarget.REMINDER -> prefs.reminderColor = colorIndex
      ColorPickerTarget.BIRTHDAY -> prefs.birthdayColor = colorIndex
    }
    refreshState()
  }

  fun onSelectGoogleCalendarClicked() {
    viewModelScope.launch(dispatcherProvider.default()) {
      calendars = calendarUtils.getCalendarsList().map { GoogleCalendar(id = it.id, name = it.name) }
      if (calendars.isEmpty()) {
        Logger.e(TAG, "No Google Calendars found.")
        return@launch
      }
      val selectedPosition = calendars.indexOfFirst { it.id == selectedCalendarId }
      withContext(dispatcherProvider.main()) {
        _showSelectGoogleCalendarDialog.value =
          Event(
            ShowSelectGoogleCalendarDialog(calendars = calendars, selectedPosition = selectedPosition),
          )
      }
    }
  }

  fun onCalendarReset() {
    selectedCalendarId = -1L
    selectedCalendarName = null
    prefs.googleCalendarReminderId = selectedCalendarId
    refreshState()
  }

  fun onCalendarSelected(position: Int) {
    val calendar = calendars.getOrNull(position) ?: return
    selectedCalendarId = calendar.id
    selectedCalendarName = calendar.name
    prefs.googleCalendarReminderId = selectedCalendarId
    refreshState()
  }

  fun onExportToggle() {
    prefs.addRemindersToGoogleCalendar = !prefs.addRemindersToGoogleCalendar
    refreshState()
  }

  fun onScanToggle() {
    prefs.scanGoogleCalendarEvents = !prefs.scanGoogleCalendarEvents
    refreshState()
  }

  private fun loadSelectedCalendar() {
    viewModelScope.launch(dispatcherProvider.default()) {
      selectedCalendarId = prefs.googleCalendarReminderId
      val calendar = calendarUtils.getCalendarById(selectedCalendarId)
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
      todayColorIndex = prefs.todayColor,
      reminderColorIndex = prefs.reminderColor,
      birthdayColorIndex = prefs.birthdayColor,
      selectedCalendarName = selectedCalendarName ?: "",
      isCalendarSelected = isCalendarSelected,
      isExportChecked = prefs.addRemindersToGoogleCalendar,
      isScanChecked = prefs.scanGoogleCalendarEvents,
    )
  }

  private fun firstDayOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.sunday),
      textProvider.getString(R.string.monday),
    )

  data class ShowSelectGoogleCalendarDialog(
    val calendars: List<GoogleCalendar>,
    val selectedPosition: Int,
  )

  data class GoogleCalendar(
    val id: Long,
    val name: String?,
  )

  companion object {
    private const val TAG = "CalendarSettingsViewModel"
  }
}
