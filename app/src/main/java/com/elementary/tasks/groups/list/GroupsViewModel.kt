package com.elementary.tasks.groups.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.SaveReminderGroupUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class GroupsViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  tableChangeListenerFactory: TableChangeListenerFactory,
  private val deleteReminderGroupUseCase: DeleteReminderGroupUseCase,
  private val saveReminderGroupUseCase: SaveReminderGroupUseCase
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
      deleteReminderGroupUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
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
      saveReminderGroupUseCase(reminderGroup.copy(groupColor = color))
      withUIContext { postInProgress(false) }
    }
  }
}
