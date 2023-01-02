package com.elementary.tasks.day_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class DayViewViewModel(
  private val eventControlFactory: EventControlFactory,
  dayViewProvider: DayViewProvider,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderDao: ReminderDao,
  private val birthdaysDao: BirthdaysDao,
  reminderGroupDao: ReminderGroupDao,
  private val prefs: Prefs
) : BaseProgressViewModel(dispatcherProvider) {

  private var _events: MutableLiveData<Pair<EventsPagerItem, List<EventModel>>> = MutableLiveData()
  var events: LiveData<Pair<EventsPagerItem, List<EventModel>>> = _events

  private var _groups: MutableList<ReminderGroup> = mutableListOf()
  val groups: List<ReminderGroup>
    get() = _groups

  private val liveData = DayViewLiveData(dayViewProvider)

  init {
    reminderGroupDao.loadAll().observeForever {
      if (it != null) {
        _groups.clear()
        _groups.addAll(it)
      }
    }
  }

  fun findEvents(item: EventsPagerItem) {
    runCatching {
      liveData.findEvents(item, true) { eventsPagerItem, list ->
        _events.postValue(Pair(eventsPagerItem, list))
      }
    }
  }

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.insert(reminder)
      postInProgress(false)
      postCommand(Commands.SAVED)
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
    }
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
    }
  }

  fun moveToTrash(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        fromDb.isRemoved = true
        eventControlFactory.getController(fromDb).stop()
        reminderDao.insert(fromDb)
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  fun skip(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  private inner class DayViewLiveData(
    private val dayViewProvider: DayViewProvider
  ) : LiveData<Pair<EventsPagerItem, List<EventModel>>>() {

    private val reminderData = ArrayList<EventModel>()
    private val birthdayData = ArrayList<EventModel>()
    private val birthdays = birthdaysDao.loadAll()
    private val reminders = reminderDao.loadType(active = true, removed = false)

    private var eventsPagerItem: EventsPagerItem? = null
    private var job: Job? = null
    private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null
    private var sort = false

    private val birthdayObserver: Observer<in List<Birthday>> = Observer { list ->
      Timber.d("birthdaysChanged: ")
      viewModelScope.launch(dispatcherProvider.default()) {
        birthdayData.clear()
        birthdayData.addAll(
          list.map { dayViewProvider.toEventModel(it) }
        )
        repeatSearch()
      }
    }
    private val reminderObserver: Observer<in List<Reminder>> = Observer {
      Timber.d("remindersChanged: ")
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
      reminders.observeForever(reminderObserver)
    }

    fun findEvents(
      eventsPagerItem: EventsPagerItem,
      sort: Boolean,
      listener: ((EventsPagerItem, List<EventModel>) -> Unit)?
    ) {
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

    private fun findMatches(
      list: List<EventModel>,
      eventsPagerItem: EventsPagerItem,
      sort: Boolean
    ) {
      this.job?.cancel()
      this.job = viewModelScope.launch(dispatcherProvider.default()) {
        val res = ArrayList<EventModel>()
        Timber.d("Search events: $eventsPagerItem")
        for (item in list) {
          if (item.viewType == EventModel.BIRTHDAY && item.day == eventsPagerItem.day &&
            item.monthValue == eventsPagerItem.month
          ) {
            res.add(item)
          } else if (item.day == eventsPagerItem.day && item.monthValue == eventsPagerItem.month &&
            item.year == eventsPagerItem.year
          ) {
            res.add(item)
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
