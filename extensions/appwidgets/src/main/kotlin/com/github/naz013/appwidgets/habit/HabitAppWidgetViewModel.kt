package com.github.naz013.appwidgets.habit

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.first

internal class HabitAppWidgetViewModel(
  private val prefsProvider: HabitWidgetPrefsProvider,
  private val habitConfig: HabitConfig,
  private val observeHabitsWithProgressUseCase: ObserveHabitsWithProgressUseCase,
  private val textProvider: TextProvider
) {

  suspend fun getState(): HabitAppWidgetState {
    val widgetId = prefsProvider.widgetId
    val backgroundColor = prefsProvider.getBackground()
    if (!habitConfig.isEnabled) {
      return HabitAppWidgetState(
        widgetId = widgetId,
        backgroundColor = backgroundColor,
        isUnlocked = false
      )
    }
    val items = observeHabitsWithProgressUseCase().first()
      .sortedByDescending { it.habit.isPinned }
      .map { it.toWidgetItem() }
    return HabitAppWidgetState(widgetId = widgetId, backgroundColor = backgroundColor, items = items)
  }

  private fun HabitWithProgress.toWidgetItem(): UiHabitWidgetItem {
    val label = if (habit.frequency == HabitFrequency.WEEKLY) {
      textProvider.getString(R.string.habit_week_progress, periodCount, habit.targetCount)
    } else {
      "$periodCount/${habit.targetCount}"
    }
    return UiHabitWidgetItem(
      id = habit.id,
      name = habit.name,
      progressLabel = label,
      isDone = isPeriodTargetReached
    )
  }
}
