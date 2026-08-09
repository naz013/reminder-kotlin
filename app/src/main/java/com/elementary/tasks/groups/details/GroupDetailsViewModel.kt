package com.elementary.tasks.groups.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.groups.NotificationOverrideSubtitleFormatter
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.usecase.reminders.GetActiveRemindersV2ByGroupIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailsViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val groupV2Repository: GroupV2Repository,
  private val getActiveRemindersV2ByGroupIdUseCase: GetActiveRemindersV2ByGroupIdUseCase,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val notificationOverrideSubtitleFormatter: NotificationOverrideSubtitleFormatter,
) : ViewModel() {

  private val _state = MutableStateFlow(GroupDetailsState())
  val state = _state.stateInWhileSubscribed(GroupDetailsState())
    .onStart { load() }

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  fun refreshState() {
    load()
  }

  fun onAddReminderClicked() {
    navigationEvent.emit(NavigationEvent.OpenAddReminder(id))
  }

  fun onEditClick() {
    navigationEvent.emit(NavigationEvent.OpenEdit(id))
  }

  fun onDeleteClick() {
    navigationEvent.emit(NavigationEvent.ConfirmDelete(id))
  }

  fun onDeleteConfirmed() {
    if (!_state.value.canDelete) {
      Logger.e(TAG, "Can't delete group, id: $id")
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteGroupUseCase(id)
      Logger.i(TAG, "Deleted group, id: $id")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Deleted)
      }
    }
  }

  fun onReminderClick(reminderId: String) {
    navigationEvent.emit(NavigationEvent.OpenReminderPreview(reminderId))
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val group = groupV2Repository.getById(id) ?: run {
        Logger.w(TAG, "Group not found, id: $id")
        return@launch
      }
      val canBeDeleted = groupV2Repository.countAll() > 1 && !group.isDefault
      val subtitles = notificationOverrideSubtitleFormatter.format(group.notification, excludeDefault = true)
      val reminders = getActiveRemindersV2ByGroupIdUseCase(id).map { uiReminderListAdapter.createV2(it, group) }
      val color = uiGroupListAdapter.convert(group).color

      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(
            isLoading = false,
            title = group.title,
            color = color,
            canDelete = canBeDeleted,
            notificationSubtitles = subtitles,
            reminders = reminders,
          )
        }
      }
    }
  }

  sealed interface NavigationEvent {
    data class OpenEdit(val id: String) : NavigationEvent

    data class ConfirmDelete(val id: String) : NavigationEvent

    data object Deleted : NavigationEvent

    data class OpenReminderPreview(val id: String) : NavigationEvent

    data class OpenAddReminder(val groupUuId: String) : NavigationEvent
  }

  companion object {
    private const val TAG = "GroupDetailsViewModel"
  }
}
