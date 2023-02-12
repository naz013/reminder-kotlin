package com.elementary.tasks.monthview

import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.day_view.day.EventModel
import hirondelle.date4j.DateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import timber.log.Timber

class CalendarViewModel(
  dayViewProvider: DayViewProvider,
  dispatcherProvider: DispatcherProvider,
  birthdaysDao: BirthdaysDao,
  reminderDao: ReminderDao,
  private val prefs: Prefs,
  private val contextProvider: ContextProvider,
  private val dateTimeManager: DateTimeManager
) : BaseProgressViewModel(dispatcherProvider) {

  private var reminderData = listOf<EventModel>()
  private var birthdayData = listOf<EventModel>()

  private var lastMonthPagerItem: MonthPagerItem? = null
  private var monthJob: Job? = null
  private var dayJob: Job? = null

  private val _events = mutableLiveDataOf<List<EventModel>>()
  val events = _events.toLiveData()

  private val _map = mutableLiveDataOf<Map<DateTime, EventsCursor>>()
  val map = _map.toLiveData()

  private val birthdays = birthdaysDao.loadAll()
  private val reminders = reminderDao.loadType(active = true, removed = false)

  private val birthdayObserver: Observer<in List<Birthday>> = Observer { list ->
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdayData = list.map { dayViewProvider.toEventModel(it) }
      onDataChanged()
    }
  }
  private val reminderObserver: Observer<in List<Reminder>> = Observer {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderData = dayViewProvider.loadReminders(prefs.isFutureEventEnabled, it)
      onDataChanged()
    }
  }

  init {
    birthdays.observeForever(birthdayObserver)
    if (prefs.isRemindersInCalendarEnabled) {
      reminders.observeForever(reminderObserver)
    }
  }

  fun onDateLongClicked(date: LocalDate) {
    this.dayJob?.cancel()
    this.dayJob = viewModelScope.launch(dispatcherProvider.default()) {
      Timber.d("find: $date")
      val list = getData().filter { it.isSameDate(date) }
      Timber.d("find: found -> ${list.size}")
      withUIContext { _events.postValue(list) }
    }
  }

  fun find(monthPagerItem: MonthPagerItem) {
    this.lastMonthPagerItem = monthPagerItem
    runSearch(monthPagerItem)
  }

  private fun onDataChanged() {
    lastMonthPagerItem?.also { runSearch(it) }
  }

  private fun EventModel.isSameDate(date: LocalDate): Boolean {
    return this.year == date.year && this.monthValue == date.monthValue &&
      this.day == date.dayOfMonth
  }

  private fun runSearch(monthPagerItem: MonthPagerItem, sort: Boolean = false) {
    this.monthJob?.cancel()
    this.monthJob = viewModelScope.launch(dispatcherProvider.default()) {
      val res = ArrayList<EventModel>()
      val list = getData()
      Timber.d("runSearch: $monthPagerItem, size=${list.size}")
      for (item in list) {
        if (item.viewType == EventModel.BIRTHDAY && item.monthValue == monthPagerItem.monthValue) {
          res.add(item)
        } else if (item.monthValue == monthPagerItem.monthValue && item.year == monthPagerItem.year) {
          res.add(item)
        }
      }
      Timber.d("runSearch: found=${res.size}")
      (if (sort) {
        try {
          res.sortedBy { it.getMillis() }.toList()
        } catch (e: Throwable) {
          res
        }
      } else {
        res
      }).let { mapData(it) }
        .also {
          withUIContext { notifyObserver(it) }
        }
    }
  }

  private fun notifyObserver(map: Map<DateTime, EventsCursor>) {
    _map.postValue(map)
  }

  private fun getData(): List<EventModel> {
    val toSearch = mutableListOf<EventModel>()
    toSearch.addAll(birthdayData)
    if (prefs.isRemindersInCalendarEnabled) {
      toSearch.addAll(reminderData)
    }
    return toSearch
  }

  private fun mapData(list: List<EventModel>): Map<DateTime, EventsCursor> {
    val birthdayColor = birthdayColor()
    val reminderColor = reminderColor()

    val map = mutableMapOf<DateTime, EventsCursor>()
    for (model in list) {
      val obj = model.model
      if (obj is Birthday) {
        var date = dateTimeManager.parseBirthdayDate(obj.date)
        val year = LocalDate.now().year
        if (date != null) {
          var i = -1
          while (i < 2) {
            date = date?.withYear(year + 1)
            date?.also { setEvent(it, obj.name, birthdayColor, EventsCursor.Type.BIRTHDAY, map) }
            i++
          }
        }
      } else if (obj is UiReminderListData) {
        val eventTime = obj.due?.localDateTime ?: continue
        setEvent(
          eventTime.toLocalDate(),
          obj.summary,
          reminderColor,
          EventsCursor.Type.REMINDER,
          map
        )
      }
    }
    Timber.d("mapData: $map")
    return map
  }

  private fun setEvent(
    date: LocalDate,
    summary: String,
    color: Int,
    type: EventsCursor.Type,
    map: MutableMap<DateTime, EventsCursor>
  ) {
    val key = DateTime(date.year, date.monthValue, date.dayOfMonth, 0, 0, 0, 0)
    if (map.containsKey(key)) {
      val eventsCursor = map[key] ?: EventsCursor()
      eventsCursor.addEvent(summary, color, type, date)
      map[key] = eventsCursor
    } else {
      val eventsCursor = EventsCursor(summary, color, type, date)
      map[key] = eventsCursor
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
    birthdays.removeObserver(birthdayObserver)
    if (prefs.isRemindersInCalendarEnabled) {
      reminders.removeObserver(reminderObserver)
    }
  }
}
