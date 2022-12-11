package com.elementary.tasks.core.view_models.google_tasks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import timber.log.Timber

class GoogleTaskViewModel(
  id: String,
  prefs: Prefs,
  context: Context,
  gTasks: GTasks,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  updatesHelper: UpdatesHelper,
  googleTasksDao: GoogleTasksDao,
  googleTaskListsDao: GoogleTaskListsDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao
) : BaseTaskListsViewModel(
  prefs,
  context,
  gTasks,
  dispatcherProvider,
  workManagerProvider,
  updatesHelper,
  googleTasksDao,
  googleTaskListsDao
) {

  val googleTask = googleTasksDao.loadById(id)
  val defaultTaskList = googleTaskListsDao.loadDefault()
  val googleTaskLists = googleTaskListsDao.loadAll()

  val isLogged = gTasks.isLogged
  private var _reminder = MutableLiveData<Reminder>()
  var reminder: LiveData<Reminder> = _reminder

  fun loadReminder(uuId: String) {
    postInProgress(true)
    launchDefault {
      val reminderItem = reminderDao.getById(uuId)
      _reminder.postValue(reminderItem)
      postInProgress(false)
    }
  }

  private fun saveReminder(reminder: Reminder?) {
    Timber.d("saveReminder: $reminder")
    if (reminder != null) {
      launchDefault {
        val group = reminderGroupDao.defaultGroup()
        if (group != null) {
          reminder.groupColor = group.groupColor
          reminder.groupTitle = group.groupTitle
          reminder.groupUuId = group.groupUuId
          reminderDao.insert(reminder)
        }
        if (reminder.groupUuId != "") {
          eventControlFactory.getController(reminder).start()
          startWork(
            com.elementary.tasks.reminder.work.ReminderSingleBackupWorker::class.java,
            Constants.INTENT_ID, reminder.uuId
          )
        }
      }
    }
  }

  fun deleteGoogleTask(googleTask: GoogleTask) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      try {
        gTasks.deleteTask(googleTask)
        googleTasksDao.delete(googleTask)
        postInProgress(false)
        postCommand(Commands.DELETED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun newGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      try {
        gTasks.insertTask(googleTask)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun updateGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      googleTasksDao.insert(googleTask)
      try {
        gTasks.updateTask(googleTask)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun updateAndMoveGoogleTask(googleTask: GoogleTask, oldListId: String, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      googleTasksDao.insert(googleTask)
      try {
        gTasks.updateTask(googleTask)
        gTasks.moveTask(googleTask, oldListId)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun moveGoogleTask(googleTask: GoogleTask, oldListId: String) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      googleTasksDao.insert(googleTask)
      gTasks.moveTask(googleTask, oldListId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
