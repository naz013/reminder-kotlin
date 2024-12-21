package com.elementary.tasks.groups.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class GroupsViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  val allGroups = viewModelScope.observeTable(
    table = Table.ReminderGroup,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderGroupRepository.getAll() }
  ).map { list ->
    list.map { uiGroupListAdapter.convert(it) }
  }

  fun deleteGroup(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminderGroup = reminderGroupRepository.getById(id)
      if (reminderGroup == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      reminderGroupRepository.delete(reminderGroup.groupUuId)
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
      val reminderGroup = reminderGroupRepository.getById(id)
      if (reminderGroup == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }

      reminderGroupRepository.save(reminderGroup.copy(groupColor = color))
      workerLauncher.startWork(
        GroupSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
      withUIContext { postInProgress(false) }
    }
  }
}
