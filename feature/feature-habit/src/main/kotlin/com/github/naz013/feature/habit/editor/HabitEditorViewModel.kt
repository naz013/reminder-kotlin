package com.github.naz013.feature.habit.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.DateTimePreferences
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.GetHabitUseCase
import com.github.naz013.logic.habit.usecase.SaveHabitUseCase
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.routine.RoutineColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalTime
import java.util.UUID

internal class HabitEditorViewModel(
  private val id: String?,
  private val dispatcherProvider: DispatcherProvider,
  private val getHabitUseCase: GetHabitUseCase,
  private val saveHabitUseCase: SaveHabitUseCase,
  private val deleteHabitUseCase: DeleteHabitUseCase,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val textProvider: TextProvider,
  private val appPreferences: AppPreferences,
  private val dateTimeManager: DateTimeManager,
  private val dateTimePreferences: DateTimePreferences,
) : ViewModel() {

  private val stableHabitId = id ?: UUID.randomUUID().toString()
  private var originalHabit: Habit? = null
  private var targetCount = DEFAULT_TARGET

  val state: StateFlow<HabitEditorState> field = MutableStateFlow(HabitEditorState(id = id, canDelete = id != null))
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    state.update {
      it.copy(
        hapticFeedbackEnabled = appPreferences.hapticsEnabled,
        is24Hour = dateTimePreferences.is24HourFormat,
        sliderColors = RoutineColors.ALL,
        targetLabel = targetLabel(it.frequency, targetCount),
        reminderTimeLabel = formatTime(it.reminderTime),
      )
    }
    load()
  }

  private fun load() {
    val habitId = id ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      val habit = getHabitUseCase(habitId) ?: run {
        Logger.w(TAG, "Habit not found, id: $habitId")
        return@launch
      }
      originalHabit = habit
      targetCount = habit.targetCount.coerceIn(MIN_TARGET, MAX_TARGET)
      withContext(dispatcherProvider.main()) {
        state.update {
          it.copy(
            name = habit.name,
            description = habit.description.orEmpty(),
            frequency = habit.frequency,
            colorPosition = habit.color,
            iconIndex = habit.icon,
            isReminderEnabled = habit.reminderTime != null,
            reminderTime = habit.reminderTime ?: it.reminderTime,
            reminderTimeLabel = formatTime(habit.reminderTime ?: it.reminderTime),
            remindOnlyIfNotDone = habit.remindOnlyIfNotDone,
            canSave = habit.name.isNotBlank(),
            canDelete = true,
          ).withTargetFields()
        }
      }
    }
  }

  fun onNameChange(name: String) {
    val limited = name.take(MAX_NAME_LENGTH)
    state.update { it.copy(name = limited, nameError = null, canSave = limited.isNotBlank()) }
  }

  fun onDescriptionChange(description: String) {
    state.update { it.copy(description = description) }
  }

  fun onFrequencyChange(frequency: HabitFrequency) {
    state.update { it.copy(frequency = frequency).withTargetFields() }
  }

  fun onTargetDecrease() {
    targetCount = (targetCount - 1).coerceAtLeast(MIN_TARGET)
    state.update { it.withTargetFields() }
  }

  fun onTargetIncrease() {
    targetCount = (targetCount + 1).coerceAtMost(MAX_TARGET)
    state.update { it.withTargetFields() }
  }

  fun onColorSelected(position: Int) {
    state.update { it.copy(colorPosition = position) }
  }

  fun onIconSelected(index: Int?) {
    state.update { it.copy(iconIndex = index) }
  }

  fun onReminderToggle(enabled: Boolean) {
    if (enabled) {
      navigationEvent.emit(NavigationEvent.RequestNotificationPermission)
    } else {
      state.update { it.copy(isReminderEnabled = false) }
    }
  }

  fun onNotificationPermissionGranted() {
    state.update { it.copy(isReminderEnabled = true) }
  }

  fun onReminderTimeChange(time: LocalTime) {
    state.update { it.copy(reminderTime = time, reminderTimeLabel = formatTime(time)) }
  }

  fun onRemindOnlyIfNotDoneChange(enabled: Boolean) {
    state.update { it.copy(remindOnlyIfNotDone = enabled) }
  }

  fun onBackClick() {
    navigationEvent.emit(NavigationEvent.Back)
  }

  fun onSaveClick() {
    val current = state.value
    val name = current.name.trim()
    if (name.isEmpty()) {
      state.update { it.copy(nameError = textProvider.getString(R.string.habit_name_required), canSave = false) }
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val now = nowDateTimeProvider.nowDateTime()
      val base = originalHabit ?: Habit(id = stableHabitId, createdAt = now, updatedAt = now)
      val habit = base.copy(
        name = name,
        description = current.description.trim().ifBlank { null },
        frequency = current.frequency,
        targetCount = targetCount,
        color = current.colorPosition,
        icon = current.iconIndex,
        reminderTime = current.reminderTime.takeIf { current.isReminderEnabled },
        remindOnlyIfNotDone = current.remindOnlyIfNotDone,
      )
      saveHabitUseCase(habit)
      Logger.i(TAG, "Saved habit, id: ${habit.id}")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  fun onDeleteClick() {
    if (id == null) return
    state.update { it.copy(isDeleteConfirmationVisible = true) }
  }

  fun onDeleteDismissed() {
    state.update { it.copy(isDeleteConfirmationVisible = false) }
  }

  fun onDeleteConfirmed() {
    val habitId = id ?: return
    state.update { it.copy(isDeleteConfirmationVisible = false) }
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteHabitUseCase(habitId)
      Logger.i(TAG, "Deleted habit, id: $habitId")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  private fun HabitEditorState.withTargetFields(): HabitEditorState = copy(
    targetLabel = targetLabel(frequency, targetCount),
    canDecreaseTarget = targetCount > MIN_TARGET,
    canIncreaseTarget = targetCount < MAX_TARGET,
  )

  private fun targetLabel(frequency: HabitFrequency, count: Int): String {
    val res = when (frequency) {
      HabitFrequency.DAILY -> R.string.habit_target_per_day
      HabitFrequency.WEEKLY -> R.string.habit_target_per_week
    }
    return textProvider.getString(res, count)
  }

  private fun formatTime(time: LocalTime): String = dateTimeManager.getTime(time)

  sealed interface NavigationEvent {
    data object Back : NavigationEvent

    data object RequestNotificationPermission : NavigationEvent
  }

  companion object {
    private const val TAG = "HabitEditorViewModel"
    private const val MAX_NAME_LENGTH = 100
    private const val MIN_TARGET = 1
    private const val MAX_TARGET = 99
    private const val DEFAULT_TARGET = 1
  }
}
