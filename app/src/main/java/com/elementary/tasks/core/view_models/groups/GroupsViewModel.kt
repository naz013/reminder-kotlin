package com.elementary.tasks.core.view_models.groups

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import kotlinx.coroutines.launch

class GroupsViewModel(
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  reminderGroupDao: ReminderGroupDao
) : BaseGroupsViewModel(dispatcherProvider, workerLauncher, reminderGroupDao) {

  fun changeGroupColor(reminderGroup: ReminderGroup, color: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroup.groupColor = color
      reminderGroupDao.insert(reminderGroup)
      workerLauncher.startWork(
        GroupSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
      withUIContext { postInProgress(false) }
    }
  }
}
