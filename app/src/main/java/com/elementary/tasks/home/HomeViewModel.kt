package com.elementary.tasks.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.UiBirthdayListAdapter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
  private val currentStateHolder: CurrentStateHolder,
  private val eventControlFactory: EventControlFactory,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val reminderDao: ReminderDao,
  private val birthdaysDao: BirthdaysDao,
  private val dateTimeManager: DateTimeManager
) : BaseProgressViewModel(dispatcherProvider), (String) -> Unit {

  private val prefs = currentStateHolder.preferences

  private val _reminders = mutableLiveDataOf<List<UiReminderList>>()
  private var liveData: LiveData<List<UiReminderList>>? = null
  val reminders: LiveData<List<UiReminderList>> = _reminders
  val birthdays = birthdaysDao.findAll(
    dateTimeManager.getBirthdayDayMonthList(
      duration = prefs.birthdayDurationInDays + 1
    )
  ).map { list -> list.map { uiBirthdayListAdapter.convert(it) } }
  var topScrollX = 0

  init {
    prefs.addObserver(PrefsConstants.SHOW_PERMANENT_REMINDERS, this)
    initReminders()
  }

  fun skip(reminder: UiReminderList) {
    withResult {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, fromDb.uuId)
        Commands.SAVED
      }
      Commands.FAILED
    }
  }

  fun toggleReminder(reminder: UiReminderList) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = reminderDao.getById(reminder.id) ?: return@launch
      if (!eventControlFactory.getController(item).onOff()) {
        postInProgress(false)
        postCommand(Commands.OUTDATED)
      } else {
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, item.uuId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
    }
  }

  fun moveToTrash(reminder: UiReminderList) {
    withResult {
      reminderDao.getById(reminder.id)?.let {
        it.isRemoved = true
        eventControlFactory.getController(it).stop()
        reminderDao.insert(it)
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
        Commands.DELETED
      } ?: run {
        Commands.FAILED
      }
    }
  }

  private fun initReminders() {
    val remindersLiveData = if (prefs.showPermanentOnHome) {
      reminderDao.loadAllTypesInRangeWithPermanent(
        fromTime = dateTimeManager.getDayStart(),
        toTime = dateTimeManager.getDayEnd()
      )
    } else {
      reminderDao.loadAllTypesInRange(
        fromTime = dateTimeManager.getDayStart(),
        toTime = dateTimeManager.getDayEnd()
      )
    }
    val mapped = Transformations.map(remindersLiveData) { list ->
      list.map { uiReminderListAdapter.create(it) as UiReminderList }
    }
    mapped.observeForever {
      _reminders.postValue(it)
    }
    liveData = mapped
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      updateBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun updateBirthdayPermanent() {
    if (prefs.isBirthdayPermanentEnabled) {
      currentStateHolder.notifier.showBirthdayPermanent()
    }
  }

  override fun invoke(p1: String) {
    Timber.d("invoke: $p1")
    if (p1 == PrefsConstants.SHOW_PERMANENT_REMINDERS) {
      initReminders()
    }
  }
}
