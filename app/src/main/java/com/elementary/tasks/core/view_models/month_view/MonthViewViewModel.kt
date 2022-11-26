package com.elementary.tasks.core.view_models.month_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.month_view.MonthPagerItem
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.*

class MonthViewViewModel(
  private val addReminders: Boolean,
  private val calculateFuture: Boolean,
  dayViewProvider: DayViewProvider,
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider) {

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
    private val birthdays = appDb.birthdaysDao().loadAll()
    private val reminders = appDb.reminderDao().loadType(active = true, removed = false)

    private var monthPagerItem: MonthPagerItem? = null
    private var job: Job? = null
    private var listener: ((MonthPagerItem, List<EventModel>) -> Unit)? = null
    private var sort = false

    private val birthdayObserver: Observer<in List<Birthday>> = Observer { list ->
      launchDefault {
        birthdayData.clear()
        birthdayData.addAll(list.map { dayViewProvider.toEventModel(it) })
        repeatSearch()
      }
    }
    private val reminderObserver: Observer<in List<Reminder>> = Observer {
      launchDefault {
        if (it != null) {
          reminderData.clear()
          reminderData.addAll(dayViewProvider.loadReminders(calculateFuture, it))
          repeatSearch()
        }
      }
    }

    init {
      birthdays.observeForever(birthdayObserver)
      if (addReminders) {
        reminders.observeForever(reminderObserver)
      }
    }

    fun findEvents(monthPagerItem: MonthPagerItem, sort: Boolean, listener: ((MonthPagerItem, List<EventModel>) -> Unit)) {
      this.listener = listener
      this.monthPagerItem = monthPagerItem
      this.sort = sort
      val toSearch = mutableListOf<EventModel>()
      toSearch.addAll(birthdayData)
      if (addReminders) {
        toSearch.addAll(reminderData)
      }
      findMatches(toSearch, monthPagerItem, sort)
    }

    override fun onInactive() {
      super.onInactive()
      birthdays.observeForever(birthdayObserver)
      if (addReminders) {
        reminders.observeForever(reminderObserver)
      }
    }

    override fun onActive() {
      super.onActive()
      birthdays.removeObserver(birthdayObserver)
      if (addReminders) {
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
      this.job = launchDefault {
        val res = ArrayList<EventModel>()
        Timber.d("Search events: $monthPagerItem")
        for (item in list) {
          val mMonth = item.month
          val mYear = item.year
          val type = item.viewType
          if (type == EventModel.BIRTHDAY && mMonth == monthPagerItem.month) {
            res.add(item)
          } else if (mMonth == monthPagerItem.month && mYear == monthPagerItem.year) {
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
