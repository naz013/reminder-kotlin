package com.elementary.tasks.calendar.data

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class DayLiveData(
  private val dispatcherProvider: DispatcherProvider,
  private val calendarDataEngine: CalendarDataEngine,
  private val calendarDataEngineBroadcast: CalendarDataEngineBroadcast,
  private val scope: CoroutineScope = CoroutineScope(Job())
) : LiveData<List<EventModel>>(), CalendarDataEngineBroadcastCallback {

  private var lastDate: LocalDate? = null

  @MainThread
  fun onDateChanged(date: LocalDate) {
    this.lastDate = date
    loadData(date)
  }

  override fun onActive() {
    super.onActive()
    calendarDataEngineBroadcast.observerEvent(
      parent = this.toString(),
      action = CalendarDataEngineBroadcast.EVENT_READY,
      callback = this
    )
    lastDate?.also { loadData(it) }
  }

  override fun onInactive() {
    super.onInactive()
    calendarDataEngineBroadcast.removeObserver(this.toString())
  }

  override fun invoke() {
    lastDate?.also { loadData(it) }
  }

  private fun loadData(date: LocalDate) {
    scope.launch(dispatcherProvider.default()) {
      calendarDataEngine.getByDateRange(
        dateStart = date,
        dateEnd = date,
        reminderMode = calendarDataEngine.getReminderMode(
          includeReminders = true,
          calculateFuture = true
        )
      )
        .sortedBy { it.millis }
        .also { postValue(it) }
        .also {
          Logger.d("loadData: ${it.size}, date=$date")
        }
    }
  }
}
