package com.github.naz013.feature.habit.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.IncrementHabitCheckInUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.habit.usecase.ToggleHabitCheckInUseCase
import com.github.naz013.ui.common.R
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HabitListViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val observeHabitsWithProgressUseCase: ObserveHabitsWithProgressUseCase,
  private val toggleHabitCheckInUseCase: ToggleHabitCheckInUseCase,
  private val incrementHabitCheckInUseCase: IncrementHabitCheckInUseCase,
  private val textProvider: TextProvider,
) : ViewModel() {

  val state: StateFlow<HabitListState> field = MutableStateFlow(HabitListState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private var progressById: Map<String, HabitWithProgress> = emptyMap()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      observeHabitsWithProgressUseCase()
        .catch { Logger.e(TAG, "Failed to observe habits", it) }
        .collect { habits ->
          progressById = habits.associateBy { it.habit.id }
          state.update { it.copy(listState = toDisplayState(habits)) }
        }
    }
  }

  private fun toDisplayState(habits: List<HabitWithProgress>): HabitListDisplayState {
    return if (habits.isEmpty()) {
      HabitListDisplayState.Empty
    } else {
      HabitListDisplayState.Ready(habits.map { toUiItem(it) })
    }
  }

  private fun toUiItem(progress: HabitWithProgress): UiHabitListItem {
    val habit = progress.habit
    val backgroundColor = RoutineColors.ALL.getOrElse(habit.color) { DEFAULT_COLOR }
    val contentColor = if (backgroundColor.luminance() > CONTRAST_LUMINANCE_THRESHOLD) Color.Black else Color.White
    val isWeekly = habit.frequency == HabitFrequency.WEEKLY
    return UiHabitListItem(
      id = habit.id,
      name = habit.name,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      iconRes = habit.icon?.let { RoutineIconSet.ALL.getOrNull(it) },
      isPinned = habit.isPinned,
      todayProgressLabel = if (isWeekly) "${progress.todayCount}" else "${progress.todayCount}/${habit.targetCount}",
      weekProgressLabel = if (isWeekly) {
        textProvider.getString(R.string.habit_week_progress, progress.periodCount, habit.targetCount)
      } else {
        null
      },
      streakLabel = "${progress.streak}",
      streakDescription = textProvider.getString(R.string.habit_streak_description, progress.streak),
      isTargetReached = progress.isPeriodTargetReached,
    )
  }

  fun onAddClick() {
    navigationEvent.emit(NavigationEvent.OpenEditor(null))
  }

  fun onHabitClick(id: String) {
    navigationEvent.emit(NavigationEvent.OpenEditor(id))
  }

  fun onCheckInClick(id: String) {
    val progress = progressById[id] ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      if (shouldToggle(progress)) {
        toggleHabitCheckInUseCase(id)
      } else {
        incrementHabitCheckInUseCase(id)
      }
    }
  }

  private fun shouldToggle(progress: HabitWithProgress): Boolean {
    val habit = progress.habit
    return when (habit.frequency) {
      HabitFrequency.DAILY -> habit.targetCount <= 1 || progress.todayCount >= habit.targetCount
      HabitFrequency.WEEKLY -> progress.todayCount > 0
    }
  }

  sealed interface NavigationEvent {
    data class OpenEditor(
      val id: String?
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "HabitListViewModel"
    private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
    private val DEFAULT_COLOR = Color(0xFF86E3CE)
  }
}
