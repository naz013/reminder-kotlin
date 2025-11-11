package com.elementary.tasks.settings.calendar

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val calendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
) : ViewModel(), DefaultLifecycleObserver {

  private val _selectedCalendar = mutableLiveDataOf<GoogleCalendar>()
  val selectedCalendar = _selectedCalendar.toLiveData()

  private val _showSelectGoogleCalendarDialog = mutableLiveEventOf<ShowSelectGoogleCalendarDialog>()
  val showSelectGoogleCalendarDialog = _showSelectGoogleCalendarDialog.toLiveData()

  private var selectedCalendarId: Long = -1L
  private var calendars: List<GoogleCalendar> = emptyList()

  init {
    loadSelectedCalendar()
  }

  fun onSelectGoogleCalendarClicked() {
    viewModelScope.launch(dispatcherProvider.default()) {
      calendars = calendarUtils.getCalendarsList().map {
        GoogleCalendar(
          id = it.id,
          name = it.name
        )
      }
      if (calendars.isEmpty()) {
        Logger.e(TAG, "No Google Calendars found.")
        return@launch
      }
      val selectedPosition = calendars.indexOfFirst { it.id == selectedCalendarId }
      withContext(dispatcherProvider.main()) {
        _showSelectGoogleCalendarDialog.value = Event(
          ShowSelectGoogleCalendarDialog(
            calendars = calendars,
            selectedPosition = selectedPosition
          )
        )
      }
    }
  }

  fun onCalendarReset() {
    selectedCalendarId = -1L
    prefs.googleCalendarReminderId = selectedCalendarId
    _selectedCalendar.value = NO_CALENDAR
  }

  fun onCalendarSelected(position: Int) {
    val calendar = calendars.getOrNull(position) ?: return
    selectedCalendarId = calendar.id
    prefs.googleCalendarReminderId = selectedCalendarId
    _selectedCalendar.value = calendar
  }

  private fun loadSelectedCalendar() {
    viewModelScope.launch(dispatcherProvider.default()) {
      selectedCalendarId = prefs.googleCalendarReminderId
      val calendar = calendarUtils.getCalendarById(selectedCalendarId)
      if (calendar != null) {
        withContext(dispatcherProvider.main()) {
          _selectedCalendar.value = GoogleCalendar(
            id = calendar.id,
            name = calendar.name
          )
        }
      } else {
        Logger.e(TAG, "Selected calendar not found for id: $selectedCalendarId")
        withContext(dispatcherProvider.main()) {
          _selectedCalendar.value = NO_CALENDAR
        }
      }
    }
  }

  data class ShowSelectGoogleCalendarDialog(
    val calendars: List<GoogleCalendar>,
    val selectedPosition: Int
  )

  data class GoogleCalendar(
    val id: Long,
    val name: String?,
  )

  companion object {
    private const val TAG = "CalendarSettingsViewModel"
    private val NO_CALENDAR = GoogleCalendar(
      id = -1L,
      name = null
    )
  }
}
