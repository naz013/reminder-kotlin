package com.github.naz013.feature.routine.preview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.routine.Routine
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.logic.routine.usecase.DeleteRoutineUseCase
import com.github.naz013.logic.routine.usecase.ResetRoutineStepsUseCase
import com.github.naz013.logic.routine.usecase.RoutineRecurrenceResetUseCase
import com.github.naz013.logic.routine.usecase.SaveRoutineUseCase
import com.github.naz013.logic.routine.usecase.ToggleRoutinePinUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RoutinePreviewViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val routineRepository: RoutineRepository,
  private val saveRoutineUseCase: SaveRoutineUseCase,
  private val toggleRoutinePinUseCase: ToggleRoutinePinUseCase,
  private val resetRoutineStepsUseCase: ResetRoutineStepsUseCase,
  private val deleteRoutineUseCase: DeleteRoutineUseCase,
  private val routineRecurrenceResetUseCase: RoutineRecurrenceResetUseCase,
  private val routineDurationCalculator: RoutineDurationCalculator,
  private val themeProvider: ThemeProvider,
  private val textProvider: TextProvider,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  private val _state = MutableStateFlow<RoutinePreviewState>(RoutinePreviewState.Loading)
  val state = _state.stateInWhileSubscribed(RoutinePreviewState.Loading)
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private var tags: List<TagChipState> = emptyList()

  init {
    load()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val routine = routineRepository.getById(id)?.let { routineRecurrenceResetUseCase(it) } ?: run {
        Logger.w(TAG, "Routine not found, id: $id")
        return@launch
      }
      tags = tagAssignmentRepository.getTagsForItem(id, TaggedItemType.ROUTINE).map { tagChipStateAdapter(it) }
      withContext(dispatcherProvider.main()) {
        _state.update { routine.toReadyState() }
      }
    }
  }

  private fun Routine.toReadyState(): RoutinePreviewState.Ready {
    val backgroundColor = themeProvider.colorsForSliderThemed().getOrElse(color) { DEFAULT_COLOR }
    val contentColor = if (backgroundColor.luminance() > CONTRAST_LUMINANCE_THRESHOLD) Color.Black else Color.White
    return RoutinePreviewState.Ready(
      id = id,
      title = title,
      description = description,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      isPinned = isPinned,
      durationLabel = routineDurationCalculator.formatDuration(totalDurationSeconds),
      stepCountLabel = textProvider.getString(R.string.routine_step_count, steps.size),
      recurrenceLabel = recurrenceLabel(),
      tags = tags,
      steps = sortedSteps.map {
        RoutinePreviewStepUiState(
          id = it.id,
          title = it.title,
          scheduledTime = it.scheduledTime,
          durationLabel = routineDurationCalculator.formatDuration(it.durationSeconds),
          isCompleted = it.isCompleted,
        )
      },
    )
  }

  private fun Routine.recurrenceLabel(): String = when (val rule = recurrence) {
    null -> textProvider.getString(R.string.routine_on_demand)
    is RecurrenceRule.Daily -> textProvider.getString(R.string.repeat_daily)
    is RecurrenceRule.Weekly -> textProvider.getString(
      R.string.repeat_weekly_days,
      rule.weekdays.sorted().joinToString(", ") { textProvider.getString(WEEKDAY_LABEL_RES[it]) },
    )
    is RecurrenceRule.Monthly -> textProvider.getString(R.string.repeat_monthly_day, rule.dayOfMonth)
    else -> textProvider.getString(R.string.repeat_daily)
  }

  fun onStepCheckToggle(stepId: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val routine = routineRepository.getById(id) ?: return@launch
      val updated = routine.copy(
        steps = routine.steps.map { if (it.id == stepId) it.copy(isCompleted = !it.isCompleted) else it }
      )
      saveRoutineUseCase(updated)
      withContext(dispatcherProvider.main()) { _state.update { updated.toReadyState() } }
    }
  }

  fun onPinToggleClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val routine = routineRepository.getById(id) ?: return@launch
      val updated = toggleRoutinePinUseCase(routine)
      withContext(dispatcherProvider.main()) { _state.update { updated.toReadyState() } }
    }
  }

  fun onResetStepsClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val updated = resetRoutineStepsUseCase(id) ?: return@launch
      withContext(dispatcherProvider.main()) { _state.update { updated.toReadyState() } }
    }
  }

  fun onEditClick() {
    navigationEvent.emit(NavigationEvent.OpenEdit(id))
  }

  fun onStartClick() {
    navigationEvent.emit(NavigationEvent.OpenExecute(id))
  }

  fun onDeleteClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteRoutineUseCase(id)
      Logger.i(TAG, "Deleted routine, id: $id")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  sealed interface NavigationEvent {
    data class OpenEdit(
      val id: String
    ) : NavigationEvent

    data class OpenExecute(
      val id: String
    ) : NavigationEvent

    data object Back : NavigationEvent
  }

  companion object {
    private const val TAG = "RoutinePreviewViewModel"
    private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
    private val DEFAULT_COLOR = Color(0xFF86E3CE)

    /** 0=Sunday..6=Saturday, matching the app-wide weekday convention. */
    private val WEEKDAY_LABEL_RES = listOf(
      R.string.sun,
      R.string.mon,
      R.string.tue,
      R.string.wed,
      R.string.thu,
      R.string.fri,
      R.string.sat,
    )
  }
}
