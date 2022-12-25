package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class EditReminderViewModel(
  id: String,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupDao: ReminderGroupDao,
  private val reminderDao: ReminderDao,
  private val placesDao: PlacesDao
) : BaseProgressViewModel(dispatcherProvider) {

  private val _note = mutableLiveDataOf<NoteWithImages>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<Pair<GoogleTaskList?, GoogleTask?>>()
  val googleTask = _googleTask.toLiveData()

  val reminder = reminderDao.loadById(id)
  var hasSameInDb: Boolean = false

  private var _allGroups: MutableLiveData<List<ReminderGroup>> = MutableLiveData()
  var allGroups: LiveData<List<ReminderGroup>> = _allGroups

  val groups = mutableListOf<ReminderGroup>()

  init {
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

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id)
      hasSameInDb = reminder != null
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

  fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
    if (showMessage) {
      withResult {
        eventControlFactory.getController(reminder).stop()
        reminderDao.delete(reminder)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        Commands.DELETED
      }
    } else {
      withProgress {
        eventControlFactory.getController(reminder).stop()
        reminderDao.delete(reminder)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      }
    }
  }

  private fun backupReminder(uuId: String) {
    Timber.d("backupReminder: start backup")
    workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
  }
}
