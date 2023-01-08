package com.elementary.tasks.month_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.day_view.day.EventModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class MonthViewViewModel(
  dayViewProvider: DayViewProvider,
  dispatcherProvider: DispatcherProvider,
  private val birthdaysDao: BirthdaysDao,
  private val reminderDao: ReminderDao,
  private val prefs: Prefs
) : BaseProgressViewModel(dispatcherProvider) {

  private val liveData: MonthViewLiveData = MonthViewLiveData(dayViewProvider)
  private var _events: MutableLiveData<Pair<MonthPagerItem, List<EventModel>>> = MutableLiveData()
  var events: LiveData<Pair<MonthPagerItem, List<EventModel>>> = _events

  fun findEvents(item: MonthPagerItem) {
    Timber.d("findEvents: $item")
    liveData.findEvents(item, false) { eventsPagerItem, list ->
      _events.postValue(Pair(eventsPagerItem, list))
    }
  }

  private inner class MonthViewLiveData(
    private val dayViewProvider: DayViewProvider
  ) : LiveData<Pair<MonthPagerItem, List<EventModel>>>() {

    private val reminderData = ArrayList<EventModel>()
    private val birthdayData = ArrayList<EventModel>()
    private val birthdays = birthdaysDao.loadAll()
    private val reminders = reminderDao.loadType(active = true, removed = false)

    private var monthPagerItem: MonthPagerItem? = null
    private var job: Job? = null
    private var listener: ((MonthPagerItem, List<EventModel>) -> Unit)? = null
    private var sort = false

    private val birthdayObserver: Observer<in List<Birthday>> = Observer { list ->
      viewModelScope.launch(dispatcherProvider.default()) {
        birthdayData.clear()
        birthdayData.addAll(list.map { dayViewProvider.toEventModel(it) })
        repeatSearch()
      }
    }
    private val reminderObserver: Observer<in List<Reminder>> = Observer {
      viewModelScope.launch(dispatcherProvider.default()) {
        if (it != null) {
          reminderData.clear()
          reminderData.addAll(dayViewProvider.loadReminders(prefs.isFutureEventEnabled, it))
          repeatSearch()
        }
      }
    }

    init {
      birthdays.observeForever(birthdayObserver)
      if (prefs.isRemindersInCalendarEnabled) {
        reminders.observeForever(reminderObserver)
      }
    }

    fun findEvents(monthPagerItem: MonthPagerItem, sort: Boolean, listener: ((MonthPagerItem, List<EventModel>) -> Unit)) {
      this.listener = listener
      this.monthPagerItem = monthPagerItem
      this.sort = sort
      val toSearch = mutableListOf<EventModel>()
      toSearch.addAll(birthdayData)
      if (prefs.isRemindersInCalendarEnabled) {
        toSearch.addAll(reminderData)
      }
      findMatches(toSearch, monthPagerItem, sort)
    }

    override fun onInactive() {
      super.onInactive()
      birthdays.observeForever(birthdayObserver)
      if (prefs.isRemindersInCalendarEnabled) {
        reminders.observeForever(reminderObserver)
      }
    }

    override fun onActive() {
      super.onActive()
      birthdays.removeObserver(birthdayObserver)
      if (prefs.isRemindersInCalendarEnabled) {
        reminders.removeObserver(reminderObserver)
      }
    }

    private fun notifyObserver(monthPagerItem: MonthPagerItem, list: List<EventModel>) {
      listener?.invoke(monthPagerItem, list)
    }

    private fun repeatSearch() {
      val item = monthPagerItem ?: return
      val callback = listener ?: return
      findEvents(item, this.sort, callback)
    }

    private fun findMatches(list: List<EventModel>, monthPagerItem: MonthPagerItem, sort: Boolean) {
      this.job?.cancel()
      this.job = viewModelScope.launch(dispatcherProvider.default()) {
        val res = ArrayList<EventModel>()
        Timber.d("Search events: $monthPagerItem")
        for (item in list) {
          if (item.viewType == EventModel.BIRTHDAY && item.monthValue == monthPagerItem.monthValue) {
            res.add(item)
          } else if (item.monthValue == monthPagerItem.monthValue && item.year == monthPagerItem.year) {
            res.add(item)
          }
        }
        Timber.d("Search events: found -> %d", res.size)
        if (!sort) {
          withUIContext { notifyObserver(monthPagerItem, res) }
        } else {
          val sorted = try {
            res.asSequence().sortedBy { it.getMillis() }.toList()
          } catch (e: IllegalArgumentException) {
            res
          }
          withUIContext { notifyObserver(monthPagerItem, sorted) }
        }
      }
    }
  }
}