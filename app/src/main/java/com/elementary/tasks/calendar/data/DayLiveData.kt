package com.elementary.tasks.calendar.data

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class DayLiveData(
  private val context: ContextProvider,
  private val dispatcherProvider: DispatcherProvider,
  private val calendarDataEngine: CalendarDataEngine,
  private val calendarDataEngineBroadcast: CalendarDataEngineBroadcast,
  private val prefs: Prefs,
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
          includeReminders = prefs.isRemindersInCalendarEnabled,
          calculateFuture = prefs.isFutureEventEnabled
        )
      )
        .sortedBy { it.millis }
        .also { postValue(it) }
        .also {
          Logger.d("loadData: ${it.size}, date=$date")
        }
    }
  }

  private fun mapData(list: List<EventModel>): Map<LocalDate, EventsCursor> {
    val birthdayColor = birthdayColor()
    val reminderColor = reminderColor()

    val map = mutableMapOf<LocalDate, EventsCursor>()
    for (model in list) {
      when (model) {
        is BirthdayEventModel -> {
          setEvent(
            model.model.nextBirthdayDate.toLocalDate(),
            model.model.name,
            birthdayColor,
            EventsCursor.Type.BIRTHDAY,
            map
          )
        }

        is ReminderEventModel -> {
          val eventTime = model.model.due?.localDateTime ?: continue
          setEvent(
            eventTime.toLocalDate(),
            model.model.summary,
            reminderColor,
            EventsCursor.Type.REMINDER,
            map
          )
        }
      }
    }
    return map
  }

  private fun setEvent(
    date: LocalDate,
    summary: String,
    color: Int,
    type: EventsCursor.Type,
    map: MutableMap<LocalDate, EventsCursor>
  ) {
    if (map.containsKey(date)) {
      val eventsCursor = map[date] ?: EventsCursor()
      eventsCursor.addEvent(summary, color, type, date)
      map[date] = eventsCursor
    } else {
      val eventsCursor = EventsCursor(summary, color, type, date)
      map[date] = eventsCursor
    }
  }

  private fun birthdayColor(): Int {
    return ThemeProvider.colorBirthdayCalendar(context.themedContext, prefs.birthdayColor)
  }

  private fun reminderColor(): Int {
    return ThemeProvider.colorReminderCalendar(context.themedContext, prefs.reminderColor)
  }
}
