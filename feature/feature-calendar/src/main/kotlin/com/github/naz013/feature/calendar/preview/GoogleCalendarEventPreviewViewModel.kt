package com.github.naz013.feature.calendar.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleCalendarEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GoogleCalendarEventPreviewViewModel(
  private val id: String,
  private val googleCalendarEventRepository: GoogleCalendarEventRepository,
  private val dismissGoogleCalendarEventUseCase: DismissGoogleCalendarEventUseCase,
  private val dateTimeManager: DateTimeManager,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  val state: StateFlow<GoogleCalendarEventPreviewState> field = MutableStateFlow(GoogleCalendarEventPreviewState())
  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var loadedEvent: GoogleCalendarEvent? = null

  init {
    load()
  }

  fun onDeleteClick() {
    state.update { it.copy(showDeleteOptions = true) }
  }

  fun onDeleteOptionsDismiss() {
    state.update { it.copy(showDeleteOptions = false) }
  }

  fun onDeleteLocalOnly() {
    dismiss(alsoDeleteFromDeviceCalendar = false)
  }

  fun onDeleteFromDeviceCalendarToo() {
    dismiss(alsoDeleteFromDeviceCalendar = true)
  }

  private fun dismiss(alsoDeleteFromDeviceCalendar: Boolean) {
    val current = loadedEvent ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      dismissGoogleCalendarEventUseCase(current, alsoDeleteFromDeviceCalendar)
      withContext(dispatcherProvider.main()) {
        event.value = Event(ViewModelEvent.MoveBack)
      }
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val loaded = googleCalendarEventRepository.getById(id)
      if (loaded == null || loaded.isDismissed) {
        Logger.w(TAG, "Google Calendar event not found or already dismissed: $id")
        withContext(dispatcherProvider.main()) { event.value = Event(ViewModelEvent.MoveBack) }
        return@launch
      }
      loadedEvent = loaded
      withContext(dispatcherProvider.main()) {
        state.update {
          it.copy(
            isLoading = false,
            title = loaded.title,
            calendarName = loaded.calendarName,
            description = loaded.description,
            dateTimeFormatted = formatDateTime(loaded),
            allDay = loaded.allDay,
          )
        }
      }
    }
  }

  private fun formatDateTime(event: GoogleCalendarEvent): String {
    val start = dateTimeManager.utcToLocal(event.startDateTime)
    if (event.allDay) return dateTimeManager.formatCalendarDate(start.toLocalDate())
    return dateTimeManager.getFullDateTime(start)
  }

  internal sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent
  }

  companion object {
    private const val TAG = "GoogleCalendarEventPreviewViewModel"
  }
}
