package com.github.naz013.group.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitleFormatter
import com.github.naz013.ui.reminder.UiReminderListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupDetailsViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val groupV2Repository: GroupV2Repository,
  private val reminderV2Repository: ReminderV2Repository,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val notificationOverrideSubtitleFormatter: NotificationOverrideSubtitleFormatter,
) : ViewModel() {

  private val _state = MutableStateFlow(GroupDetailsState())
  val state = _state.stateInWhileSubscribed(GroupDetailsState())

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      groupV2Repository.observeById(id)
        .combine(reminderV2Repository.observeActiveByGroupId(id)) { group, reminders -> group to reminders }
        .collect { (group, reminders) -> applyGroup(group, reminders) }
    }
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

  // Driven by groupV2Repository.observeById/reminderV2Repository.observeActiveByGroupId in init -
  // no manual reload needed after onDeleteConfirmed or a mutation from another pane, the Flows
  // re-emit on their own once the write goes through.
  private suspend fun applyGroup(group: GroupV2?, reminders: List<ReminderV2>) {
    if (group == null) {
      Logger.w(TAG, "Group not found, id: $id")
      return
    }
    val canBeDeleted = groupV2Repository.countAll() > 1 && !group.isDefault
    val subtitles = notificationOverrideSubtitleFormatter.format(group.notification, excludeDefault = true)
    val uiReminders = reminders.map { uiReminderListAdapter.createV2(it, group) }
    val color = uiGroupListAdapter.convert(group).color

    _state.update {
      it.copy(
        isLoading = false,
        title = group.title,
        color = color,
        canDelete = canBeDeleted,
        notificationSubtitles = subtitles,
        reminders = uiReminders,
      )
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
