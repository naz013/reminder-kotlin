package com.github.naz013.feature.habit.preview

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
import com.github.naz013.logic.habit.HabitDay
import com.github.naz013.logic.habit.HabitDetails
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitDetailsUseCase
import com.github.naz013.ui.common.R
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.format.TextStyle
import java.util.Locale

internal class HabitPreviewViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val observeHabitDetailsUseCase: ObserveHabitDetailsUseCase,
  private val checkInHabitUseCase: CheckInHabitUseCase,
  private val deleteHabitUseCase: DeleteHabitUseCase,
  private val textProvider: TextProvider,
) : ViewModel() {

  val state: StateFlow<HabitPreviewState> field = MutableStateFlow(HabitPreviewState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private var details: HabitDetails? = null
  private var isClosing = false

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      observeHabitDetailsUseCase(id, ACTIVITY_WEEKS)
        .catch { Logger.e(TAG, "Failed to observe habit details", it) }
        .collect { onDetails(it) }
    }
  }

  private fun onDetails(newDetails: HabitDetails?) {
    details = newDetails
    if (newDetails == null) {
      Logger.w(TAG, "Habit not found, id: $id")
      close()
      return
    }
    state.update { it.copy(content = toReadyContent(newDetails)) }
  }

  private fun toReadyContent(details: HabitDetails): HabitPreviewContent.Ready {
    val habit = details.habit
    val backgroundColor = RoutineColors.ALL.getOrElse(habit.color) { DEFAULT_COLOR }
    val contentColor = if (backgroundColor.luminance() > CONTRAST_LUMINANCE_THRESHOLD) Color.Black else Color.White
    val isWeekly = habit.frequency == HabitFrequency.WEEKLY
    val targetReached = details.progress.isPeriodTargetReached
    return HabitPreviewContent.Ready(
      name = habit.name,
      description = habit.description,
      frequencyLabel = frequencyLabel(details),
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      iconRes = habit.icon?.let { RoutineIconSet.ALL.getOrNull(it) },
      todayProgressLabel = if (isWeekly) "${details.todayCount}" else "${details.todayCount}/${habit.targetCount}",
      weekProgressLabel = if (isWeekly) {
        textProvider.getString(R.string.habit_week_progress, details.periodCount, habit.targetCount)
      } else {
        null
      },
      checkButtonContainerColor = if (targetReached) contentColor else backgroundColor,
      checkButtonContentColor = if (targetReached) backgroundColor else contentColor,
      stats = listOf(
        UiHabitStat(textProvider.getString(R.string.habit_current_streak), "${details.streak}"),
        UiHabitStat(textProvider.getString(R.string.habit_best_streak), "${details.longestStreak}"),
        UiHabitStat(textProvider.getString(R.string.habit_total_check_ins), "${details.totalCheckIns}"),
      ),
      activityWeeks = toActivityWeeks(details.days),
      activitySummary = textProvider.getString(
        R.string.habit_activity_description,
        details.days.sumOf { it.count },
        ACTIVITY_WEEKS
      ),
    )
  }

  private fun frequencyLabel(details: HabitDetails): String {
    val habit = details.habit
    val frequency = when (habit.frequency) {
      HabitFrequency.DAILY -> textProvider.getString(R.string.habit_frequency_daily)
      HabitFrequency.WEEKLY -> textProvider.getString(R.string.habit_frequency_weekly)
    }
    val target = when (habit.frequency) {
      HabitFrequency.DAILY -> textProvider.getString(R.string.habit_target_per_day, habit.targetCount)
      HabitFrequency.WEEKLY -> textProvider.getString(R.string.habit_target_per_week, habit.targetCount)
    }
    return "$frequency · $target"
  }

  private fun toActivityWeeks(days: List<HabitDay>): List<UiHabitActivityWeek> {
    var previousMonth: Int? = null
    return days.chunked(DAYS_IN_WEEK).map { week ->
      val month = week.first().date.month
      val label = if (month.value != previousMonth) {
        month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
      } else {
        null
      }
      previousMonth = month.value
      UiHabitActivityWeek(monthLabel = label, levels = week.map { it.level })
    }
  }

  fun onBackClick() {
    navigationEvent.emit(NavigationEvent.Back)
  }

  fun onEditClick() {
    navigationEvent.emit(NavigationEvent.OpenEditor(id))
  }

  fun onCheckInClick() {
    val progress = details?.progress ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      checkInHabitUseCase(progress)
    }
  }

  fun onDeleteClick() {
    state.update { it.copy(isDeleteConfirmationVisible = true) }
  }

  fun onDeleteDismissed() {
    state.update { it.copy(isDeleteConfirmationVisible = false) }
  }

  fun onDeleteConfirmed() {
    state.update { it.copy(isDeleteConfirmationVisible = false) }
    isClosing = true
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteHabitUseCase(id)
      Logger.i(TAG, "Deleted habit, id: $id")
      navigationEvent.emit(NavigationEvent.Back)
    }
  }

  /** The habit vanishing right after our own delete must not emit a second Back. */
  private fun close() {
    if (isClosing) return
    isClosing = true
    navigationEvent.emit(NavigationEvent.Back)
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent

    data class OpenEditor(
      val id: String
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "HabitPreviewViewModel"
    private const val ACTIVITY_WEEKS = 26
    private const val DAYS_IN_WEEK = 7
    private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
    private val DEFAULT_COLOR = Color(0xFF86E3CE)
  }
}
