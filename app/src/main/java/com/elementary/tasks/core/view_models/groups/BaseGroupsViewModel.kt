package com.elementary.tasks.core.view_models.groups

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import kotlinx.coroutines.launch

abstract class BaseGroupsViewModel(
  dispatcherProvider: DispatcherProvider,
  protected val workerLauncher: WorkerLauncher,
  protected val reminderGroupDao: ReminderGroupDao
) : BaseProgressViewModel(dispatcherProvider) {

  val allGroups = reminderGroupDao.loadAll()

  fun deleteGroup(reminderGroup: ReminderGroup) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroupDao.delete(reminderGroup)
      postInProgress(false)
      postCommand(Commands.DELETED)
      workerLauncher.startWork(
        GroupDeleteBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
    }
  }
}
