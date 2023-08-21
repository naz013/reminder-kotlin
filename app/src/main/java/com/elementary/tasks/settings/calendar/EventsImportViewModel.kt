package com.elementary.tasks.settings.calendar

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.arch.OneWayLiveData
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.EventImportProcessor
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.launch
import timber.log.Timber

class EventsImportViewModel(
  dispatcherProvider: DispatcherProvider,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs,
  private val eventImportProcessor: EventImportProcessor,
  private val updatesHelper: UpdatesHelper
) : BaseProgressViewModel(dispatcherProvider) {

  val calendars = OneWayLiveData<List<SelectableCalendar>>()
  val selectedCalendars = OneWayLiveData<List<Long>>()
  val action = OneWayLiveData<ImportAction>()

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
      calendars.viewModelPost(list)
      selectedCalendars.viewModelPost(prefs.trackCalendarIds.toList())
    }
  }

  private fun import(ids: List<Long>) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val result = eventImportProcessor.importEventsFor(ids)
      Timber.d("import: result=$result")
      withUIContext {
        postInProgress(false)
        if (result.importCount == 0) {
          action.viewModelPost(NoEventsAction)
        } else {
          action.viewModelPost(EventsImportedAction(result.importCount))
          updatesHelper.updateCalendarWidget()
        }
      }
    }
  }

  sealed class ImportAction

  object NoEventsAction : ImportAction()

  data class EventsImportedAction(
    val count: Int
  ) : ImportAction()
}

data class SelectableCalendar(
  val calendar: GoogleCalendarUtils.CalendarItem,
  var isSelected: Boolean = false
)
