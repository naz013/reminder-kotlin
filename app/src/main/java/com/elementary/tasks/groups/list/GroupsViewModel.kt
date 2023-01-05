package com.elementary.tasks.groups.list

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import kotlinx.coroutines.launch

class GroupsViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupDao: ReminderGroupDao,
  private val uiGroupListAdapter: UiGroupListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  val allGroups = Transformations.map(reminderGroupDao.loadAll()) { list ->
    list.map { uiGroupListAdapter.convert(it) }
  }

  fun deleteGroup(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminderGroup = reminderGroupDao.getById(id)
      if (reminderGroup == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
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

  fun changeGroupColor(id: String, color: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminderGroup = reminderGroupDao.getById(id)
      if (reminderGroup == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }

      reminderGroupDao.insert(reminderGroup.copy(groupColor = color))
      workerLauncher.startWork(
        GroupSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
      withUIContext { postInProgress(false) }
    }
  }
}
