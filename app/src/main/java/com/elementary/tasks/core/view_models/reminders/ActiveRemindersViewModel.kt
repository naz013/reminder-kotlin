package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch

class ActiveRemindersViewModel(
  private val reminderDao: ReminderDao,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListAdapter: UiReminderListAdapter
) : BaseProgressViewModel(dispatcherProvider) {
  val events = Transformations.map(reminderDao.loadNotRemoved(removed = false)) { list ->
    list.map { uiReminderListAdapter.create(it) }
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
}
