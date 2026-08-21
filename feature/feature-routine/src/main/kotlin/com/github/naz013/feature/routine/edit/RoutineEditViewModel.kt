package com.github.naz013.feature.routine.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.usecase.DeleteRoutineUseCase
import com.github.naz013.logic.routine.usecase.SaveRoutineUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalTime
import java.util.UUID

internal class RoutineEditViewModel(
  private val id: String?,
  private val dispatcherProvider: DispatcherProvider,
  private val routineRepository: RoutineRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val toggleTagAssignmentUseCase: ToggleTagAssignmentUseCase,
  private val saveRoutineUseCase: SaveRoutineUseCase,
  private val deleteRoutineUseCase: DeleteRoutineUseCase,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val themeProvider: ThemeProvider,
  private val appPreferences: AppPreferences,
) : ViewModel() {

  private val stableRoutineId = id ?: UUID.randomUUID().toString()
  private var originalRoutine: Routine? = null

  private val _state = MutableStateFlow(RoutineEditState(id = id, canDelete = id != null))
  val state = _state.stateInWhileSubscribed(RoutineEditState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        hapticFeedbackEnabled = appPreferences.hapticsEnabled,
        sliderColors = themeProvider.colorsForSliderThemed(),
      )
    }
    load()
    observeTags()
  }

  private fun load() {
    val routineId = id ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      val routine = routineRepository.getById(routineId) ?: run {
        Logger.w(TAG, "Routine not found, id: $routineId")
        return@launch
      }
      originalRoutine = routine
      val steps = routine.sortedSteps.map(RoutineStep::toUiState)
      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(
            title = routine.title,
            description = routine.description.orEmpty(),
            colorPosition = routine.color,
            isPinned = routine.isPinned,
            steps = steps,
            recurrenceOption = routine.recurrence.toRecurrenceOption(),
            canDelete = true,
            canSave = canSave(routine.title, steps, routine.recurrence.toRecurrenceOption()),
          )
        }
      }
    }
  }

  private fun observeTags() {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags -> _state.update { it.copy(allTags = tags) } }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(stableRoutineId, TaggedItemType.ROUTINE).collect { tags ->
        _state.update { it.copy(selectedTagIds = tags.map(Tag::id).toSet()) }
      }
    }
  }

  fun onTitleChange(title: String) {
    _state.update { it.copy(title = title, canSave = canSave(title, it.steps, it.recurrenceOption)) }
  }

  fun onDescriptionChange(description: String) {
    _state.update { it.copy(description = description) }
  }

  fun onColorSelected(position: Int) {
    _state.update { it.copy(colorPosition = position) }
  }

  fun onPinToggleClick() {
    _state.update { it.copy(isPinned = !it.isPinned) }
  }

  fun onRecurrenceOptionChange(option: RoutineRecurrenceOption) {
    _state.update { it.copy(recurrenceOption = option, canSave = canSave(it.title, it.steps, option)) }
  }

  fun onAddStepClick() {
    val newStep = RoutineStepUiState(
      id = UUID.randomUUID().toString(),
      title = "",
      durationSeconds = 0,
      scheduledTime = null,
    )
    _state.update {
      val steps = it.steps + newStep
      it.copy(steps = steps, canSave = canSave(it.title, steps, it.recurrenceOption))
    }
  }

  fun onStepTitleChange(stepId: String, title: String) {
    updateStep(stepId) { it.copy(title = title) }
  }

  fun onStepDurationSelected(stepId: String, durationSeconds: Int) {
    updateStep(stepId) { it.copy(durationSeconds = durationSeconds) }
  }

  fun onStepTimeSelected(stepId: String, time: LocalTime?) {
    val formatted = time?.let { "%02d:%02d".format(it.hour, it.minute) }
    updateStep(stepId) { it.copy(scheduledTime = formatted) }
  }

  fun onRemoveStepClick(stepId: String) {
    _state.update {
      val steps = it.steps.filterNot { step -> step.id == stepId }
      it.copy(steps = steps, canSave = canSave(it.title, steps, it.recurrenceOption))
    }
  }

  fun onMoveStepUp(stepId: String) {
    moveStep(stepId, offset = -1)
  }

  fun onMoveStepDown(stepId: String) {
    moveStep(stepId, offset = 1)
  }

  private fun moveStep(stepId: String, offset: Int) {
    _state.update { state ->
      val steps = state.steps.toMutableList()
      val fromIndex = steps.indexOfFirst { it.id == stepId }
      if (fromIndex == -1) return@update state
      val toIndex = (fromIndex + offset).coerceIn(0, steps.size - 1)
      if (toIndex == fromIndex) return@update state
      val step = steps.removeAt(fromIndex)
      steps.add(toIndex, step)
      state.copy(steps = steps)
    }
  }

  private fun updateStep(stepId: String, transform: (RoutineStepUiState) -> RoutineStepUiState) {
    _state.update { state ->
      state.copy(steps = state.steps.map { if (it.id == stepId) transform(it) else it })
    }
  }

  fun onTagToggle(tag: TagChipState) {
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(stableRoutineId, TaggedItemType.ROUTINE, tag.id, isSelected)
    }
  }

  fun onManageTagsClick() {
    navigationEvent.emit(NavigationEvent.OpenManageTags)
  }

  fun onSaveClick() {
    val stateValue = _state.value
    val title = stateValue.title.trim()
    if (!canSave(title, stateValue.steps, stateValue.recurrenceOption)) {
      _state.update { it.copy(canSave = false) }
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val now = nowDateTimeProvider.nowDateTime()
      val base = originalRoutine ?: Routine(id = stableRoutineId, createdAt = now, updatedAt = now)
      val steps = stateValue.steps.mapIndexed { index, step ->
        RoutineStep(
          id = step.id,
          title = step.title.trim(),
          durationSeconds = step.durationSeconds,
          scheduledTime = step.scheduledTime,
          order = index,
        )
      }
      val recurrence = stateValue.recurrenceOption.toRecurrenceRule()
      // "The recurrence period starts after you save" - a fresh cycle only opens the moment
      // recurrence goes from off to on (new routine, or turning it on during an edit); resaving
      // an already-recurring routine must not reset lastResetAt and silently drop the user's
      // in-progress cycle, and turning recurrence off drops the anchor since on-demand routines
      // are never auto-reset (see RoutineRecurrenceResetUseCase).
      val lastResetAt = when {
        recurrence == null -> null
        base.recurrence == null -> now
        else -> base.lastResetAt
      }
      val routine = base.copy(
        title = title,
        description = stateValue.description.trim().ifBlank { null },
        color = stateValue.colorPosition,
        isPinned = stateValue.isPinned,
        steps = steps,
        recurrence = recurrence,
        lastResetAt = lastResetAt,
      )
      saveRoutineUseCase(routine)
      Logger.i(TAG, "Saved routine, id: ${routine.id}")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  private fun canSave(
    title: String,
    steps: List<RoutineStepUiState>,
    recurrenceOption: RoutineRecurrenceOption,
  ): Boolean {
    val recurrenceIsValid = recurrenceOption !is RoutineRecurrenceOption.Weekly || recurrenceOption.weekdays.isNotEmpty()
    return title.isNotBlank() && steps.isNotEmpty() && recurrenceIsValid
  }

  fun onDeleteClick() {
    val routineId = id ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteRoutineUseCase(routineId)
      Logger.i(TAG, "Deleted routine, id: $routineId")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
    data object OpenManageTags : NavigationEvent
  }

  companion object {
    private const val TAG = "RoutineEditViewModel"
  }
}

private fun RoutineStep.toUiState(): RoutineStepUiState = RoutineStepUiState(
  id = id,
  title = title,
  durationSeconds = durationSeconds,
  scheduledTime = scheduledTime,
)

/** Routines only ever save None/Daily/Weekly/Monthly (see [RoutineRecurrenceOption.toRecurrenceRule]),
 * so any other [RecurrenceRule] variant found on load falls back to [RoutineRecurrenceOption.None] -
 * defensive, not expected to be hit in practice. */
private fun RecurrenceRule?.toRecurrenceOption(): RoutineRecurrenceOption = when (this) {
  null -> RoutineRecurrenceOption.None
  is RecurrenceRule.Daily -> RoutineRecurrenceOption.Daily
  is RecurrenceRule.Weekly -> RoutineRecurrenceOption.Weekly(weekdays.toSet())
  is RecurrenceRule.Monthly -> RoutineRecurrenceOption.Monthly(dayOfMonth)
  else -> RoutineRecurrenceOption.None
}

private fun RoutineRecurrenceOption.toRecurrenceRule(): RecurrenceRule? = when (this) {
  RoutineRecurrenceOption.None -> null
  RoutineRecurrenceOption.Daily -> RecurrenceRule.Daily()
  is RoutineRecurrenceOption.Weekly -> RecurrenceRule.Weekly(weekdays = weekdays.sorted())
  is RoutineRecurrenceOption.Monthly -> RecurrenceRule.Monthly(dayOfMonth = dayOfMonth)
}
