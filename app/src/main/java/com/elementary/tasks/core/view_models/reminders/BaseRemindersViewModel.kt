package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Calendar

abstract class BaseRemindersViewModel(
  prefs: Prefs,
  protected val calendarUtils: CalendarUtils,
  protected val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  protected val updatesHelper: UpdatesHelper,
  protected val reminderDao: ReminderDao,
  protected val reminderGroupDao: ReminderGroupDao,
  protected val placesDao: PlacesDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  private var _defaultReminderGroup: MutableLiveData<ReminderGroup> = MutableLiveData()
  var defaultReminderGroup: LiveData<ReminderGroup> = _defaultReminderGroup

  private var _allGroups: MutableLiveData<List<ReminderGroup>> = MutableLiveData()
  var allGroups: LiveData<List<ReminderGroup>> = _allGroups

  val groups = mutableListOf<ReminderGroup>()
  var defaultGroup: ReminderGroup? = null

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroupDao.defaultGroup(true)?.also {
        defaultGroup = it
        _defaultReminderGroup.postValue(it)
      }
    }
    reminderGroupDao.loadAll().observeForever {
      _allGroups.postValue(it)
      if (it != null) {
        groups.clear()
        groups.addAll(it)
      }
    }
  }

  fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        Timber.d("saveAndStartReminder: save START")
        if (reminder.groupUuId == "") {
          val group = reminderGroupDao.defaultGroup()
          if (group != null) {
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
          }
        }
        reminderDao.insert(reminder)
        if (!isEdit) {
          if (Reminder.isGpsType(reminder.type)) {
            val places = reminder.places
            if (places.isNotEmpty()) {
              placesDao.insert(places[0])
            }
          }
        }
        eventControlFactory.getController(reminder).start()
        Timber.d("saveAndStartReminder: save DONE")
      }
      backupReminder(reminder.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun copyReminder(reminder: Reminder, time: Long, name: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        if (reminder.groupUuId == "") {
          val group = reminderGroupDao.defaultGroup()
          if (group != null) {
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
          }
        }
        val newItem = reminder.copy()
        newItem.summary = name
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.timeInMillis = time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(newItem.eventTime)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        while (calendar.timeInMillis < System.currentTimeMillis()) {
          calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        newItem.eventTime = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
        newItem.startTime = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
        reminderDao.insert(newItem)
        eventControlFactory.getController(newItem).start()
      }
      postCommand(Commands.SAVED)
    }
  }

  fun stopReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).stop()
      postInProgress(false)
    }
  }

  fun pauseReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).pause()
      postInProgress(false)
    }
  }

  fun resumeReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).resume()
      postInProgress(false)
    }
  }

  fun toggleReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      if (!eventControlFactory.getController(reminder).onOff()) {
        postInProgress(false)
        postCommand(Commands.OUTDATED)
      } else {
        backupReminder(reminder.uuId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
    }
  }

  fun moveToTrash(reminder: Reminder) {
    withResult {
      reminder.isRemoved = true
      eventControlFactory.getController(reminder).stop()
      reminderDao.insert(reminder)
      backupReminder(reminder.uuId)
      Commands.DELETED
    }
  }

  private fun backupReminder(uuId: String) {
    Timber.d("backupReminder: start backup")
    startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
  }

  fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
    if (showMessage) {
      withResult {
        eventControlFactory.getController(reminder).stop()
        reminderDao.delete(reminder)
        calendarUtils.deleteEvents(reminder.uuId)
        startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        Commands.DELETED
      }
    } else {
      withProgress {
        eventControlFactory.getController(reminder).stop()
        reminderDao.delete(reminder)
        calendarUtils.deleteEvents(reminder.uuId)
        startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      }
    }
  }

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        reminderDao.insert(reminder)
      }
      updatesHelper.updateTasksWidget()
      backupReminder(reminder.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun skip(reminder: Reminder) {
    withResult {
      val fromDb = reminderDao.getById(reminder.uuId)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
      }
      backupReminder(reminder.uuId)
      Commands.SAVED
    }
  }
}
