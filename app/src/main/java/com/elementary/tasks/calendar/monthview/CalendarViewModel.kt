package com.elementary.tasks.calendar.monthview

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.calendar.BirthdayEventModel
import com.elementary.tasks.calendar.CalendarDataProvider
import com.elementary.tasks.calendar.EventModel
import com.elementary.tasks.calendar.ReminderEventModel
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import timber.log.Timber

class CalendarViewModel(
  private val calendarDataProvider: CalendarDataProvider,
  dispatcherProvider: DispatcherProvider,
  private val prefs: Prefs,
  private val contextProvider: ContextProvider
) : BaseProgressViewModel(dispatcherProvider), CalendarDataProvider.DataChangeObserver {

  private var lastMonthPagerItem: MonthPagerItem? = null
  private var monthJob: Job? = null

  private val _map = mutableLiveDataOf<Map<LocalDate, EventsCursor>>()
  val map = _map.toLiveData()

  var currentDate: LocalDate = LocalDate.now().withDayOfMonth(15)

  init {
    calendarDataProvider.observe(javaClass, this)
  }

  fun find(monthPagerItem: MonthPagerItem) {
    this.lastMonthPagerItem = monthPagerItem
    runSearch(monthPagerItem)
  }

  private fun runSearch(monthPagerItem: MonthPagerItem, sort: Boolean = false) {
    this.monthJob?.cancel()
    this.monthJob = viewModelScope.launch(dispatcherProvider.default()) {
      val list = calendarDataProvider.getByMonth(
        localDate = monthPagerItem.date,
        reminderMode = calendarDataProvider.getReminderMode(
          prefs.isRemindersInCalendarEnabled,
          prefs.isFutureEventEnabled
        )
      )
      Timber.d("runSearch: $monthPagerItem, found=${list.size}")
      val data = if (sort) {
        try {
          list.sortedBy { it.millis }.toList()
        } catch (e: Throwable) {
          list
        }
      } else {
        list
      }
      mapData(data).also {
        withContext(dispatcherProvider.main()) { notifyObserver(it) }
      }
    }
  }

  private fun notifyObserver(map: Map<LocalDate, EventsCursor>) {
    _map.postValue(map)
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
    return ThemeProvider.colorBirthdayCalendar(contextProvider.context, prefs)
  }

  private fun reminderColor(): Int {
    return ThemeProvider.colorReminderCalendar(contextProvider.context, prefs)
  }

  override fun onCleared() {
    super.onCleared()
    calendarDataProvider.removeObserver(javaClass)
  }

  override fun onCalendarDataChanged() {
    lastMonthPagerItem?.also { runSearch(it) }
  }
}
