package com.elementary.tasks.settings.calendar

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.EventImportProcessor
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch

class EventsImportViewModel(
  dispatcherProvider: DispatcherProvider,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs,
  private val eventImportProcessor: EventImportProcessor,
  private val updatesHelper: UpdatesHelper
) : BaseProgressViewModel(dispatcherProvider) {

  private val _calendars = mutableLiveDataOf<List<SelectableCalendar>>()
  val calendars = _calendars.toLiveData()

  private val _selectedCalendars = mutableLiveDataOf<List<Long>>()
  val selectedCalendars = _selectedCalendars.toLiveData()

  private val _action = mutableLiveDataOf<ImportAction>()
  val action = _action.toLiveData()

  fun importEvents(calendarIds: List<Long>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val isEnabled = prefs.isCalendarEnabled
      if (!isEnabled) {
        prefs.isCalendarEnabled = true
        prefs.defaultCalendarId = calendarIds[0]
      }
      prefs.trackCalendarIds = calendarIds.toTypedArray()
      import(calendarIds)
    }
  }

  fun loadCalendars() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = googleCalendarUtils.getCalendarsList().map { SelectableCalendar(it) }
      _calendars.postValue(list)
      _selectedCalendars.postValue(prefs.trackCalendarIds.toList())
    }
  }

  private fun import(ids: List<Long>) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val result = eventImportProcessor.importEventsFor(ids)
      Logger.d("import: result=$result")
      withUIContext {
        postInProgress(false)
        if (result.importCount == 0) {
          _action.postValue(NoEventsAction)
        } else {
          _action.postValue(EventsImportedAction(result.importCount))
          updatesHelper.updateCalendarWidget()
        }
      }
    }
  }

  sealed class ImportAction

  data object NoEventsAction : ImportAction()

  data class EventsImportedAction(
    val count: Int
  ) : ImportAction()
}

data class SelectableCalendar(
  val calendar: GoogleCalendarUtils.CalendarItem,
  var isSelected: Boolean = false
)
