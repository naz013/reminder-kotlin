package com.elementary.tasks.core.view_models.groups

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import kotlinx.coroutines.launch

class GroupViewModel(
  id: String,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  reminderGroupDao: ReminderGroupDao
) : BaseGroupsViewModel(dispatcherProvider, workerLauncher, reminderGroupDao) {

  val reminderGroup = reminderGroupDao.loadById(id)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val group = reminderGroupDao.getById(id)
      hasSameInDb = group != null
    }
  }

  fun saveGroup(reminderGroup: ReminderGroup, wasDefault: Boolean) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      if (!wasDefault && reminderGroup.isDefaultGroup) {
        val groups = reminderGroupDao.all()
        for (g in groups) g.isDefaultGroup = false
        reminderGroupDao.insertAll(groups)
      }
      reminderGroupDao.insert(reminderGroup)
      workerLauncher.startWork(
        GroupSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
