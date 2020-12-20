package com.elementary.tasks.core.view_models.day_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.day_view.EventsPagerItem
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.*

class DayViewViewModel(
  private val calculateFuture: Boolean,
  private val eventControlFactory: EventControlFactory,
  dayViewProvider: DayViewProvider,
  appDb: AppDb,
  prefs: Prefs
) : BaseDbViewModel(appDb, prefs) {

  private var _events: MutableLiveData<Pair<EventsPagerItem, List<EventModel>>> = MutableLiveData()
  var events: LiveData<Pair<EventsPagerItem, List<EventModel>>> = _events

  private var _groups: MutableList<ReminderGroup> = mutableListOf()
  val groups: List<ReminderGroup>
    get() = _groups

  private val liveData = DayViewLiveData(dayViewProvider)

  init {
    appDb.reminderGroupDao().loadAll().observeForever {
      if (it != null) {
        _groups.clear()
        _groups.addAll(it)
      }
    }
  }

  fun findEvents(item: EventsPagerItem) {
    try {
      liveData.findEvents(item, true) { eventsPagerItem, list ->
        _events.postValue(Pair(eventsPagerItem, list))
      }
    } catch (e: UninitializedPropertyAccessException) {
    }
  }

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    launchDefault {
      appDb.reminderDao().insert(reminder)
      postInProgress(false)
      postCommand(Commands.SAVED)
      startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
    }
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    launchDefault {
      appDb.birthdaysDao().delete(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
      startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
    }
  }

  fun moveToTrash(reminder: Reminder) {
    postInProgress(true)
    launchDefault {
      val fromDb = appDb.reminderDao().getById(reminder.uuId)
      if (fromDb != null) {
        fromDb.isRemoved = true
        eventControlFactory.getController(fromDb).stop()
        appDb.reminderDao().insert(fromDb)
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
      startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
    }
  }

  fun skip(reminder: Reminder) {
    postInProgress(true)
    launchDefault {
      val fromDb = appDb.reminderDao().getById(reminder.uuId)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
      startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
    }
  }

  private inner class DayViewLiveData(
    private val dayViewProvider: DayViewProvider
  ) : LiveData<Pair<EventsPagerItem, List<EventModel>>>() {

    private val reminderData = ArrayList<EventModel>()
    private val birthdayData = ArrayList<EventModel>()
    private val birthdays = appDb.birthdaysDao().loadAll()
    private val reminders = appDb.reminderDao().loadType(active = true, removed = false)

    private var eventsPagerItem: EventsPagerItem? = null
    private var job: Job? = null
    private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null
    private var sort = false

    private val birthdayObserver: Observer<in List<Birthday>> = Observer { list ->
      Timber.d("birthdaysChanged: ")
      launchDefault {
        birthdayData.clear()
        birthdayData.addAll(
          list.map { dayViewProvider.toEventModel(it) }
        )
        repeatSearch()
      }
    }
    private val reminderObserver: Observer<in List<Reminder>> = Observer {
      Timber.d("remindersChanged: ")
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
      reminders.observeForever(reminderObserver)
    }

    fun findEvents(eventsPagerItem: EventsPagerItem, sort: Boolean, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?) {
      if (listener == null) return
      this.listener = listener
      this.eventsPagerItem = eventsPagerItem
      this.sort = sort
      val toSearch = mutableListOf<EventModel>()
      toSearch.addAll(birthdayData)
      toSearch.addAll(reminderData)
      findMatches(toSearch, eventsPagerItem, sort)
    }

    override fun onInactive() {
      super.onInactive()
      Timber.d("onInactive: ")
      birthdays.observeForever(birthdayObserver)
      reminders.observeForever(reminderObserver)
      this.eventsPagerItem = null
    }

    override fun onActive() {
      super.onActive()
      Timber.d("onActive: ")
      birthdays.removeObserver(birthdayObserver)
      reminders.removeObserver(reminderObserver)
    }

    private fun notifyObserver(eventsPagerItem: EventsPagerItem, list: List<EventModel>) {
      listener?.invoke(eventsPagerItem, list)
    }

    private fun repeatSearch() {
      val item = eventsPagerItem ?: return
      findEvents(item, this.sort, listener)
    }

    private fun findMatches(list: List<EventModel>, eventsPagerItem: EventsPagerItem, sort: Boolean) {
      this.job?.cancel()
      this.job = launchDefault {
        val res = ArrayList<EventModel>()
        Timber.d("Search events: $eventsPagerItem")
        for (item in list) {
          val mDay = item.day
          val mMonth = item.month
          val mYear = item.year
          val type = item.viewType
          if (type == EventModel.BIRTHDAY && mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month) {
            res.add(item)
          } else {
            if (mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month && mYear == eventsPagerItem.year) {
              res.add(item)
            }
          }
        }
        Timber.d("Search events: found -> %d", res.size)
        if (!sort) {
          withUIContext { notifyObserver(eventsPagerItem, res) }
        } else {
          val sorted = try {
            res.asSequence().sortedBy { it.getMillis() }.toList()
          } catch (e: IllegalArgumentException) {
            res
          }
          withUIContext { notifyObserver(eventsPagerItem, sorted) }
        }
      }
    }
  }
}
