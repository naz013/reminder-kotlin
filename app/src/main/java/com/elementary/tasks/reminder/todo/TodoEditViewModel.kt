package com.elementary.tasks.reminder.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TodoEditViewModel(
  navKey: TodoEditNavKey.Main,
  private val dispatcherProvider: DispatcherProvider,
  private val biFactory: BiFactory,
  private val biToReminderAdapter: BiToReminderAdapter,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val toggleTagAssignmentUseCase: ToggleTagAssignmentUseCase,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val dateTimeManager: DateTimeManager,
  private val todoSeedHolder: TodoSeedHolder,
) : ViewModel() {

  /** Stable for the whole editing session, like [com.elementary.tasks.reminder.build.BuildReminderViewModel]'s
   *  own copy - tags need one fixed id to attach to from the very first frame, well before the
   *  reminder is actually saved. */
  private val stableReminderId: String = navKey.id.ifEmpty { UUID.randomUUID().toString() }

  private val _state = MutableStateFlow(TodoEditState())
  val state: StateFlow<TodoEditState> = _state.asStateFlow()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      val subTasksItem = biFactory.create(BiType.SUB_TASKS) as SubTasksBuilderItem
      val groupItem = biFactory.create(BiType.GROUP) as GroupBuilderItem
      _state.update { it.copy(subTasksItem = subTasksItem, groupItem = groupItem) }
    }
    observeTags()
  }

  private fun observeTags() {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags -> _state.update { it.copy(allTags = tags) } }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(stableReminderId, TaggedItemType.REMINDER).collect { tags ->
        _state.update { it.copy(selectedTagIds = tags.map(Tag::id).toSet()) }
      }
    }
  }

  fun onTitleChange(title: String) {
    _state.update { it.copy(title = title) }
  }

  fun onSubTasksChanged(builderItem: BuilderItem<*>) {
    val subTasksItem = builderItem as? SubTasksBuilderItem ?: return
    _state.update { it.copy(subTasksItem = subTasksItem, canSave = subTasksItem.modifier.isCorrect()) }
  }

  fun onGroupChanged(builderItem: BuilderItem<*>) {
    val groupItem = builderItem as? GroupBuilderItem ?: return
    _state.update { it.copy(groupItem = groupItem) }
  }

  fun onTagToggle(tag: TagChipState) {
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(stableReminderId, TaggedItemType.REMINDER, tag.id, isSelected)
    }
  }

  fun onSaveClick() {
    val builderItems = currentBuilderItems() ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      when (val result = biToReminderAdapter(newBaseReminder(), builderItems, isEdited = false)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Todo saved, id = ${result.reminderV2.uuId}")
          activateReminderUseCase(result.reminderV2, startAnyway = true)
          event.emit(ViewModelEvent.MoveBack)
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Todo save failed, error = ${result.error}")
          event.emit(ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder))
        }
      }
    }
  }

  fun onExtendClick() {
    val builderItems = currentBuilderItems() ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      when (val result = biToReminderAdapter(newBaseReminder(), builderItems, isEdited = false)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Todo extended into builder, id = ${result.reminderV2.uuId}")
          todoSeedHolder.pendingSeed = result.reminderV2
          event.emit(ViewModelEvent.OpenBuilder(stableReminderId))
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Todo extend failed, error = ${result.error}")
          event.emit(ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder))
        }
      }
    }
  }

  private fun currentBuilderItems(): List<BuilderItem<*>>? {
    val current = _state.value
    val subTasksItem = current.subTasksItem ?: return null
    val groupItem = current.groupItem ?: return null
    val summaryItem = SummaryBuilderItem(title = current.title, description = null)
    return listOf(summaryItem, subTasksItem, groupItem)
  }

  private fun newBaseReminder(): ReminderV2 =
    ReminderV2(
      uuId = stableReminderId,
      schedule = ReminderSchedule(startDateTime = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())),
    )

  sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent

    data class ShowMessage(
      val messageRes: Int,
    ) : ViewModelEvent

    data class OpenBuilder(
      val reminderId: String,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "TodoEditViewModel"
  }
}
