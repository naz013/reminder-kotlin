package com.elementary.tasks.reminder.lists.active

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderListsAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch

class ActiveRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderDao: ReminderDao,
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListsAdapter: UiReminderListsAdapter
) : BaseProgressViewModel(dispatcherProvider) {
  val events = Transformations.map(reminderDao.loadByRemovedStatus(removed = false)) {
    uiReminderListsAdapter.convert(it)
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
