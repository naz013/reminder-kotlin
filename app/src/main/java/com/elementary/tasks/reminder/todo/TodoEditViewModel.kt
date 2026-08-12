package com.elementary.tasks.reminder.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.elementary.tasks.reminder.build.reminder.ReminderToBiDecomposer
import com.elementary.tasks.reminder.scheduling.usecase.ResumeReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
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
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoEditViewModel(
  private val navKey: TodoEditNavKey.Main,
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
  private val reminderV2Repository: ReminderV2Repository,
  private val reminderToBiDecomposer: ReminderToBiDecomposer,
  private val pauseReminderUseCase: PauseReminderUseCase,
  private val resumeReminderUseCase: ResumeReminderUseCase,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
) : ViewModel() {

  /** Stable for the whole editing session, like [com.elementary.tasks.reminder.build.BuildReminderViewModel]'s
   *  own copy - tags need one fixed id to attach to from the very first frame, well before the
   *  reminder is actually saved. */
  private val stableReminderId: String = navKey.id.ifEmpty { UUID.randomUUID().toString() }

  private val _state = MutableStateFlow(TodoEditState())
  val state: StateFlow<TodoEditState> = _state.asStateFlow()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var originalV2: ReminderV2? = null
  private var isPaused = false
  private var isSaving = false

  /** Everything from the loaded reminder except SUMMARY/SUB_TASKS/GROUP - round-tripped
   *  untouched on save. [BiToReminderAdapter] always resets the whole reminder to blank and
   *  rebuilds it purely from whatever item list it's given, so this is what preserves fields this
   *  screen never shows (priority, etc.) when editing an existing reminder. */
  private var extraBuilderItems: List<BuilderItem<*>> = emptyList()

  /** For [resumeReminder], which onCleared() calls after AndroidX has already cancelled
   *  [viewModelScope]'s Job as part of clearing this ViewModel - launching there would create a
   *  coroutine that never runs its body, silently leaving a paused reminder inactive forever. */
  private val cleanupScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      val loadedExisting = navKey.id.isNotEmpty() && loadExistingReminder(navKey.id)
      if (!loadedExisting) {
        // Also the fallback when navKey.id is non-empty but doesn't resolve to a reminder (e.g.
        // deleted between the redirect that sent us here and this load) - better to land in a
        // usable create-mode screen than leave subTasksItem null forever.
        val subTasksItem = biFactory.create(BiType.SUB_TASKS) as SubTasksBuilderItem
        // Only the available group list is taken from here - "No group" is this screen's own
        // default (see onSaveClick/onExtendClick), unlike the full builder's GroupBuilderItem,
        // which always falls back to the app-wide default group.
        val availableGroups = (biFactory.create(BiType.GROUP) as GroupBuilderItem).groups
        _state.update { it.copy(subTasksItem = subTasksItem, availableGroups = availableGroups) }
      }
    }
    observeTags()
  }

  private suspend fun loadExistingReminder(id: String): Boolean {
    val reminder = reminderV2Repository.getById(id) ?: return false
    Logger.i(TAG, "Loaded existing reminder for Todo edit, id = $id")

    originalV2 = reminder
    pauseReminder(reminder)

    val decomposed = reminderToBiDecomposer(reminder)
    extraBuilderItems = decomposed.filterNot { it.biType in SEEDED_BI_TYPES }

    val subTasksItem =
      decomposed.filterIsInstance<SubTasksBuilderItem>().firstOrNull()
        ?: (biFactory.create(BiType.SUB_TASKS) as SubTasksBuilderItem)
    val title = decomposed.filterIsInstance<SummaryBuilderItem>().firstOrNull()?.modifier?.getValue() ?: ""
    // The decomposed GROUP item is only present when the reminder actually has a group - an
    // ungrouped reminder must still see the full chip list, so availableGroups is always fetched
    // independently rather than only harvested off a possibly-absent decomposed item.
    val selectedGroup = decomposed.filterIsInstance<GroupBuilderItem>().firstOrNull()?.modifier?.getValue()
    val availableGroups = (biFactory.create(BiType.GROUP) as GroupBuilderItem).groups

    _state.update {
      it.copy(
        title = title,
        subTasksItem = subTasksItem,
        canSave = subTasksItem.modifier.isCorrect(),
        availableGroups = availableGroups,
        selectedGroup = selectedGroup,
        isEditing = true,
        isRemoved = reminder.isRemoved,
      )
    }
    return true
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

  fun onGroupSelected(group: UiGroupList?) {
    _state.update { it.copy(selectedGroup = group) }
  }

  fun onTagToggle(tag: TagChipState) {
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(stableReminderId, TaggedItemType.REMINDER, tag.id, isSelected)
    }
  }

  fun onSaveClick() {
    val builderItems = currentBuilderItems() ?: return
    val base = originalV2 ?: newBaseReminder()
    val isEdited = originalV2 != null
    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      when (val result = biToReminderAdapter(base, builderItems, isEdited = isEdited)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Todo saved, id = ${result.reminderV2.uuId}")
          activateReminderUseCase(result.reminderV2, startAnyway = true)
          withContext(dispatcherProvider.main()) { event.emit(ViewModelEvent.MoveBack) }
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Todo save failed, error = ${result.error}")
          isSaving = false
          withContext(dispatcherProvider.main()) {
            event.emit(ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder))
          }
        }
      }
    }
  }

  fun onExtendClick() {
    val builderItems = currentBuilderItems() ?: return
    val base = originalV2 ?: newBaseReminder()
    val isEdited = originalV2 != null
    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      when (val result = biToReminderAdapter(base, builderItems, isEdited = isEdited)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Todo extended into builder, id = ${result.reminderV2.uuId}")
          todoSeedHolder.pendingSeed = result.reminderV2
          withContext(dispatcherProvider.main()) {
            event.emit(ViewModelEvent.OpenBuilder(stableReminderId, isEditing = _state.value.isEditing))
          }
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Todo extend failed, error = ${result.error}")
          isSaving = false
          withContext(dispatcherProvider.main()) {
            event.emit(ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder))
          }
        }
      }
    }
  }

  fun moveToTrash() {
    val reminder = originalV2
    if (reminder == null) {
      event.emit(ViewModelEvent.MoveBack)
      return
    }
    Logger.i(TAG, "Move todo reminder to Archive, id = ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      moveReminderToArchiveUseCase(reminder.uuId)
      withContext(dispatcherProvider.main()) { event.emit(ViewModelEvent.MoveBack) }
    }
  }

  fun deleteReminder(showMessage: Boolean) {
    val reminder = originalV2
    if (reminder == null) {
      event.emit(ViewModelEvent.MoveBack)
      return
    }
    Logger.i(TAG, "Delete todo reminder, id = ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      deleteReminderUseCase(reminder)
      if (showMessage) {
        withContext(dispatcherProvider.main()) { event.emit(ViewModelEvent.MoveBack) }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    if (isPaused && !isSaving) {
      originalV2?.let { resumeReminder(it) }
    }
  }

  private suspend fun pauseReminder(reminder: ReminderV2) {
    Logger.i(TAG, "Pause todo reminder, id = ${reminder.uuId}")
    isPaused = true
    pauseReminderUseCase(reminder)
  }

  private fun resumeReminder(reminder: ReminderV2) {
    Logger.i(TAG, "Resume todo reminder, id = ${reminder.uuId}")
    cleanupScope.launch {
      isPaused = false
      resumeReminderUseCase(reminder)
    }
  }

  /** No group selected simply omits a [GroupBuilderItem] - [BiToReminderAdapter] resets
   *  `groupId` to null before applying any item, so leaving the group item out is this screen's
   *  true "no group" outcome, unlike [GroupModifier][com.elementary.tasks.reminder.build.bi.GroupModifier]'s
   *  own null-falls-back-to-app-default-group behavior used by the full builder. */
  private fun currentBuilderItems(): List<BuilderItem<*>>? {
    val current = _state.value
    val subTasksItem = current.subTasksItem ?: return null
    val summaryItem =
      SummaryBuilderItem(title = "", description = null).apply { modifier.update(current.title) }
    val group = current.selectedGroup
    val groupItem =
      group?.let {
        GroupBuilderItem(title = "", description = null, groups = current.availableGroups, defaultGroup = null)
          .apply { modifier.update(it) }
      }
    return extraBuilderItems + listOfNotNull(summaryItem, subTasksItem, groupItem)
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
      val isEditing: Boolean,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "TodoEditViewModel"
    private val SEEDED_BI_TYPES = setOf(BiType.SUMMARY, BiType.SUB_TASKS, BiType.GROUP)
  }
}
