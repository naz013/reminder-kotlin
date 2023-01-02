package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ReminderViewModel(
  id: String,
  private val reminderDao: ReminderDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val updatesHelper: UpdatesHelper
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = reminderDao.loadById(id)

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        reminderDao.insert(reminder)
      }
      updatesHelper.updateTasksWidget()
      workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
